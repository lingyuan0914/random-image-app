package com.randomimage.util

import android.content.Context
import com.randomimage.data.local.AppDataStore
import com.randomimage.data.local.FavoriteData
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.util.Base64

object CloudSyncManager {
    private var webdavUrl: String = ""
    private var username: String = ""
    private var password: String = "" // TODO: encrypt or use Android Keystore instead of storing in plain text
    private var dataStore: AppDataStore? = null

    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    fun configure(url: String, user: String, pass: String) {
        webdavUrl = url
        username = user
        password = pass
    }

    fun init(store: AppDataStore) {
        dataStore = store
    }

    suspend fun uploadFile(context: Context, localFile: File, remoteName: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                if (webdavUrl.isBlank()) {
                    Timber.w("WebDAV not configured")
                    return@withContext false
                }

                val url = URL("${webdavUrl.trimEnd('/')}/$remoteName")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "PUT"
                connection.doOutput = true
                connection.connectTimeout = 15000
                connection.readTimeout = 15000

                val auth = Base64.getEncoder().encodeToString("$username:$password".toByteArray())
                connection.setRequestProperty("Authorization", "Basic $auth")
                connection.setRequestProperty("Content-Type", "application/octet-stream")
                connection.setRequestProperty("Content-Length", localFile.length().toString())

                localFile.inputStream().use { input ->
                    connection.outputStream.use { output ->
                        input.copyTo(output)
                    }
                }

                val code = connection.responseCode
                val success = code in 200..299
                Timber.d("WebDAV upload: $remoteName -> HTTP $code")
                connection.disconnect()
                success
            } catch (e: Exception) {
                Timber.e(e, "WebDAV upload failed")
                false
            }
        }
    }

    suspend fun exportFavorites(context: Context): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val store = dataStore ?: return@withContext false
                val favorites = store.getFavorites().first()
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

                val store = dataStore ?: return@withContext false
                favorites.forEach { favorite ->
                    store.addFavorite(favorite)
                }

                Timber.d("Favorites imported: ${favorites.size} items")
                true
            } catch (e: Exception) {
                Timber.e(e, "Failed to import favorites")
                false
            }
        }
    }

    private fun favoritesToJson(favorites: List<FavoriteData>): String {
        val type = Types.newParameterizedType(List::class.java, FavoriteData::class.java)
        val adapter = moshi.adapter<List<FavoriteData>>(type)
        return adapter.toJson(favorites)
    }

    private fun jsonToFavorites(json: String): List<FavoriteData> {
        val type = Types.newParameterizedType(List::class.java, FavoriteData::class.java)
        val adapter = moshi.adapter<List<FavoriteData>>(type)
        return adapter.fromJson(json) ?: emptyList()
    }
}
