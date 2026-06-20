package com.randomimage.util

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.randomimage.data.local.AppDataStore
import com.randomimage.data.local.FavoriteData
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import timber.log.Timber
import java.io.File
import java.util.concurrent.TimeUnit

object CloudSyncManager {
    private const val PREFS_NAME = "cloud_sync_prefs"
    private const val KEY_WEBDAV_URL = "webdav_url"
    private const val KEY_USERNAME = "username"
    private const val KEY_PASSWORD = "password"

    private var prefs: SharedPreferences? = null
    private var dataStore: AppDataStore? = null
    private var client: OkHttpClient? = null

    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val favoritesType = Types.newParameterizedType(List::class.java, FavoriteData::class.java)
    private val favoritesAdapter by lazy { moshi.adapter<List<FavoriteData>>(favoritesType) }

    fun configure(url: String, user: String, pass: String) {
        prefs?.edit()?.apply {
            putString(KEY_WEBDAV_URL, url)
            putString(KEY_USERNAME, user)
            putString(KEY_PASSWORD, pass)
            apply()
        }
    }

    fun init(store: AppDataStore, context: Context) {
        dataStore = store
        try {
            val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
            prefs = EncryptedSharedPreferences.create(
                PREFS_NAME,
                masterKeyAlias,
                context,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            Timber.e(e, "Failed to create encrypted prefs, falling back to regular prefs")
            prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        }

        client = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    private fun getConfig(): Triple<String, String, String> {
        val url = prefs?.getString(KEY_WEBDAV_URL, "") ?: ""
        val user = prefs?.getString(KEY_USERNAME, "") ?: ""
        val pass = prefs?.getString(KEY_PASSWORD, "") ?: ""
        return Triple(url, user, pass)
    }

    suspend fun uploadFile(context: Context, localFile: File, remoteName: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val (webdavUrl, username, password) = getConfig()
                val httpClient = client ?: return@withContext false

                if (webdavUrl.isBlank()) {
                    Timber.w("WebDAV not configured")
                    return@withContext false
                }

                val url = "${webdavUrl.trimEnd('/')}/$remoteName"
                val requestBody = localFile.readBytes().toRequestBody("application/octet-stream".toMediaType())

                val request = Request.Builder()
                    .url(url)
                    .put(requestBody)
                    .header("Authorization", "Basic ${android.util.Base64.encodeToString("$username:$password".toByteArray(), android.util.Base64.NO_WRAP)}")
                    .build()

                httpClient.newCall(request).execute().use { response ->
                    val success = response.isSuccessful
                    Timber.d("WebDAV upload: $remoteName -> HTTP ${response.code}")
                    success
                }
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
                val json = favoritesAdapter.toJson(favorites)

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
                val favorites = favoritesAdapter.fromJson(json) ?: emptyList()

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
}
