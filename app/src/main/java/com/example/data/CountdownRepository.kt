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

    suspend fun insert(timer: CountdownEntity): Long = countdownDao.insertTimer(timer)

    suspend fun update(timer: CountdownEntity) = countdownDao.updateTimer(timer)

    suspend fun updateAll(timers: List<CountdownEntity>) = countdownDao.updateTimers(timers)

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

    suspend fun updateCategory(oldName: String, category: CategoryEntity) {
        categoryDao.updateCategory(category)
        if (!oldName.equals(category.name, ignoreCase = true)) {
            countdownDao.updateCategoryForTimers(oldName, category.name)
        }
    }

    suspend fun deleteCategory(category: CategoryEntity) {
        categoryDao.deleteCategory(category)
    }

    suspend fun deleteCategoryByName(name: String) = categoryDao.deleteCategoryByName(name)

    suspend fun ensureDefaultCategoriesPopulated() {
        val existing = categoryDao.getAllCategoriesList()
        val existingNames = existing.map { it.name.lowercase() }.toSet()

        val defaultList = listOf(
            CategoryEntity(name = "Celebration", iconName = "Celebration", colorHex = 0xFFF59E0BL),
            CategoryEntity(name = "Milestone", iconName = "Flag", colorHex = 0xFF8B5CF6L),
            CategoryEntity(name = "Travel", iconName = "Flight", colorHex = 0xFF06B6D4L),
            CategoryEntity(name = "Work", iconName = "Work", colorHex = 0xFF3B82F6L),
            CategoryEntity(name = "Personal", iconName = "Favorite", colorHex = 0xFFF43F5EL),
            CategoryEntity(name = "Fitness", iconName = "Fitness", colorHex = 0xFF10B981L),
            CategoryEntity(name = "Birthday", iconName = "Cake", colorHex = 0xFFEC4899L),
            CategoryEntity(name = "Gaming", iconName = "Gaming", colorHex = 0xFF8B5CF6L)
        )

        for (item in defaultList) {
            if (!existingNames.contains(item.name.lowercase())) {
                categoryDao.insertCategory(item)
            }
        }
    }
}
