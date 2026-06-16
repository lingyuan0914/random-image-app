package com.randomimage.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        FavoriteEntity::class,
        HistoryEntity::class,
        SearchHistoryEntity::class,
        FavoriteGroupEntity::class,
        TagEntity::class,
        ArtistEntity::class
    ],
    version = 6,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun favoriteDao(): FavoriteDao
    abstract fun historyDao(): HistoryDao
    abstract fun searchHistoryDao(): SearchHistoryDao
    abstract fun favoriteGroupDao(): FavoriteGroupDao
    abstract fun tagDao(): TagDao
    abstract fun artistDao(): ArtistDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS search_history (query TEXT PRIMARY KEY NOT NULL, timestamp INTEGER NOT NULL)")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS favorite_groups (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, name TEXT NOT NULL, createdAt INTEGER NOT NULL)")
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE favorites ADD COLUMN groupId INTEGER NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS search_history_new (query TEXT PRIMARY KEY NOT NULL, timestamp INTEGER NOT NULL)")
                db.execSQL("INSERT INTO search_history_new (query, timestamp) SELECT query, timestamp FROM search_history")
                db.execSQL("DROP TABLE search_history")
                db.execSQL("ALTER TABLE search_history_new RENAME TO search_history")
            }
        }

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS tags (name TEXT PRIMARY KEY NOT NULL, usageCount INTEGER NOT NULL DEFAULT 1, lastUsedTime INTEGER NOT NULL DEFAULT 0)")
                db.execSQL("CREATE TABLE IF NOT EXISTS followed_artists (uid TEXT PRIMARY KEY NOT NULL, name TEXT NOT NULL, artworkCount INTEGER NOT NULL DEFAULT 0, followedTime INTEGER NOT NULL DEFAULT 0)")
            }
        }
    }
}
