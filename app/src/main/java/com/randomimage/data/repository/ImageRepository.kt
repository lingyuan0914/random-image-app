package com.randomimage.data.repository

import com.randomimage.data.local.ArtistDao
import com.randomimage.data.local.ArtistEntity
import com.randomimage.data.local.FavoriteDao
import com.randomimage.data.local.FavoriteEntity
import com.randomimage.data.local.HistoryDao
import com.randomimage.data.local.HistoryEntity
import com.randomimage.data.local.SearchHistoryDao
import com.randomimage.data.local.SearchHistoryEntity
import com.randomimage.data.local.TagDao
import com.randomimage.data.local.TagEntity
import com.randomimage.data.remote.ApiManager
import com.randomimage.domain.model.ImageModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImageRepository @Inject constructor(
    private val apiManager: ApiManager,
    private val favoriteDao: FavoriteDao,
    private val historyDao: HistoryDao,
    private val searchHistoryDao: SearchHistoryDao,
    private val tagDao: TagDao,
    private val artistDao: ArtistDao
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

    fun getFavorites(): Flow<List<ImageModel>> {
        return favoriteDao.getAllFavorites().map { entities ->
            entities.map { it.toImageModel() }
        }
    }

    fun getFavoritesByGroup(groupId: Long): Flow<List<ImageModel>> {
        return favoriteDao.getFavoritesByGroup(groupId).map { entities ->
            entities.map { it.toImageModel() }
        }
    }

    suspend fun addToFavorites(image: ImageModel) {
        favoriteDao.insertFavorite(FavoriteEntity.fromImageModel(image))
    }

    suspend fun addToFavoritesWithGroup(image: ImageModel, groupId: Long) {
        favoriteDao.insertFavorite(FavoriteEntity.fromImageModel(image, groupId))
    }

    suspend fun removeFromFavorites(imageId: String) {
        favoriteDao.deleteFavoriteById(imageId)
    }

    suspend fun isFavorite(imageId: String): Boolean {
        return favoriteDao.getFavoriteById(imageId) != null
    }

    fun getFavoriteCount(): Flow<Int> {
        return favoriteDao.getFavoriteCount()
    }

    fun getHistory(): Flow<List<ImageModel>> {
        return historyDao.getAllHistory().map { entities ->
            entities.map { it.toImageModel() }
        }
    }

    suspend fun addToHistory(image: ImageModel, tags: String? = null) {
        historyDao.insertHistory(HistoryEntity.fromImageModel(image, tags))
    }

    suspend fun clearHistory() {
        historyDao.clearHistory()
    }

    fun getHistoryCount(): Flow<Int> {
        return historyDao.getHistoryCount()
    }

    fun getRecentSearches(): Flow<List<String>> {
        return searchHistoryDao.getRecentSearches().map { entities ->
            entities.map { it.query }
        }
    }

    suspend fun addSearchHistory(query: String) {
        searchHistoryDao.insertSearch(SearchHistoryEntity(query = query))
    }

    suspend fun clearSearchHistory() {
        searchHistoryDao.clearSearchHistory()
    }

    suspend fun moveImageToGroup(imageId: String, groupId: Long) {
        favoriteDao.updateGroupById(imageId, groupId)
    }

    // Tags
    fun getAllTags(): Flow<List<TagEntity>> = tagDao.getAllTags()
    suspend fun searchTags(query: String): List<TagEntity> = tagDao.searchTags(query)
    suspend fun recordTagUsage(tagName: String) {
        tagDao.insertTag(TagEntity(name = tagName))
        tagDao.incrementUsage(tagName)
    }

    // Artists
    fun getFollowedArtists(): Flow<List<ArtistEntity>> = artistDao.getAllFollowed()
    suspend fun followArtist(artist: ArtistEntity) = artistDao.followArtist(artist)
    suspend fun unfollowArtist(uid: String) = artistDao.unfollowArtist(uid)
    suspend fun isFollowingArtist(uid: String): Boolean = artistDao.isFollowing(uid)
    suspend fun getFollowedUids(): List<String> = artistDao.getAllFollowedUids()
}
