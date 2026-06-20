package com.randomimage.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowInsetsControllerCompat
import com.randomimage.util.ThemeManager

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
}

@Composable
fun RandomImageTheme(
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    ThemeManager.init(context)

    val colorMode by ThemeManager.colorModeFlow.collectAsState()
    val keyColor by ThemeManager.keyColorFlow.collectAsState()
    val dynamicColor by ThemeManager.dynamicColorFlow.collectAsState()

    val darkTheme = when (colorMode) {
        ColorMode.LIGHT, ColorMode.MONET_LIGHT -> false
        ColorMode.DARK, ColorMode.MONET_DARK, ColorMode.DARK_AMOLED -> true
        else -> isSystemInDarkTheme()
    }

    val useDynamicColor = dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val isAmoled = colorMode == ColorMode.DARK_AMOLED

    val colorScheme = when {
        useDynamicColor -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> {
            if (isAmoled) {
                darkColorScheme(
                    primary = Color(keyColor),
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
                    primary = Color(keyColor),
                    secondary = PurpleGrey80,
                    tertiary = Pink80
                )
            }
        }
        else -> {
            lightColorScheme(
                primary = Color(keyColor),
                secondary = PurpleGrey40,
                tertiary = Pink40
            )
        }
    }

    LaunchedEffect(darkTheme) {
        val window = (context as? Activity)?.window ?: return@LaunchedEffect
        WindowInsetsControllerCompat(window, window.decorView).apply {
            isAppearanceLightStatusBars = !darkTheme
            isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
