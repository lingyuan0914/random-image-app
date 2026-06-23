package com.randomimage.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.randomimage.domain.model.ImageModel
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "app_data")

@Singleton
class AppDataStore @Inject constructor(
    private val context: Context,
    private val moshi: Moshi
) {

    private val favoritesAdapter by lazy {
        moshi.adapter<List<FavoriteData>>(Types.newParameterizedType(List::class.java, FavoriteData::class.java))
    }
    private val historyAdapter by lazy {
        moshi.adapter<List<HistoryData>>(Types.newParameterizedType(List::class.java, HistoryData::class.java))
    }
    private val groupsAdapter by lazy {
        moshi.adapter<List<GroupData>>(Types.newParameterizedType(List::class.java, GroupData::class.java))
    }
    private val tagsAdapter by lazy {
        moshi.adapter<List<TagData>>(Types.newParameterizedType(List::class.java, TagData::class.java))
    }
    private val artistsAdapter by lazy {
        moshi.adapter<List<ArtistData>>(Types.newParameterizedType(List::class.java, ArtistData::class.java))
    }
    private val stringListAdapter by lazy {
        moshi.adapter<List<String>>(Types.newParameterizedType(List::class.java, String::class.java))
    }

    private val backupFile by lazy { java.io.File(context.filesDir, "favorites_backup.json") }

    companion object {
        private val KEY_FAVORITES = stringPreferencesKey("favorites")
        private val KEY_HISTORY = stringPreferencesKey("history")
        private val KEY_SEARCH_HISTORY = stringPreferencesKey("search_history")
        private val KEY_GROUPS = stringPreferencesKey("groups")
        private val KEY_TAGS = stringPreferencesKey("tags")
        private val KEY_ARTISTS = stringPreferencesKey("artists")
        private const val MAX_JSON_SIZE = 800 * 1024
        private const val AVG_BYTES_PER_ITEM = 512
    }

    private val favoritesMutex = Mutex()
    private val tagsMutex = Mutex()

    private fun <T> trimToSize(
        list: List<T>,
        timeExtractor: (T) -> Long,
        sizeEstimator: (T) -> Int = { AVG_BYTES_PER_ITEM }
    ): List<T> {
        val estimatedSize = list.sumOf(sizeEstimator)
        if (estimatedSize <= MAX_JSON_SIZE) return list
        // Keep newest items that fit within MAX_JSON_SIZE
        val sorted = list.sortedByDescending(timeExtractor)
        val result = mutableListOf<T>()
        var currentSize = 0
        for (item in sorted) {
            val itemSize = sizeEstimator(item)
            if (currentSize + itemSize > MAX_JSON_SIZE) break
            result.add(item)
            currentSize += itemSize
        }
        return result
    }

    // Favorites
    fun getFavorites(): Flow<List<FavoriteData>> {
        return context.dataStore.data.map { prefs ->
            val json = prefs[KEY_FAVORITES] ?: "[]"
            try {
                val result = favoritesAdapter.fromJson(json) ?: emptyList()
                Timber.d("getFavorites: count=${result.size}, ids=${result.map { it.id }.take(5)}")
                result
            } catch (e: Exception) {
                Timber.e(e, "Failed to parse favorites JSON, trying backup")
                val backup = loadBackupFavorites()
                if (backup.isNotEmpty()) {
                    Timber.d("Restored ${backup.size} favorites from backup")
                    saveFavoritesSync(backup)
                }
                backup
            }
        }
    }

    suspend fun saveFavorites(favorites: List<FavoriteData>) {
        favoritesMutex.withLock {
            val json = favoritesAdapter.toJson(favorites)
            Timber.d("saveFavorites: count=${favorites.size}, ids=${favorites.map { it.id }.take(5)}")
            context.dataStore.edit { prefs ->
                prefs[KEY_FAVORITES] = json
            }
            saveBackupFavorites(favorites)
        }
    }

    suspend fun addFavorite(favorite: FavoriteData) {
        favoritesMutex.withLock {
            var updatedList: List<FavoriteData> = emptyList()
            context.dataStore.edit { prefs ->
                val current = try {
                    favoritesAdapter.fromJson(prefs[KEY_FAVORITES] ?: "[]") ?: emptyList()
                } catch (e: Exception) {
                    Timber.e(e, "Failed to parse favorites JSON in addFavorite")
                    loadBackupFavorites()
                }
                updatedList = current.filter { it.id != favorite.id } + favorite
                val json = favoritesAdapter.toJson(updatedList)
                Timber.d("addFavorite: id=${favorite.id}, current=${current.size}, updated=${updatedList.size}")
                prefs[KEY_FAVORITES] = json
            }
            if (updatedList.isNotEmpty()) {
                saveBackupFavorites(updatedList)
            }
        }
    }

    private fun saveBackupFavorites(favorites: List<FavoriteData>) {
        try {
            val json = favoritesAdapter.toJson(favorites)
            backupFile.writeText(json)
            Timber.d("Backup saved: ${favorites.size} favorites")
        } catch (e: Exception) {
            Timber.e(e, "Failed to save backup")
        }
    }

    private fun loadBackupFavorites(): List<FavoriteData> {
        return try {
            if (backupFile.exists()) {
                val json = backupFile.readText()
                favoritesAdapter.fromJson(json) ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to load backup")
            emptyList()
        }
    }

    private suspend fun saveFavoritesSync(favorites: List<FavoriteData>) {
        try {
            val json = favoritesAdapter.toJson(favorites)
            context.dataStore.edit { prefs ->
                prefs[KEY_FAVORITES] = json
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to save favorites sync")
        }
    }

    suspend fun removeFavorite(id: String) {
        favoritesMutex.withLock {
            context.dataStore.edit { prefs ->
                val current = try {
                    favoritesAdapter.fromJson(prefs[KEY_FAVORITES] ?: "[]") ?: emptyList()
                } catch (_: Exception) { emptyList() }
                prefs[KEY_FAVORITES] = favoritesAdapter.toJson(current.filter { it.id != id })
            }
        }
    }

    suspend fun isFavorite(id: String): Boolean {
        return favoritesMutex.withLock {
            val prefs = context.dataStore.data.map { it[KEY_FAVORITES] ?: "[]" }.first()
            val current = try {
                favoritesAdapter.fromJson(prefs) ?: emptyList()
            } catch (_: Exception) { emptyList() }
            current.any { it.id == id }
        }
    }

    suspend fun getFavoriteIds(): Set<String> {
        return try {
            val prefs = context.dataStore.data.map { it[KEY_FAVORITES] ?: "[]" }.first()
            val current = favoritesAdapter.fromJson(prefs) ?: emptyList()
            current.map { it.id }.toSet()
        } catch (_: Exception) { emptySet() }
    }

    suspend fun moveImageToGroup(imageId: String, groupId: String) {
        favoritesMutex.withLock {
            context.dataStore.edit { prefs ->
                val current = try {
                    favoritesAdapter.fromJson(prefs[KEY_FAVORITES] ?: "[]") ?: emptyList()
                } catch (_: Exception) { emptyList() }
                val updated = current.map {
                    if (it.id == imageId) it.copy(groupId = groupId.toLongOrNull() ?: 0) else it
                }
                prefs[KEY_FAVORITES] = favoritesAdapter.toJson(updated)
            }
        }
    }

    // History
    fun getHistory(): Flow<List<HistoryData>> {
        return context.dataStore.data.map { prefs ->
            val json = prefs[KEY_HISTORY] ?: "[]"
            try {
                historyAdapter.fromJson(json) ?: emptyList()
            } catch (_: Exception) { emptyList() }
        }
    }

    suspend fun addHistory(history: HistoryData) {
        context.dataStore.edit { prefs ->
            val current = try {
                historyAdapter.fromJson(prefs[KEY_HISTORY] ?: "[]") ?: emptyList()
            } catch (_: Exception) { emptyList() }
            val updated = (current + history).takeLast(100)
            prefs[KEY_HISTORY] = historyAdapter.toJson(updated)
        }
    }

    suspend fun clearHistory() {
        context.dataStore.edit { it[KEY_HISTORY] = "[]" }
    }

    // Search History
    fun getSearchHistory(): Flow<List<String>> {
        return context.dataStore.data.map { prefs ->
            val json = prefs[KEY_SEARCH_HISTORY] ?: "[]"
            try {
                stringListAdapter.fromJson(json) ?: emptyList()
            } catch (_: Exception) { emptyList() }
        }
    }

    suspend fun addSearchHistory(query: String) {
        context.dataStore.edit { prefs ->
            val current = try {
                stringListAdapter.fromJson(prefs[KEY_SEARCH_HISTORY] ?: "[]") ?: emptyList()
            } catch (_: Exception) { emptyList() }
            val updated = (listOf(query) + current).distinct().take(20)
            prefs[KEY_SEARCH_HISTORY] = stringListAdapter.toJson(updated)
        }
    }

    suspend fun clearSearchHistory() {
        context.dataStore.edit { it[KEY_SEARCH_HISTORY] = "[]" }
    }

    // Groups
    fun getGroups(): Flow<List<GroupData>> {
        return context.dataStore.data.map { prefs ->
            val json = prefs[KEY_GROUPS] ?: "[]"
            try {
                groupsAdapter.fromJson(json) ?: emptyList()
            } catch (_: Exception) { emptyList() }
        }
    }

    suspend fun addGroup(group: GroupData) {
        context.dataStore.edit { prefs ->
            val current = try {
                groupsAdapter.fromJson(prefs[KEY_GROUPS] ?: "[]") ?: emptyList()
            } catch (_: Exception) { emptyList() }
            prefs[KEY_GROUPS] = groupsAdapter.toJson(current + group)
        }
    }

    suspend fun removeGroup(id: String) {
        context.dataStore.edit { prefs ->
            val current = try {
                groupsAdapter.fromJson(prefs[KEY_GROUPS] ?: "[]") ?: emptyList()
            } catch (_: Exception) { emptyList() }
            prefs[KEY_GROUPS] = groupsAdapter.toJson(current.filter { it.id != id })
        }
    }

    suspend fun updateGroupCover(id: String, coverUrl: String) {
        context.dataStore.edit { prefs ->
            val current = try {
                groupsAdapter.fromJson(prefs[KEY_GROUPS] ?: "[]") ?: emptyList()
            } catch (_: Exception) { emptyList() }
            val updated = current.map { if (it.id == id) it.copy(coverUrl = coverUrl) else it }
            prefs[KEY_GROUPS] = groupsAdapter.toJson(updated)
        }
    }

    // Tags
    fun getTags(): Flow<List<TagData>> {
        return context.dataStore.data.map { prefs ->
            val json = prefs[KEY_TAGS] ?: "[]"
            try {
                tagsAdapter.fromJson(json) ?: emptyList()
            } catch (_: Exception) { emptyList() }
        }
    }

    suspend fun recordTag(tagName: String) {
        tagsMutex.withLock {
            context.dataStore.edit { prefs ->
                val current = try {
                    tagsAdapter.fromJson(prefs[KEY_TAGS] ?: "[]") ?: emptyList()
                } catch (_: Exception) { emptyList() }
                val existing = current.find { it.name == tagName }
                val updated = if (existing != null) {
                    current.map {
                        if (it.name == tagName) it.copy(
                            usageCount = it.usageCount + 1,
                            lastUsedTime = System.currentTimeMillis()
                        ) else it
                    }
                } else {
                    current + TagData(name = tagName, usageCount = 1)
                }
                prefs[KEY_TAGS] = tagsAdapter.toJson(updated)
            }
        }
    }

    // Artists
    fun getArtists(): Flow<List<ArtistData>> {
        return context.dataStore.data.map { prefs ->
            val json = prefs[KEY_ARTISTS] ?: "[]"
            try {
                artistsAdapter.fromJson(json) ?: emptyList()
            } catch (_: Exception) { emptyList() }
        }
    }

    suspend fun followArtist(artist: ArtistData) {
        context.dataStore.edit { prefs ->
            val current = try {
                artistsAdapter.fromJson(prefs[KEY_ARTISTS] ?: "[]") ?: emptyList()
            } catch (_: Exception) { emptyList() }
            prefs[KEY_ARTISTS] = artistsAdapter.toJson(current + artist)
        }
    }

    suspend fun unfollowArtist(uid: String) {
        context.dataStore.edit { prefs ->
            val current = try {
                artistsAdapter.fromJson(prefs[KEY_ARTISTS] ?: "[]") ?: emptyList()
            } catch (_: Exception) { emptyList() }
            prefs[KEY_ARTISTS] = artistsAdapter.toJson(current.filter { it.uid != uid })
        }
    }

    suspend fun isFollowing(uid: String): Boolean {
        val prefs = context.dataStore.data.map { it[KEY_ARTISTS] ?: "[]" }.first()
        val current = try {
            artistsAdapter.fromJson(prefs) ?: emptyList()
        } catch (_: Exception) { emptyList() }
        return current.any { it.uid == uid }
    }

    suspend fun getFollowedUids(): List<String> {
        val prefs = context.dataStore.data.map { it[KEY_ARTISTS] ?: "[]" }.first()
        val current = try {
            artistsAdapter.fromJson(prefs) ?: emptyList()
        } catch (_: Exception) { emptyList() }
        return current.map { it.uid }
    }

    suspend fun clearAll() {
        context.dataStore.edit { it.clear() }
    }
}
