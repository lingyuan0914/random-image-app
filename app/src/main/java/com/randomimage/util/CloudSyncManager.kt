package com.randomimage.util

import android.content.Context
import com.randomimage.data.local.AppDatabase
import com.randomimage.data.local.FavoriteEntity
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File

object CloudSyncManager {
    private var webdavUrl: String = ""
    private var username: String = ""
    private var password: String = ""

    fun configure(url: String, user: String, pass: String) {
        webdavUrl = url
        username = user
        password = pass
    }

    suspend fun exportFavorites(context: Context): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val db = androidx.room.Room.databaseBuilder(
                    context,
                    AppDatabase::class.java,
                    "random_image_db"
                ).build()

                val favorites = db.favoriteDao().getAllFavoritesSync()
                val json = favoritesToJson(favorites)

                val file = File(context.filesDir, "favorites_backup.json")
                file.writeText(json)

                Timber.d("Favorites exported: ${favorites.size} items")
                true
            } catch (e: Exception) {
                Timber.e(e, "Failed to export favorites")
                false
            }
        }
    }

    suspend fun importFavorites(context: Context): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val file = File(context.filesDir, "favorites_backup.json")
                if (!file.exists()) return@withContext false

                val json = file.readText()
                val favorites = jsonToFavorites(json)

                val db = androidx.room.Room.databaseBuilder(
                    context,
                    AppDatabase::class.java,
                    "random_image_db"
                ).build()

                favorites.forEach { favorite ->
                    db.favoriteDao().insertFavorite(favorite)
                }

                Timber.d("Favorites imported: ${favorites.size} items")
                true
            } catch (e: Exception) {
                Timber.e(e, "Failed to import favorites")
                false
            }
        }
    }

    private fun favoritesToJson(favorites: List<FavoriteEntity>): String {
        val moshi = Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()

        val type = Types.newParameterizedType(List::class.java, FavoriteEntity::class.java)
        val adapter = moshi.adapter<List<FavoriteEntity>>(type)
        return adapter.toJson(favorites)
    }

    private fun jsonToFavorites(json: String): List<FavoriteEntity> {
        val moshi = Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()

        val type = Types.newParameterizedType(List::class.java, FavoriteEntity::class.java)
        val adapter = moshi.adapter<List<FavoriteEntity>>(type)
        return adapter.fromJson(json) ?: emptyList()
    }
}
