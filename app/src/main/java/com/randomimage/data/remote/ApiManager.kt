package com.randomimage.data.remote

import android.content.Context
import com.randomimage.domain.model.ImageModel
import okhttp3.OkHttpClient
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ApiManager @Inject constructor(
    private val okHttpClient: OkHttpClient
) {
    private var customApis: List<CustomApiImageApi> = emptyList()
    private var currentIndex = 0

    val currentApi: ImageApi
        get() = allApis.getOrElse(currentIndex) { allApis.firstOrNull() ?: NoOpApi() }

    val allApis: List<ImageApi>
        get() = customApis.filter { it.config.enabled }

    val availableApis: List<ImageApi> get() = allApis

    fun init(context: Context) {
        CustomApiManager.init(context)
        refreshCustomApis()
    }

    fun refreshCustomApis() {
        customApis = CustomApiManager.getCustomApis()
            .filter { it.enabled }
            .map { CustomApiImageApi(it, okHttpClient) }
        if (currentIndex >= allApis.size) {
            currentIndex = 0
        }
    }

    fun switchApi(index: Int) {
        if (index in allApis.indices) {
            currentIndex = index
        }
    }

    fun switchApiByName(name: String) {
        val index = allApis.indexOfFirst { it.name == name }
        if (index >= 0) {
            currentIndex = index
        }
    }

    suspend fun fetchRandomImages(count: Int = 10): List<ImageModel> {
        return currentApi.fetchRandomImages(count)
    }

    suspend fun searchImages(query: String, count: Int = 10): List<ImageModel> {
        return currentApi.searchImages(query, count)
    }

    suspend fun fetchRandomImagesNSFW(count: Int = 10): List<ImageModel> {
        return currentApi.fetchRandomImagesNSFW(count)
    }
}

private class NoOpApi : ImageApi {
    override val name = "未配置"
    override val supportsSearch = false
    override val supportsNSFW = false
    override suspend fun fetchRandomImages(count: Int) = emptyList<ImageModel>()
    override suspend fun searchImages(query: String, count: Int) = emptyList<ImageModel>()
    override suspend fun fetchRandomImagesNSFW(count: Int) = emptyList<ImageModel>()
}
