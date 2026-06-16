package com.randomimage

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ViewCarousel
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import com.randomimage.ui.screens.HomeScreen
import com.randomimage.ui.screens.ImageCropScreen
import com.randomimage.ui.screens.ImageDetailScreen
import com.randomimage.ui.screens.LogScreen
import com.randomimage.ui.screens.SettingsScreen
import com.randomimage.ui.screens.WaterfallScreen
import com.randomimage.ui.theme.RandomImageTheme
import com.randomimage.ui.viewmodel.HomeViewModel
import com.randomimage.util.ThemeManager
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RandomImageTheme {
                val navController = rememberNavController()
                val homeViewModel: HomeViewModel = hiltViewModel()
                val homeUiState by homeViewModel.uiState.collectAsState()

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        if (!homeUiState.showDetail) {
                            TopAppBar(
                                title = { Text("随机图片") },
                                actions = {
                                    IconButton(onClick = { homeViewModel.loadImages() }) {
                                        Icon(Icons.Default.Refresh, contentDescription = "刷新")
                                    }
                                    IconButton(onClick = { homeViewModel.toggleTheme(this@MainActivity) }) {
                                        val isDark = ThemeManager.getThemeMode(this@MainActivity) == ThemeManager.THEME_DARK
                                        Icon(
                                            imageVector = if (isDark) Icons.Default.LightMode else Icons.Default.DarkMode,
                                            contentDescription = if (isDark) "浅色模式" else "深色模式"
                                        )
                                    }
                                    IconButton(onClick = { homeViewModel.setIsWaterfall(!homeUiState.isWaterfall) }) {
                                        Icon(
                                            imageVector = if (homeUiState.isWaterfall) Icons.Default.ViewCarousel else Icons.Default.GridView,
                                            contentDescription = if (homeUiState.isWaterfall) "切换卡片" else "切换瀑布流"
                                        )
                                    }
                                    IconButton(onClick = { navController.navigate("settings") }) {
                                        Icon(Icons.Default.Settings, contentDescription = "设置")
                                    }
                                },
                                colors = TopAppBarDefaults.topAppBarColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer
                                )
                            )
                        }
                    },
                    bottomBar = {
                        if (!homeUiState.showDetail) {
                            BottomNavBar(
                                currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route,
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
                    if (homeUiState.showDetail) {
                        val currentImage = homeViewModel.getCurrentImage()
                        if (currentImage != null) {
                            ImageDetailScreen(
                                image = currentImage,
                                onBack = { homeViewModel.setShowDetail(false) },
                                onSwipeLeft = {
                                    homeViewModel.swipeLeft()
                                },
                                onSwipeRight = {
                                    homeViewModel.swipeRight()
                                },
                                onFavorite = { homeViewModel.toggleFavorite() },
                                isFavorite = homeUiState.isFavorite,
                                imageIndex = homeUiState.currentIndex,
                                totalImages = homeUiState.images.size
                            )
                        }
                    } else {
                        NavHost(
                            navController = navController,
                            startDestination = "home",
                            modifier = Modifier.padding(innerPadding)
                        ) {
                            composable("home") {
                                if (homeUiState.isWaterfall) {
                                    WaterfallScreen(
                                        viewModel = homeViewModel,
                                        onImageClick = { index ->
                                            homeViewModel.setCurrentIndex(index)
                                            homeViewModel.setShowDetail(true)
                                        }
                                    )
                                } else {
                                    HomeScreen(
                                        viewModel = homeViewModel,
                                        onImageClick = { homeViewModel.setShowDetail(true) }
                                    )
                                }
                            }
                            composable("favorites") {
                                FavoritesScreen(
                                    onImageClick = { image ->
                                        homeViewModel.setCurrentImage(image)
                                        homeViewModel.setShowDetail(true)
                                    }
                                )
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
                                    onManageApis = { navController.navigate("custom_apis") }
                                )
                            }
                            composable("cache_preview") {
                                CachePreviewScreen(onBack = { navController.popBackStack() })
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
        }
    }
}
