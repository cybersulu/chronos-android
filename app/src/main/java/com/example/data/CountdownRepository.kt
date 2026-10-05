package com.example.data

import kotlinx.coroutines.flow.Flow

class CountdownRepository(
    private val countdownDao: CountdownDao,
    private val categoryDao: CategoryDao
) {

    val allTimers: Flow<List<CountdownEntity>> = countdownDao.getAllTimers()

    val allCategories: Flow<List<CategoryEntity>> = categoryDao.getAllCategories()

    fun getTimerById(id: Long): Flow<CountdownEntity?> = countdownDao.getTimerById(id)

    suspend fun getTimerByIdSync(id: Long): CountdownEntity? = countdownDao.getTimerByIdSync(id)

    suspend fun getPinnedWidgetTimer(): CountdownEntity? {
        return countdownDao.getPinnedWidgetTimer() ?: countdownDao.getFirstTimer()
    }

    suspend fun insert(timer: CountdownEntity): Long = countdownDao.insertTimer(timer)

    suspend fun update(timer: CountdownEntity) = countdownDao.updateTimer(timer)

    suspend fun updateAll(timers: List<CountdownEntity>) = countdownDao.updateTimers(timers)

    suspend fun pinToWidget(id: Long) = countdownDao.pinTimerToWidget(id)

    suspend fun delete(timer: CountdownEntity) = countdownDao.deleteTimer(timer)

    suspend fun deleteById(id: Long) = countdownDao.deleteTimerById(id)

    suspend fun reorderTimers(reorderedList: List<CountdownEntity>) {
        val updated = reorderedList.mapIndexed { index, item ->
            item.copy(orderIndex = index)
        }
        countdownDao.updateTimers(updated)
    }

    suspend fun moveItem(fromIndex: Int, toIndex: Int) {
        val currentList = countdownDao.getAllTimersList().toMutableList()
        if (fromIndex in currentList.indices && toIndex in currentList.indices && fromIndex != toIndex) {
            val moved = currentList.removeAt(fromIndex)
            currentList.add(toIndex, moved)
            reorderTimers(currentList)
        }
    }

    suspend fun insertCategory(category: CategoryEntity): Long = categoryDao.insertCategory(category)

    suspend fun deleteCategory(category: CategoryEntity) = categoryDao.deleteCategory(category)

    suspend fun deleteCategoryByName(name: String) = categoryDao.deleteCategoryByName(name)
}
