package com.randomimage.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tags")
data class TagEntity(
    @PrimaryKey
    val name: String,
    val usageCount: Int = 1,
    val lastUsedTime: Long = System.currentTimeMillis()
)
