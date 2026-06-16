package com.randomimage.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.randomimage.domain.model.ImageModel
import com.randomimage.ui.viewmodel.FavoritesViewModel

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    viewModel: FavoritesViewModel = hiltViewModel(),
    onImageClick: (ImageModel) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedImage by remember { mutableStateOf<ImageModel?>(null) }
    var selectedGroupIndex by remember { mutableIntStateOf(0) }
    var showAddGroupDialog by remember { mutableStateOf(false) }
    var newGroupName by remember { mutableStateOf("") }
    var showDeleteGroupDialog by remember { mutableStateOf(false) }
    var groupToDelete by remember { mutableStateOf<String?>(null) }

    val groups = listOf("全部") + uiState.groups.map { it.name }
    val filteredFavorites = if (selectedGroupIndex == 0) {
        uiState.favorites
    } else {
        val groupId = uiState.groups.getOrNull(selectedGroupIndex - 1)?.id ?: 0
        uiState.favorites.filter { true }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (uiState.groups.isNotEmpty()) {
                ScrollableTabRow(
                    selectedTabIndex = selectedGroupIndex,
                    modifier = Modifier.weight(1f),
                    edgePadding = 8.dp
                ) {
                    groups.forEachIndexed { index, groupName ->
                        Tab(
                            selected = selectedGroupIndex == index,
                            onClick = { selectedGroupIndex = index },
                            text = { Text(groupName) }
                        )
                    }
                }
            }
            IconButton(onClick = { showAddGroupDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "添加分组")
            }
        }

        Box(modifier = Modifier.weight(1f)) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                uiState.favorites.isEmpty() -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("还没有收藏", style = MaterialTheme.typography.headlineSmall)
                        Text("在首页点击收藏按钮即可添加", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                else -> {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(filteredFavorites) { image ->
                            Card(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .combinedClickable(
                                        onClick = { onImageClick(image) },
                                        onLongClick = { selectedImage = image }
                                    ),
                                shape = RoundedCornerShape(8.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                AsyncImage(
                                    model = image.urls.thumb,
                                    contentDescription = image.description,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddGroupDialog) {
        AlertDialog(
            onDismissRequest = { showAddGroupDialog = false },
            title = { Text("新建分组") },
            text = {
                OutlinedTextField(
                    value = newGroupName,
                    onValueChange = { newGroupName = it },
                    label = { Text("分组名称") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newGroupName.isNotBlank()) {
                        viewModel.addGroup(newGroupName)
                        newGroupName = ""
                        showAddGroupDialog = false
                    }
                }) { Text("创建") }
            },
            dismissButton = {
                TextButton(onClick = { showAddGroupDialog = false; newGroupName = "" }) { Text("取消") }
            }
        )
    }

    if (showDeleteGroupDialog && groupToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteGroupDialog = false },
            title = { Text("删除分组") },
            text = { Text("确定要删除分组「$groupToDelete」吗？") },
            confirmButton = {
                TextButton(onClick = {
                    val group = uiState.groups.find { it.name == groupToDelete }
                    if (group != null) {
                        viewModel.deleteGroup(group)
                    }
                    showDeleteGroupDialog = false
                    groupToDelete = null
                }) { Text("删除") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteGroupDialog = false; groupToDelete = null }) { Text("取消") }
            }
        )
    }

    selectedImage?.let { image ->
        AlertDialog(
            onDismissRequest = { selectedImage = null },
            title = { Text("删除收藏") },
            text = { Text("确定要删除这张图片吗？") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.removeFavorite(image.id)
                    selectedImage = null
                }) { Text("删除") }
            },
            dismissButton = {
                TextButton(onClick = { selectedImage = null }) { Text("取消") }
            }
        )
    }
}
