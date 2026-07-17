package com.tntv.tv

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest

private const val APP_UPDATE_URL = "https://sadabx.github.io/TNTV/data/app-update.json"

data class AppUpdateInfo(
    val versionCode: Int,
    val versionName: String,
    val apkUrl: String,
    val sha256: String?,
    val notes: String,
)

suspend fun checkForAppUpdate(): AppUpdateInfo? = withContext(Dispatchers.IO) {
    runCatching {
        val connection = (URL(APP_UPDATE_URL).openConnection() as HttpURLConnection).apply {
            connectTimeout = 3500
            readTimeout = 3500
            requestMethod = "GET"
            useCaches = false
        }

        try {
            if (connection.responseCode !in 200..299) return@runCatching null
            val json = JSONObject(connection.inputStream.bufferedReader().use { it.readText() })
            val versionCode = json.optInt("versionCode")
            val apkUrl = json.optString("apkUrl").trim()
            if (versionCode <= BuildConfig.VERSION_CODE || apkUrl.isBlank()) return@runCatching null

            AppUpdateInfo(
                versionCode = versionCode,
                versionName = json.optString("versionName").trim().ifBlank { versionCode.toString() },
                apkUrl = apkUrl,
                sha256 = json.optString("sha256").trim().ifBlank { null },
                notes = json.optString("notes").trim(),
            )
        } finally {
            connection.disconnect()
        }
    }.getOrNull()
}

suspend fun downloadUpdateApk(context: Context, info: AppUpdateInfo): File = withContext(Dispatchers.IO) {
    val updateDir = File(context.cacheDir, "updates").apply { mkdirs() }
    val apkFile = File(updateDir, "TNTV-${info.versionName}.apk")
    val connection = (URL(info.apkUrl).openConnection() as HttpURLConnection).apply {
        connectTimeout = 10000
        readTimeout = 30000
        requestMethod = "GET"
        useCaches = false
    }

    try {
        if (connection.responseCode !in 200..299) {
            throw IllegalStateException("APK download failed: ${connection.responseCode}")
        }
        connection.inputStream.use { input ->
            apkFile.outputStream().use { output -> input.copyTo(output) }
        }
    } finally {
        connection.disconnect()
    }

    info.sha256?.let { expected ->
        val actual = apkFile.sha256()
        require(actual.equals(expected, ignoreCase = true)) {
            "Downloaded APK checksum mismatch"
        }
    }

    apkFile
}

fun installUpdateApk(context: Context, apkFile: File) {
    val apkUri: Uri = FileProvider.getUriForFile(
        context,
        "${BuildConfig.APPLICATION_ID}.fileprovider",
        apkFile,
    )
    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(apkUri, "application/vnd.android.package-archive")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(intent)
}

private fun File.sha256(): String {
    val digest = MessageDigest.getInstance("SHA-256")
    inputStream().use { input ->
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        while (true) {
            val read = input.read(buffer)
            if (read <= 0) break
            digest.update(buffer, 0, read)
        }
    }
    return digest.digest().joinToString("") { "%02x".format(it) }
}
