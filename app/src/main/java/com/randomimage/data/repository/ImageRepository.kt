package com.randomimage.data.repository

import com.randomimage.data.local.FavoriteDao
import com.randomimage.data.local.FavoriteEntity
import com.randomimage.data.local.HistoryDao
import com.randomimage.data.local.HistoryEntity
import com.randomimage.data.local.SearchHistoryDao
import com.randomimage.data.local.SearchHistoryEntity
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
    private val searchHistoryDao: SearchHistoryDao
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
}
