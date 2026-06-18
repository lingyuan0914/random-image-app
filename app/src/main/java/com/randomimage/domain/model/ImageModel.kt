package com.randomimage.domain.model

data class ImageModel(
    val id: String,
    val urls: ImageUrls,
    val user: User,
    val description: String? = null,
    val likes: Int = 0,
    val width: Int = 0,
    val height: Int = 0,
    val groupId: Long = 0,
    val tags: List<String> = emptyList(),
    val localPath: String? = null
) {
    val aspectRatio: Float
        get() = if (width > 0 && height > 0) width.toFloat() / height.toFloat() else 1f
}

data class ImageUrls(
    val raw: String,
    val full: String,
    val regular: String,
    val small: String,
    val thumb: String
)

data class User(
    val id: String,
    val username: String,
    val name: String,
    val profileImage: ProfileImage? = null
)

data class ProfileImage(
    val small: String,
    val medium: String,
    val large: String
)
