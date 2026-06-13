package com.randomimage.data.remote

import com.randomimage.domain.model.ImageModel
import com.randomimage.domain.model.ImageUrls
import com.randomimage.domain.model.User

class XjhImageApi : ImageApi {
    override val name = "随机美图"
    override val supportsSearch = false
    override val supportsNSFW = false

    private var counter = 0

    override suspend fun fetchRandomImages(count: Int): List<ImageModel> {
        return try {
            val images = mutableListOf<ImageModel>()
            repeat(count) {
                counter++
                val timestamp = System.currentTimeMillis()
                images.add(
                    ImageModel(
                        id = "xjh_${timestamp}_${counter}_${(0..9999).random()}",
                        urls = ImageUrls(
                            raw = "https://img.xjh.me/random_img.php?return=302&t=$timestamp&_c=$counter",
                            full = "https://img.xjh.me/random_img.php?return=302&t=$timestamp&_c=$counter",
                            regular = "https://img.xjh.me/random_img.php?return=302&t=$timestamp&_c=$counter",
                            small = "https://img.xjh.me/random_img.php?return=302&t=$timestamp&_c=$counter",
                            thumb = "https://img.xjh.me/random_img.php?return=302&t=$timestamp&_c=$counter"
                        ),
                        user = User(
                            id = "xjh",
                            username = "XJH",
                            name = "随机美图"
                        ),
                        description = "随机二次元美图 #$counter",
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
}
