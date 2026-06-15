package com.randomimage.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteGroupDao {
    @Query("SELECT * FROM favorite_groups ORDER BY createdAt DESC")
    fun getAllGroups(): Flow<List<FavoriteGroupEntity>>

    @Insert
    suspend fun insertGroup(group: FavoriteGroupEntity): Long

    @Delete
    suspend fun deleteGroup(group: FavoriteGroupEntity)

    @Update
    suspend fun updateGroup(group: FavoriteGroupEntity)
}
