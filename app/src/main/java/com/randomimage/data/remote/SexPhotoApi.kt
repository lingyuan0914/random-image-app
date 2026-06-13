package com.randomimage.data.remote

import com.randomimage.domain.model.ImageModel
import com.randomimage.domain.model.ImageUrls
import com.randomimage.domain.model.User
import com.squareup.moshi.JsonClass
import retrofit2.http.GET
import retrofit2.http.Query

interface SexPhotoService {
    @GET("api/v2/")
    suspend fun getImages(
        @Query("r18") r18: Boolean = false,
        @Query("num") num: Int = 10,
        @Query("keyword") keyword: String? = null,
        @Query("tag") tag: List<String>? = null
    ): SexPhotoResponse
}

@JsonClass(generateAdapter = true)
data class SexPhotoResponse(
    val success: Boolean,
    val status: Int,
    val message: String,
    val data: List<SexPhotoItem>?
)

@JsonClass(generateAdapter = true)
data class SexPhotoItem(
    val pid: Long,
    val page: String?,
    val author: String,
    val author_uid: Int,
    val title: String,
    val width: Int,
    val height: Int,
    val file_ext: String,
    val upload_date: Long,
    val tags: List<String>,
    val url: String
) {
    fun toImageModel(): ImageModel {
        return ImageModel(
            id = "sexphoto_$pid",
            urls = ImageUrls(
                raw = url,
                full = url,
                regular = url,
                small = url,
                thumb = url
            ),
            user = User(
                id = author_uid.toString(),
                username = author,
                name = author
            ),
            description = "$title - ${tags.take(3).joinToString(", ")}",
            likes = 0
        )
    }
}

class SexPhotoImageApi(
    private val service: SexPhotoService
) : ImageApi {
    override val name = "色图API"
    override val supportsSearch = true
    override val supportsNSFW = true

    override suspend fun fetchRandomImages(count: Int): List<ImageModel> {
        return try {
            val response = service.getImages(r18 = false, num = minOf(count, 10))
            response.data?.map { it.toImageModel() } ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun searchImages(query: String, count: Int): List<ImageModel> {
        return try {
            val response = service.getImages(r18 = false, num = minOf(count, 10), keyword = query)
            response.data?.map { it.toImageModel() } ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun fetchRandomImagesNSFW(count: Int): List<ImageModel> {
        return try {
            val response = service.getImages(r18 = true, num = minOf(count, 10))
            response.data?.map { it.toImageModel() } ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}
