package com.randomimage.data.remote

import com.randomimage.domain.model.ImageModel
import com.randomimage.domain.model.ImageUrls
import com.randomimage.domain.model.User
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.http.GET
import retrofit2.http.Query

interface DanbooruService {
    @GET("posts.json")
    suspend fun getPosts(
        @Query("limit") limit: Int = 20,
        @Query("tags") tags: String? = null,
        @Query("page") page: Int = 1
    ): List<DanbooruPost>
}

@JsonClass(generateAdapter = true)
data class DanbooruPost(
    val id: Long,
    val tags: String,
    @Json(name = "file_url") val file_url: String?,
    @Json(name = "preview_file_url") val preview_url: String?,
    @Json(name = "large_file_url") val large_url: String?,
    @Json(name = "image_width") val width: Int,
    @Json(name = "image_height") val height: Int,
    @Json(name = "uploader_name") val author: String?
) {
    fun toImageModel(): ImageModel {
        val url = large_url ?: file_url ?: preview_url ?: ""
        return ImageModel(
            id = "danbooru_$id",
            urls = ImageUrls(
                raw = url,
                full = file_url ?: url,
                regular = large_url ?: url,
                small = preview_url ?: url,
                thumb = preview_url ?: url
            ),
            user = User(
                id = id.toString(),
                username = author ?: "unknown",
                name = author ?: "Unknown"
            ),
            description = tags.split(" ").take(5).joinToString(" "),
            likes = 0
        )
    }
}

class DanbooruImageApi(
    private val service: DanbooruService
) : ImageApi {
    override val name = "Danbooru"
    override val supportsSearch = true
    override val supportsNSFW = true

    override suspend fun fetchRandomImages(count: Int): List<ImageModel> {
        return try {
            val page = (1..50).random()
            service.getPosts(limit = count, page = page).map { it.toImageModel() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun searchImages(query: String, count: Int): List<ImageModel> {
        return try {
            service.getPosts(limit = count, tags = query).map { it.toImageModel() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun fetchRandomImagesNSFW(count: Int): List<ImageModel> {
        return try {
            val page = (1..50).random()
            service.getPosts(limit = count, tags = "rating:explicit", page = page).map { it.toImageModel() }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
