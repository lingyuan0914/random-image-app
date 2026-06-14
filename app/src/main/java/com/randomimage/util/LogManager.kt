package com.randomimage.util

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import timber.log.Timber
import java.io.File
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
        return try {
            logFile?.readText() ?: "No logs available"
        } catch (e: Exception) {
            "Error reading logs: ${e.message}"
        }
    }

    fun shareLogs(context: Context) {
        flushLogs()
        logFile?.let { file ->
            if (file.exists() && file.readText().isNotEmpty()) {
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
    }

    fun clearLogs() {
        logBuffer.clear()
        logFile?.delete()
        Timber.d("Logs cleared")
    }
}
