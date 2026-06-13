package com.randomimage.data.remote

import com.randomimage.domain.model.ImageModel
import com.randomimage.domain.model.ImageUrls
import com.randomimage.domain.model.User
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.http.GET
import retrofit2.http.Query

interface GelbooruService {
    @GET("index.php?page=dapi&s=post&q=index&json=1")
    suspend fun getPosts(
        @Query("limit") limit: Int = 20,
        @Query("tags") tags: String? = null,
        @Query("pid") page: Int = 1
    ): GelbooruResponse
}

@JsonClass(generateAdapter = true)
data class GelbooruResponse(
    @Json(name = "post") val posts: List<GelbooruPost>
)

@JsonClass(generateAdapter = true)
data class GelbooruPost(
    val id: Long,
    val tags: String,
    @Json(name = "file_url") val file_url: String,
    @Json(name = "preview_url") val preview_url: String,
    @Json(name = "sample_url") val sample_url: String,
    val width: Int,
    val height: Int,
    val owner: String
) {
    fun toImageModel(): ImageModel {
        return ImageModel(
            id = "gelbooru_$id",
            urls = ImageUrls(
                raw = file_url,
                full = file_url,
                regular = sample_url,
                small = preview_url,
                thumb = preview_url
            ),
            user = User(
                id = id.toString(),
                username = owner,
                name = owner
            ),
            description = tags.split(" ").take(5).joinToString(" "),
            likes = 0
        )
    }
}

class GelbooruImageApi(
    private val service: GelbooruService
) : ImageApi {
    override val name = "Gelbooru"
    override val supportsSearch = true
    override val supportsNSFW = true

    override suspend fun fetchRandomImages(count: Int): List<ImageModel> {
        return try {
            val page = (1..50).random()
            service.getPosts(limit = count, page = page).posts.map { it.toImageModel() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun searchImages(query: String, count: Int): List<ImageModel> {
        return try {
            service.getPosts(limit = count, tags = query).posts.map { it.toImageModel() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun fetchRandomImagesNSFW(count: Int): List<ImageModel> {
        return try {
            val page = (1..50).random()
            service.getPosts(limit = count, tags = "rating:explicit", page = page).posts.map { it.toImageModel() }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
