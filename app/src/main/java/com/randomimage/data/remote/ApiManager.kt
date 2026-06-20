package com.randomimage.data.remote

import android.content.Context
import com.randomimage.domain.model.ImageModel
import okhttp3.OkHttpClient
import timber.log.Timber
import java.io.File
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ApiManager @Inject constructor(
    private val okHttpClient: OkHttpClient,
    private val customApiManager: CustomApiManager
) {
    @Volatile
    private var customApis: List<CustomApiImageApi> = emptyList()
    private val currentIndex = AtomicInteger(0)
    private var cacheDir: File? = null

    val currentApi: ImageApi
        get() {
            val apis = allApis
            val idx = currentIndex.get()
            return apis.getOrElse(idx) { apis.firstOrNull() ?: NoOpApi() }
        }

    val allApis: List<ImageApi>
        get() {
            val enabledApis = customApis.filter { it.config.enabled }
            return listOf(AggregateApi(enabledApis)) + enabledApis
        }

    val availableApis: List<ImageApi> get() = allApis

    fun init(context: Context) {
        customApiManager.init(context)
        cacheDir = context.cacheDir
        refreshCustomApis()
    }

    fun refreshCustomApis() {
        customApis = customApiManager.getCustomApis()
            .filter { it.enabled }
            .map { CustomApiImageApi(it, okHttpClient, cacheDir) }
        val maxIndex = allApis.size - 1
        if (currentIndex.get() > maxIndex) {
            currentIndex.set(0)
        }
    }

    fun switchApi(index: Int) {
        if (index in allApis.indices) {
            currentIndex.set(index)
        }
    }

    fun switchApiByName(name: String) {
        val index = allApis.indexOfFirst { it.name == name }
        if (index >= 0) {
            currentIndex.set(index)
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

private class AggregateApi(private val apis: List<ImageApi>) : ImageApi {
    override val name = "聚合"
    override val supportsSearch = apis.any { it.supportsSearch }
    override val supportsNSFW = apis.any { it.supportsNSFW }

    private fun getRandomApi(): ImageApi {
        return apis.filter { it.name != "聚合" }.randomOrNull() ?: NoOpApi()
    }

    override suspend fun fetchRandomImages(count: Int): List<ImageModel> {
        val api = getRandomApi()
        return api.fetchRandomImages(count)
    }

    override suspend fun searchImages(query: String, count: Int): List<ImageModel> {
        val api = getRandomApi()
        return api.searchImages(query, count)
    }

    override suspend fun fetchRandomImagesNSFW(count: Int): List<ImageModel> {
        val api = getRandomApi()
        return api.fetchRandomImagesNSFW(count)
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
