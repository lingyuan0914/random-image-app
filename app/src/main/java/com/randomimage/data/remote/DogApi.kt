package com.randomimage.data.remote

import com.randomimage.domain.model.ImageModel
import com.randomimage.domain.model.ImageUrls
import com.randomimage.domain.model.User
import com.squareup.moshi.JsonClass
import retrofit2.http.GET

interface DogService {
    @GET("breeds/image/random")
    suspend fun getRandomImage(): DogResponse

    @GET("breeds/image/random/10")
    suspend fun getImages(): DogListResponse
}

@JsonClass(generateAdapter = true)
data class DogResponse(
    val message: String,
    val status: String
)

@JsonClass(generateAdapter = true)
data class DogListResponse(
    val message: List<String>,
    val status: String
)

class DogImageApi(
    private val service: DogService
) : ImageApi {
    override val name = "Dog API"
    override val supportsSearch = false
    override val supportsNSFW = false

    override suspend fun fetchRandomImages(count: Int): List<ImageModel> {
        return try {
            val images = mutableListOf<ImageModel>()
            repeat(count) {
                val response = service.getRandomImage()
                images.add(
                    ImageModel(
                        id = "dog_${response.message.hashCode()}",
                        urls = ImageUrls(
                            raw = response.message,
                            full = response.message,
                            regular = response.message,
                            small = response.message,
                            thumb = response.message
                        ),
                        user = User(
                            id = "dog_api",
                            username = "DogCEO",
                            name = "Dog"
                        ),
                        description = "Random Dog",
                        likes = 0
                    )
                )
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
        return fetchRandomImages(count)
    }
}
