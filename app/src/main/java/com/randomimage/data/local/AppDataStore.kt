package com.randomimage.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.randomimage.domain.model.ImageModel
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
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
    private val context: Context
) {
    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val favoritesType = Types.newParameterizedType(List::class.java, FavoriteData::class.java)
    private val historyType = Types.newParameterizedType(List::class.java, HistoryData::class.java)
    private val groupsType = Types.newParameterizedType(List::class.java, GroupData::class.java)

    private val favoritesAdapter = moshi.adapter<List<FavoriteData>>(favoritesType)
    private val historyAdapter = moshi.adapter<List<HistoryData>>(historyType)
    private val groupsAdapter = moshi.adapter<List<GroupData>>(groupsType)

    private val backupFile by lazy { java.io.File(context.filesDir, "favorites_backup.json") }

    companion object {
        private val KEY_FAVORITES = stringPreferencesKey("favorites")
        private val KEY_HISTORY = stringPreferencesKey("history")
        private val KEY_SEARCH_HISTORY = stringPreferencesKey("search_history")
        private val KEY_GROUPS = stringPreferencesKey("groups")
        private val KEY_TAGS = stringPreferencesKey("tags")
        private val KEY_ARTISTS = stringPreferencesKey("artists")
    }

    private val MAX_JSON_SIZE = 800 * 1024
    private val favoritesMutex = Mutex()
    private val tagsMutex = Mutex()

    private fun <T> trimToSize(
        adapter: JsonAdapter<List<T>>,
        list: List<T>,
        timeExtractor: (T) -> Long
    ): List<T> {
        var trimmed = list
        while (adapter.toJson(trimmed).toByteArray(Charsets.UTF_8).size > MAX_JSON_SIZE && trimmed.isNotEmpty()) {
            trimmed = trimmed.sortedBy { timeExtractor(it) }.drop(1)
        }
        return trimmed
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
            context.dataStore.edit { prefs ->
                val current = try { favoritesAdapter.fromJson(prefs[KEY_FAVORITES] ?: "[]") ?: emptyList() } catch (e: Exception) { 
                    Timber.e(e, "Failed to parse favorites JSON in addFavorite")
                    val backup = loadBackupFavorites()
                    backup
                }
                val updated = current.filter { it.id != favorite.id } + favorite
                val json = favoritesAdapter.toJson(updated)
                Timber.d("addFavorite: id=${favorite.id}, current=${current.size}, updated=${updated.size}")
                prefs[KEY_FAVORITES] = json
            }
            saveBackupFavorites(loadCurrentFavorites())
        }
    }

    private suspend fun loadCurrentFavorites(): List<FavoriteData> {
        return try {
            val prefs = context.dataStore.data.map { it[KEY_FAVORITES] ?: "[]" }.first()
            favoritesAdapter.fromJson(prefs) ?: emptyList()
        } catch (e: Exception) {
            Timber.e(e, "Failed to load current favorites")
            emptyList()
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
                val current = try { favoritesAdapter.fromJson(prefs[KEY_FAVORITES] ?: "[]") ?: emptyList() } catch (_: Exception) { emptyList() }
                prefs[KEY_FAVORITES] = favoritesAdapter.toJson(current.filter { it.id != id })
            }
        }
    }

    suspend fun isFavorite(id: String): Boolean {
        return favoritesMutex.withLock {
            val prefs = context.dataStore.data.map { it[KEY_FAVORITES] ?: "[]" }.first()
            val current = try { favoritesAdapter.fromJson(prefs) ?: emptyList() } catch (_: Exception) { emptyList() }
            current.any { it.id == id }
        }
    }

    suspend fun moveImageToGroup(imageId: String, groupId: String) {
        favoritesMutex.withLock {
            context.dataStore.edit { prefs ->
                val current = try { favoritesAdapter.fromJson(prefs[KEY_FAVORITES] ?: "[]") ?: emptyList() } catch (_: Exception) { emptyList() }
                val updated = current.map { if (it.id == imageId) it.copy(groupId = groupId.toLongOrNull() ?: 0) else it }
                prefs[KEY_FAVORITES] = favoritesAdapter.toJson(updated)
            }
        }
    }

    // History
    fun getHistory(): Flow<List<HistoryData>> {
        return context.dataStore.data.map { prefs ->
            val json = prefs[KEY_HISTORY] ?: "[]"
            try { historyAdapter.fromJson(json) ?: emptyList() } catch (_: Exception) { emptyList() }
        }
    }

    suspend fun addHistory(history: HistoryData) {
        context.dataStore.edit { prefs ->
            val current = try { historyAdapter.fromJson(prefs[KEY_HISTORY] ?: "[]") ?: emptyList() } catch (_: Exception) { emptyList() }
            val updated = (current + history).takeLast(100)
            val trimmed = trimToSize(historyAdapter, updated) { it.timestamp }
            prefs[KEY_HISTORY] = historyAdapter.toJson(trimmed)
        }
    }

    suspend fun clearHistory() {
        context.dataStore.edit { it[KEY_HISTORY] = "[]" }
    }

    // Search History
    fun getSearchHistory(): Flow<List<String>> {
        return context.dataStore.data.map { prefs ->
            val json = prefs[KEY_SEARCH_HISTORY] ?: "[]"
            try { moshi.adapter<List<String>>(Types.newParameterizedType(List::class.java, String::class.java)).fromJson(json) ?: emptyList() } catch (_: Exception) { emptyList() }
        }
    }

    suspend fun addSearchHistory(query: String) {
        context.dataStore.edit { prefs ->
            val current = try {
                moshi.adapter<List<String>>(Types.newParameterizedType(List::class.java, String::class.java))
                    .fromJson(prefs[KEY_SEARCH_HISTORY] ?: "[]") ?: emptyList()
            } catch (_: Exception) { emptyList() }
            val updated = (listOf(query) + current).distinct().take(20)
            prefs[KEY_SEARCH_HISTORY] = moshi.adapter<List<String>>(Types.newParameterizedType(List::class.java, String::class.java)).toJson(updated)
        }
    }

    suspend fun clearSearchHistory() {
        context.dataStore.edit { it[KEY_SEARCH_HISTORY] = "[]" }
    }

    // Groups
    fun getGroups(): Flow<List<GroupData>> {
        return context.dataStore.data.map { prefs ->
            val json = prefs[KEY_GROUPS] ?: "[]"
            try { groupsAdapter.fromJson(json) ?: emptyList() } catch (_: Exception) { emptyList() }
        }
    }

    suspend fun addGroup(group: GroupData) {
        context.dataStore.edit { prefs ->
            val current = try { groupsAdapter.fromJson(prefs[KEY_GROUPS] ?: "[]") ?: emptyList() } catch (_: Exception) { emptyList() }
            prefs[KEY_GROUPS] = groupsAdapter.toJson(current + group)
        }
    }

    suspend fun removeGroup(id: String) {
        context.dataStore.edit { prefs ->
            val current = try { groupsAdapter.fromJson(prefs[KEY_GROUPS] ?: "[]") ?: emptyList() } catch (_: Exception) { emptyList() }
            prefs[KEY_GROUPS] = groupsAdapter.toJson(current.filter { it.id != id })
        }
    }

    suspend fun updateGroupCover(id: String, coverUrl: String) {
        context.dataStore.edit { prefs ->
            val current = try { groupsAdapter.fromJson(prefs[KEY_GROUPS] ?: "[]") ?: emptyList() } catch (_: Exception) { emptyList() }
            val updated = current.map { if (it.id == id) it.copy(coverUrl = coverUrl) else it }
            prefs[KEY_GROUPS] = groupsAdapter.toJson(updated)
        }
    }

    // Tags
    fun getTags(): Flow<List<TagData>> {
        return context.dataStore.data.map { prefs ->
            val json = prefs[KEY_TAGS] ?: "[]"
            try { moshi.adapter<List<TagData>>(Types.newParameterizedType(List::class.java, TagData::class.java)).fromJson(json) ?: emptyList() } catch (_: Exception) { emptyList() }
        }
    }

    suspend fun recordTag(tagName: String) {
        val tagAdapter = moshi.adapter<List<TagData>>(Types.newParameterizedType(List::class.java, TagData::class.java))
        tagsMutex.withLock {
            context.dataStore.edit { prefs ->
                val current = try {
                    tagAdapter.fromJson(prefs[KEY_TAGS] ?: "[]") ?: emptyList()
                } catch (_: Exception) { emptyList() }
                val existing = current.find { it.name == tagName }
                val updated = if (existing != null) {
                    current.map { if (it.name == tagName) it.copy(usageCount = it.usageCount + 1) else it }
                } else {
                    current + TagData(name = tagName, usageCount = 1)
                }
                val trimmed = trimToSize(tagAdapter, updated) { it.lastUsedTime }
                prefs[KEY_TAGS] = tagAdapter.toJson(trimmed)
            }
        }
    }

    // Artists
    fun getArtists(): Flow<List<ArtistData>> {
        return context.dataStore.data.map { prefs ->
            val json = prefs[KEY_ARTISTS] ?: "[]"
            try { moshi.adapter<List<ArtistData>>(Types.newParameterizedType(List::class.java, ArtistData::class.java)).fromJson(json) ?: emptyList() } catch (_: Exception) { emptyList() }
        }
    }

    suspend fun followArtist(artist: ArtistData) {
        context.dataStore.edit { prefs ->
            val current = try {
                moshi.adapter<List<ArtistData>>(Types.newParameterizedType(List::class.java, ArtistData::class.java))
                    .fromJson(prefs[KEY_ARTISTS] ?: "[]") ?: emptyList()
            } catch (_: Exception) { emptyList() }
            prefs[KEY_ARTISTS] = moshi.adapter<List<ArtistData>>(Types.newParameterizedType(List::class.java, ArtistData::class.java)).toJson(current + artist)
        }
    }

    suspend fun unfollowArtist(uid: String) {
        context.dataStore.edit { prefs ->
            val current = try {
                moshi.adapter<List<ArtistData>>(Types.newParameterizedType(List::class.java, ArtistData::class.java))
                    .fromJson(prefs[KEY_ARTISTS] ?: "[]") ?: emptyList()
            } catch (_: Exception) { emptyList() }
            prefs[KEY_ARTISTS] = moshi.adapter<List<ArtistData>>(Types.newParameterizedType(List::class.java, ArtistData::class.java)).toJson(current.filter { it.uid != uid })
        }
    }

    suspend fun isFollowing(uid: String): Boolean {
        val prefs = context.dataStore.data.map { it[KEY_ARTISTS] ?: "[]" }.first()
        val current = try { moshi.adapter<List<ArtistData>>(Types.newParameterizedType(List::class.java, ArtistData::class.java)).fromJson(prefs) ?: emptyList() } catch (_: Exception) { emptyList() }
        return current.any { it.uid == uid }
    }

    suspend fun getAllFollowedUids(): List<String> {
        val prefs = context.dataStore.data.map { it[KEY_ARTISTS] ?: "[]" }.first()
        val current = try { moshi.adapter<List<ArtistData>>(Types.newParameterizedType(List::class.java, ArtistData::class.java)).fromJson(prefs) ?: emptyList() } catch (_: Exception) { emptyList() }
        return current.map { it.uid }
    }

    suspend fun clearAll() {
        context.dataStore.edit { it.clear() }
    }
}

// Data classes for DataStore serialization
data class FavoriteData(
    val id: String,
    val imageUrl: String,
    val thumbnailUrl: String,
    val photographerName: String,
    val photographerUsername: String,
    val description: String?,
    val groupId: Long = 0,
    val tags: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val localPath: String? = null
) {
    fun toImageModel(): ImageModel {
        return ImageModel(
            id = id,
            urls = com.randomimage.domain.model.ImageUrls(
                raw = imageUrl, full = imageUrl, regular = imageUrl,
                small = imageUrl, thumb = thumbnailUrl
            ),
            user = com.randomimage.domain.model.User(
                id = photographerUsername, username = photographerUsername, name = photographerName
            ),
            description = description,
            groupId = groupId,
            tags = if (tags.isNotBlank()) tags.split(",") else emptyList(),
            localPath = localPath
        )
    }

    companion object {
        fun fromImageModel(image: ImageModel, groupId: Long = 0, localPath: String? = null): FavoriteData {
            return FavoriteData(
                id = image.id, imageUrl = image.urls.regular, thumbnailUrl = image.urls.thumb,
                photographerName = image.user.name, photographerUsername = image.user.username,
                description = image.description, groupId = groupId, tags = image.tags.joinToString(","),
                localPath = localPath
            )
        }
    }
}

data class HistoryData(
    val id: String,
    val imageUrl: String,
    val thumbnailUrl: String,
    val photographerName: String,
    val photographerUsername: String,
    val description: String?,
    val tags: String = "",
    val timestamp: Long = System.currentTimeMillis()
) {
    fun toImageModel(): ImageModel {
        return ImageModel(
            id = id,
            urls = com.randomimage.domain.model.ImageUrls(
                raw = imageUrl, full = imageUrl, regular = imageUrl,
                small = imageUrl, thumb = thumbnailUrl
            ),
            user = com.randomimage.domain.model.User(
                id = photographerUsername, username = photographerUsername, name = photographerName
            ),
            description = description,
            tags = if (tags.isNotBlank()) tags.split(",") else emptyList()
        )
    }

    companion object {
        fun fromImageModel(image: ImageModel, tags: String? = null): HistoryData {
            return HistoryData(
                id = image.id, imageUrl = image.urls.regular, thumbnailUrl = image.urls.thumb,
                photographerName = image.user.name, photographerUsername = image.user.username,
                description = image.description, tags = tags ?: image.tags.joinToString(",")
            )
        }
    }
}

data class GroupData(
    val id: String,
    val name: String,
    val coverUrl: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

data class TagData(
    val name: String,
    val usageCount: Int = 1,
    val lastUsedTime: Long = System.currentTimeMillis()
)

data class ArtistData(
    val uid: String,
    val name: String,
    val artworkCount: Int = 0,
    val followedTime: Long = System.currentTimeMillis()
)
