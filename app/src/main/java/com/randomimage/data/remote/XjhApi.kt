package com.randomimage.data.remote

import com.randomimage.domain.model.ImageModel
import com.randomimage.domain.model.ImageUrls
import com.randomimage.domain.model.User
import java.util.concurrent.atomic.AtomicInteger

class XjhImageApi : ImageApi {
    override val name = "随机美图"
    override val supportsSearch = false
    override val supportsNSFW = false

    private val counter = AtomicInteger(0)

    override suspend fun fetchRandomImages(count: Int): List<ImageModel> {
        return try {
            val images = mutableListOf<ImageModel>()
            repeat(count) {
                val c = counter.incrementAndGet()
                val timestamp = System.currentTimeMillis()
                images.add(
                    ImageModel(
                        id = "xjh_${timestamp}_${c}_${(0..9999).random()}",
                        urls = ImageUrls(
                            raw = "https://img.xjh.me/random_img.php?return=302&t=$timestamp&_c=$c",
                            full = "https://img.xjh.me/random_img.php?return=302&t=$timestamp&_c=$c",
                            regular = "https://img.xjh.me/random_img.php?return=302&t=$timestamp&_c=$c",
                            small = "https://img.xjh.me/random_img.php?return=302&t=$timestamp&_c=$c",
                            thumb = "https://img.xjh.me/random_img.php?return=302&t=$timestamp&_c=$c"
                        ),
                        user = User(
                            id = "xjh",
                            username = "XJH",
                            name = "随机美图"
                        ),
                        description = "随机二次元美图 #$c",
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
