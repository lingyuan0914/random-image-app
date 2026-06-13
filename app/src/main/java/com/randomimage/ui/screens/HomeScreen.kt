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
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.randomimage.ui.components.SwipeCard
import com.randomimage.ui.viewmodel.HomeViewModel
import com.randomimage.util.ImageUtils
import kotlinx.coroutines.launch

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
    var searchQuery by remember { mutableStateOf("") }

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
                    onCheckedChange = { viewModel.toggleNSFW() }
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
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("搜索标签", style = MaterialTheme.typography.labelSmall) },
                placeholder = { Text("输入关键词...", style = MaterialTheme.typography.bodySmall) },
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium,
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = {
                            searchQuery = ""
                            viewModel.clearSearch()
                        }) {
                            Icon(Icons.Default.Clear, contentDescription = "清除")
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = {
                    if (searchQuery.isNotBlank()) {
                        viewModel.searchImages(searchQuery)
                    }
                }
            ) {
                Icon(Icons.Default.Search, contentDescription = "搜索")
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
                                searchQuery = tag.name
                                viewModel.searchImages(tag.name)
                            },
                            label = { Text(tag.displayName, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Box(modifier = Modifier.weight(1f)) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                uiState.error != null -> {
                    TextButton(
                        onClick = { viewModel.loadImages() },
                        modifier = Modifier.align(Alignment.Center)
                    ) {
                        Text(
                            text = uiState.error ?: "错误",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
                uiState.images.isNotEmpty() -> {
                    val currentImage = uiState.images[uiState.currentIndex]
                    SwipeCard(
                        image = currentImage,
                        onSwipeRight = { viewModel.swipeRight() },
                        onSwipeLeft = { viewModel.swipeLeft() },
                        onLike = { viewModel.toggleFavorite() },
                        onShare = { ImageUtils.shareImage(context, currentImage.urls.regular) },
                        onDownload = {
                            scope.launch {
                                val success = ImageUtils.downloadImage(context, currentImage.urls.regular)
                                Toast.makeText(
                                    context,
                                    if (success) "下载成功" else "下载失败",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
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
                }
            }
        }
    }
}
