package com.randomimage

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.randomimage.ui.components.BottomNavBar
import com.randomimage.ui.screens.CachePreviewScreen
import com.randomimage.ui.screens.CloudSyncScreen
import com.randomimage.ui.screens.CustomApisScreen
import com.randomimage.ui.screens.FavoritesScreen
import com.randomimage.ui.screens.ImageCropScreen
import com.randomimage.ui.screens.ImageDetailScreen
import com.randomimage.ui.screens.LogScreen
import com.randomimage.ui.screens.SettingsScreen
import com.randomimage.ui.screens.ThemeSettingsScreen
import com.randomimage.ui.screens.WaterfallScreen
import com.randomimage.ui.theme.RandomImageTheme
import com.randomimage.ui.theme.UiMode
import com.randomimage.ui.viewmodel.HomeViewModel
import com.randomimage.util.ThemeManager
import dagger.hilt.android.AndroidEntryPoint
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.theme.MiuixTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        ThemeManager.init(this)
        setContent {
            val uiStyle by ThemeManager.uiStyleFlow.collectAsState()
            val uiMode = UiMode.fromValue(uiStyle)

            RandomImageTheme(uiMode = uiMode) {
                MainContent()
            }
        }
    }

    @Composable
    private fun MainContent() {
        val navController = rememberNavController()
        val homeViewModel: HomeViewModel = hiltViewModel()
        val homeUiState by homeViewModel.uiState.collectAsState()
        val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
        val showBottomBar = currentRoute != "detail"

        Scaffold(
            topBar = {
                if (currentRoute != "detail") {
                    TopAppBar(
                        title = "随机图片",
                        actions = {
                            IconButton(onClick = { homeViewModel.loadImages() }) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "刷新",
                                    tint = MiuixTheme.colorScheme.onBackground
                                )
                            }
                            IconButton(onClick = {
                                val context = this@MainActivity
                                val currentMode = ThemeManager.getColorMode(context)
                                val newMode = if (currentMode.isDark) {
                                    com.randomimage.ui.theme.ColorMode.LIGHT
                                } else {
                                    com.randomimage.ui.theme.ColorMode.DARK
                                }
                                ThemeManager.setColorMode(context, newMode)
                            }) {
                                val isDark = ThemeManager.isDarkMode(this@MainActivity)
                                Icon(
                                    imageVector = if (isDark) Icons.Default.LightMode else Icons.Default.DarkMode,
                                    contentDescription = if (isDark) "浅色模式" else "深色模式",
                                    tint = MiuixTheme.colorScheme.onBackground
                                )
                            }
                            IconButton(onClick = { navController.navigate("settings") }) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "设置",
                                    tint = MiuixTheme.colorScheme.onBackground
                                )
                            }
                        }
                    )
                }
            },
            bottomBar = {
                if (showBottomBar) {
                    BottomNavBar(
                        currentRoute = currentRoute,
                        onNavigate = { route ->
                            navController.navigate(route) {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        favoriteCount = homeUiState.favorites.size
                    )
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = "home",
                modifier = Modifier.padding(innerPadding)
            ) {
                composable("home") {
                    WaterfallScreen(
                        viewModel = homeViewModel,
                        onImageClick = { image ->
                            homeViewModel.setDetailImage(image)
                            navController.navigate("detail")
                        }
                    )
                }
                composable("favorites") {
                    FavoritesScreen(
                        onImageClick = { image ->
                            homeViewModel.setDetailImage(image)
                            navController.navigate("detail")
                        }
                    )
                }
                composable("detail") {
                    val detailImage = homeUiState.detailImage
                    if (detailImage != null) {
                        ImageDetailScreen(
                            image = detailImage,
                            onBack = { navController.popBackStack() },
                            onSwipeLeft = { homeViewModel.swipeToNext() },
                            onSwipeRight = { homeViewModel.swipeToPrev() },
                            onFavorite = { homeViewModel.toggleFavorite() },
                            isFavorite = homeUiState.isFavorite,
                            onFollow = { homeViewModel.toggleFollowArtist() },
                            isFollowing = homeUiState.isFollowingArtist
                        )
                    }
                }
                composable("logs") {
                    LogScreen(onBack = { navController.popBackStack() })
                }
                composable("settings") {
                    SettingsScreen(
                        onBack = { navController.popBackStack() },
                        onClearCache = { homeViewModel.clearCache() },
                        onClearHistory = { homeViewModel.clearHistory() },
                        onClearSearchHistory = { homeViewModel.clearSearchHistory() },
                        onPreviewCache = { navController.navigate("cache_preview") },
                        onCloudSync = { navController.navigate("cloud_sync") },
                        onLogs = { navController.navigate("logs") },
                        onManageApis = { navController.navigate("custom_apis") },
                        onThemeSettings = { navController.navigate("theme_settings") },
                        onQualityChanged = { quality ->
                            val q = when (quality) {
                                "缩略图" -> com.randomimage.ui.viewmodel.ImageQuality.THUMBNAIL
                                "原图" -> com.randomimage.ui.viewmodel.ImageQuality.ORIGINAL
                                else -> com.randomimage.ui.viewmodel.ImageQuality.MEDIUM
                            }
                            homeViewModel.setImageQuality(q)
                        }
                    )
                }
                composable("theme_settings") {
                    ThemeSettingsScreen(onBack = { navController.popBackStack() })
                }
                composable("cache_preview") {
                    CachePreviewScreen(
                        onBack = { navController.popBackStack() },
                        onImageClick = { image ->
                            homeViewModel.setDetailImage(image)
                            navController.navigate("detail")
                        }
                    )
                }
                composable("image_crop/{imageUrl}") { backStackEntry ->
                    val imageUrl = backStackEntry.arguments?.getString("imageUrl") ?: ""
                    ImageCropScreen(
                        imageUrl = imageUrl,
                        onBack = { navController.popBackStack() },
                        onCropped = { navController.popBackStack() }
                    )
                }
                composable("cloud_sync") {
                    CloudSyncScreen(onBack = { navController.popBackStack() })
                }
                composable("custom_apis") {
                    CustomApisScreen(
                        onBack = { navController.popBackStack() },
                        onApisChanged = { homeViewModel.refreshApis() }
                    )
                }
            }
        }
    }
}
