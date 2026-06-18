package com.randomimage.data.remote

import com.randomimage.domain.model.ImageModel
import com.randomimage.domain.model.ImageUrls
import com.randomimage.domain.model.User
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.Request
import timber.log.Timber
import java.util.concurrent.atomic.AtomicInteger

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
    val urls: CustomLoliconUrls?,
    val tags: List<String>? = null
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

    private val counter = AtomicInteger(0)
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
            val images = mutableListOf<ImageModel>()

            repeat(count) {
                val c = counter.incrementAndGet()
                val hasQuery = baseUrl.contains("?")
                val separator = if (hasQuery) "&" else "?"
                val url = "${baseUrl}${separator}r18=$r18&num=1&size=original"
                val request = Request.Builder()
                    .url(url)
                    .header("User-Agent", "RandomImageApp/1.0")
                    .build()

                val response = client.newCall(request).execute()
                val contentType = response.header("Content-Type") ?: ""

                if (contentType.contains("image/")) {
                    // Direct image response (e.g., Elaina API)
                    val imageUrl = response.request.url.toString()
                    images.add(ImageModel(
                        id = "custom_${config.id}_${System.nanoTime()}_$c",
                        urls = ImageUrls(raw = imageUrl, full = imageUrl, regular = imageUrl, small = imageUrl, thumb = imageUrl),
                        user = User(id = config.id, username = config.name, name = config.name),
                        description = "${config.name} #$c",
                        likes = 0
                    ))
                    response.close()
                } else if (contentType.contains("json")) {
                    // Lolicon-compatible JSON response
                    val body = response.body?.string() ?: ""
                    response.close()
                    val adapter = moshi.adapter(CustomLoliconResponse::class.java)
                    val parsed = adapter.fromJson(body) ?: return@repeat

                    if (parsed.error == "success" && !parsed.data.isNullOrEmpty()) {
                        val item = parsed.data.first()
                        val urls = item.urls ?: return@repeat
                        val originalUrl = urls.original ?: return@repeat
                        val smallUrl = urls.small ?: originalUrl
                        val thumbUrl = urls.thumb ?: smallUrl
                        images.add(ImageModel(
                            id = "custom_${config.id}_${item.pid}_${item.p}_${System.nanoTime()}",
                            urls = ImageUrls(raw = originalUrl, full = originalUrl, regular = originalUrl, small = smallUrl, thumb = thumbUrl),
                            user = User(id = (item.uid ?: 0).toString(), username = item.author ?: config.name, name = item.author ?: config.name),
                            description = item.title,
                            likes = 0,
                            width = item.width ?: 0,
                            height = item.height ?: 0,
                            tags = item.tags?.toList() ?: listOf()
                        ))
                    }
                } else {
                    // Unknown content type, try to use URL directly
                    val imageUrl = response.request.url.toString()
                    images.add(ImageModel(
                        id = "custom_${config.id}_${System.nanoTime()}_$c",
                        urls = ImageUrls(raw = imageUrl, full = imageUrl, regular = imageUrl, small = imageUrl, thumb = imageUrl),
                        user = User(id = config.id, username = config.name, name = config.name),
                        description = "${config.name} #$c",
                        likes = 0
                    ))
                    response.close()
                }
            }
            images
        } catch (e: Exception) {
            Timber.e(e, "Custom API ${config.name} failed")
            emptyList()
        }
    }
}
