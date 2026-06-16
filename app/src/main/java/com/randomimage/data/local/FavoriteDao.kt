package com.randomimage.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {
    @Query("SELECT * FROM favorites ORDER BY timestamp DESC")
    fun getAllFavorites(): Flow<List<FavoriteEntity>>

    @Query("SELECT * FROM favorites WHERE groupId = :groupId ORDER BY timestamp DESC")
    fun getFavoritesByGroup(groupId: Long): Flow<List<FavoriteEntity>>

    @Query("SELECT * FROM favorites WHERE id = :imageId")
    suspend fun getFavoriteById(imageId: String): FavoriteEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: FavoriteEntity)

    @Delete
    suspend fun deleteFavorite(favorite: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE id = :imageId")
    suspend fun deleteFavoriteById(imageId: String)

    @Query("SELECT COUNT(*) FROM favorites")
    fun getFavoriteCount(): Flow<Int>
}