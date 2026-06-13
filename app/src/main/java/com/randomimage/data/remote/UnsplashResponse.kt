package com.randomimage.data.remote

import com.randomimage.domain.model.ImageModel
import com.randomimage.domain.model.ImageUrls
import com.randomimage.domain.model.User
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UnsplashPhoto(
    val id: String,
    val urls: UnsplashUrls,
    val user: UnsplashUser,
    val description: String? = null,
    val likes: Int = 0
) {
    fun toImageModel(): ImageModel {
        return ImageModel(
            id = id,
            urls = ImageUrls(
                raw = urls.raw,
                full = urls.full,
                regular = urls.regular,
                small = urls.small,
                thumb = urls.thumb
            ),
            user = User(
                id = user.id,
                username = user.username,
                name = user.name,
                profileImage = user.profileImage?.let {
                    com.randomimage.domain.model.ProfileImage(
                        small = it.small,
                        medium = it.medium,
                        large = it.large
                    )
                }
            ),
            description = description,
            likes = likes
        )
    }
}

@JsonClass(generateAdapter = true)
data class UnsplashUrls(
    val raw: String,
    val full: String,
    val regular: String,
    val small: String,
    val thumb: String
)

@JsonClass(generateAdapter = true)
data class UnsplashUser(
    val id: String,
    val username: String,
    val name: String,
    @Json(name = "profile_image") val profileImage: UnsplashProfileImage? = null
)

@JsonClass(generateAdapter = true)
data class UnsplashProfileImage(
    val small: String,
    val medium: String,
    val large: String
)

@JsonClass(generateAdapter = true)
data class UnsplashSearchResponse(
    val results: List<UnsplashPhoto>
)