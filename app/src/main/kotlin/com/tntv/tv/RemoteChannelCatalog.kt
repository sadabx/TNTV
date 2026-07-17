package com.tntv.tv

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

private const val REMOTE_CHANNEL_CATALOG_URL =
    "https://sadabx.github.io/TNTV/data/channels.json"

/**
 * Optional channel refresh feed for the Android TV app.
 *
 * The generated ChannelCatalog.kt snapshot remains the instant/offline fallback.
 */
suspend fun loadRemoteChannelCatalog(): List<ChannelCategory>? = withContext(Dispatchers.IO) {
    runCatching {
        val connection = (URL(REMOTE_CHANNEL_CATALOG_URL).openConnection() as HttpURLConnection).apply {
            connectTimeout = 3500
            readTimeout = 3500
            requestMethod = "GET"
            useCaches = false
        }

        try {
            if (connection.responseCode !in 200..299) return@runCatching null
            val body = connection.inputStream.bufferedReader().use { it.readText() }
            parseRemoteChannelCatalog(JSONObject(body)).takeIf { it.isNotEmpty() }
        } finally {
            connection.disconnect()
        }
    }.getOrNull()
}

private fun parseRemoteChannelCatalog(root: JSONObject): List<ChannelCategory> {
    val categories = root.optJSONArray("categories") ?: return emptyList()
    return categories.mapObjectsNotNull { categoryJson ->
        val name = categoryJson.optString("name").trim()
        val channels = categoryJson.optJSONArray("channels")
            ?.mapObjectsNotNull { channelJson -> parseRemoteChannel(name, channelJson) }
            .orEmpty()

        if (name.isBlank()) null else ChannelCategory(name = name, channels = channels)
    }
}

private fun parseRemoteChannel(category: String, json: JSONObject): Channel? {
    val id = json.optString("id").trim()
    val name = json.optString("name").trim()
    val logo = json.optString("logo").trim()
    val streams = json.optJSONArray("streams")
        ?.mapObjectsNotNull { streamJson ->
            val label = streamJson.optString("label").trim().ifBlank { "Direct" }
            val url = streamJson.optString("url").trim()
            if (url.isBlank()) null else StreamSource(label = label, url = url)
        }
        .orEmpty()

    if (id.isBlank() || name.isBlank() || logo.isBlank() || streams.isEmpty()) return null

    return Channel(
        id = id,
        name = name,
        shortName = json.optString("shortName").trim().ifBlank { name.take(4).uppercase() },
        category = category,
        logo = logo,
        streams = streams,
    )
}

private inline fun <T> JSONArray.mapObjectsNotNull(transform: (JSONObject) -> T?): List<T> {
    val items = mutableListOf<T>()
    for (index in 0 until length()) {
        val item = optJSONObject(index) ?: continue
        transform(item)?.let(items::add)
    }
    return items
}
