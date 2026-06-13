package com.randomimage.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_groups")
data class FavoriteGroupEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val timestamp: Long = System.currentTimeMillis()
)
