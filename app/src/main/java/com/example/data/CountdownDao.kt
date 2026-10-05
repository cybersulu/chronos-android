package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CountdownDao {

    @Query("SELECT * FROM countdown_timers ORDER BY orderIndex ASC, id ASC")
    fun getAllTimers(): Flow<List<CountdownEntity>>

    @Query("SELECT * FROM countdown_timers ORDER BY orderIndex ASC, id ASC")
    suspend fun getAllTimersList(): List<CountdownEntity>

    @Query("SELECT * FROM countdown_timers WHERE targetEpochMillis > :nowEpoch ORDER BY targetEpochMillis ASC")
    suspend fun getActiveTimers(nowEpoch: Long): List<CountdownEntity>

    @Query("SELECT * FROM countdown_timers WHERE id = :id LIMIT 1")
    fun getTimerById(id: Long): Flow<CountdownEntity?>

    @Query("SELECT * FROM countdown_timers WHERE id = :id LIMIT 1")
    suspend fun getTimerByIdSync(id: Long): CountdownEntity?

    @Query("SELECT * FROM countdown_timers ORDER BY orderIndex ASC LIMIT 1")
    suspend fun getFirstTimer(): CountdownEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTimer(timer: CountdownEntity): Long

    @Update
    suspend fun updateTimer(timer: CountdownEntity)

    @Update
    suspend fun updateTimers(timers: List<CountdownEntity>)

    @Delete
    suspend fun deleteTimer(timer: CountdownEntity)

    @Query("DELETE FROM countdown_timers WHERE id = :id")
    suspend fun deleteTimerById(id: Long)

    @Query("UPDATE countdown_timers SET category = :newCategory WHERE category = :oldCategory COLLATE NOCASE")
    suspend fun updateCategoryForTimers(oldCategory: String, newCategory: String)

    @Query("SELECT COUNT(*) FROM countdown_timers")
    suspend fun getCount(): Int
}
