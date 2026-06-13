package com.randomimage.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class ToolCategory(
    val name: String,
    val count: Int,
    val tools: List<ToolItem>
)

data class ToolItem(
    val name: String,
    val url: String? = null,
    val description: String = ""
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ToolboxScreen() {
    val context = LocalContext.current

    val categories = listOf(
        ToolCategory("图片类API", 7, listOf(
            ToolItem("Lolicon", "https://api.lolicon.app/setu/v2", "二次元图片API"),
            ToolItem("色图API", "https://sex.nyan.run/api/v2/", "二次元色图API"),
            ToolItem("Kori图库", "http://api.kori.moe/img", "随机图片API"),
            ToolItem("随机美图", "https://img.xjh.me/random_img.php", "随机美图API"),
            ToolItem("必应每日一图", "https://api.codelife.cc/api/top/bing", "必应每日壁纸"),
            ToolItem("B站封面", "https://api.vvhan.com/api/bilibili", "获取B站视频封面"),
            ToolItem("二次元风景", "https://t.mwm.moe/fj/", "二次元风景图片")
        )),
        ToolCategory("文字类API", 3, listOf(
            ToolItem("一言", "https://v1.hitokoto.cn", "随机一言API"),
            ToolItem("古诗词", "https://v2.jinrishici.com/one.json", "古诗词API"),
            ToolItem("彩虹屁", "https://api.03c3.cn/api/zb", "彩虹屁API")
        )),
        ToolCategory("工具类API", 4, listOf(
            ToolItem("IP查询", "https://api.vvhan.com/api/ip", "在线IP查询"),
            ToolItem("快递查询", "https://api.vvhan.com/api/kuaidi", "快递查询API"),
            ToolItem("二维码生成", "https://api.vvhan.com/api/ewm", "二维码生成"),
            ToolItem("短网址生成", "https://api.vvhan.com/api/dw", "短网址生成")
        )),
        ToolCategory("加密解密与安全", 4, listOf(
            ToolItem("恶意文件检测", "https://www.virustotal.com", "VirusTotal"),
            ToolItem("腾讯安全中心", "https://urlsec.qq.com", "网址安全检测"),
            ToolItem("MD5加密", "https://www.cmd5.com", "MD5在线加密"),
            ToolItem("AES/DES加密", "https://www.toolnb.com/tools/aes.html", "对称加密工具")
        )),
        ToolCategory("图像处理与设计", 6, listOf(
            ToolItem("图片压缩", "https://tinypng.com", "在线图片压缩"),
            ToolItem("图片格式转换", "https://convertio.co", "格式转换工具"),
            ToolItem("图片去背景", "https://www.remove.bg", "AI去背景"),
            ToolItem("图片放大", "https://bigjpg.com", "AI图片放大"),
            ToolItem("图片EXIF", "https://exif.tools", "EXIF信息查看"),
            ToolItem("AI去除水印", "https://www.apowersoft.cn/online-watermark-remover", "AI去水印")
        )),
        ToolCategory("音频视频处理", 4, listOf(
            ToolItem("视频转GIF", "https://ezgif.com/video-to-gif", "视频转GIF工具"),
            ToolItem("音频剪辑", "https://audacityteam.org", "Audacity音频编辑"),
            ToolItem("白噪音", "https://www.noisli.com", "白噪音生成器"),
            ToolItem("环境音效", "https://mynoise.net", "环境音效生成")
        )),
        ToolCategory("文件处理", 4, listOf(
            ToolItem("PDF转换", "https://smallpdf.com", "PDF在线转换"),
            ToolItem("文件压缩", "https://www.rarlab.com", "RAR/ZIP压缩"),
            ToolItem("OCR识别", "https://www.onlineocr.net", "图片文字识别"),
            ToolItem("Markdown预览", "https://markdownlivepreview.com", "MD在线预览")
        ))
    )

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("工具箱") }
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(1),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(categories) { category ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = category.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        Text(
                            text = "${category.count}个",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            category.tools.forEach { tool ->
                                FilterChip(
                                    selected = false,
                                    onClick = {
                                        tool.url?.let { url ->
                                            try {
                                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                                context.startActivity(intent)
                                            } catch (e: Exception) {
                                                Toast.makeText(context, "无法打开: ${tool.name}", Toast.LENGTH_SHORT).show()
                                            }
                                        } ?: run {
                                            Toast.makeText(context, "${tool.name} - ${tool.description}", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    label = { Text(tool.name) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
