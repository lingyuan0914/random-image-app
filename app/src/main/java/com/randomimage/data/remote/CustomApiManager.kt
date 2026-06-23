package com.randomimage.data.remote

import android.content.Context
import android.content.SharedPreferences
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

enum class ApiType(val label: String) {
    AUTO("自动检测"),
    LOLICON("Lolicon格式"),
    DIRECT_IMAGE("直连图片")
}

data class CustomApiConfig(
    val id: String = "",
    val name: String = "",
    val url: String = "",
    val enabled: Boolean = true,
    val apiType: String = ApiType.AUTO.name,
    val rateLimit: Int = 0,
    val rateLimitWindow: Int = 10
)

data class PresetApi(
    val name: String,
    val url: String,
    val apiType: ApiType = ApiType.AUTO,
    val description: String = ""
)

@Singleton
class CustomApiManager @Inject constructor(
    private val moshi: Moshi
) {
    private var prefs: SharedPreferences? = null

    val presetApis = listOf(
        PresetApi("Lolicon", "https://api.lolicon.app/setu/v2", ApiType.LOLICON, "二次元插画API"),
        PresetApi("Elaina", "https://api.elaina.cat/random/", ApiType.DIRECT_IMAGE, "随机壁纸"),
        PresetApi("TheCatAPI", "https://api.thecatapi.com/v1/images/search", ApiType.AUTO, "猫咪图片"),
        PresetApi("TheDogAPI", "https://dog.ceo/api/breeds/image/random", ApiType.AUTO, "狗狗图片"),
        PresetApi("Picsum", "https://picsum.photos/v2/list", ApiType.AUTO, "高清摄影图"),
        PresetApi("随机壁纸", "https://t.mwm.moe/fj/", ApiType.DIRECT_IMAGE, "二次元风景壁纸")
    )

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

    fun addCustomApi(name: String, url: String, apiType: ApiType = ApiType.AUTO, rateLimit: Int = 0, rateLimitWindow: Int = 10) {
        val apis = getCustomApis().toMutableList()
        if (apis.any { it.url == url }) return
        val id = "custom_${System.currentTimeMillis()}"
        apis.add(CustomApiConfig(id = id, name = name, url = url, apiType = apiType.name, rateLimit = rateLimit, rateLimitWindow = rateLimitWindow))
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

    fun updateRateLimit(id: String, rateLimit: Int, rateLimitWindow: Int) {
        val apis = getCustomApis().toMutableList()
        val index = apis.indexOfFirst { it.id == id }
        if (index >= 0) {
            apis[index] = apis[index].copy(rateLimit = rateLimit, rateLimitWindow = rateLimitWindow)
            saveApis(apis)
        }
    }

    private fun saveApis(apis: List<CustomApiConfig>) {
        val type = Types.newParameterizedType(List::class.java, CustomApiConfig::class.java)
        val json = moshi.adapter<List<CustomApiConfig>>(type).toJson(apis)
        prefs?.edit()?.putString(KEY_APIS, json)?.apply()
    }

    companion object {
        private const val PREFS_NAME = "custom_api_prefs"
        private const val KEY_APIS = "custom_apis"
    }
}
