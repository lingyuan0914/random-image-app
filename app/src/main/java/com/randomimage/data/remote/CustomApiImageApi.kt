package com.randomimage.data.remote

import com.randomimage.domain.model.ImageModel
import com.randomimage.domain.model.ImageUrls
import com.randomimage.domain.model.User
import com.squareup.moshi.JsonClass
import okhttp3.OkHttpClient
import okhttp3.Request
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import timber.log.Timber

@JsonClass(generateAdapter = true)
data class CustomLoliconResponse(
    val error: String?,
    val data: List<CustomLoliconItem>?
)

@JsonClass(generateAdapter = true)
data class CustomLoliconItem(
    val pid: Long?,
    val p: Int?,
    val uid: Long?,
    val title: String?,
    val author: String?,
    val width: Int?,
    val height: Int?,
    val ext: String?,
    val urls: CustomLoliconUrls?
)

@JsonClass(generateAdapter = true)
data class CustomLoliconUrls(
    val original: String?,
    val small: String?,
    val thumb: String?
)

class CustomApiImageApi(
    val config: CustomApiConfig,
    private val client: OkHttpClient
) : ImageApi {

    override val name: String get() = config.name
    override val supportsSearch: Boolean = false
    override val supportsNSFW: Boolean = true

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    override suspend fun fetchRandomImages(count: Int): List<ImageModel> {
        return fetchImages(count, r18 = 0)
    }

    override suspend fun searchImages(query: String, count: Int): List<ImageModel> {
        return emptyList()
    }

    override suspend fun fetchRandomImagesNSFW(count: Int): List<ImageModel> {
        return fetchImages(count, r18 = 1)
    }

    private fun fetchImages(count: Int, r18: Int): List<ImageModel> {
        return try {
            val baseUrl = config.url.trimEnd('/')
            val url = "$baseUrl?r18=$r18&num=$count&size=original"
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "RandomImageApp/1.0")
                .build()

            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: return emptyList()

            if (!response.isSuccessful) {
                Timber.w("Custom API ${config.name}: HTTP ${response.code}")
                return emptyList()
            }

            val adapter = moshi.adapter(CustomLoliconResponse::class.java)
            val parsed = adapter.fromJson(body) ?: return emptyList()

            if (parsed.error != "success" || parsed.data.isNullOrEmpty()) {
                Timber.w("Custom API ${config.name}: error=${parsed.error}, data.size=${parsed.data?.size}")
                return emptyList()
            }

            parsed.data.mapNotNull { item ->
                val urls = item.urls ?: return@mapNotNull null
                val originalUrl = urls.original ?: return@mapNotNull null
                val smallUrl = urls.small ?: originalUrl
                val thumbUrl = urls.thumb ?: smallUrl

                ImageModel(
                    id = "custom_${config.id}_${item.pid}_${item.p}_${System.nanoTime()}",
                    urls = ImageUrls(
                        raw = originalUrl,
                        full = originalUrl,
                        regular = originalUrl,
                        small = smallUrl,
                        thumb = thumbUrl
                    ),
                    user = User(
                        id = (item.uid ?: 0).toString(),
                        username = item.author ?: config.name,
                        name = item.author ?: config.name
                    ),
                    description = item.title,
                    likes = 0,
                    width = item.width ?: 0,
                    height = item.height ?: 0
                )
            }
        } catch (e: Exception) {
            Timber.e(e, "Custom API ${config.name} failed")
            emptyList()
        }
    }
}
