package com.randomimage.data.remote

import com.randomimage.domain.model.ImageModel
import com.randomimage.domain.model.ImageUrls
import com.randomimage.domain.model.User
import com.squareup.moshi.JsonClass
import retrofit2.http.GET
import retrofit2.http.Query

interface KoriService {
    @GET("img")
    suspend fun getImage(
        @Query("return") returnType: String = "json",
        @Query("category") category: String? = null
    ): KoriResponse
}

@JsonClass(generateAdapter = true)
data class KoriResponse(
    val code: String?,
    val imgurl: String?,
    val width: Int?,
    val height: Int?
) {
    fun toImageModel(counter: Int): ImageModel {
        return ImageModel(
            id = "kori_${System.currentTimeMillis()}_${counter}_${(0..9999).random()}",
            urls = ImageUrls(
                raw = imgurl ?: "",
                full = imgurl ?: "",
                regular = imgurl ?: "",
                small = imgurl ?: "",
                thumb = imgurl ?: ""
            ),
            user = User(
                id = "kori",
                username = "Kori API",
                name = "Random"
            ),
            description = "Kori Random Image #$counter",
            likes = 0
        )
    }
}

class KoriImageApi(
    private val service: KoriService
) : ImageApi {
    override val name = "Kori图库"
    override val supportsSearch = false
    override val supportsNSFW = true

    private var counter = 0

    override suspend fun fetchRandomImages(count: Int): List<ImageModel> {
        return try {
            val images = mutableListOf<ImageModel>()
            repeat(count) {
                counter++
                try {
                    val response = service.getImage(returnType = "json")
                    if (response.code == "200" && response.imgurl != null) {
                        images.add(response.toImageModel(counter))
                    }
                } catch (e: Exception) {
                    // skip failed request
                }
            }
            images
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun searchImages(query: String, count: Int): List<ImageModel> {
        return fetchRandomImages(count)
    }

    override suspend fun fetchRandomImagesNSFW(count: Int): List<ImageModel> {
        return try {
            val images = mutableListOf<ImageModel>()
            repeat(count) {
                counter++
                try {
                    val response = service.getImage(returnType = "json", category = "R18")
                    if (response.code == "200" && response.imgurl != null) {
                        images.add(response.toImageModel(counter))
                    }
                } catch (e: Exception) {
                    // skip failed request
                }
            }
            images
        } catch (e: Exception) {
            emptyList()
        }
    }
}
