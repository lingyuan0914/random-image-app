# Pixiv-Style Features Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use compose:subagent (recommended) or compose:execute to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add Pixiv-inspired features to the random image app: tag system, rankings, artist following, enhanced collections, and recommendation engine.

**Architecture:** Leverage existing Lolicon API data (which contains Pixiv artwork metadata including artist uid/name/tags) to build Pixiv-like features entirely client-side. No new API integrations needed — Lolicon already provides Pixiv artwork with full metadata.

**Tech Stack:** Kotlin, Jetpack Compose, Room, Hilt, SharedPreferences, Coil

---

## Feature Overview

| Feature | Description | Priority |
|---------|-------------|----------|
| Tag System | Tag cloud, autocomplete, multi-tag filtering | P0 |
| Artist Following | Follow/unfollow artists using Lolicon uid | P0 |
| Rankings | Daily/weekly/monthly view-based rankings | P1 |
| Collections | Enhanced favorites with cover images, sort, batch ops | P1 |
| Recommendations | Suggest images based on followed artists + tags | P2 |
| Enhanced Detail | Tags, resolution, artist info, related works | P2 |

---

## Task 1: Tag Data Model & Storage

**Files:**
- Create: `app/src/main/java/com/randomimage/data/local/TagEntity.kt`
- Create: `app/src/main/java/com/randomimage/data/local/TagDao.kt`
- Modify: `app/src/main/java/com/randomimage/data/local/AppDatabase.kt` — add TagEntity, bump version to 6
- Modify: `app/src/main/java/com/randomimage/di/DatabaseModule.kt` — provide TagDao

- [ ] **Step 1: Create TagEntity**

```kotlin
// TagEntity.kt
package com.randomimage.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tags")
data class TagEntity(
    @PrimaryKey
    val name: String,
    val usageCount: Int = 1,
    val lastUsedTime: Long = System.currentTimeMillis()
)
```

- [ ] **Step 2: Create TagDao**

```kotlin
// TagDao.kt
package com.randomimage.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TagDao {
    @Query("SELECT * FROM tags ORDER BY usageCount DESC")
    fun getAllTags(): Flow<List<TagEntity>>

    @Query("SELECT * FROM tags WHERE name LIKE '%' || :query || '%' ORDER BY usageCount DESC LIMIT 10")
    suspend fun searchTags(query: String): List<TagEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTag(tag: TagEntity)

    @Query("UPDATE tags SET usageCount = usageCount + 1, lastUsedTime = :time WHERE name = :name")
    suspend fun incrementUsage(name: String, time: Long = System.currentTimeMillis())

    @Query("DELETE FROM tags")
    suspend fun clearTags()
}
```

- [ ] **Step 3: Add migration 5→6 in AppDatabase**

```kotlin
// In AppDatabase.kt, bump version to 6, add:
val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("CREATE TABLE IF NOT EXISTS tags (name TEXT PRIMARY KEY NOT NULL, usageCount INTEGER NOT NULL DEFAULT 1, lastUsedTime INTEGER NOT NULL DEFAULT 0)")
    }
}
```

- [ ] **Step 4: Register TagDao in DatabaseModule**

```kotlin
// In DatabaseModule.kt, add:
@Provides
@Singleton
fun provideTagDao(database: AppDatabase): TagDao {
    return database.tagDao()
}
```

- [ ] **Step 5: Build and verify compilation**

Run: `gradle assembleDebug`

---

## Task 2: Artist Data Model & Following

**Files:**
- Create: `app/src/main/java/com/randomimage/data/local/ArtistEntity.kt`
- Create: `app/src/main/java/com/randomimage/data/local/ArtistDao.kt`
- Modify: `app/src/main/java/com/randomimage/data/local/AppDatabase.kt` — add ArtistEntity, bump version to 7
- Modify: `app/src/main/java/com/randomimage/di/DatabaseModule.kt` — provide ArtistDao

- [ ] **Step 1: Create ArtistEntity**

```kotlin
// ArtistEntity.kt
package com.randomimage.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "followed_artists")
data class ArtistEntity(
    @PrimaryKey
    val uid: String,
    val name: String,
    val artworkCount: Int = 0,
    val followedTime: Long = System.currentTimeMillis()
)
```

- [ ] **Step 2: Create ArtistDao**

```kotlin
// ArtistDao.kt
package com.randomimage.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ArtistDao {
    @Query("SELECT * FROM followed_artists ORDER BY followedTime DESC")
    fun getAllFollowed(): Flow<List<ArtistEntity>>

    @Query("SELECT * FROM followed_artists WHERE uid = :uid")
    suspend fun getArtist(uid: String): ArtistEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun followArtist(artist: ArtistEntity)

    @Query("DELETE FROM followed_artists WHERE uid = :uid")
    suspend fun unfollowArtist(uid: String)

    @Query("SELECT EXISTS(SELECT 1 FROM followed_artists WHERE uid = :uid)")
    suspend fun isFollowing(uid: String): Boolean

    @Query("SELECT uid FROM followed_artists")
    suspend fun getAllFollowedUids(): List<String>
}
```

- [ ] **Step 3: Add migration 6→7 in AppDatabase**

```kotlin
val MIGRATION_6_7 = object : Migration(6, 7) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("CREATE TABLE IF NOT EXISTS followed_artists (uid TEXT PRIMARY KEY NOT NULL, name TEXT NOT NULL, artworkCount INTEGER NOT NULL DEFAULT 0, followedTime INTEGER NOT NULL DEFAULT 0)")
    }
}
```

- [ ] **Step 4: Register ArtistDao in DatabaseModule**

- [ ] **Step 5: Build and verify compilation**

---

## Task 3: Enhanced Tag & Artist Repository Methods

**Files:**
- Modify: `app/src/main/java/com/randomimage/data/repository/ImageRepository.kt` — add tag/artist methods

- [ ] **Step 1: Inject TagDao and ArtistDao, add methods**

```kotlin
// In ImageRepository constructor, add:
private val tagDao: TagDao,
private val artistDao: ArtistDao

// Add methods:
fun getAllTags(): Flow<List<TagEntity>> = tagDao.getAllTags()
suspend fun searchTags(query: String) = tagDao.searchTags(query)
suspend fun recordTagUsage(tagName: String) {
    tagDao.incrementUsage(tagName)
}

fun getFollowedArtists(): Flow<List<ArtistEntity>> = artistDao.getAllFollowed()
suspend fun followArtist(artist: ArtistEntity) = artistDao.followArtist(artist)
suspend fun unfollowArtist(uid: String) = artistDao.unfollowArtist(uid)
suspend fun isFollowingArtist(uid: String) = artistDao.isFollowing(uid)
suspend fun getFollowedUids(): List<String> = artistDao.getAllFollowedUids()
```

- [ ] **Step 2: Build and verify**

---

## Task 4: LoliconApi — Extract Tags from Response

**Files:**
- Modify: `app/src/main/java/com/randomimage/data/remote/LoliconApi.kt` — ensure tags are preserved in LoliconItem

- [ ] **Step 1: Verify LoliconItem already has tags field**

```kotlin
// LoliconItem already has:
val tags: List<String>
```
No change needed — tags are already in the API response.

- [ ] **Step 2: Add tags to ImageModel**

```kotlin
// In ImageModel.kt, add field:
val tags: List<String> = emptyList()

// In LoliconItem.toImageModel(), add:
tags = tags
```

- [ ] **Step 3: Update FavoriteEntity and HistoryEntity to store tags**

```kotlin
// In FavoriteEntity.kt, add:
val tags: String = ""

// In FavoriteEntity.fromImageModel(), add:
tags = image.tags.joinToString(",")

// In HistoryEntity, tags field already exists
```

- [ ] **Step 4: Migration 7→8 for tags column in favorites**

```kotlin
val MIGRATION_7_8 = object : Migration(7, 8) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE favorites ADD COLUMN tags TEXT NOT NULL DEFAULT ''")
    }
}
```

- [ ] **Step 5: Build and verify**

---

## Task 5: Tag Cloud UI Component

**Files:**
- Create: `app/src/main/java/com/randomimage/ui/components/TagCloud.kt`
- Modify: `app/src/main/java/com/randomimage/ui/screens/HomeScreen.kt` — add tag cloud section

- [ ] **Step 1: Create TagCloud composable**

```kotlin
// TagCloud.kt
package com.randomimage.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.randomimage.data.local.TagEntity

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TagCloud(
    tags: List<TagEntity>,
    onTagClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        tags.take(20).forEach { tag ->
            Text(
                text = tag.name,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier
                    .clickable { onTagClick(tag.name) }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}
```

- [ ] **Step 2: Add TagCloud to HomeScreen between recommended tags and image area**

- [ ] **Step 3: Build and verify**

---

## Task 6: Artist Follow Button in Image Detail

**Files:**
- Modify: `app/src/main/java/com/randomimage/ui/screens/ImageDetailScreen.kt` — add follow button
- Modify: `app/src/main/java/com/randomimage/ui/viewmodel/HomeViewModel.kt` — add follow/unfollow methods

- [ ] **Step 1: Add follow state to HomeUiState**

```kotlin
// In HomeUiState, add:
val isFollowingArtist: Boolean = false
```

- [ ] **Step 2: Add follow/unfollow methods to HomeViewModel**

```kotlin
fun toggleFollowArtist() {
    val image = getCurrentImage() ?: return
    viewModelScope.launch {
        if (_uiState.value.isFollowingArtist) {
            repository.unfollowArtist(image.user.id)
            _uiState.value = _uiState.value.copy(isFollowingArtist = false)
        } else {
            repository.followArtist(ArtistEntity(uid = image.user.id, name = image.user.name))
            _uiState.value = _uiState.value.copy(isFollowingArtist = true)
        }
    }
}

fun checkFollowingArtist() {
    val image = getCurrentImage() ?: return
    viewModelScope.launch {
        val following = repository.isFollowingArtist(image.user.id)
        _uiState.value = _uiState.value.copy(isFollowingArtist = following)
    }
}
```

- [ ] **Step 3: Add follow button to ImageDetailScreen bottom bar**

```kotlin
// Add to bottom Row:
IconButton(onClick = onFollow) {
    Icon(
        if (isFollowing) Icons.Default.PersonRemove else Icons.Default.PersonAdd,
        contentDescription = if (isFollowing) "取消关注" else "关注画师",
        tint = if (isFollowing) MaterialTheme.colorScheme.primary else Color.White,
        modifier = Modifier.size(28.dp)
    )
}
```

- [ ] **Step 4: Wire up in MainActivity**

- [ ] **Step 5: Build and verify**

---

## Task 7: Rankings Screen

**Files:**
- Create: `app/src/main/java/com/randomimage/ui/screens/RankingsScreen.kt`
- Create: `app/src/main/java/com/randomimage/ui/viewmodel/RankingsViewModel.kt`
- Modify: `app/src/main/java/com/randomimage/MainActivity.kt` — add rankings route
- Modify: `app/src/main/java/com/randomimage/ui/components/BottomNavBar.kt` — add rankings tab

- [ ] **Step 1: Create RankingsViewModel**

```kotlin
// RankingsViewModel.kt
@HiltViewModel
class RankingsViewModel @Inject constructor(
    private val repository: ImageRepository
) : ViewModel() {
    private val _rankings = MutableStateFlow<List<ImageModel>>(emptyList())
    val rankings: StateFlow<List<ImageModel>> = _rankings

    init { loadRankings() }

    fun loadRankings() {
        viewModelScope.launch {
            // Use recent history + favorites as ranking data
            val favorites = repository.getFavorites().first()
            val history = repository.getHistory().first()
            val combined = (favorites + history).distinctBy { it.id }
            _rankings.value = combined.sortedByDescending { it.likes }.take(50)
        }
    }
}
```

- [ ] **Step 2: Create RankingsScreen with tabs (日榜/周榜/月榜)**

- [ ] **Step 3: Add to navigation and bottom bar**

- [ ] **Step 4: Build and verify**

---

## Task 8: Collection Cover & Sort

**Files:**
- Modify: `app/src/main/java/com/randomimage/ui/screens/FavoritesScreen.kt` — add sort, cover preview
- Modify: `app/src/main/java/com/randomimage/data/local/FavoriteGroupEntity.kt` — add coverUrl field

- [ ] **Step 1: Add coverUrl to FavoriteGroupEntity**

```kotlin
data class FavoriteGroupEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val coverUrl: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
```

- [ ] **Step 2: Migration 8→9**

- [ ] **Step 3: Add sort options to FavoritesScreen (by time/name/count)**

- [ ] **Step 4: Show group cover image in tab**

- [ ] **Step 5: Build and verify**

---

## Task 9: Recommendation Engine

**Files:**
- Create: `app/src/main/java/com/randomimage/util/RecommendEngine.kt`
- Modify: `app/src/main/java/com/randomimage/ui/viewmodel/HomeViewModel.kt` — integrate recommendations

- [ ] **Step 1: Create RecommendEngine**

```kotlin
// RecommendEngine.kt
object RecommendEngine {
    fun getRecommendedTags(
        favorites: List<ImageModel>,
        history: List<ImageModel>
    ): List<String> {
        val allTags = (favorites + history).flatMap { it.tags }
        return allTags.groupingBy { it }.eachCount()
            .sortedByDescending { it.value }
            .take(10)
            .map { it.key }
    }

    fun getRecommendedArtists(
        favorites: List<ImageModel>
    ): List<Pair<String, String>> {
        return favorites.groupBy { it.user.id }
            .map { (uid, images) -> uid to images.first().user.name }
            .sortedByDescending { it.second }
            .take(10)
    }
}
```

- [ ] **Step 2: Add "猜你喜欢" section to HomeScreen using recommended tags**

- [ ] **Step 3: Build and verify**

---

## Task 10: Enhanced Detail View

**Files:**
- Modify: `app/src/main/java/com/randomimage/ui/screens/ImageDetailScreen.kt` — show tags, resolution, artist link

- [ ] **Step 1: Add tags display below image description**

```kotlin
// In ImageDetailScreen, add tags row:
if (image.tags.isNotEmpty()) {
    FlowRow(modifier = Modifier.padding(horizontal = 16.dp)) {
        image.tags.forEach { tag ->
            SuggestionChip(
                onClick = { /* search by tag */ },
                label = { Text(tag, style = MaterialTheme.typography.labelSmall) }
            )
        }
    }
}
```

- [ ] **Step 2: Show resolution info**

```kotlin
if (image.width > 0 && image.height > 0) {
    Text("${image.width}×${image.height}", style = MaterialTheme.typography.bodySmall)
}
```

- [ ] **Step 3: Build and verify**

---

## Task 11: Version Bump & Release

- [ ] **Step 1: Bump version to 3.0.0 in build.gradle.kts**
- [ ] **Step 2: Build release APK**
- [ ] **Step 3: Commit, push, create GitHub release**
