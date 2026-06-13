package com.randomimage.data.remote

import com.randomimage.domain.model.ImageModel

interface ImageApi {
    val name: String
    val supportsSearch: Boolean
    val supportsNSFW: Boolean

    suspend fun fetchRandomImages(count: Int = 10): List<ImageModel>
    suspend fun searchImages(query: String, count: Int = 10): List<ImageModel>
    suspend fun fetchRandomImagesNSFW(count: Int = 10): List<ImageModel>
}
