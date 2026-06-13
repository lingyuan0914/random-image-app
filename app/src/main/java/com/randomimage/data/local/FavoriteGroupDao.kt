package com.randomimage.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteGroupDao {
    @Query("SELECT * FROM favorite_groups ORDER BY timestamp DESC")
    fun getAllGroups(): Flow<List<FavoriteGroupEntity>>

    @Insert
    suspend fun insertGroup(group: FavoriteGroupEntity): Long

    @Delete
    suspend fun deleteGroup(group: FavoriteGroupEntity)

    @Query("DELETE FROM favorite_groups WHERE id = :groupId")
    suspend fun deleteGroupById(groupId: Long)
}
