# Bug Fixes + Log Export Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use compose:subagent (recommended) or compose:execute to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Fix all 20 identified bugs in the random-image-app and add a log export feature for debugging.

**Architecture:** Bug fixes focus on memory leaks, state management, UI functionality gaps, and security issues. Log export adds a new screen to view/share app logs using Timber for structured logging.

**Tech Stack:** Kotlin, Jetpack Compose, Hilt, Room, Coil, Timber, OkHttp

---

## Task 1: Fix ImageLoader Memory Leak + Lifecycle

**Covers:** Bug #1, #2

**Files:**
- Modify: `app/src/main/java/com/randomimage/di/AppModule.kt`
- Modify: `app/src/main/java/com/randomimage/util/ImageUtils.kt`
- Modify: `app/src/main/java/com/randomimage/util/DownloadManager.kt`

- [ ] **Step 1: Add Timber dependency to build.gradle.kts**

```kotlin
// app/build.gradle.kts
dependencies {
    implementation("com.jakewharton.timber:timber:5.0.1")
}
```

- [ ] **Step 2: Provide singleton ImageLoader in AppModule**

```kotlin
// app/src/main/java/com/randomimage/di/AppModule.kt
@Provides
@Singleton
fun provideImageLoader(
    @ApplicationContext context: Context,
    okHttpClient: OkHttpClient
): ImageLoader {
    return ImageLoader.Builder(context)
        .okHttpClient(okHttpClient)
        .crossfade(true)
        .memoryCache {
            MemoryCache.Builder(context)
                .maxSizePercent(0.25)
                .build()
        }
        .diskCache {
            DiskCache.Builder()
                .directory(context.cacheDir.resolve("image_cache"))
                .maxSizePercent(0.02)
                .build()
        }
        .build()
}
```

- [ ] **Step 3: Update ImageUtils to use injected ImageLoader**

```kotlin
// app/src/main/java/com/randomimage/util/ImageUtils.kt
object ImageUtils {
    private var imageLoader: ImageLoader? = null

    fun init(loader: ImageLoader) {
        imageLoader = loader
    }

    suspend fun downloadImage(context: Context, imageUrl: String): Boolean {
        return DownloadManager.downloadImage(context, imageUrl)
    }

    fun shareImage(context: Context, imageUrl: String) {
        CoroutineScope(Dispatchers.IO).launch {
            ShareUtils.shareImage(context, imageUrl)
        }
    }

    fun shareToWechat(context: Context, imageUrl: String) {
        ShareUtils.shareToWechat(context, imageUrl)
    }

    fun shareToQQ(context: Context, imageUrl: String) {
        ShareUtils.shareToQQ(context, imageUrl)
    }

    suspend fun setWallpaper(context: Context, imageUrl: String): Boolean {
        return try {
            val loader = imageLoader ?: return false
            val request = ImageRequest.Builder(context)
                .data(imageUrl)
                .allowHardware(false)
                .build()

            val result = loader.execute(request)
            if (result is SuccessResult) {
                val bitmap = (result.drawable as BitmapDrawable).bitmap
                val wallpaperManager = WallpaperManager.getInstance(context)
                wallpaperManager.setBitmap(bitmap)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to set wallpaper")
            false
        }
    }

    suspend fun getBitmap(context: Context, imageUrl: String): Bitmap? {
        return try {
            val loader = imageLoader ?: return null
            val request = ImageRequest.Builder(context)
                .data(imageUrl)
                .allowHardware(false)
                .build()

            val result = loader.execute(request)
            if (result is SuccessResult) {
                (result.drawable as BitmapDrawable).bitmap
            } else {
                null
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to get bitmap")
            null
        }
    }
}
```

- [ ] **Step 4: Update DownloadManager to use injected ImageLoader**

```kotlin
// app/src/main/java/com/randomimage/util/DownloadManager.kt
object DownloadManager {
    private var imageLoader: ImageLoader? = null
    private val _downloadState = MutableStateFlow<DownloadState>(DownloadState.Idle)
    val downloadState: StateFlow<DownloadState> = _downloadState

    fun init(loader: ImageLoader) {
        imageLoader = loader
    }

    // ... rest of implementation using imageLoader
}
```

- [ ] **Step 5: Initialize ImageLoader in RandomImageApp**

```kotlin
// app/src/main/java/com/randomimage/RandomImageApp.kt
@HiltAndroidApp
class RandomImageApp : Application() {
    @Inject
    lateinit var imageLoader: ImageLoader

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
        ImageUtils.init(imageLoader)
        DownloadManager.init(imageLoader)
    }
}
```

- [ ] **Step 6: Update HomeViewModel.clearCache**

```kotlin
// app/src/main/java/com/randomimage/ui/viewmodel/HomeViewModel.kt
fun clearCache() {
    val context = getApplication<Application>()
    val imageLoader = ImageLoader(context)
    imageLoader.diskCache?.clear()
    imageLoader.memoryCache?.clear()
}
```

Remove this method and provide via DI.

---

## Task 2: Fix Race Condition in loadMoreImages

**Covers:** Bug #3

**Files:**
- Modify: `app/src/main/java/com/randomimage/ui/viewmodel/HomeViewModel.kt`

- [ ] **Step 1: Add Mutex for loadMoreImages**

```kotlin
// app/src/main/java/com/randomimage/ui/viewmodel/HomeViewModel.kt
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@HiltViewModel
class HomeViewModel @Inject constructor(
    // ...
) : AndroidViewModel(application) {

    private val loadMoreMutex = Mutex()

    fun loadMoreImages() {
        viewModelScope.launch {
            loadMoreMutex.withLock {
                if (_uiState.value.isLoading) return@withLock
                // ... rest of implementation
            }
        }
    }
}
```

---

## Task 3: Fix SwipeCard Duplicate Trigger

**Covers:** Bug #4

**Files:**
- Modify: `app/src/main/java/com/randomimage/ui/components/SwipeCard.kt`

- [ ] **Step 1: Add isAnimating flag**

```kotlin
// app/src/main/java/com/randomimage/ui/components/SwipeCard.kt
@Composable
fun SwipeCard(
    // ...
) {
    var offsetX by remember { mutableFloatStateOf(0f) }
    var isAnimating by remember { mutableStateOf(false) }
    val rotation by animateFloatAsState(
        targetValue = offsetX / 20,
        label = "rotation",
        finishedListener = { isAnimating = false }
    )

    Card(
        modifier = modifier
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        if (offsetX > 300 && !isAnimating) {
                            isAnimating = true
                            onSwipeRight()
                        } else if (offsetX < -300 && !isAnimating) {
                            isAnimating = true
                            onSwipeLeft()
                        }
                        offsetX = 0f
                    },
                    onHorizontalDrag = { _, dragAmount ->
                        offsetX += dragAmount
                    }
                )
            }
    )
}
```

---

## Task 4: Fix Database Entity ID Duplicate Risk

**Covers:** Bug #5

**Files:**
- Modify: `app/src/main/java/com/randomimage/data/remote/LoliconApi.kt`

- [ ] **Step 1: Use nanoTime for unique ID**

```kotlin
// app/src/main/java/com/randomimage/data/remote/LoliconApi.kt
fun toImageModel(): ImageModel {
    val imageUrl = urls.original ?: ""
    return ImageModel(
        id = "lolicon_${pid}_${p}_${System.nanoTime()}",
        // ... rest
    )
}
```

---

## Task 5: Fix Theme Switching State Loss

**Covers:** Bug #7

**Files:**
- Modify: `app/src/main/java/com/randomimage/MainActivity.kt`
- Modify: `app/src/main/java/com/randomimage/ui/viewmodel/HomeViewModel.kt`

- [ ] **Step 1: Move UI state to ViewModel**

```kotlin
// app/src/main/java/com/randomimage/ui/viewmodel/HomeViewModel.kt
data class HomeUiState(
    // ... existing fields
    val showDetail: Boolean = false,
    val isWaterfall: Boolean = false
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    // ...
) : AndroidViewModel(application) {
    // ... existing code

    fun setShowDetail(show: Boolean) {
        _uiState.value = _uiState.value.copy(showDetail = show)
    }

    fun setIsWaterfall(waterfall: Boolean) {
        _uiState.value = _uiState.value.copy(isWaterfall = waterfall)
    }
}
```

- [ ] **Step 2: Update MainActivity to use ViewModel state**

```kotlin
// app/src/main/java/com/randomimage/MainActivity.kt
@Composable
fun RandomImageTheme {
    val homeViewModel: HomeViewModel = hiltViewModel()
    val homeUiState by homeViewModel.uiState.collectAsState()

    // Remove local state variables
    // var showDetail by remember { mutableStateOf(false) }  // REMOVE
    // var isWaterfall by remember { mutableStateOf(false) }  // REMOVE

    // Use homeUiState.showDetail and homeUiState.isWaterfall
}
```

---

## Task 6: Fix Search State Sync

**Covers:** Bug #8

**Files:**
- Modify: `app/src/main/java/com/randomimage/ui/viewmodel/HomeViewModel.kt`
- Modify: `app/src/main/java/com/randomimage/ui/screens/HomeScreen.kt`
- Modify: `app/src/main/java/com/randomimage/ui/screens/WaterfallScreen.kt`

- [ ] **Step 1: Move searchQuery to ViewModel**

```kotlin
// app/src/main/java/com/randomimage/ui/viewmodel/HomeUiState
data class HomeUiState(
    // ... existing fields
    val searchQuery: String = ""
)
```

- [ ] **Step 2: Update HomeScreen to use ViewModel searchQuery**

```kotlin
// app/src/main/java/com/randomimage/ui/screens/HomeScreen.kt
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onImageClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    // Remove: var searchQuery by remember { mutableStateOf("") }

    OutlinedTextField(
        value = uiState.searchQuery,
        onValueChange = { viewModel.setSearchQuery(it) },
        // ...
    )
}
```

---

## Task 7: Fix StatsManager First Open Time

**Covers:** Bug #9

**Files:**
- Modify: `app/src/main/java/com/randomimage/util/StatsManager.kt`

- [ ] **Step 1: Add first open time tracking**

```kotlin
// app/src/main/java/com/randomimage/util/StatsManager.kt
object StatsManager {
    private const val PREFS_NAME = "stats_prefs"
    private const val KEY_FIRST_OPEN_TIME = "first_open_time"
    private const val KEY_LAST_OPEN_TIME = "last_open_time"

    fun updateFirstOpenTime(context: Context) {
        val prefs = getPrefs(context)
        if (prefs.getLong(KEY_FIRST_OPEN_TIME, 0) == 0L) {
            prefs.edit().putLong(KEY_FIRST_OPEN_TIME, System.currentTimeMillis()).apply()
        }
    }

    fun getDaysSinceFirstOpen(context: Context): Int {
        val firstOpen = getPrefs(context).getLong(KEY_FIRST_OPEN_TIME, 0)
        if (firstOpen == 0L) return 0
        val diff = System.currentTimeMillis() - firstOpen
        return (diff / (24 * 60 * 60 * 1000)).toInt() + 1
    }
}
```

---

## Task 8: Fix Download Count Not Updated

**Covers:** Bug #10

**Files:**
- Modify: `app/src/main/java/com/randomimage/util/DownloadManager.kt`

- [ ] **Step 1: Increment download count on success**

```kotlin
// app/src/main/java/com/randomimage/util/DownloadManager.kt
suspend fun downloadImage(context: Context, imageUrl: String): Boolean {
    return try {
        _downloadState.value = DownloadState.Downloading
        // ... download logic
        if (result is SuccessResult) {
            // ... save to gallery
            StatsManager.incrementDownloadCount(context)  // ADD THIS
            _downloadState.value = DownloadState.Success(filePath)
            true
        }
        // ...
    }
}
```

---

## Task 9: Fix HTTP Cleartext Traffic

**Covers:** Bug #11

**Files:**
- Create: `app/src/main/res/xml/network_security_config.xml`
- Modify: `app/src/main/AndroidManifest.xml`
- Modify: `app/src/main/java/com/randomimage/di/AppModule.kt`

- [ ] **Step 1: Create network security config**

```xml
<!-- app/src/main/res/xml/network_security_config.xml -->
<?xml version="1.0" encoding="utf-8"?>
<network-security-config>
    <domain-config cleartextTrafficPermitted="true">
        <domain includeSubdomains="true">api.kori.moe</domain>
    </domain-config>
</network-security-config>
```

- [ ] **Step 2: Add to AndroidManifest.xml**

```xml
<application
    android:networkSecurityConfig="@xml/network_security_config"
    ...>
```

---

## Task 10: Fix FavoritesScreen Click to View

**Covers:** Bug #13

**Files:**
- Modify: `app/src/main/java/com/randomimage/ui/screens/FavoritesScreen.kt`

- [ ] **Step 1: Add onClick callback**

```kotlin
// app/src/main/java/com/randomimage/ui/screens/FavoritesScreen.kt
@Composable
fun FavoritesScreen(
    viewModel: FavoritesViewModel = hiltViewModel(),
    onImageClick: (ImageModel) -> Unit = {}
) {
    // ...
    LazyVerticalGrid {
        items(uiState.favorites) { image ->
            Card(
                modifier = Modifier
                    .combinedClickable(
                        onClick = { onImageClick(image) },
                        onLongClick = { selectedImage = image }
                    )
            )
        }
    }
}
```

---

## Task 11: Fix ImageDetailScreen Favorite Button

**Covers:** Bug #14

**Files:**
- Modify: `app/src/main/java/com/randomimage/ui/screens/ImageDetailScreen.kt`
- Modify: `app/src/main/java/com/randomimage/MainActivity.kt`

- [ ] **Step 1: Add onFavorite parameter**

```kotlin
// app/src/main/java/com/randomimage/ui/screens/ImageDetailScreen.kt
@Composable
fun ImageDetailScreen(
    image: ImageModel,
    onBack: () -> Unit,
    onSwipeLeft: () -> Unit,
    onSwipeRight: () -> Unit,
    onFavorite: () -> Unit = {},
    isFavorite: Boolean = false,
    imageIndex: Int = 0,
    totalImages: Int = 1
) {
    // ...
    IconButton(onClick = onFavorite) {
        Icon(
            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
            contentDescription = "收藏",
            tint = if (isFavorite) Color.Red else Color.White
        )
    }
}
```

- [ ] **Step 2: Update MainActivity to pass favorite state**

```kotlin
// app/src/main/java/com/randomimage/MainActivity.kt
ImageDetailScreen(
    image = currentImage,
    onBack = { homeViewModel.setShowDetail(false) },
    onSwipeLeft = { homeViewModel.swipeLeft() },
    onSwipeRight = { homeViewModel.swipeRight() },
    onFavorite = { homeViewModel.toggleFavorite() },
    isFavorite = homeUiState.isFavorite,
    imageIndex = homeUiState.currentIndex,
    totalImages = homeUiState.images.size
)
```

---

## Task 12: Fix Bottom Navigation Bar Favorite Count

**Covers:** Bug #15

**Files:**
- Modify: `app/src/main/java/com/randomimage/MainActivity.kt`

- [ ] **Step 1: Pass favorite count from ViewModel**

```kotlin
// app/src/main/java/com/randomimage/MainActivity.kt
BottomNavBar(
    currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route,
    onNavigate = { route ->
        navController.navigate(route) {
            popUpTo(navController.graph.startDestinationId) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    },
    favoriteCount = homeUiState.favorites.size  // FIX: was hardcoded to 0
)
```

---

## Task 13: Fix WaterfallScreen Screen Width Comparison

**Covers:** Bug #16

**Files:**
- Modify: `app/src/main/java/com/randomimage/ui/screens/WaterfallScreen.kt`

- [ ] **Step 1: Use Int comparison**

```kotlin
// app/src/main/java/com/randomimage/ui/screens/WaterfallScreen.kt
val columns = when {
    configuration.screenWidthDp < 400 -> 2
    configuration.screenWidthDp < 600 -> 3
    else -> 4
}
```

---

## Task 14: Remove Non-Functional Settings

**Covers:** Bug #17, #18, #19

**Files:**
- Modify: `app/src/main/java/com/randomimage/ui/screens/SettingsScreen.kt`

- [ ] **Step 1: Remove unimplemented features**

Remove the following from SettingsScreen:
- Auto refresh switch and slider
- HD image switch
- Preload switch

Keep only the working settings: theme mode, clear cache, clear history, clear search history, usage stats, about.

---

## Task 15: Add Database Migration Fallback

**Covers:** Bug #20

**Files:**
- Modify: `app/src/main/java/com/randomimage/data/local/AppDatabase.kt`

- [ ] **Step 1: Add fallback migration**

```kotlin
// app/src/main/java/com/randomimage/data/local/AppDatabase.kt
@Database(
    entities = [
        FavoriteEntity::class,
        HistoryEntity::class,
        FavoriteGroupEntity::class,
        SearchHistoryEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun favoriteDao(): FavoriteDao
    abstract fun historyDao(): HistoryDao
    abstract fun favoriteGroupDao(): FavoriteGroupDao
    abstract fun searchHistoryDao(): SearchHistoryDao
}

// In DatabaseModule.kt
@Provides
@Singleton
fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
    return Room.databaseBuilder(
        context,
        AppDatabase::class.java,
        "random_image_db"
    )
    .fallbackToDestructiveMigration()
    .build()
}
```

---

## Task 16: Add NSFW Confirmation Dialog

**Covers:** Bug #12

**Files:**
- Modify: `app/src/main/java/com/randomimage/ui/screens/HomeScreen.kt`

- [ ] **Step 1: Add confirmation dialog**

```kotlin
// app/src/main/java/com/randomimage/ui/screens/HomeScreen.kt
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onImageClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    var showNsfwDialog by remember { mutableStateOf(false) }

    // NSFW switch with confirmation
    Switch(
        checked = uiState.isNSFW,
        onCheckedChange = { showNsfwDialog = true }
    )

    if (showNsfwDialog) {
        AlertDialog(
            onDismissRequest = { showNsfwDialog = false },
            title = { Text("NSFW 模式") },
            text = { Text("开启后将显示成人内容，确定要继续吗？") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.toggleNSFW()
                    showNsfwDialog = false
                }) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNsfwDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
}
```

---

## Task 17: Create Log Export Feature

**Covers:** New Feature - Log Export

**Files:**
- Create: `app/src/main/java/com/randomimage/util/LogManager.kt`
- Create: `app/src/main/java/com/randomimage/ui/screens/LogScreen.kt`
- Modify: `app/src/main/java/com/randomimage/MainActivity.kt`
- Modify: `app/src/main/java/com/randomimage/ui/components/BottomNavBar.kt`
- Modify: `app/src/main/java/com/randomimage/RandomImageApp.kt`

- [ ] **Step 1: Create LogManager**

```kotlin
// app/src/main/java/com/randomimage/util/LogManager.kt
package com.randomimage.util

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import timber.log.Timber
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*

object LogManager {
    private var logFile: File? = null
    private val logBuffer = mutableListOf<String>()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    fun init(context: Context) {
        logFile = File(context.filesDir, "app_logs.txt")
        Timber.d("LogManager initialized")
    }

    fun addLog(level: String, tag: String, message: String) {
        val timestamp = dateFormat.format(Date())
        val logEntry = "[$timestamp] $level/$tag: $message"
        logBuffer.add(logEntry)

        // Write to file periodically
        if (logBuffer.size >= 50) {
            flushLogs()
        }
    }

    fun flushLogs() {
        logBuffer.forEach { entry ->
            logFile?.appendText("$entry\n")
        }
        logBuffer.clear()
    }

    fun getLogs(): String {
        flushLogs()
        return logFile?.readText() ?: "No logs available"
    }

    fun shareLogs(context: Context) {
        flushLogs()
        logFile?.let { file ->
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "App Logs - Random Image")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "分享日志"))
        }
    }

    fun clearLogs() {
        logBuffer.clear()
        logFile?.delete()
        Timber.d("Logs cleared")
    }
}
```

- [ ] **Step 2: Create FileProvider in AndroidManifest**

```xml
<!-- app/src/main/AndroidManifest.xml -->
<provider
    android:name="androidx.core.content.FileProvider"
    android:authorities="${applicationId}.fileprovider"
    android:exported="false"
    android:grantUriPermissions="true">
    <meta-data
        android:name="android.support.FILE_PROVIDER_PATHS"
        android:resource="@xml/file_paths" />
</provider>
```

- [ ] **Step 3: Create file_paths.xml**

```xml
<!-- app/src/main/res/xml/file_paths.xml -->
<?xml version="1.0" encoding="utf-8"?>
<paths>
    <files-path
        name="logs"
        path="." />
</paths>
```

- [ ] **Step 4: Create LogScreen**

```kotlin
// app/src/main/java/com/randomimage/ui/screens/LogScreen.kt
package com.randomimage.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.randomimage.util.LogManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var logs by remember { mutableStateOf("") }
    var showClearDialog by remember { mutableStateOf(false) }

    BackHandler { onBack() }

    LaunchedEffect(Unit) {
        logs = LogManager.getLogs()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("应用日志") },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                }
            },
            actions = {
                IconButton(onClick = { LogManager.shareLogs(context) }) {
                    Icon(Icons.Default.Share, contentDescription = "分享")
                }
                IconButton(onClick = { showClearDialog = true }) {
                    Icon(Icons.Default.Delete, contentDescription = "清除")
                }
            }
        )

        Text(
            text = logs,
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            style = MaterialTheme.typography.bodySmall
        )
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("清除日志") },
            text = { Text("确定要清除所有日志吗？") },
            confirmButton = {
                TextButton(onClick = {
                    LogManager.clearLogs()
                    logs = ""
                    showClearDialog = false
                    Toast.makeText(context, "日志已清除", Toast.LENGTH_SHORT).show()
                }) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
}
```

- [ ] **Step 5: Update BottomNavBar to add Logs tab**

```kotlin
// app/src/main/java/com/randomimage/ui/components/BottomNavBar.kt
@Composable
fun BottomNavBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    favoriteCount: Int
) {
    NavigationBar {
        NavigationBarItem(
            icon = { Icon(Icons.Default.Home, contentDescription = null) },
            label = { Text("首页") },
            selected = currentRoute == "home",
            onClick = { onNavigate("home") }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Favorite, contentDescription = null) },
            label = { Text("收藏") },
            selected = currentRoute == "favorites",
            onClick = { onNavigate("favorites") }
        )
        NavigationBarItem(
            icon = {
                BadgedBox(badge = { Badge { Text("$favoriteCount") } }) {
                    Icon(Icons.Default.Favorite, contentDescription = null)
                }
            },
            label = { Text("收藏") },
            selected = currentRoute == "favorites",
            onClick = { onNavigate("favorites") }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Build, contentDescription = null) },
            label = { Text("工具") },
            selected = currentRoute == "toolbox",
            onClick = { onNavigate("toolbox") }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Info, contentDescription = null) },
            label = { Text("日志") },
            selected = currentRoute == "logs",
            onClick = { onNavigate("logs") }
        )
    }
}
```

- [ ] **Step 6: Add Logs route to MainActivity NavHost**

```kotlin
// app/src/main/java/com/randomimage/MainActivity.kt
NavHost(
    navController = navController,
    startDestination = "home",
    modifier = Modifier.padding(innerPadding)
) {
    composable("home") { /* ... */ }
    composable("favorites") { /* ... */ }
    composable("toolbox") { /* ... */ }
    composable("logs") {
        LogScreen(onBack = { navController.popBackStack() })
    }
}
```

---

## Task 18: Update MainActivity and Fix Flow Collection

**Covers:** Bug #6, #7 (continued)

**Files:**
- Modify: `app/src/main/java/com/randomimage/ui/viewmodel/HomeViewModel.kt`

- [ ] **Step 1: Use stateIn for Flow collection**

```kotlin
// app/src/main/java/com/randomimage/ui/viewmodel/HomeViewModel.kt
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class HomeViewModel @Inject constructor(
    // ...
) : AndroidViewModel(application) {

    private val favorites = repository.getFavorites()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val history = repository.getHistory()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val recentSearches = repository.getRecentSearches()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // Update uiState when flows emit
        viewModelScope.launch {
            favorites.collect { list ->
                _uiState.value = _uiState.value.copy(favorites = list)
            }
        }
        viewModelScope.launch {
            history.collect { list ->
                _uiState.value = _uiState.value.copy(history = list)
            }
        }
        viewModelScope.launch {
            recentSearches.collect { list ->
                _uiState.value = _uiState.value.copy(recentSearches = list)
            }
        }
    }
}
```

---

## Task 19: Run Verification

**Files:** None (verification only)

- [ ] **Step 1: Build the project**

```bash
cd D:\mimo\random-image-app
.\gradlew assembleDebug
```

Expected: BUILD SUCCESSFUL

- [ ] **Step 2: Run lint check**

```bash
.\gradlew lintDebug
```

Expected: No errors

---

## Task 20: Commit All Changes

- [ ] **Step 1: Stage and commit**

```bash
git add -A
git commit -m "fix: resolve all 20 bugs and add log export feature

- Fix ImageLoader memory leak with singleton pattern
- Fix race condition in loadMoreImages with Mutex
- Fix SwipeCard duplicate trigger with isAnimating flag
- Fix database entity ID with nanoTime
- Fix theme switching state loss by moving to ViewModel
- Fix search state sync between Home/Waterfall screens
- Fix StatsManager first open time tracking
- Fix download count not incrementing
- Add network security config for HTTP traffic
- Add onClick to FavoritesScreen for image preview
- Fix ImageDetailScreen favorite button functionality
- Fix bottom nav favorite count display
- Fix WaterfallScreen screen width comparison
- Remove non-functional settings (auto refresh, HD, preload)
- Add database migration fallback
- Add NSFW confirmation dialog
- Add LogManager for structured logging
- Add LogScreen for viewing/sharing logs
- Add FileProvider for log export
- Fix Flow collection with stateIn

Closes #1, #2, #3, #4, #5"
```

---

## Summary

| Task | Bug Fixed | Status |
|------|-----------|--------|
| 1 | #1, #2 | ImageLoader memory leak |
| 2 | #3 | Race condition |
| 3 | #4 | SwipeCard trigger |
| 4 | #5 | Database ID |
| 5 | #7 | Theme state |
| 6 | #8 | Search sync |
| 7 | #9 | Stats time |
| 8 | #10 | Download count |
| 9 | #11 | HTTP security |
| 10 | #13 | Favorites click |
| 11 | #14 | Favorite button |
| 12 | #15 | Nav count |
| 13 | #16 | Width comparison |
| 14 | #17, #18, #19 | Remove non-functional |
| 15 | #20 | DB migration |
| 16 | #12 | NSFW dialog |
| 17 | New | Log export |
| 18 | #6 | Flow collection |
| 19 | - | Verification |
| 20 | - | Commit |
