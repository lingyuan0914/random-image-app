package com.randomimage.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        FavoriteEntity::class,
        HistoryEntity::class,
        SearchHistoryEntity::class,
        FavoriteGroupEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun favoriteDao(): FavoriteDao
    abstract fun historyDao(): HistoryDao
    abstract fun searchHistoryDao(): SearchHistoryDao
    abstract fun favoriteGroupDao(): FavoriteGroupDao
}
