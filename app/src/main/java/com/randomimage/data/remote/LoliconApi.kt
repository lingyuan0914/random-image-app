package com.randomimage.data.remote

import com.randomimage.domain.model.ImageModel
import com.randomimage.domain.model.ImageUrls
import com.randomimage.domain.model.User
import com.squareup.moshi.JsonClass
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
        val imageUrl = urls.original ?: ""
        return ImageModel(
            id = "lolicon_${pid}_${p}_${System.nanoTime()}",
            urls = ImageUrls(
                raw = imageUrl,
                full = imageUrl,
                regular = imageUrl,
                small = imageUrl,
                thumb = imageUrl
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
        return try {
            val response = service.getImages(r18 = 0, num = count)
            Timber.d("Lolicon API response: error=${response.error}, data.size=${response.data.size}")
            response.data.map { it.toImageModel() }
        } catch (e: Exception) {
            Timber.e(e, "Lolicon API failed")
            emptyList()
        }
    }

    override suspend fun searchImages(query: String, count: Int): List<ImageModel> {
        return try {
            service.getImages(r18 = 0, num = count, keyword = query).data.map { it.toImageModel() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun fetchRandomImagesNSFW(count: Int): List<ImageModel> {
        return try {
            service.getImages(r18 = 1, num = count).data.map { it.toImageModel() }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
