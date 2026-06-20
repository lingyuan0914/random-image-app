package com.randomimage.data.local

import com.randomimage.domain.model.ImageModel

data class FavoriteData(
    val id: String,
    val imageUrl: String,
    val thumbnailUrl: String,
    val photographerName: String,
    val photographerUsername: String,
    val description: String?,
    val groupId: Long = 0,
    val tags: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val localPath: String? = null
) {
    fun toImageModel(): ImageModel {
        return ImageModel(
            id = id,
            urls = com.randomimage.domain.model.ImageUrls(
                raw = imageUrl, full = imageUrl, regular = imageUrl,
                small = imageUrl, thumb = thumbnailUrl
            ),
            user = com.randomimage.domain.model.User(
                id = photographerUsername, username = photographerUsername, name = photographerName
            ),
            description = description,
            groupId = groupId,
            tags = if (tags.isNotBlank()) tags.split(",") else emptyList(),
            localPath = localPath
        )
    }

    companion object {
        fun fromImageModel(image: ImageModel, groupId: Long = 0, localPath: String? = null): FavoriteData {
            return FavoriteData(
                id = image.id, imageUrl = image.urls.regular, thumbnailUrl = image.urls.thumb,
                photographerName = image.user.name, photographerUsername = image.user.username,
                description = image.description, groupId = groupId, tags = image.tags.joinToString(","),
                localPath = localPath
            )
        }
    }
}

data class HistoryData(
    val id: String,
    val imageUrl: String,
    val thumbnailUrl: String,
    val photographerName: String,
    val photographerUsername: String,
    val description: String?,
    val tags: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val viewedAt: Long = System.currentTimeMillis()
) {
    fun toImageModel(): ImageModel {
        return ImageModel(
            id = id,
            urls = com.randomimage.domain.model.ImageUrls(
                raw = imageUrl, full = imageUrl, regular = imageUrl,
                small = imageUrl, thumb = thumbnailUrl
            ),
            user = com.randomimage.domain.model.User(
                id = photographerUsername, username = photographerUsername, name = photographerName
            ),
            description = description,
            tags = if (tags.isNotBlank()) tags.split(",") else emptyList(),
            viewedAt = viewedAt
        )
    }

    companion object {
        fun fromImageModel(image: ImageModel, tags: String? = null): HistoryData {
            return HistoryData(
                id = image.id, imageUrl = image.urls.regular, thumbnailUrl = image.urls.thumb,
                photographerName = image.user.name, photographerUsername = image.user.username,
                description = image.description, tags = tags ?: image.tags.joinToString(","),
                viewedAt = System.currentTimeMillis()
            )
        }
    }
}

data class GroupData(
    val id: String,
    val name: String,
    val coverUrl: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

data class TagData(
    val name: String,
    val usageCount: Int = 1,
    val lastUsedTime: Long = System.currentTimeMillis()
)

data class ArtistData(
    val uid: String,
    val name: String,
    val artworkCount: Int = 0,
    val followedTime: Long = System.currentTimeMillis()
)
