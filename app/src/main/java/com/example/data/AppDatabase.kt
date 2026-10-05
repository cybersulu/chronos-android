package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.ZoneId
import java.time.ZonedDateTime

@Database(entities = [CountdownEntity::class, CategoryEntity::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun countdownDao(): CountdownDao
    abstract fun categoryDao(): CategoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope = CoroutineScope(Dispatchers.IO)): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "chronos_database"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch {
                        populateInitialData(database.countdownDao(), database.categoryDao())
                    }
                }
            }

            suspend fun populateInitialData(dao: CountdownDao, categoryDao: CategoryDao) {
                val userTz = ZoneId.systemDefault().id
                val now = ZonedDateTime.now()

                // Event 1: New Year's Eve Celebration (e.g. Dec 31 midnight)
                val newYearYear = if (now.monthValue == 12 && now.dayOfMonth == 31) now.year + 1 else now.year
                val newYear = ZonedDateTime.of(newYearYear, 12, 31, 23, 59, 59, 0, ZoneId.systemDefault())

                // Event 2: Tokyo Summer Adventure (3 months out)
                val tripDate = now.plusMonths(3).withDayOfMonth(15).withHour(9).withMinute(0).withSecond(0)

                // Event 3: Next Major Product Launch (21 days out)
                val launchDate = now.plusDays(21).withHour(10).withMinute(0).withSecond(0)

                // Event 4: Mars Rover Mission Milestone (1 year out)
                val milestoneDate = now.plusYears(1).plusMonths(2).withHour(14).withMinute(30).withSecond(0)

                val defaultTimers = listOf(
                    CountdownEntity(
                        title = "New Year's Eve Celebration",
                        targetEpochMillis = newYear.toInstant().toEpochMilli(),
                        timeZoneId = userTz,
                        category = "Celebration",
                        colorIndex = 0, // AmberGold
                        iconName = "Celebration",
                        orderIndex = 0,
                        notifyOnFinish = true,
                        notifyAdvanceMinutes = 60,
                        isPinnedToWidget = true,
                        notes = "Gather friends, pop champagne, and watch fireworks!"
                    ),
                    CountdownEntity(
                        title = "Tokyo Summer Adventure",
                        targetEpochMillis = tripDate.toInstant().toEpochMilli(),
                        timeZoneId = "Asia/Tokyo",
                        category = "Travel",
                        colorIndex = 1, // NeonCyan
                        iconName = "Flight",
                        orderIndex = 1,
                        notifyOnFinish = true,
                        notifyAdvanceMinutes = 1440,
                        isPinnedToWidget = false,
                        notes = "Flight NH106 departing Haneda. Check-in luggage packed!"
                    ),
                    CountdownEntity(
                        title = "Major Product Keynote",
                        targetEpochMillis = launchDate.toInstant().toEpochMilli(),
                        timeZoneId = "America/Los_Angeles",
                        category = "Work",
                        colorIndex = 2, // ElectricIndigo
                        iconName = "Rocket",
                        orderIndex = 2,
                        notifyOnFinish = true,
                        notifyAdvanceMinutes = 15,
                        isPinnedToWidget = false,
                        notes = "Presenting version 3.0 to stakeholders worldwide."
                    ),
                    CountdownEntity(
                        title = "Cosmic Solar Horizon",
                        targetEpochMillis = milestoneDate.toInstant().toEpochMilli(),
                        timeZoneId = "UTC",
                        category = "Milestone",
                        colorIndex = 3, // CosmicPurple
                        iconName = "Star",
                        orderIndex = 3,
                        notifyOnFinish = true,
                        notifyAdvanceMinutes = 0,
                        isPinnedToWidget = false,
                        notes = "Rare astronomical celestial alignment observed globally."
                    )
                )

                for (timer in defaultTimers) {
                    dao.insertTimer(timer)
                }

                // Initial popular starter custom categories
                val starterCategories = listOf(
                    CategoryEntity(name = "Fitness", iconName = "Fitness", colorHex = 0xFF10B981),
                    CategoryEntity(name = "Birthday", iconName = "Cake", colorHex = 0xFFF43F5E),
                    CategoryEntity(name = "Gaming", iconName = "Gaming", colorHex = 0xFF8B5CF6)
                )
                for (cat in starterCategories) {
                    categoryDao.insertCategory(cat)
                }
            }
        }
    }
}
