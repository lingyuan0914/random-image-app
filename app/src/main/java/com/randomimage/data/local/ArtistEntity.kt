package com.randomimage.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "followed_artists")
data class ArtistEntity(
    @PrimaryKey
    val uid: String,
    val name: String,
    val artworkCount: Int = 0,
    val followedTime: Long = System.currentTimeMillis()
)
