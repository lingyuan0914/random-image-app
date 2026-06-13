package com.randomimage.data.remote

import com.randomimage.domain.model.ImageModel
import com.randomimage.domain.model.ImageUrls
import com.randomimage.domain.model.User
import com.squareup.moshi.JsonClass
import retrofit2.http.GET
import retrofit2.http.Query

interface CatService {
    @GET("v1/images/search")
    suspend fun getImages(
        @Query("limit") limit: Int = 10
    ): List<CatItem>
}

@JsonClass(generateAdapter = true)
data class CatItem(
    val id: String,
    val url: String,
    val width: Int,
    val height: Int
) {
    fun toImageModel(): ImageModel {
        return ImageModel(
            id = "cat_$id",
            urls = ImageUrls(
                raw = url,
                full = url,
                regular = url,
                small = url,
                thumb = url
            ),
            user = User(
                id = "cat_api",
                username = "TheCatAPI",
                name = "Cat"
            ),
            description = "Random Cat",
            likes = 0
        )
    }
}

class CatImageApi(
    private val service: CatService
) : ImageApi {
    override val name = "Cat API"
    override val supportsSearch = false
    override val supportsNSFW = false

    override suspend fun fetchRandomImages(count: Int): List<ImageModel> {
        return try {
            service.getImages(count).map { it.toImageModel() }
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
