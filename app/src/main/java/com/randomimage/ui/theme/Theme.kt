package com.randomimage.ui.theme

import android.content.Context
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext

enum class ColorMode(val value: Int) {
    SYSTEM(0),
    LIGHT(1),
    DARK(2),
    MONET_SYSTEM(3),
    MONET_LIGHT(4),
    MONET_DARK(5),
    DARK_AMOLED(6);

    companion object {
        fun fromValue(value: Int) = entries.find { it.value == value } ?: SYSTEM
    }

    val isSystem: Boolean get() = value == 0 || value == 3
    val isDark: Boolean get() = value == 2 || value == 5 || value == 6
    val isAmoled: Boolean get() = value == 6
    val isMonet: Boolean get() = value >= 3

    fun toNonMonetMode(): Int = when (this) {
        MONET_SYSTEM -> 0
        MONET_LIGHT -> 1
        MONET_DARK, DARK_AMOLED -> 2
        else -> value
    }

    fun toMonetMode(): Int = when (this) {
        SYSTEM -> 3
        LIGHT -> 4
        DARK -> 5
        else -> value
    }
}

data class AppSettings(
    val colorMode: ColorMode,
    val keyColor: Int,
    val useDynamicColor: Boolean,
)

object ThemeController {
    private const val PREFS_NAME = "theme_prefs"

    fun getAppSettings(context: Context): AppSettings {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val colorModeValue = prefs.getInt("color_mode", ColorMode.SYSTEM.value)
        val keyColor = prefs.getInt("key_color", Color(0xFF2196F3).toArgb())
        val useDynamicColor = prefs.getBoolean("use_dynamic_color", true)

        return AppSettings(ColorMode.fromValue(colorModeValue), keyColor, useDynamicColor)
    }

    fun saveColorMode(context: Context, mode: ColorMode) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putInt("color_mode", mode.value).apply()
    }

    fun saveKeyColor(context: Context, color: Int) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putInt("key_color", color).apply()
    }

    fun saveDynamicColor(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putBoolean("use_dynamic_color", enabled).apply()
    }
}

@Composable
fun RandomImageTheme(
    appSettings: AppSettings? = null,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val currentAppSettings = appSettings ?: ThemeController.getAppSettings(context)

    val darkTheme = when (currentAppSettings.colorMode) {
        ColorMode.LIGHT, ColorMode.MONET_LIGHT -> false
        ColorMode.DARK, ColorMode.MONET_DARK, ColorMode.DARK_AMOLED -> true
        else -> isSystemInDarkTheme()
    }

    val useDynamicColor = currentAppSettings.useDynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val isAmoled = currentAppSettings.colorMode == ColorMode.DARK_AMOLED

    val colorScheme = when {
        useDynamicColor -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> {
            if (isAmoled) {
                darkColorScheme(
                    primary = Color(currentAppSettings.keyColor),
                    secondary = PurpleGrey80,
                    tertiary = Pink80,
                    background = Color.Black,
                    surface = Color.Black,
                    surfaceVariant = Color(0xFF1C1C1C),
                    onBackground = Color.White,
                    onSurface = Color.White,
                )
            } else {
                darkColorScheme(
                    primary = Color(currentAppSettings.keyColor),
                    secondary = PurpleGrey80,
                    tertiary = Pink80
                )
            }
        }
        else -> {
            lightColorScheme(
                primary = Color(currentAppSettings.keyColor),
                secondary = PurpleGrey40,
                tertiary = Pink40
            )
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
@ReadOnlyComposable
fun isInDarkTheme(): Boolean {
    return when (LocalColorMode.current) {
        1, 4 -> false
        2, 5, 6 -> true
        else -> isSystemInDarkTheme()
    }
}

val LocalColorMode = staticCompositionLocalOf { 0 }
val LocalEnableBlur = staticCompositionLocalOf { false }
