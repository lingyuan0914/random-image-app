package com.randomimage.data.remote

import com.randomimage.domain.model.ImageModel
import com.randomimage.domain.model.ImageUrls
import com.randomimage.domain.model.User
import com.squareup.moshi.JsonClass
import retrofit2.http.GET
import retrofit2.http.Query

interface YandeService {
    @GET("post.json")
    suspend fun getPosts(
        @Query("limit") limit: Int = 20,
        @Query("tags") tags: String? = null,
        @Query("page") page: Int = 1
    ): List<YandePost>
}

@JsonClass(generateAdapter = true)
data class YandePost(
    val id: Long,
    val tags: String,
    val file_url: String?,
    val preview_url: String?,
    val sample_url: String?,
    val width: Int,
    val height: Int,
    val author: String
) {
    fun toImageModel(): ImageModel {
        val url = sample_url ?: file_url ?: preview_url ?: ""
        return ImageModel(
            id = "yande_$id",
            urls = ImageUrls(
                raw = url,
                full = file_url ?: url,
                regular = sample_url ?: url,
                small = preview_url ?: url,
                thumb = preview_url ?: url
            ),
            user = User(
                id = id.toString(),
                username = author,
                name = author
            ),
            description = tags.split(" ").take(5).joinToString(" "),
            likes = 0
        )
    }
}

class YandeImageApi(
    private val service: YandeService
) : ImageApi {
    override val name = "Yande.re"
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
