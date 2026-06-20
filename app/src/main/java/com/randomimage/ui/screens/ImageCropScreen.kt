package com.randomimage.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.randomimage.util.ImageUtils
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageCropScreen(imageUrl: String, onBack: () -> Unit, onCropped: (String) -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }
    var aspectRatio by remember { mutableFloatStateOf(1f) }

    BackHandler { onBack() }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("裁剪图片") },
            navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回") } },
            actions = {
                IconButton(onClick = {
                    scope.launch {
                        val success = ImageUtils.downloadImage(context, imageUrl)
                        if (success) { onCropped(imageUrl); Toast.makeText(context, "图片已保存", Toast.LENGTH_SHORT).show() }
                        else { Toast.makeText(context, "保存失败", Toast.LENGTH_SHORT).show() }
                    }
                }) { Icon(Icons.Default.Check, contentDescription = "保存") }
            }
        )

        Box(modifier = Modifier.fillMaxSize().weight(1f).background(Color.Black)) {
            AsyncImage(
                model = ImageRequest.Builder(context).data(imageUrl).crossfade(false).size(coil.size.Size.ORIGINAL).build(),
                contentDescription = "裁剪预览", contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize().pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        scale = (scale * zoom).coerceIn(0.5f, 3f)
                        offsetX += pan.x; offsetY += pan.y
                    }
                }
            )
        }

        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text("裁剪比例", style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("1:1", style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.width(8.dp))
                Slider(value = aspectRatio, onValueChange = { aspectRatio = it }, valueRange = 0.5f..2f, steps = 5, modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.width(8.dp))
                Text("2:1", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
