package com.randomimage.data.remote

import com.randomimage.domain.model.ImageModel
import com.randomimage.domain.model.ImageUrls
import com.randomimage.domain.model.User
import com.squareup.moshi.JsonClass
import retrofit2.http.GET
import retrofit2.http.Query

interface PicsumService {
    @GET("v2/list")
    suspend fun getImages(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10
    ): List<PicsumItem>
}

@JsonClass(generateAdapter = true)
data class PicsumItem(
    val id: String,
    val author: String,
    val width: Int,
    val height: Int,
    val url: String,
    val download_url: String
) {
    fun toImageModel(): ImageModel {
        return ImageModel(
            id = "picsum_$id",
            urls = ImageUrls(
                raw = download_url,
                full = download_url,
                regular = download_url,
                small = download_url,
                thumb = download_url
            ),
            user = User(
                id = id,
                username = author,
                name = author
            ),
            description = "Photo by $author",
            likes = 0
        )
    }
}

class PicsumImageApi(
    private val service: PicsumService
) : ImageApi {
    override val name = "Lorem Picsum"
    override val supportsSearch = false
    override val supportsNSFW = false

    override suspend fun fetchRandomImages(count: Int): List<ImageModel> {
        return try {
            val page = (1..100).random()
            service.getImages(page = page, limit = count).map { it.toImageModel() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun searchImages(query: String, count: Int): List<ImageModel> {
        return fetchRandomImages(count)
    }

    override suspend fun fetchRandomImagesNSFW(count: Int): List<ImageModel> {
        return fetchRandomImages(count)
    }
}
