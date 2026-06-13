package com.randomimage.data.remote

import com.randomimage.domain.model.ImageModel
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ApiManager @Inject constructor(
    private val loliconApi: LoliconImageApi,
    private val moeImgApi: MoeImgImageApi,
    private val sexPhotoApi: SexPhotoImageApi,
    private val koriApi: KoriImageApi,
    private val xjhApi: XjhImageApi,
    private val mwmApi: MwmImageApi
) {
    private val apis = listOf(loliconApi, moeImgApi, sexPhotoApi, koriApi, xjhApi, mwmApi)
    private var currentIndex = 0

    val currentApi: ImageApi get() = apis[currentIndex]
    val availableApis: List<ImageApi> get() = apis

    fun switchApi(index: Int) {
        if (index in apis.indices) {
            currentIndex = index
        }
    }

    fun switchApiByName(name: String) {
        val index = apis.indexOfFirst { it.name == name }
        if (index >= 0) {
            currentIndex = index
        }
    }

    suspend fun fetchRandomImages(count: Int = 10): List<ImageModel> {
        return currentApi.fetchRandomImages(count)
    }

    suspend fun searchImages(query: String, count: Int = 10): List<ImageModel> {
        return if (currentApi.supportsSearch) {
            currentApi.searchImages(query, count)
        } else {
            emptyList()
        }
    }

    suspend fun fetchRandomImagesNSFW(count: Int = 10): List<ImageModel> {
        return if (currentApi.supportsNSFW) {
            currentApi.fetchRandomImagesNSFW(count)
        } else {
            fetchRandomImages(count)
        }
    }
}
