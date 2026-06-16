package com.randomimage.data.remote

import android.content.Context
import android.content.SharedPreferences
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import timber.log.Timber

data class CustomApiConfig(
    val id: String,
    val name: String,
    val url: String,
    val enabled: Boolean = true
)

object CustomApiManager {
    private const val PREFS_NAME = "custom_api_prefs"
    private const val KEY_APIS = "custom_apis"

    private var prefs: SharedPreferences? = null
    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun getCustomApis(): List<CustomApiConfig> {
        val json = prefs?.getString(KEY_APIS, null) ?: return emptyList()
        return try {
            val type = Types.newParameterizedType(List::class.java, CustomApiConfig::class.java)
            moshi.adapter<List<CustomApiConfig>>(type).fromJson(json) ?: emptyList()
        } catch (e: Exception) {
            Timber.e(e, "Failed to parse custom APIs")
            emptyList()
        }
    }

    fun addCustomApi(name: String, url: String) {
        val apis = getCustomApis().toMutableList()
        val id = "custom_${System.currentTimeMillis()}"
        apis.add(CustomApiConfig(id = id, name = name, url = url))
        saveApis(apis)
    }

    fun removeCustomApi(id: String) {
        val apis = getCustomApis().toMutableList()
        apis.removeAll { it.id == id }
        saveApis(apis)
    }

    fun toggleCustomApi(id: String) {
        val apis = getCustomApis().toMutableList()
        val index = apis.indexOfFirst { it.id == id }
        if (index >= 0) {
            apis[index] = apis[index].copy(enabled = !apis[index].enabled)
            saveApis(apis)
        }
    }

    private fun saveApis(apis: List<CustomApiConfig>) {
        val type = Types.newParameterizedType(List::class.java, CustomApiConfig::class.java)
        val json = moshi.adapter<List<CustomApiConfig>>(type).toJson(apis)
        prefs?.edit()?.putString(KEY_APIS, json)?.apply()
    }
}
