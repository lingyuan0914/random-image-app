package com.randomimage.data.repository

import android.content.Context
import android.graphics.drawable.BitmapDrawable
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.randomimage.data.local.AppDataStore
import com.randomimage.data.local.FavoriteData
import com.randomimage.data.local.GroupData
import com.randomimage.data.local.HistoryData
import com.randomimage.data.local.TagData
import com.randomimage.data.local.ArtistData
import com.randomimage.data.remote.CustomApiManager
import com.randomimage.data.remote.ApiManager
import com.randomimage.domain.model.ImageModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first as flowFirst
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImageRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dataStore: AppDataStore,
    private val apiManager: ApiManager,
    private val imageLoader: ImageLoader
) {
    private val favoritesCacheDir by lazy { File(context.filesDir, "favorite_images").also { it.mkdirs() } }

    private suspend fun downloadToCache(imageUrl: String, imageId: String): String? {
        return withContext(Dispatchers.IO) {
            try {
                val file = File(favoritesCacheDir, "${imageId.hashCode()}.jpg")
                if (file.exists() && file.length() > 0) {
                    Timber.d("Image already cached: ${file.absolutePath}")
                    return@withContext file.absolutePath
                }

                val request = ImageRequest.Builder(context)
                    .data(imageUrl)
                    .allowHardware(false)
                    .build()

                val result = imageLoader.execute(request)
                if (result is SuccessResult) {
                    val bitmap = (result.drawable as BitmapDrawable).bitmap
                    file.outputStream().use { out ->
                        bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 90, out)
                    }
                    Timber.d("Image cached to: ${file.absolutePath}")
                    file.absolutePath
                } else {
                    Timber.w("Failed to download image for caching: $imageUrl")
                    null
                }
            } catch (e: Exception) {
                Timber.e(e, "Error caching image: $imageUrl")
                null
            }
        }
    }
    suspend fun fetchRandomImages(count: Int = 10): List<ImageModel> {
        return apiManager.fetchRandomImages(count)
    }

    suspend fun searchImages(query: String, count: Int = 10): List<ImageModel> {
        return apiManager.searchImages(query, count)
    }

    suspend fun fetchRandomImagesNSFW(count: Int = 10): List<ImageModel> {
        return apiManager.fetchRandomImagesNSFW(count)
    }

    // Favorites
    fun getFavorites(): Flow<List<ImageModel>> {
        return dataStore.getFavorites().map { favorites ->
            favorites.map { it.toImageModel() }
        }
    }

    fun getFavoritesByGroup(groupId: String): Flow<List<ImageModel>> {
        return dataStore.getFavorites().map { favorites ->
            val numericId = groupId.removePrefix("group_").toLongOrNull() ?: 0L
            favorites.filter { it.groupId == numericId }.map { it.toImageModel() }
        }
    }

    suspend fun addToFavorites(image: ImageModel) {
        val localPath = downloadToCache(image.urls.raw, image.id)
        dataStore.addFavorite(FavoriteData.fromImageModel(image, localPath = localPath))
    }

    suspend fun addToFavoritesWithGroup(image: ImageModel, groupId: String) {
        val localPath = downloadToCache(image.urls.raw, image.id)
        dataStore.addFavorite(FavoriteData.fromImageModel(image, groupId.toLongOrNull() ?: 0, localPath = localPath))
    }

    suspend fun removeFromFavorites(imageId: String) {
        dataStore.removeFavorite(imageId)
    }

    suspend fun isFavorite(imageId: String): Boolean {
        return dataStore.isFavorite(imageId)
    }

    suspend fun getFavoriteIds(): Set<String> {
        return dataStore.getFavoriteIds()
    }

    fun getFavoriteCount(): Flow<Int> {
        return dataStore.getFavorites().map { it.size }
    }

    // History
    fun getHistory(): Flow<List<ImageModel>> {
        return dataStore.getHistory().map { history ->
            history.map { it.toImageModel() }
        }
    }

    suspend fun addToHistory(image: ImageModel, tags: String? = null) {
        dataStore.addHistory(HistoryData.fromImageModel(image, tags))
    }

    suspend fun clearHistory() {
        dataStore.clearHistory()
    }

    fun getHistoryCount(): Flow<Int> {
        return dataStore.getHistory().map { it.size }
    }

    // Search History
    fun getRecentSearches(): Flow<List<String>> {
        return dataStore.getSearchHistory()
    }

    suspend fun addSearchHistory(query: String) {
        dataStore.addSearchHistory(query)
    }

    suspend fun clearSearchHistory() {
        dataStore.clearSearchHistory()
    }

    // Groups
    fun getGroups(): Flow<List<GroupData>> = dataStore.getGroups()

    suspend fun addGroup(group: GroupData) = dataStore.addGroup(group)

    suspend fun removeGroup(id: String) = dataStore.removeGroup(id)

    suspend fun updateGroupCover(id: String, coverUrl: String) = dataStore.updateGroupCover(id, coverUrl)

    suspend fun moveImageToGroup(imageId: String, groupId: String) {
        dataStore.moveImageToGroup(imageId, groupId)
    }

    // Tags
    fun getAllTags(): Flow<List<TagData>> = dataStore.getTags()

    suspend fun recordTagUsage(tagName: String) = dataStore.recordTag(tagName)

    // Artists
    fun getFollowedArtists(): Flow<List<ArtistData>> = dataStore.getArtists()

    suspend fun followArtist(artist: ArtistData) = dataStore.followArtist(artist)

    suspend fun unfollowArtist(uid: String) = dataStore.unfollowArtist(uid)

    suspend fun isFollowingArtist(uid: String): Boolean = dataStore.isFollowing(uid)

    suspend fun getFollowedUids(): List<String> = dataStore.getFollowedUids()
}
