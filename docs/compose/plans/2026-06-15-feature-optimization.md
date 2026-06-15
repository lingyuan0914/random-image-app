# Feature Enhancement & Optimization Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use compose:subagent (recommended) or compose:execute to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add image cache management, preloading, favorites grouping, image info details, and performance optimizations.

**Architecture:** Extend existing ViewModel/Repository pattern with new UI components and database entities.

**Tech Stack:** Kotlin, Jetpack Compose, Hilt, Room, Coil, Kotlin Coroutines

---

## Task 1: Image Cache Management

**Files:**
- Modify: `app/src/main/java/com/randomimage/util/CacheManager.kt` (new)
- Modify: `app/src/main/java/com/randomimage/ui/screens/SettingsScreen.kt`

- [ ] **Step 1: Create CacheManager**

```kotlin
// app/src/main/java/com/randomimage/util/CacheManager.kt
package com.randomimage.util

import android.content.Context
import coil.ImageLoader
import timber.log.Timber
import java.io.File

object CacheManager {
    fun getCacheSize(context: Context): Long {
        var size = 0L
        // Disk cache
        val diskCacheDir = File(context.cacheDir, "image_cache")
        if (diskCacheDir.exists()) {
            size += diskCacheDir.walkTopDown().filter { it.isFile }.sumOf { it.length() }
        }
        // Files cache
        val filesDir = context.filesDir
        size += filesDir.walkTopDown()
            .filter { it.isFile && it.name.endsWith(".tmp") }
            .sumOf { it.length() }
        return size
    }

    fun clearDiskCache(context: Context) {
        val diskCacheDir = File(context.cacheDir, "image_cache")
        if (diskCacheDir.exists()) {
            diskCacheDir.deleteRecursively()
            Timber.d("Disk cache cleared")
        }
    }

    fun formatSize(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> "${bytes / 1024} KB"
            else -> "${"%.2f".format(bytes / (1024.0 * 1024))} MB"
        }
    }
}
```

- [ ] **Step 2: Update SettingsScreen to show cache size**

Add cache size display and clear button in SettingsScreen.

- [ ] **Step 3: Commit**

---

## Task 2: Image Preloading

**Files:**
- Modify: `app/src/main/java/com/randomimage/ui/viewmodel/HomeViewModel.kt`

- [ ] **Step 1: Add preload function to HomeViewModel**

```kotlin
// Add to HomeViewModel.kt
fun preloadNextImages() {
    val state = _uiState.value
    val nextIndices = listOf(
        state.currentIndex + 1,
        state.currentIndex + 2,
        state.currentIndex + 3
    ).filter { it < state.images.size }

    nextIndices.forEach { index ->
        val image = state.images[index]
        // Trigger Coil to preload
        val request = ImageRequest.Builder(getApplication())
            .data(image.urls.regular)
            .preload()
            .build()
        ImageLoader(getApplication()).enqueue(request)
    }
}
```

- [ ] **Step 2: Call preload after image load in nextImage()**

- [ ] **Step 3: Commit**

---

## Task 3: Favorites Grouping

**Files:**
- Create: `app/src/main/java/com/randomimage/data/local/FavoriteGroupEntity.kt`
- Create: `app/src/main/java/com/randomimage/data/local/FavoriteGroupDao.kt`
- Modify: `app/src/main/java/com/randomimage/data/local/AppDatabase.kt`
- Modify: `app/src/main/java/com/randomimage/data/local/FavoriteEntity.kt`
- Modify: `app/src/main/java/com/randomimage/ui/screens/FavoritesScreen.kt`

- [ ] **Step 1: Create FavoriteGroupEntity**

```kotlin
// app/src/main/java/com/randomimage/data/local/FavoriteGroupEntity.kt
package com.randomimage.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_groups")
data class FavoriteGroupEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val createdAt: Long = System.currentTimeMillis()
)
```

- [ ] **Step 2: Create FavoriteGroupDao**

```kotlin
// app/src/main/java/com/randomimage/data/local/FavoriteGroupDao.kt
package com.randomimage.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteGroupDao {
    @Query("SELECT * FROM favorite_groups ORDER BY createdAt DESC")
    fun getAllGroups(): Flow<List<FavoriteGroupEntity>>

    @Insert
    suspend fun insertGroup(group: FavoriteGroupEntity): Long

    @Delete
    suspend fun deleteGroup(group: FavoriteGroupEntity)

    @Update
    suspend fun updateGroup(group: FavoriteGroupEntity)
}
```

- [ ] **Step 3: Update FavoriteEntity to include groupId**

Add `groupId: Long = 0` field.

- [ ] **Step 4: Update FavoritesScreen with group tabs**

- [ ] **Step 5: Commit**

---

## Task 4: Image Info Details

**Files:**
- Modify: `app/src/main/java/com/randomimage/ui/screens/ImageDetailScreen.kt`

- [ ] **Step 1: Add image info panel to ImageDetailScreen**

Show: title, author, dimensions, tags, source API.

- [ ] **Step 2: Add info toggle button**

- [ ] **Step 3: Commit**

---

## Task 5: Performance Optimizations

**Files:**
- Modify: `app/src/main/java/com/randomimage/ui/screens/WaterfallScreen.kt`
- Modify: `app/src/main/java/com/randomimage/data/local/AppDatabase.kt`
- Modify: `app/src/main/java/com/randomimage/ui/viewmodel/HomeViewModel.kt`

- [ ] **Step 1: Add database indexes**

```sql
CREATE INDEX IF NOT EXISTS idx_favorites_id ON favorites(id);
CREATE INDEX IF NOT EXISTS idx_history_id ON history(id);
```

- [ ] **Step 2: Optimize WaterfallScreen lazy loading**

Use `rememberLazyListState` for scroll position.

- [ ] **Step 3: Add exponential backoff for API retries**

- [ ] **Step 4: Commit**

---

## Summary

| Task | Feature | Status |
|------|---------|--------|
| 1 | Cache Management | ☐ |
| 2 | Image Preloading | ☐ |
| 3 | Favorites Grouping | ☐ |
| 4 | Image Info Details | ☐ |
| 5 | Performance Optimizations | ☐ |
