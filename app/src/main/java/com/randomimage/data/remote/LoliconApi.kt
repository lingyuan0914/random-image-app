package com.randomimage.data.remote

import com.randomimage.domain.model.ImageModel
import com.randomimage.domain.model.ImageUrls
import com.randomimage.domain.model.User
import com.squareup.moshi.JsonClass
import kotlinx.coroutines.delay
import retrofit2.http.GET
import retrofit2.http.Query
import timber.log.Timber

interface LoliconService {
    @GET("setu/v2")
    suspend fun getImages(
        @Query("r18") r18: Int = 0,
        @Query("num") num: Int = 10,
        @Query("size") size: String = "original",
        @Query("keyword") keyword: String? = null
    ): LoliconResponse
}

@JsonClass(generateAdapter = true)
data class LoliconResponse(
    val error: String,
    val data: List<LoliconItem>
)

@JsonClass(generateAdapter = true)
data class LoliconItem(
    val pid: Long,
    val p: Int,
    val uid: Long,
    val title: String,
    val author: String,
    val r18: Boolean,
    val width: Int,
    val height: Int,
    val tags: List<String>,
    val ext: String,
    val urls: LoliconUrls
) {
    fun toImageModel(): ImageModel {
        val originalUrl = urls.original ?: ""
        val smallUrl = urls.small ?: originalUrl
        val thumbUrl = urls.thumb ?: smallUrl
        return ImageModel(
            id = "lolicon_${pid}_${p}_${System.nanoTime()}",
            urls = ImageUrls(
                raw = originalUrl,
                full = originalUrl,
                regular = originalUrl,
                small = smallUrl,
                thumb = thumbUrl
            ),
            user = User(
                id = uid.toString(),
                username = author,
                name = author
            ),
            description = title,
            likes = 0,
            width = width,
            height = height
        )
    }
}

@JsonClass(generateAdapter = true)
data class LoliconUrls(
    val original: String? = null,
    val regular: String? = null,
    val small: String? = null,
    val thumb: String? = null
)

class LoliconImageApi(
    private val service: LoliconService
) : ImageApi {
    override val name = "Lolicon"
    override val supportsSearch = true
    override val supportsNSFW = true

    override suspend fun fetchRandomImages(count: Int): List<ImageModel> {
        repeat(3) { attempt ->
            if (attempt > 0) {
                val delayMs = 1000L * (1 shl attempt)
                delay(delayMs)
            }
            try {
                val response = service.getImages(r18 = 0, num = count)
                Timber.d("Lolicon API attempt ${attempt + 1}: error=${response.error}, data.size=${response.data.size}")
                if (response.error == "success" && response.data.isNotEmpty()) {
                    return response.data.map { it.toImageModel() }
                }
            } catch (e: Exception) {
                Timber.e(e, "Lolicon API attempt ${attempt + 1} failed")
            }
        }
        Timber.w("Lolicon API: all 3 attempts returned empty")
        return emptyList()
    }

    override suspend fun searchImages(query: String, count: Int): List<ImageModel> {
        return try {
            service.getImages(r18 = 0, num = count, keyword = query).data.map { it.toImageModel() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun fetchRandomImagesNSFW(count: Int): List<ImageModel> {
        repeat(3) { attempt ->
            if (attempt > 0) {
                delay(2000L)
            }
            try {
                val response = service.getImages(r18 = 1, num = count)
                Timber.d("Lolicon NSFW attempt ${attempt + 1}: error=${response.error}, data.size=${response.data.size}")
                if (response.error == "success" && response.data.isNotEmpty()) {
                    return response.data.map { it.toImageModel() }
                }
            } catch (e: Exception) {
                Timber.e(e, "Lolicon NSFW attempt ${attempt + 1} failed")
            }
        }
        return emptyList()
    }
}
