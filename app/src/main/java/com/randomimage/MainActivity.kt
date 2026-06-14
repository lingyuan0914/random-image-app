package com.randomimage

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.OnBackPressedCallback
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GridView
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.randomimage.ui.components.BottomNavBar
import com.randomimage.ui.screens.FavoritesScreen
import com.randomimage.ui.screens.HomeScreen
import com.randomimage.ui.screens.ImageDetailScreen
import com.randomimage.ui.screens.SettingsScreen
import com.randomimage.ui.screens.ToolboxScreen
import com.randomimage.ui.screens.WaterfallScreen
import com.randomimage.ui.theme.RandomImageTheme
import com.randomimage.ui.viewmodel.HomeViewModel
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
                var showDetail by remember { mutableStateOf(false) }
                var isWaterfall by remember { mutableStateOf(false) }
                var showSettings by remember { mutableStateOf(false) }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        if (!showDetail && !showSettings) {
                            TopAppBar(
                                title = { Text("随机图片") },
                                actions = {
                                    IconButton(onClick = { isWaterfall = !isWaterfall }) {
                                        Icon(
                                            imageVector = if (isWaterfall) Icons.Default.ViewCarousel else Icons.Default.GridView,
                                            contentDescription = if (isWaterfall) "切换卡片" else "切换瀑布流"
                                        )
                                    }
                                    IconButton(onClick = { showSettings = true }) {
                                        Icon(
                                            imageVector = Icons.Default.Settings,
                                            contentDescription = "设置"
                                        )
                                    }
                                },
                                colors = TopAppBarDefaults.topAppBarColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer
                                )
                            )
                        }
                    },
                    bottomBar = {
                        if (!showDetail && !showSettings) {
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
                                favoriteCount = 0
                            )
                        }
                    }
                ) { innerPadding ->
                    if (showSettings) {
                        SettingsScreen(
                            onBack = { showSettings = false },
                            onClearCache = { homeViewModel.clearCache() },
                            onClearHistory = { homeViewModel.clearHistory() },
                            onClearSearchHistory = { homeViewModel.clearSearchHistory() }
                        )
                    } else if (showDetail) {
                        val currentImage = homeViewModel.getCurrentImage()
                        if (currentImage != null) {
                            ImageDetailScreen(
                                image = currentImage,
                                onBack = { showDetail = false },
                                onSwipeLeft = {
                                    homeViewModel.swipeLeft()
                                },
                                onSwipeRight = {
                                    homeViewModel.swipeRight()
                                },
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
                                if (isWaterfall) {
                                    WaterfallScreen(
                                        viewModel = homeViewModel,
                                        onImageClick = { index ->
                                            homeViewModel.setCurrentIndex(index)
                                            showDetail = true
                                        }
                                    )
                                } else {
                                    HomeScreen(
                                        viewModel = homeViewModel,
                                        onImageClick = { showDetail = true }
                                    )
                                }
                            }
                            composable("favorites") {
                                FavoritesScreen()
                            }
                            composable("toolbox") {
                                ToolboxScreen()
                            }
                        }
                    }
                }
            }
        }
    }
}
