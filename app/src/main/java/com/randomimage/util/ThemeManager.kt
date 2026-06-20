package com.randomimage.util

import android.content.Context
import android.content.SharedPreferences
import com.randomimage.ui.theme.ColorMode
import com.randomimage.ui.theme.ThemeController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object ThemeManager {
    private const val PREFS_NAME = "theme_prefs"
    private const val KEY_THEME_MODE = "theme_mode"
    private const val KEY_PREDICTIVE_BACK = "predictive_back"

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

    private var initialized = false

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun init(context: Context) {
        if (initialized) return
        initialized = true
        val prefs = getPrefs(context)
        _themeModeFlow.value = prefs.getInt(KEY_THEME_MODE, THEME_SYSTEM)
        _keyColorFlow.value = prefs.getInt("key_color", 0xFF2196F3.toInt())
        _amoledFlow.value = prefs.getBoolean("amoled_mode", false)
        _dynamicColorFlow.value = prefs.getBoolean("use_dynamic_color", true)

        val colorModeValue = prefs.getInt("color_mode", ColorMode.SYSTEM.value)
        _colorModeFlow.value = ColorMode.fromValue(colorModeValue)

        prefs.registerOnSharedPreferenceChangeListener { _, key ->
            when (key) {
                KEY_THEME_MODE -> _themeModeFlow.value = prefs.getInt(KEY_THEME_MODE, THEME_SYSTEM)
                "key_color" -> _keyColorFlow.value = prefs.getInt("key_color", 0xFF2196F3.toInt())
                "amoled_mode" -> _amoledFlow.value = prefs.getBoolean("amoled_mode", false)
                "use_dynamic_color" -> _dynamicColorFlow.value = prefs.getBoolean("use_dynamic_color", true)
                "color_mode" -> {
                    val value = prefs.getInt("color_mode", ColorMode.SYSTEM.value)
                    _colorModeFlow.value = ColorMode.fromValue(value)
                }
            }
        }
    }

    fun getThemeMode(context: Context): Int {
        return getPrefs(context).getInt(KEY_THEME_MODE, THEME_SYSTEM)
    }

    fun setThemeMode(context: Context, mode: Int) {
        getPrefs(context).edit().putInt(KEY_THEME_MODE, mode).apply()
    }

    fun getColorMode(context: Context): ColorMode {
        val value = getPrefs(context).getInt("color_mode", ColorMode.SYSTEM.value)
        return ColorMode.fromValue(value)
    }

    fun setColorMode(context: Context, mode: ColorMode) {
        getPrefs(context).edit().putInt("color_mode", mode.value).apply()
        ThemeController.saveColorMode(context, mode)
    }

    fun getKeyColor(context: Context): Int {
        return getPrefs(context).getInt("key_color", 0xFF2196F3.toInt())
    }

    fun setKeyColor(context: Context, color: Int) {
        getPrefs(context).edit().putInt("key_color", color).apply()
        ThemeController.saveKeyColor(context, color)
    }

    fun getAmoled(context: Context): Boolean {
        return getPrefs(context).getBoolean("amoled_mode", false)
    }

    fun setAmoled(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean("amoled_mode", enabled).apply()
        if (enabled) {
            setColorMode(context, ColorMode.DARK_AMOLED)
        } else {
            val currentMode = getColorMode(context)
            if (currentMode == ColorMode.DARK_AMOLED) {
                setColorMode(context, ColorMode.DARK)
            }
        }
    }

    fun getDynamicColor(context: Context): Boolean {
        return getPrefs(context).getBoolean("use_dynamic_color", true)
    }

    fun setDynamicColor(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean("use_dynamic_color", enabled).apply()
        ThemeController.saveDynamicColor(context, enabled)
    }

    fun getPredictiveBack(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_PREDICTIVE_BACK, true)
    }

    fun setPredictiveBack(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_PREDICTIVE_BACK, enabled).apply()
    }
}
