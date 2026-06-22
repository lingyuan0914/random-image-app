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

@Composable
fun RandomImageTheme(
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    ThemeManager.init(context)

    val themeMode by ThemeManager.themeModeFlow.collectAsState()
    val keyColor by ThemeManager.keyColorFlow.collectAsState()
    val amoled by ThemeManager.amoledFlow.collectAsState()

    val darkTheme = when (themeMode) {
        ThemeManager.MODE_LIGHT -> false
        ThemeManager.MODE_DARK -> true
        else -> isSystemInDarkTheme()
    }

    val colorScheme = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> {
            if (amoled) {
                darkColorScheme(
                    primary = Color(keyColor),
                    background = Color.Black,
                    surface = Color.Black,
                    surfaceVariant = Color(0xFF1C1C1C),
                    onBackground = Color.White,
                    onSurface = Color.White,
                )
            } else {
                darkColorScheme(primary = Color(keyColor))
            }
        }
        else -> {
            lightColorScheme(primary = Color(keyColor))
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
