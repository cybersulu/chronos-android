package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.CategoryEntity
import com.example.data.CountdownEntity
import com.example.data.CountdownRepository
import com.example.model.CountdownCategories
import com.example.notification.NotificationScheduler
import com.example.ui.components.CategoryCountItem
import com.example.widget.CountdownAppWidgetProvider
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class CountdownViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: CountdownRepository

    init {
        val database = AppDatabase.getDatabase(application, viewModelScope)
        repository = CountdownRepository(database.countdownDao(), database.categoryDao())
    }

    val allTimers: StateFlow<List<CountdownEntity>> = repository.allTimers
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val customCategories: StateFlow<List<CategoryEntity>> = repository.allCategories
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _selectedCategory = MutableStateFlow(CountdownCategories.ALL)
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    // 1-second global ticker for fluid second-by-second countdown updates across all UI
    private val _currentTickerTime = MutableStateFlow(System.currentTimeMillis())
    val currentTickerTime: StateFlow<Long> = _currentTickerTime.asStateFlow()

    init {
        viewModelScope.launch {
            while (isActive) {
                _currentTickerTime.value = System.currentTimeMillis()
                delay(1000L)
            }
        }

        // Register custom categories with CountdownCategories lookup cache
        viewModelScope.launch {
            customCategories.collect { categories ->
                for (cat in categories) {
                    CountdownCategories.registerCustomCategory(cat.name, cat.iconName, cat.colorHex)
                }
            }
        }
    }

    // Category Counts calculation combining Default categories + Custom categories + Timers
    val categoryCounts: StateFlow<List<CategoryCountItem>> = combine(allTimers, customCategories) { timers, customCats ->
        // Register each custom category in CountdownCategories
        for (cat in customCats) {
            CountdownCategories.registerCustomCategory(cat.name, cat.iconName, cat.colorHex)
        }

        val list = mutableListOf<CategoryCountItem>()
        list.add(CategoryCountItem(CountdownCategories.ALL, timers.size))

        val baseCategories = CountdownCategories.DEFAULT_LIST.map { it.name }
        val customNames = customCats.map { it.name }
        val timerCategories = timers.map { it.category }

        val allCategories = (baseCategories + customNames + timerCategories).distinct()
        for (cat in allCategories) {
            val count = timers.count { it.category.equals(cat, ignoreCase = true) }
            list.add(CategoryCountItem(cat, count))
        }
        list
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = listOf(CategoryCountItem(CountdownCategories.ALL, 0))
    )

    // Filtered Timers based on category selection
    val filteredTimers: StateFlow<List<CountdownEntity>> = combine(allTimers, selectedCategory) { timers, category ->
        if (category == CountdownCategories.ALL) {
            timers
        } else {
            timers.filter { it.category.equals(category, ignoreCase = true) }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun selectCategory(category: String) {
        _selectedCategory.value = category
    }

    fun addCategory(name: String, iconName: String, colorHex: Long) {
        viewModelScope.launch {
            CountdownCategories.registerCustomCategory(name, iconName, colorHex)
            repository.insertCategory(
                CategoryEntity(
                    name = name,
                    iconName = iconName,
                    colorHex = colorHex
                )
            )
        }
    }

    fun deleteCategory(category: CategoryEntity) {
        viewModelScope.launch {
            repository.deleteCategory(category)
        }
    }

    fun saveTimer(
        existingId: Long = 0,
        title: String,
        targetEpochMillis: Long,
        timeZoneId: String,
        category: String,
        colorIndex: Int,
        iconName: String,
        notifyOnFinish: Boolean,
        notifyAdvanceMinutes: Int,
        isPinnedToWidget: Boolean,
        notes: String
    ) {
        viewModelScope.launch {
            val context = getApplication<Application>()
            val timer = CountdownEntity(
                id = existingId,
                title = title,
                targetEpochMillis = targetEpochMillis,
                timeZoneId = timeZoneId,
                category = category,
                colorIndex = colorIndex,
                iconName = iconName,
                notifyOnFinish = notifyOnFinish,
                notifyAdvanceMinutes = notifyAdvanceMinutes,
                isPinnedToWidget = isPinnedToWidget,
                notes = notes
            )

            val savedId = if (existingId == 0L) {
                repository.insert(timer)
            } else {
                repository.update(timer)
                existingId
            }

            // Pin to widget if user toggled it
            if (isPinnedToWidget) {
                repository.pinToWidget(savedId)
            }

            // Reschedule notification alarm for this timer
            if (notifyOnFinish) {
                NotificationScheduler.scheduleAlert(context, timer.copy(id = savedId))
            } else {
                NotificationScheduler.cancelAlert(context, savedId)
            }

            // Immediately notify all AppWidgets (4x1, 3x1, 5x1)
            CountdownAppWidgetProvider.triggerUpdate(context)
        }
    }

    fun pinTimerToWidget(timerId: Long) {
        viewModelScope.launch {
            repository.pinToWidget(timerId)
            CountdownAppWidgetProvider.triggerUpdate(getApplication())
        }
    }

    fun deleteTimer(timer: CountdownEntity) {
        viewModelScope.launch {
            val context = getApplication<Application>()
            NotificationScheduler.cancelAlert(context, timer.id)
            repository.delete(timer)
            CountdownAppWidgetProvider.triggerUpdate(context)
        }
    }

    fun moveTimer(fromIndex: Int, toIndex: Int) {
        viewModelScope.launch {
            repository.moveItem(fromIndex, toIndex)
            CountdownAppWidgetProvider.triggerUpdate(getApplication())
        }
    }

    fun moveTimerUp(timer: CountdownEntity) {
        viewModelScope.launch {
            val currentList = allTimers.value
            val index = currentList.indexOfFirst { it.id == timer.id }
            if (index > 0) {
                repository.moveItem(index, index - 1)
                CountdownAppWidgetProvider.triggerUpdate(getApplication())
            }
        }
    }

    fun moveTimerDown(timer: CountdownEntity) {
        viewModelScope.launch {
            val currentList = allTimers.value
            val index = currentList.indexOfFirst { it.id == timer.id }
            if (index >= 0 && index < currentList.size - 1) {
                repository.moveItem(index, index + 1)
                CountdownAppWidgetProvider.triggerUpdate(getApplication())
            }
        }
    }
}
