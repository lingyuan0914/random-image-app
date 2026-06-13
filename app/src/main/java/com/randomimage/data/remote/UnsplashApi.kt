package com.randomimage.data.remote

import com.randomimage.BuildConfig
import com.randomimage.domain.model.ImageModel
import retrofit2.http.GET
import retrofit2.http.Query

interface UnsplashService {
    @GET("photos/random")
    suspend fun getRandomPhotos(
        @Query("count") count: Int = 10,
        @Query("client_id") clientId: String
    ): List<UnsplashPhoto>

    @GET("search/photos")
    suspend fun searchPhotos(
        @Query("query") query: String,
        @Query("per_page") perPage: Int = 10,
        @Query("client_id") clientId: String
    ): UnsplashSearchResponse
}

class UnsplashImageApi(
    private val service: UnsplashService
) : ImageApi {
    override val name = "Unsplash"
    override val supportsSearch = true
    override val supportsNSFW = false

    override suspend fun fetchRandomImages(count: Int): List<ImageModel> {
        return try {
            service.getRandomPhotos(count, BuildConfig.UNSPLASH_API_KEY).map { it.toImageModel() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun searchImages(query: String, count: Int): List<ImageModel> {
        return try {
            service.searchPhotos(query, count, BuildConfig.UNSPLASH_API_KEY).results.map { it.toImageModel() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun fetchRandomImagesNSFW(count: Int): List<ImageModel> {
        return fetchRandomImages(count)
    }
}
