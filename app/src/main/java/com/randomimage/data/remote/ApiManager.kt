package com.randomimage.data.remote

import android.content.Context
import com.randomimage.domain.model.ImageModel
import okhttp3.OkHttpClient
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ApiManager @Inject constructor(
    private val loliconApi: LoliconImageApi,
    private val moeImgApi: MoeImgImageApi,
    private val sexPhotoApi: SexPhotoImageApi,
    private val koriApi: KoriImageApi,
    private val xjhApi: XjhImageApi,
    private val mwmApi: MwmImageApi,
    private val okHttpClient: OkHttpClient
) {
    private val builtInApis = listOf(loliconApi, moeImgApi, sexPhotoApi, koriApi, xjhApi, mwmApi)
    private var customApis: List<CustomApiImageApi> = emptyList()
    private var currentIndex = 0

    val currentApi: ImageApi
        get() {
            val all = allApis
            return all.getOrElse(currentIndex) { builtInApis.first() }
        }

    val allApis: List<ImageApi>
        get() = builtInApis + customApis.filter { it.config.enabled }

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
