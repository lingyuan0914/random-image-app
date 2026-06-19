package com.randomimage.data.repository

import com.randomimage.data.local.AppDataStore
import com.randomimage.data.local.FavoriteData
import com.randomimage.data.local.GroupData
import com.randomimage.data.local.HistoryData
import com.randomimage.data.local.TagData
import com.randomimage.data.local.ArtistData
import com.randomimage.data.remote.CustomApiManager
import com.randomimage.data.remote.ApiManager
import com.randomimage.domain.model.ImageModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first as flowFirst
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImageRepository @Inject constructor(
    private val dataStore: AppDataStore,
    private val apiManager: ApiManager
) {
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
            favorites.filter { it.groupId.toString() == groupId }.map { it.toImageModel() }
        }
    }

    suspend fun addToFavorites(image: ImageModel) {
        dataStore.addFavorite(FavoriteData.fromImageModel(image))
    }

    suspend fun addToFavoritesWithGroup(image: ImageModel, groupId: String) {
        dataStore.addFavorite(FavoriteData.fromImageModel(image, groupId.toLongOrNull() ?: 0))
    }

    suspend fun removeFromFavorites(imageId: String) {
        dataStore.removeFavorite(imageId)
    }

    suspend fun isFavorite(imageId: String): Boolean {
        return dataStore.isFavorite(imageId)
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
        val favorites = dataStore.getFavorites().flowFirst()
        val updated = favorites.map { if (it.id == imageId) it.copy(groupId = groupId.toLongOrNull() ?: 0) else it }
        dataStore.saveFavorites(updated)
    }

    // Tags
    fun getAllTags(): Flow<List<TagData>> = dataStore.getTags()

    suspend fun recordTagUsage(tagName: String) = dataStore.recordTag(tagName)

    // Artists
    fun getFollowedArtists(): Flow<List<ArtistData>> = dataStore.getArtists()

    suspend fun followArtist(artist: ArtistData) = dataStore.followArtist(artist)

    suspend fun unfollowArtist(uid: String) = dataStore.unfollowArtist(uid)

    suspend fun isFollowingArtist(uid: String): Boolean = dataStore.isFollowing(uid)

    suspend fun getFollowedUids(): List<String> = dataStore.getAllFollowedUids()
}
