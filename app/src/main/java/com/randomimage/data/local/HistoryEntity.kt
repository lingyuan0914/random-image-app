package com.randomimage.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.randomimage.domain.model.ImageModel

@Entity(tableName = "history")
data class HistoryEntity(
    @PrimaryKey
    val id: String,
    val imageUrl: String,
    val thumbnailUrl: String,
    val photographerName: String,
    val photographerUsername: String,
    val description: String?,
    val tags: String?,
    val timestamp: Long = System.currentTimeMillis()
) {
    fun toImageModel(): ImageModel {
        return ImageModel(
            id = id,
            urls = com.randomimage.domain.model.ImageUrls(
                raw = imageUrl,
                full = imageUrl,
                regular = imageUrl,
                small = imageUrl,
                thumb = thumbnailUrl
            ),
            user = com.randomimage.domain.model.User(
                id = photographerUsername,
                username = photographerUsername,
                name = photographerName
            ),
            description = description
        )
    }

    companion object {
        fun fromImageModel(image: ImageModel, tags: String? = null): HistoryEntity {
            return HistoryEntity(
                id = image.id,
                imageUrl = image.urls.regular,
                thumbnailUrl = image.urls.thumb,
                photographerName = image.user.name,
                photographerUsername = image.user.username,
                description = image.description,
                tags = tags
            )
        }
    }
}
