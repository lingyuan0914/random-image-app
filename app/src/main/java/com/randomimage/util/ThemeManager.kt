package com.randomimage.util

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 主题模式：0=跟随系统, 1=浅色, 2=深色
 */
object ThemeManager {
    private const val PREFS_NAME = "theme_prefs"
    private const val KEY_THEME_MODE = "theme_mode"
    private const val KEY_KEY_COLOR = "key_color"
    private const val KEY_AMOLED = "amoled_mode"
    private const val KEY_UI_STYLE = "ui_style"

    const val MODE_SYSTEM = 0
    const val MODE_LIGHT = 1
    const val MODE_DARK = 2

    val keyColorOptions = listOf(
        0xFFF44336.toInt(), // Red
        0xFFE91E63.toInt(), // Pink
        0xFF9C27B0.toInt(), // Purple
        0xFF673AB7.toInt(), // Deep Purple
        0xFF3F51B5.toInt(), // Indigo
        0xFF2196F3.toInt(), // Blue
        0xFF00BCD4.toInt(), // Cyan
        0xFF009688.toInt(), // Teal
        0xFF4CAF50.toInt(), // Green
        0xFFFFEB3B.toInt(), // Yellow
        0xFFFFC107.toInt(), // Amber
        0xFFFF9800.toInt(), // Orange
        0xFF795548.toInt(), // Brown
        0xFF607D8B.toInt(), // Blue Grey
    )

    private val _themeModeFlow = MutableStateFlow(MODE_SYSTEM)
    val themeModeFlow: StateFlow<Int> = _themeModeFlow.asStateFlow()

    private val _keyColorFlow = MutableStateFlow(0xFF2196F3.toInt())
    val keyColorFlow: StateFlow<Int> = _keyColorFlow.asStateFlow()

    private val _amoledFlow = MutableStateFlow(false)
    val amoledFlow: StateFlow<Boolean> = _amoledFlow.asStateFlow()

    private val _uiStyleFlow = MutableStateFlow("material")
    val uiStyleFlow: StateFlow<String> = _uiStyleFlow.asStateFlow()

    private var initialized = false

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun init(context: Context) {
        if (initialized) return
        initialized = true
        val prefs = getPrefs(context)

        _themeModeFlow.value = prefs.getInt(KEY_THEME_MODE, MODE_SYSTEM)
        _keyColorFlow.value = prefs.getInt(KEY_KEY_COLOR, 0xFF2196F3.toInt())
        _amoledFlow.value = prefs.getBoolean(KEY_AMOLED, false)
        _uiStyleFlow.value = prefs.getString(KEY_UI_STYLE, "material") ?: "material"

        prefs.registerOnSharedPreferenceChangeListener { _, key ->
            when (key) {
                KEY_THEME_MODE -> _themeModeFlow.value = prefs.getInt(KEY_THEME_MODE, MODE_SYSTEM)
                KEY_KEY_COLOR -> _keyColorFlow.value = prefs.getInt(KEY_KEY_COLOR, 0xFF2196F3.toInt())
                KEY_AMOLED -> _amoledFlow.value = prefs.getBoolean(KEY_AMOLED, false)
                KEY_UI_STYLE -> _uiStyleFlow.value = prefs.getString(KEY_UI_STYLE, "material") ?: "material"
            }
        }
    }

    fun getThemeMode(context: Context): Int {
        return getPrefs(context).getInt(KEY_THEME_MODE, MODE_SYSTEM)
    }

    fun setThemeMode(context: Context, mode: Int) {
        getPrefs(context).edit().putInt(KEY_THEME_MODE, mode).apply()
    }

    fun getKeyColor(context: Context): Int {
        return getPrefs(context).getInt(KEY_KEY_COLOR, 0xFF2196F3.toInt())
    }

    fun setKeyColor(context: Context, color: Int) {
        getPrefs(context).edit().putInt(KEY_KEY_COLOR, color).apply()
    }

    fun getAmoled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_AMOLED, false)
    }

    fun setAmoled(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_AMOLED, enabled).apply()
    }

    fun getUiStyle(context: Context): String {
        return getPrefs(context).getString(KEY_UI_STYLE, "material") ?: "material"
    }

    fun setUiStyle(context: Context, style: String) {
        getPrefs(context).edit().putString(KEY_UI_STYLE, style).apply()
    }

    fun isDarkMode(context: Context): Boolean {
        val mode = getThemeMode(context)
        return when (mode) {
            MODE_LIGHT -> false
            MODE_DARK -> true
            else -> {
                val nightModeFlags = context.resources.configuration.uiMode and
                        android.content.res.Configuration.UI_MODE_NIGHT_MASK
                nightModeFlags == android.content.res.Configuration.UI_MODE_NIGHT_YES
            }
        }
    }
}
