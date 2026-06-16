package com.randomimage.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TagDao {
    @Query("SELECT * FROM tags ORDER BY usageCount DESC")
    fun getAllTags(): Flow<List<TagEntity>>

    @Query("SELECT * FROM tags WHERE name LIKE '%' || :query || '%' ORDER BY usageCount DESC LIMIT 10")
    suspend fun searchTags(query: String): List<TagEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTag(tag: TagEntity)

    @Query("UPDATE tags SET usageCount = usageCount + 1, lastUsedTime = :time WHERE name = :name")
    suspend fun incrementUsage(name: String, time: Long = System.currentTimeMillis())

    @Query("DELETE FROM tags")
    suspend fun clearTags()
}
