package com.randomimage.data.remote

import com.randomimage.domain.model.ImageModel
import com.randomimage.domain.model.ImageUrls
import com.randomimage.domain.model.User
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import timber.log.Timber
import java.util.concurrent.atomic.AtomicInteger

class CustomApiImageApi(
    val config: CustomApiConfig,
    private val client: OkHttpClient
) : ImageApi {

    override val name: String get() = config.name
    override val supportsSearch: Boolean = false
    override val supportsNSFW: Boolean = true

    private val counter = AtomicInteger(0)

    override suspend fun fetchRandomImages(count: Int): List<ImageModel> {
        return fetchImages(count)
    }

    override suspend fun searchImages(query: String, count: Int): List<ImageModel> {
        return emptyList()
    }

    override suspend fun fetchRandomImagesNSFW(count: Int): List<ImageModel> {
        return fetchImages(count)
    }

    private fun fetchImages(count: Int): List<ImageModel> {
        val apiType = try { ApiType.valueOf(config.apiType) } catch (_: Exception) { ApiType.AUTO }

        return when (apiType) {
            ApiType.DIRECT_IMAGE -> fetchDirectImages(count)
            ApiType.LOLICON -> fetchLoliconImages(count)
            ApiType.AUTO -> fetchAutoDetect(count)
        }
    }

    private fun fetchDirectImages(count: Int): List<ImageModel> {
        return try {
            val baseUrl = config.url.trimEnd('/')
            val images = mutableListOf<ImageModel>()

            repeat(count) {
                val c = counter.incrementAndGet()
                val request = Request.Builder()
                    .url(baseUrl)
                    .header("User-Agent", "RandomImageApp/1.0")
                    .build()

                val response = client.newCall(request).execute()
                val imageUrl = response.request.url.toString()
                response.close()

                images.add(ImageModel(
                    id = "custom_${config.id}_${System.nanoTime()}_$c",
                    urls = ImageUrls(raw = imageUrl, full = imageUrl, regular = imageUrl, small = imageUrl, thumb = imageUrl),
                    user = User(id = config.id, username = config.name, name = config.name),
                    description = "${config.name} #$c",
                    likes = 0
                ))
            }
            images
        } catch (e: Exception) {
            Timber.e(e, "Custom API ${config.name} direct failed")
            emptyList()
        }
    }

    private fun fetchLoliconImages(count: Int): List<ImageModel> {
        return try {
            val baseUrl = config.url.trimEnd('/')
            val images = mutableListOf<ImageModel>()

            repeat(count) {
                val c = counter.incrementAndGet()
                val hasQuery = baseUrl.contains("?")
                val separator = if (hasQuery) "&" else "?"
                val url = "${baseUrl}${separator}r18=0&num=1&size=original"
                val request = Request.Builder()
                    .url(url)
                    .header("User-Agent", "RandomImageApp/1.0")
                    .build()

                val response = client.newCall(request).execute()
                val body = response.body?.string() ?: ""
                response.close()

                val json = JSONObject(body)
                if (json.optString("error") == "success") {
                    val dataArray = json.optJSONArray("data")
                    if (dataArray != null && dataArray.length() > 0) {
                        val item = dataArray.getJSONObject(0)
                        val urls = item.optJSONObject("urls")
                        if (urls != null) {
                            val originalUrl = urls.optString("original", "")
                            val smallUrl = urls.optString("small", originalUrl)
                            val thumbUrl = urls.optString("thumb", smallUrl)
                            if (originalUrl.isNotEmpty()) {
                                images.add(ImageModel(
                                    id = "custom_${config.id}_${item.optLong("pid")}_${item.optInt("p")}_$c",
                                    urls = ImageUrls(raw = originalUrl, full = originalUrl, regular = originalUrl, small = smallUrl, thumb = thumbUrl),
                                    user = User(id = item.optLong("uid").toString(), username = item.optString("author", config.name), name = item.optString("author", config.name)),
                                    description = item.optString("title"),
                                    likes = 0,
                                    width = item.optInt("width"),
                                    height = item.optInt("height"),
                                    tags = parseJsonArrayToStringList(item.optJSONArray("tags"))
                                ))
                            }
                        }
                    }
                }
            }
            images
        } catch (e: Exception) {
            Timber.e(e, "Custom API ${config.name} lolicon failed")
            emptyList()
        }
    }

    private fun fetchAutoDetect(count: Int): List<ImageModel> {
        return try {
            val baseUrl = config.url.trimEnd('/')
            val images = mutableListOf<ImageModel>()

            repeat(count) {
                val c = counter.incrementAndGet()
                val request = Request.Builder()
                    .url(baseUrl)
                    .header("User-Agent", "RandomImageApp/1.0")
                    .build()

                val response = client.newCall(request).execute()
                val contentType = response.header("Content-Type") ?: ""

                if (contentType.contains("image/")) {
                    val imageUrl = response.request.url.toString()
                    images.add(makeImageModel(imageUrl, c))
                    response.close()
                } else if (contentType.contains("json")) {
                    val body = response.body?.string() ?: ""
                    response.close()
                    val parsed = parseGenericJson(body, c)
                    images.addAll(parsed)
                } else {
                    val imageUrl = response.request.url.toString()
                    images.add(makeImageModel(imageUrl, c))
                    response.close()
                }
            }
            images
        } catch (e: Exception) {
            Timber.e(e, "Custom API ${config.name} auto failed")
            emptyList()
        }
    }

    private fun parseGenericJson(body: String, counter: Int): List<ImageModel> {
        val images = mutableListOf<ImageModel>()
        try {
            val trimmed = body.trim()

            if (trimmed.startsWith("[")) {
                val array = JSONArray(trimmed)
                if (array.length() > 0) {
                    val item = array.getJSONObject(0)
                    val url = extractUrlFromJson(item)
                    if (url != null) {
                        images.add(makeImageModel(url, counter, item))
                    }
                }
            } else if (trimmed.startsWith("{")) {
                val json = JSONObject(trimmed)

                // Try Lolicon format first
                if (json.has("error") && json.has("data")) {
                    images.addAll(parseLoliconFormat(json, counter))
                } else {
                    // Try to extract URL from the object itself
                    val url = extractUrlFromJson(json)
                    if (url != null) {
                        images.add(makeImageModel(url, counter, json))
                    }
                }
            }
        } catch (e: Exception) {
            Timber.w(e, "Failed to parse generic JSON")
        }
        return images
    }

    private fun parseLoliconFormat(json: JSONObject, counter: Int): List<ImageModel> {
        val images = mutableListOf<ImageModel>()
        try {
            val dataArray = json.optJSONArray("data") ?: return images
            val item = dataArray.optJSONObject(0) ?: return images
            val urls = item.optJSONObject("urls") ?: return images
            val originalUrl = urls.optString("original", "")
            val smallUrl = urls.optString("small", originalUrl)
            val thumbUrl = urls.optString("thumb", smallUrl)

            if (originalUrl.isNotEmpty()) {
                images.add(ImageModel(
                    id = "custom_${config.id}_${item.optLong("pid")}_${item.optInt("p")}_$counter",
                    urls = ImageUrls(raw = originalUrl, full = originalUrl, regular = originalUrl, small = smallUrl, thumb = thumbUrl),
                    user = User(id = item.optLong("uid").toString(), username = item.optString("author", config.name), name = item.optString("author", config.name)),
                    description = item.optString("title"),
                    likes = 0,
                    width = item.optInt("width"),
                    height = item.optInt("height"),
                    tags = parseJsonArrayToStringList(item.optJSONArray("tags"))
                ))
            }
        } catch (e: Exception) {
            Timber.w(e, "Failed to parse Lolicon format")
        }
        return images
    }

    private fun extractUrlFromJson(json: JSONObject): String? {
        val urlFields = listOf("url", "download_url", "src", "image", "image_url", "link", "file", "path")
        for (field in urlFields) {
            val value = json.optString(field, "")
            if (value.isNotEmpty() && (value.startsWith("http") || value.startsWith("/"))) {
                return value
            }
        }
        // Check nested objects
        val nestedFields = listOf("urls", "images", "data", "file", "image")
        for (field in nestedFields) {
            val obj = json.optJSONObject(field)
            if (obj != null) {
                val nestedUrl = extractUrlFromJson(obj)
                if (nestedUrl != null) return nestedUrl
            }
        }
        return null
    }

    private fun makeImageModel(url: String, counter: Int, json: JSONObject? = null): ImageModel {
        return ImageModel(
            id = "custom_${config.id}_${System.nanoTime()}_$counter",
            urls = ImageUrls(raw = url, full = url, regular = url, small = url, thumb = url),
            user = User(id = config.id, username = config.name, name = config.name),
            description = json?.optString("title") ?: json?.optString("description") ?: "${config.name} #$counter",
            likes = 0,
            width = json?.optInt("width") ?: 0,
            height = json?.optInt("height") ?: 0
        )
    }

    private fun parseJsonArrayToStringList(array: JSONArray?): List<String> {
        if (array == null) return emptyList()
        return (0 until array.length()).mapNotNull { array.optString(it) }
    }
}
