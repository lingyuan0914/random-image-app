package com.randomimage.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.randomimage.data.local.TagData
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TagCloud(
    tags: List<TagData>,
    onTagClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colorScheme = MiuixTheme.colorScheme

    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        tags.take(20).forEach { tag ->
            Text(
                text = tag.name,
                fontSize = 12.sp,
                color = colorScheme.primary,
                modifier = Modifier
                    .clickable { onTagClick(tag.name) }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}
