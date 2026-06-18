package com.randomimage

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import coil.size.Size
import com.randomimage.ui.components.BottomNavBar
import com.randomimage.ui.screens.CachePreviewScreen
import com.randomimage.ui.screens.CloudSyncScreen
import com.randomimage.ui.screens.CustomApisScreen
import com.randomimage.ui.screens.FavoritesScreen
import com.randomimage.ui.screens.ImageCropScreen
import com.randomimage.ui.screens.ImageDetailScreen
import com.randomimage.ui.screens.LogScreen
import com.randomimage.ui.screens.SettingsScreen
import com.randomimage.ui.screens.WaterfallScreen
import com.randomimage.ui.theme.RandomImageTheme
import com.randomimage.ui.viewmodel.HomeViewModel
import com.randomimage.util.ThemeManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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
                val context = LocalContext.current
                val predictiveBack = remember { ThemeManager.getPredictiveBack(context) }

                Box(modifier = Modifier.fillMaxSize()) {
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
                        AnimatedContent(
                            targetState = if (homeUiState.showDetail) "detail" else navController.currentBackStackEntryAsState().value?.destination?.route ?: "home",
                            transitionSpec = {
                                if (targetState == "detail") {
                                    fadeIn(tween(300)) togetherWith fadeOut(tween(300))
                                } else {
                                    slideInHorizontally(tween(300)) { -it / 3 } + fadeIn(tween(300)) togetherWith
                                    slideOutHorizontally(tween(300)) { it / 3 } + fadeOut(tween(300))
                                }
                            },
                            modifier = Modifier.padding(innerPadding),
                            label = "nav"
                        ) { targetRoute ->
                            if (targetRoute == "detail") {
                                val currentImage = homeUiState.detailImage ?: homeViewModel.getCurrentImage()
                                if (currentImage != null) {
                                    ImageDetailScreen(
                                        image = currentImage,
                                        onBack = {
                                            homeViewModel.clearExpandBounds()
                                            homeViewModel.setShowDetail(false)
                                        },
                                        onSwipeLeft = { homeViewModel.swipeToNext() },
                                        onSwipeRight = { homeViewModel.swipeToPrev() },
                                        onFavorite = { homeViewModel.toggleFavorite() },
                                        isFavorite = homeUiState.isFavorite,
                                        onFollow = { homeViewModel.toggleFollowArtist() },
                                        isFollowing = homeUiState.isFollowingArtist
                                    )
                                }
                            } else {
                                when (targetRoute) {
                                    "home" -> WaterfallScreen(
                                        viewModel = homeViewModel,
                                        onImageClick = { image ->
                                            homeViewModel.setDetailImage(image)
                                        }
                                    )
                                    "favorites" -> FavoritesScreen(
                                        onImageClick = { image ->
                                            homeViewModel.setCurrentImage(image)
                                            homeViewModel.setShowDetail(true)
                                        }
                                    )
                                    "logs" -> LogScreen(onBack = { navController.popBackStack() })
                                    "settings" -> SettingsScreen(
                                        onBack = { navController.popBackStack() },
                                        onClearCache = { homeViewModel.clearCache() },
                                        onClearHistory = { homeViewModel.clearHistory() },
                                        onClearSearchHistory = { homeViewModel.clearSearchHistory() },
                                        onPreviewCache = { navController.navigate("cache_preview") },
                                        onCloudSync = { navController.navigate("cloud_sync") },
                                        onLogs = { navController.navigate("logs") },
                                        onManageApis = { navController.navigate("custom_apis") }
                                    )
                                    "cache_preview" -> CachePreviewScreen(
                                        onBack = { navController.popBackStack() },
                                        onImageClick = { image -> homeViewModel.setDetailImage(image) }
                                    )
                                    "cloud_sync" -> CloudSyncScreen(onBack = { navController.popBackStack() })
                                    "custom_apis" -> CustomApisScreen(
                                        onBack = { navController.popBackStack() },
                                        onApisChanged = { homeViewModel.refreshApis() }
                                    )
                                    else -> WaterfallScreen(
                                        viewModel = homeViewModel,
                                        onImageClick = { image ->
                                            homeViewModel.setDetailImage(image)
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Expand animation overlay
                    ExpandImageOverlay(
                        image = homeUiState.detailImage,
                        bounds = homeUiState.expandImageBounds,
                        showDetail = homeUiState.showDetail,
                        onAnimationDone = { homeViewModel.clearExpandBounds() }
                    )
                }
            }
        }
    }
}

@Composable
private fun ExpandImageOverlay(
    image: com.randomimage.domain.model.ImageModel?,
    bounds: Rect?,
    showDetail: Boolean,
    onAnimationDone: () -> Unit
) {
    if (image == null || bounds == Rect.Zero) return

    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    val screenWidth = with(density) { androidx.compose.ui.platform.LocalConfiguration.current.screenWidthDp.dp.toPx() }
    val screenHeight = with(density) { androidx.compose.ui.platform.LocalConfiguration.current.screenHeightDp.dp.toPx() }

    val progress = remember { Animatable(0f) }
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(showDetail) {
        if (showDetail && bounds != Rect.Zero) {
            isVisible = true
            progress.snapTo(0f)
            progress.animateTo(1f, animationSpec = tween(350))
            onAnimationDone()
        } else if (!showDetail && isVisible) {
            progress.animateTo(0f, animationSpec = tween(300))
            isVisible = false
        }
    }

    if (isVisible && bounds != null && bounds != Rect.Zero) {
        val currentBounds = Rect(
            left = bounds.left,
            top = bounds.top,
            right = bounds.left + bounds.width + (screenWidth - bounds.width - bounds.left) * progress.value,
            bottom = bounds.top + bounds.height + (screenHeight - bounds.height - bounds.top) * progress.value
        )

        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(image.localPath ?: image.urls.thumb)
                .memoryCacheKey(image.id)
                .crossfade(false)
                .size(Size.ORIGINAL)
                .allowHardware(true)
                .build(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .graphicsLayer {
                    translationX = currentBounds.left
                    translationY = currentBounds.top
                    scaleX = currentBounds.width / bounds.width
                    scaleY = currentBounds.height / bounds.height
                    alpha = 1f - progress.value * 0.3f
                }
                .background(Color.Black)
        )
    }
}
