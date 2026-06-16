package com.randomimage.data.remote

import com.randomimage.domain.model.ImageModel
import com.randomimage.domain.model.ImageUrls
import com.randomimage.domain.model.User
import java.util.concurrent.atomic.AtomicInteger

class MwmImageApi : ImageApi {
    override val name = "二次元风景"
    override val supportsSearch = false
    override val supportsNSFW = false

    private val imageUrls = listOf(
        "https://t.mwm.moe/fj/",
        "https://t.mwm.moe/mp/",
        "https://t.mwm.moe/sj/"
    )

    private val counter = AtomicInteger(0)

    override suspend fun fetchRandomImages(count: Int): List<ImageModel> {
        return try {
            val images = mutableListOf<ImageModel>()
            repeat(count) {
                val c = counter.incrementAndGet()
                val url = imageUrls.random()
                val timestamp = System.currentTimeMillis()
                images.add(
                    ImageModel(
                        id = "mwm_${timestamp}_${c}_${(0..9999).random()}",
                        urls = ImageUrls(
                            raw = url,
                            full = url,
                            regular = url,
                            small = url,
                            thumb = url
                        ),
                        user = User(
                            id = "mwm",
                            username = "MWM",
                            name = "二次元风景"
                        ),
                        description = "二次元风景 #$c",
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
