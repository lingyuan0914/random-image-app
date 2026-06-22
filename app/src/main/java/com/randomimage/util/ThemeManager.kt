package com.randomimage.util

import android.content.Context
import android.content.SharedPreferences
import com.randomimage.ui.theme.ColorMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object ThemeManager {
    private const val PREFS_NAME = "theme_prefs"

    const val THEME_LIGHT = 0
    const val THEME_DARK = 1
    const val THEME_SYSTEM = 2

    val keyColorOptions = listOf(
        0xFFF44336.toInt(),
        0xFFE91E63.toInt(),
        0xFF9C27B0.toInt(),
        0xFF673AB7.toInt(),
        0xFF3F51B5.toInt(),
        0xFF2196F3.toInt(),
        0xFF00BCD4.toInt(),
        0xFF009688.toInt(),
        0xFF4CAF50.toInt(),
        0xFFFFEB3B.toInt(),
        0xFFFFC107.toInt(),
        0xFFFF9800.toInt(),
        0xFF795548.toInt(),
        0xFF607D8B.toInt(),
    )

    private val _themeModeFlow = MutableStateFlow(THEME_SYSTEM)
    val themeModeFlow: StateFlow<Int> = _themeModeFlow.asStateFlow()

    private val _colorModeFlow = MutableStateFlow(ColorMode.SYSTEM)
    val colorModeFlow: StateFlow<ColorMode> = _colorModeFlow.asStateFlow()

    private val _keyColorFlow = MutableStateFlow(0xFF2196F3.toInt())
    val keyColorFlow: StateFlow<Int> = _keyColorFlow.asStateFlow()

    private val _dynamicColorFlow = MutableStateFlow(true)
    val dynamicColorFlow: StateFlow<Boolean> = _dynamicColorFlow.asStateFlow()

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
        _themeModeFlow.value = prefs.getInt("theme_mode", THEME_SYSTEM)
        _keyColorFlow.value = prefs.getInt("key_color", 0xFF2196F3.toInt())
        _amoledFlow.value = prefs.getBoolean("amoled_mode", false)
        _dynamicColorFlow.value = prefs.getBoolean("use_dynamic_color", true)
        _uiStyleFlow.value = prefs.getString("ui_style", "material") ?: "material"
        _colorModeFlow.value = ColorMode.fromValue(prefs.getInt("color_mode", ColorMode.SYSTEM.value))

        prefs.registerOnSharedPreferenceChangeListener { _, key ->
            when (key) {
                "theme_mode" -> _themeModeFlow.value = prefs.getInt("theme_mode", THEME_SYSTEM)
                "key_color" -> _keyColorFlow.value = prefs.getInt("key_color", 0xFF2196F3.toInt())
                "amoled_mode" -> _amoledFlow.value = prefs.getBoolean("amoled_mode", false)
                "use_dynamic_color" -> _dynamicColorFlow.value = prefs.getBoolean("use_dynamic_color", true)
                "ui_style" -> _uiStyleFlow.value = prefs.getString("ui_style", "material") ?: "material"
                "color_mode" -> _colorModeFlow.value = ColorMode.fromValue(prefs.getInt("color_mode", ColorMode.SYSTEM.value))
            }
        }
    }

    fun getThemeMode(context: Context): Int {
        return getPrefs(context).getInt("theme_mode", THEME_SYSTEM)
    }

    fun setThemeMode(context: Context, mode: Int) {
        getPrefs(context).edit().putInt("theme_mode", mode).apply()
    }

    fun getColorMode(context: Context): ColorMode {
        return ColorMode.fromValue(getPrefs(context).getInt("color_mode", ColorMode.SYSTEM.value))
    }

    fun setColorMode(context: Context, mode: ColorMode) {
        getPrefs(context).edit().putInt("color_mode", mode.value).apply()
    }

    fun getKeyColor(context: Context): Int {
        return getPrefs(context).getInt("key_color", 0xFF2196F3.toInt())
    }

    fun setKeyColor(context: Context, color: Int) {
        getPrefs(context).edit().putInt("key_color", color).apply()
    }

    fun getAmoled(context: Context): Boolean {
        return getPrefs(context).getBoolean("amoled_mode", false)
    }

    fun setAmoled(context: Context, enabled: Boolean) {
        val prefs = getPrefs(context)
        prefs.edit().putBoolean("amoled_mode", enabled).apply()
        if (enabled) {
            prefs.edit().putInt("color_mode", ColorMode.DARK_AMOLED.value).apply()
        } else if (getColorMode(context) == ColorMode.DARK_AMOLED) {
            prefs.edit().putInt("color_mode", ColorMode.DARK.value).apply()
        }
    }

    fun getDynamicColor(context: Context): Boolean {
        return getPrefs(context).getBoolean("use_dynamic_color", true)
    }

    fun setDynamicColor(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean("use_dynamic_color", enabled).apply()
    }

    fun getPredictiveBack(context: Context): Boolean {
        return getPrefs(context).getBoolean("predictive_back", true)
    }

    fun setPredictiveBack(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean("predictive_back", enabled).apply()
    }

    fun getUiStyle(context: Context): String {
        return getPrefs(context).getString("ui_style", "material") ?: "material"
    }

    fun setUiStyle(context: Context, style: String) {
        getPrefs(context).edit().putString("ui_style", style).apply()
    }
}
