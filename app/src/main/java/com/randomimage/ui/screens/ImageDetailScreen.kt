package com.randomimage.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import coil.size.Size
import com.randomimage.domain.model.ImageModel
import com.randomimage.util.ImageUtils
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ImageDetailScreen(
    image: ImageModel,
    onBack: () -> Unit,
    onSwipeLeft: () -> Unit,
    onSwipeRight: () -> Unit,
    onFavorite: () -> Unit = {},
    isFavorite: Boolean = false,
    onFollow: () -> Unit = {},
    isFollowing: Boolean = false
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }
    var showInfo by remember { mutableStateOf(false) }
    var showTopBar by remember { mutableStateOf(true) }
    var showBottomBar by remember { mutableStateOf(true) }

    BackHandler { onBack() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        scale = if (scale > 1.5f) 1f else 2.5f
                        if (scale <= 1f) { offsetX = 0f; offsetY = 0f }
                    },
                    onTap = {
                        showTopBar = !showTopBar
                        showBottomBar = !showBottomBar
                    }
                )
            }
    ) {
        SubcomposeAsyncImage(
            model = ImageRequest.Builder(context).data(image.localPath ?: image.urls.regular).memoryCacheKey("${image.id}_blur").crossfade(false).size(300, 300).allowHardware(true).build(),
            contentDescription = null, contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize().blur(20.dp)
        )

        SubcomposeAsyncImage(
            model = ImageRequest.Builder(context).data(image.localPath ?: image.urls.regular).memoryCacheKey("${image.id}_detail").crossfade(false).size(Size.ORIGINAL).allowHardware(true).build(),
            contentDescription = image.description, contentScale = ContentScale.Fit,
            loading = { CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color.White) },
            modifier = Modifier.fillMaxSize()
                .graphicsLayer { scaleX = scale; scaleY = scale; translationX = offsetX; translationY = offsetY }
                .pointerInput(Unit) { detectTransformGestures { _, pan, zoom, _ -> scale = (scale * zoom).coerceIn(0.5f, 5f); if (scale > 1f) { offsetX += pan.x; offsetY += pan.y } else { offsetX = 0f; offsetY = 0f } } }
                .pointerInput(Unit) { detectHorizontalDragGestures(onDragEnd = { if (scale <= 1f) { if (offsetX > 100) onSwipeRight(); else if (offsetX < -100) onSwipeLeft(); offsetX = 0f } }, onHorizontalDrag = { _, d -> if (scale <= 1f) offsetX += d }) }
        )

        AnimatedVisibility(visible = showTopBar, enter = fadeIn() + slideInVertically(), exit = fadeOut() + slideOutVertically(), modifier = Modifier.align(Alignment.TopCenter)) {
            Box(modifier = Modifier.fillMaxWidth().height(80.dp).background(brush = androidx.compose.ui.graphics.Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.7f), Color.Transparent)))) {
                Row(modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White) }
                    Text(text = image.user.name, style = MaterialTheme.typography.bodyMedium, color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    IconButton(onClick = { showInfo = !showInfo }) { Icon(Icons.Default.Info, contentDescription = "Info", tint = if (showInfo) MaterialTheme.colorScheme.primary else Color.White) }
                }
            }
        }

        AnimatedVisibility(visible = showBottomBar, enter = fadeIn() + slideInVertically(), exit = fadeOut() + slideOutVertically(), modifier = Modifier.align(Alignment.BottomCenter)) {
            Box(modifier = Modifier.fillMaxWidth().height(100.dp).background(brush = androidx.compose.ui.graphics.Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))))) {
                Row(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceEvenly) {
                    IconButton(onClick = onFavorite) { Icon(if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder, contentDescription = "Favorite", tint = if (isFavorite) Color.Red else Color.White, modifier = Modifier.size(28.dp)) }
                    IconButton(onClick = onFollow) { Icon(if (isFollowing) Icons.Default.PersonRemove else Icons.Default.PersonAdd, contentDescription = "Follow", tint = if (isFollowing) MaterialTheme.colorScheme.primary else Color.White, modifier = Modifier.size(28.dp)) }
                    IconButton(onClick = { ImageUtils.shareImage(context, image.urls.raw) }) { Icon(Icons.Default.Share, contentDescription = "Share", tint = Color.White, modifier = Modifier.size(28.dp)) }
                    IconButton(onClick = { scope.launch { val s = ImageUtils.downloadImage(context, image.urls.raw); Toast.makeText(context, if (s) "已下载" else "下载失败", Toast.LENGTH_SHORT).show() } }) { Icon(Icons.Default.Download, contentDescription = "Download", tint = Color.White, modifier = Modifier.size(28.dp)) }
                }
            }
        }

        AnimatedVisibility(visible = showInfo, enter = slideInVertically(initialOffsetY = { it }) + fadeIn(), exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(), modifier = Modifier.align(Alignment.BottomCenter)) {
            Card(modifier = Modifier.fillMaxWidth().height(320.dp), shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp), colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.9f))) {
                Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Image Info", style = MaterialTheme.typography.titleMedium, color = Color.White)
                        IconButton(onClick = { showInfo = false }) { Icon(Icons.Default.Info, contentDescription = "Close", tint = Color.White) }
                    }
                    HorizontalDivider(color = Color.White.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(12.dp))
                    InfoRow("Author", image.user.name)
                    if (image.user.username != image.user.name) InfoRow("Username", "@${image.user.username}")
                    if (image.description != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Description", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.6f))
                        Text(image.description, style = MaterialTheme.typography.bodySmall, color = Color.White)
                    }
                    if (image.width > 0 && image.height > 0) {
                        InfoRow("Resolution", "${image.width} × ${image.height}")
                        InfoRow("Aspect Ratio", "%.2f".format(image.aspectRatio))
                        InfoRow("Megapixels", "%.1f MP".format((image.width.toLong() * image.height.toLong()) / 1_000_000.0))
                    }
                    if (image.urls.raw.isNotEmpty()) {
                        val apiSource = when {
                            image.urls.raw.contains("lolicon") -> "Lolicon API"
                            image.urls.raw.contains("elaina") -> "Elaina API"
                            image.urls.raw.contains("yppp") -> "Yppp API"
                            image.urls.raw.contains("picsum") -> "Picsum Photos"
                            image.urls.raw.contains("unsplash") -> "Unsplash API"
                            else -> "Custom API"
                        }
                        InfoRow("Source", apiSource)
                    }
                    InfoRow("Image ID", image.id.take(12) + "...")
                    if (image.tags.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Tags", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.6f))
                        Spacer(modifier = Modifier.height(4.dp))
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            image.tags.forEach { tag -> SuggestionChip(onClick = {}, label = { Text(tag, style = MaterialTheme.typography.labelSmall) }) }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.6f))
        Text(text = value, style = MaterialTheme.typography.bodySmall, color = Color.White, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
    }
}
