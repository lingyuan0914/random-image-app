package com.randomimage.ui.screens

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.randomimage.ui.components.DownloadProgress
import com.randomimage.ui.components.SwipeCard
import com.randomimage.ui.viewmodel.HomeViewModel
import com.randomimage.util.ImageUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onImageClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var expanded by remember { mutableStateOf(false) }
    var showNsfwDialog by remember { mutableStateOf(false) }
    var showGroupDialog by remember { mutableStateOf(false) }
    var groups by remember { mutableStateOf<List<com.randomimage.data.local.FavoriteGroupEntity>>(emptyList()) }

    LaunchedEffect(Unit) {
        groups = with(kotlinx.coroutines.Dispatchers.IO) { viewModel.getGroups() }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = it },
                modifier = Modifier.weight(1f)
            ) {
                OutlinedTextField(
                    value = uiState.currentApiName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("数据源", style = MaterialTheme.typography.labelSmall) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                        .height(56.dp),
                    textStyle = MaterialTheme.typography.bodyMedium
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    uiState.availableApis.forEachIndexed { index, apiName ->
                        DropdownMenuItem(
                            text = { Text(apiName) },
                            onClick = {
                                viewModel.switchApi(index)
                                expanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "NSFW",
                    style = MaterialTheme.typography.labelSmall
                )
                Spacer(modifier = Modifier.width(4.dp))
                Switch(
                    checked = uiState.isNSFW,
                    onCheckedChange = { showNsfwDialog = true }
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                label = { Text("搜索标签", style = MaterialTheme.typography.labelSmall) },
                placeholder = { Text("输入关键词...", style = MaterialTheme.typography.bodySmall) },
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium,
                trailingIcon = {
                    if (uiState.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.clearSearch() }) {
                            Icon(Icons.Default.Clear, contentDescription = "清除")
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = {
                    if (uiState.searchQuery.isNotBlank()) {
                        viewModel.searchImages(uiState.searchQuery)
                    }
                }
            ) {
                Icon(Icons.Default.Search, contentDescription = "搜索")
            }
        }

        if (uiState.searchQuery.isEmpty() && uiState.recentSearches.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("历史:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.width(8.dp))
                FlowRow(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    uiState.recentSearches.take(5).forEach { search ->
                        SuggestionChip(
                            onClick = {
                                viewModel.setSearchQuery(search)
                                viewModel.searchImages(search)
                            },
                            label = { Text(search, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
            }
        }

        if (uiState.recommendedTags.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 2.dp)
                    .height(36.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "推荐:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(8.dp))
                FlowRow(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    uiState.recommendedTags.take(6).forEach { tag ->
                        SuggestionChip(
                            onClick = {
                                viewModel.setSearchQuery(tag.name)
                                viewModel.searchImages(tag.name)
                            },
                            label = { Text(tag.displayName, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        if (uiState.memoryImages.isNotEmpty() && uiState.searchQuery.isEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("回忆:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.width(8.dp))
                androidx.compose.foundation.lazy.LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(uiState.memoryImages.size) { index ->
                        val memImage = uiState.memoryImages[index]
                        coil.compose.AsyncImage(
                            model = memImage.urls.thumb,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    viewModel.setCurrentImage(memImage)
                                    onImageClick()
                                }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        if (uiState.popularTags.isNotEmpty() && uiState.searchQuery.isEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("标签:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.width(8.dp))
                com.randomimage.ui.components.TagCloud(
                    tags = uiState.popularTags,
                    onTagClick = { tag ->
                        viewModel.setSearchQuery(tag)
                        viewModel.searchImages(tag)
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator()
                }
                uiState.error != null -> {
                    TextButton(
                        onClick = { viewModel.loadImages() }
                    ) {
                        Text(
                            text = uiState.error ?: "错误",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
                uiState.images.isEmpty() -> {
                    TextButton(
                        onClick = { viewModel.loadImages() }
                    ) {
                        Text(
                            text = "点击加载图片",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
                uiState.images.isNotEmpty() -> {
                    val currentImage = uiState.images[uiState.currentIndex]
                    Box(modifier = Modifier.fillMaxSize()) {
                        SwipeCard(
                            image = currentImage,
                            onSwipeRight = { viewModel.swipeRight() },
                            onSwipeLeft = { viewModel.swipeLeft() },
                            onLike = {
                                if (uiState.isFavorite) {
                                    viewModel.toggleFavorite()
                                } else {
                                    showGroupDialog = true
                                }
                            },
                            onShare = { ImageUtils.shareImage(context, currentImage.urls.regular) },
                            onDownload = { viewModel.downloadCurrentImage() },
                            onSetWallpaper = {
                                scope.launch {
                                    val success = ImageUtils.setWallpaper(context, currentImage.urls.regular)
                                    Toast.makeText(
                                        context,
                                        if (success) "壁纸设置成功" else "壁纸设置失败",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            },
                            onClick = { onImageClick() },
                            isFavorite = uiState.isFavorite,
                            modifier = Modifier.fillMaxSize()
                        )
                        if (uiState.isDownloading) {
                            DownloadProgress(
                                isVisible = true,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }
        }
    }

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

    if (showGroupDialog) {
        val currentImage = uiState.images.getOrNull(uiState.currentIndex)
        AlertDialog(
            onDismissRequest = { showGroupDialog = false },
            title = { Text("选择收藏分组") },
            text = {
                Column {
                    TextButton(
                        onClick = {
                            if (currentImage != null) {
                                viewModel.addToFavoritesWithGroup(currentImage, 0)
                            }
                            showGroupDialog = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("未分组")
                    }
                    groups.forEach { group ->
                        TextButton(
                            onClick = {
                                if (currentImage != null) {
                                    viewModel.addToFavoritesWithGroup(currentImage, group.id)
                                }
                                showGroupDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(group.name)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showGroupDialog = false }) { Text("取消") }
            }
        )
    }
}
