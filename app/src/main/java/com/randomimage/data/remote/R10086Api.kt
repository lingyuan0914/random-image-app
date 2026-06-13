package com.randomimage.data.remote

import com.randomimage.domain.model.ImageModel
import com.randomimage.domain.model.ImageUrls
import com.randomimage.domain.model.User

class R10086ImageApi : ImageApi {
    override val name = "樱道图库"
    override val supportsSearch = false
    override val supportsNSFW = false

    private val series = listOf(
        "少女写真1",
        "少女写真2",
        "P站系列1",
        "P站系列2",
        "P站系列3",
        "二次元1",
        "二次元2",
        "cosplay1",
        "cosplay2"
    )

    private val baseUrl = "https://api.r10086.com/樱道随机图片api接口.php"

    override suspend fun fetchRandomImages(count: Int): List<ImageModel> {
        return try {
            val images = mutableListOf<ImageModel>()
            repeat(count) {
                val seriesName = series.random()
                val url = "$baseUrl?图片系列=$seriesName"
                images.add(
                    ImageModel(
                        id = "r10086_${System.currentTimeMillis()}_$it",
                        urls = ImageUrls(
                            raw = url,
                            full = url,
                            regular = url,
                            small = url,
                            thumb = url
                        ),
                        user = User(
                            id = "r10086",
                            username = "樱道图库",
                            name = seriesName
                        ),
                        description = "樱道图库 - $seriesName",
                        likes = 0
                    )
                )
            }
            images
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun searchImages(query: String, count: Int): List<ImageModel> {
        return fetchRandomImages(count)
    }

    override suspend fun fetchRandomImagesNSFW(count: Int): List<ImageModel> {
        return fetchRandomImages(count)
    }

    fun getSeries(): List<String> = series
}
