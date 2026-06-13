package com.randomimage.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {
    @Query("SELECT * FROM history ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<HistoryEntity>>

    @Query("SELECT * FROM history WHERE id = :imageId")
    suspend fun getHistoryById(imageId: String): HistoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: HistoryEntity)

    @Delete
    suspend fun deleteHistory(history: HistoryEntity)

    @Query("DELETE FROM history WHERE id = :imageId")
    suspend fun deleteHistoryById(imageId: String)

    @Query("DELETE FROM history")
    suspend fun clearHistory()

    @Query("SELECT COUNT(*) FROM history")
    fun getHistoryCount(): Flow<Int>
}
