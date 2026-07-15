package com.tntv.tv

import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.KeyEvent
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Article
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.outlined.ChildCare
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.SportsSoccer
import androidx.compose.material.icons.outlined.Tv
import androidx.compose.material.icons.rounded.Fullscreen
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PictureInPictureAlt
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

private val Bg = Color(0xFF08090D)
private val RailBg = Color(0xFF090909)
private val Text = Color(0xFFF5F5F5)
private val Text2 = Color(0xFFB8BBC2)
private val Accent = Color(0xFF35D6A4)
private val Red = Color(0xFFFF0000)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                TntvApp()
            }
        }
    }
}

data class StreamSource(val label: String, val url: String)

data class Channel(
    val id: String,
    val name: String,
    val shortName: String,
    val category: String,
    val logo: String,
    val streams: List<StreamSource>,
)

data class ChannelCategory(
    val name: String,
    val channels: List<Channel>,
)

private val demoCategories = listOf(
    ChannelCategory(
        "Sports",
        listOf(
            Channel(
                "beinsports-1",
                "BeinSports-1",
                "BEIN",
                "Sports",
                "logos/beinsports-1.png",
                listOf(StreamSource("Server 1", "https://1nyaler.streamhostingcdn.top/stream/23/index.m3u8")),
            ),
            Channel(
                "eurosport-hd",
                "Eurosport HD",
                "EURO",
                "Sports",
                "logos/eurosport-hd.png",
                listOf(StreamSource("Server 1", "http://151.80.18.177:86/Eurosport_HD/index.m3u8")),
            ),
        ),
    ),
    ChannelCategory(
        "News",
        listOf(
            Channel(
                "atn-news",
                "ATN News",
                "ATN",
                "News",
                "logos/atn-news.png",
                listOf(StreamSource("Server 1", "https://owrcovcrpy.gpcdn.net/bpk-tv/1706/output/index.m3u8")),
            ),
            Channel(
                "channel-24",
                "Channel 24",
                "C24",
                "News",
                "logos/channel-24.png",
                listOf(StreamSource("Server 1", "https://owrcovcrpy.gpcdn.net/bpk-tv/1703/output/index.m3u8")),
            ),
            Channel(
                "dbc-news",
                "DBC News",
                "DBC",
                "News",
                "logos/dbc-news.png",
                listOf(StreamSource("Server 1", "https://owrcovcrpy.gpcdn.net/bpk-tv/1728/output/index.m3u8")),
            ),
        ),
    ),
    ChannelCategory(
        "International",
        listOf(
            Channel(
                "al-jazeera-english",
                "Al Jazeera English",
                "AJE",
                "International",
                "logos/al-jazeera.png",
                listOf(StreamSource("Server 1", "https://owrcovcrpy.gpcdn.net/bpk-tv/1721/output/index.m3u8")),
            ),
        ),
    ),
)

@Composable
fun TntvApp() {
    var activeChannel by remember { mutableStateOf<Channel?>(null) }

    Surface(color = Bg, modifier = Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxSize()) {
            LeftRail(isWatching = activeChannel != null, categories = demoCategories)
            if (activeChannel == null) {
                HomeBrowse(
                    categories = demoCategories,
                    onChannelSelected = { activeChannel = it },
                    modifier = Modifier.weight(1f),
                )
            } else {
                PlayerScreen(
                    channel = activeChannel!!,
                    onBack = { activeChannel = null },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun LeftRail(isWatching: Boolean, categories: List<ChannelCategory>) {
    Column(
        modifier = Modifier
            .width(72.dp)
            .fillMaxHeight()
            .background(RailBg)
            .padding(horizontal = 8.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        RailIcon(Icons.Outlined.Menu)
        if (isWatching) RailIcon(Icons.Outlined.Home)
        RailIcon(Icons.Outlined.Search)
        categories.forEach { category ->
            RailIcon(iconForCategory(category.name))
        }
        Spacer(Modifier.weight(1f))
        RailIcon(Icons.Outlined.Settings)
    }
}

@Composable
private fun RailIcon(icon: androidx.compose.ui.graphics.vector.ImageVector) {
    var focused by remember { mutableStateOf(false) }
    val bg = if (focused) Color.White else Color.Transparent
    val fg = if (focused) Color(0xFF080808) else Text2

    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(bg)
            .border(
                width = if (focused) 2.dp else 0.dp,
                color = if (focused) Accent.copy(alpha = 0.75f) else Color.Transparent,
                shape = RoundedCornerShape(16.dp),
            )
            .onFocusChanged { focused = it.isFocused }
            .focusable(),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = null, tint = fg, modifier = Modifier.size(22.dp))
    }
}

@Composable
private fun HomeBrowse(
    categories: List<ChannelCategory>,
    onChannelSelected: (Channel) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Bg),
        contentPadding = PaddingValues(start = 34.dp, top = 28.dp, end = 34.dp, bottom = 56.dp),
        verticalArrangement = Arrangement.spacedBy(34.dp),
    ) {
        items(categories) { category ->
            ChannelRow(category, onChannelSelected)
        }
    }
}

@Composable
private fun ChannelRow(category: ChannelCategory, onChannelSelected: (Channel) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text(
            category.name,
            color = Text,
            fontSize = 30.sp,
            fontWeight = FontWeight.Black,
        )
        LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            items(category.channels) { channel ->
                ChannelCard(channel = channel, onSelected = { onChannelSelected(channel) })
            }
        }
    }
}

@Composable
private fun ChannelCard(channel: Channel, onSelected: () -> Unit) {
    var focused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (focused) 1.08f else 1f, label = "cardScale")
    val shellColor = if (focused) Color.White else Color.Transparent
    val titleColor = if (focused) Color(0xFF080808) else Text

    Column(
        modifier = Modifier
            .width(250.dp)
            .scale(scale)
            .clip(RoundedCornerShape(if (focused) 14.dp else 8.dp))
            .background(shellColor)
            .padding(if (focused) 10.dp else 0.dp)
            .onFocusChanged { focused = it.isFocused }
            .onPreviewKeyEvent {
                if (it.nativeKeyEvent.action == KeyEvent.ACTION_UP &&
                    (it.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_DPAD_CENTER ||
                        it.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_ENTER)
                ) {
                    onSelected()
                    true
                } else {
                    false
                }
            }
            .focusable(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White)
                .border(
                    1.dp,
                    if (focused) Color.Transparent else Color.White.copy(alpha = 0.16f),
                    RoundedCornerShape(8.dp),
                )
                .padding(18.dp),
        ) {
            AssetLogo(channel.logo, channel.shortName, Modifier.align(Alignment.Center))
            LiveBadge(Modifier.align(Alignment.TopStart))
        }
        Text(
            channel.name,
            color = titleColor,
            fontSize = 15.sp,
            fontWeight = FontWeight.ExtraBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun PlayerScreen(channel: Channel, onBack: () -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val player = remember(channel.id) {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(channel.streams.first().url))
            prepare()
            playWhenReady = true
        }
    }

    DisposableEffect(player) {
        onDispose { player.release() }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .onPreviewKeyEvent {
                if (it.nativeKeyEvent.action == KeyEvent.ACTION_UP &&
                    it.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_BACK
                ) {
                    onBack()
                    true
                } else {
                    false
                }
            },
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { viewContext ->
                PlayerView(viewContext).apply {
                    this.player = player
                    useController = false
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                    )
                }
            },
        )
        PlayerControls(
            channel = channel,
            isPlaying = player.isPlaying,
            onPlayPause = { if (player.isPlaying) player.pause() else player.play() },
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

@Composable
private fun PlayerControls(
    channel: Channel,
    isPlaying: Boolean,
    onPlayPause: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.Black.copy(alpha = 0.72f))
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        PlayerControlIcon(
            icon = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
            onClick = onPlayPause,
        )
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(Color.Black.copy(alpha = 0.5f))
                .padding(horizontal = 12.dp, vertical = 7.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(Modifier.size(6.dp).clip(CircleShape).background(Red))
                Text("LIVE", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
        PlayerControlIcon(icon = Icons.AutoMirrored.Rounded.VolumeUp, onClick = {})
        Spacer(Modifier.weight(1f))
        Text(
            channel.name,
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.weight(1f))
        PlayerControlIcon(icon = Icons.Rounded.PictureInPictureAlt, onClick = {})
        PlayerControlIcon(icon = Icons.Rounded.Fullscreen, onClick = {})
    }
}

@Composable
private fun PlayerControlIcon(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
) {
    var focused by remember { mutableStateOf(false) }
    val bg = if (focused) Color.White else Color.Transparent
    val fg = if (focused) Color(0xFF080808) else Color.White

    Box(
        modifier = Modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(bg)
            .border(
                if (focused) 2.dp else 0.dp,
                if (focused) Accent else Color.Transparent,
                CircleShape,
            )
            .onFocusChanged { focused = it.isFocused }
            .onPreviewKeyEvent {
                if (it.nativeKeyEvent.action == KeyEvent.ACTION_UP &&
                    (it.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_DPAD_CENTER ||
                        it.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_ENTER)
                ) {
                    onClick()
                    true
                } else {
                    false
                }
            }
            .focusable(),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = null, tint = fg, modifier = Modifier.size(22.dp))
    }
}

@Composable
private fun AssetLogo(assetPath: String, fallback: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val bitmap = remember(assetPath) {
        runCatching {
            context.assets.open(assetPath).use { BitmapFactory.decodeStream(it) }
        }.getOrNull()
    }

    if (bitmap != null) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = fallback,
            modifier = modifier.fillMaxSize(),
            contentScale = ContentScale.Fit,
        )
    } else {
        Text(
            fallback.take(3).uppercase(),
            modifier = modifier,
            color = Color(0xFF101010),
            fontSize = 24.sp,
            fontWeight = FontWeight.Black,
        )
    }
}

@Composable
private fun LiveBadge(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Red)
            .padding(horizontal = 8.dp, vertical = 4.dp),
    ) {
        Text("LIVE", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Black)
    }
}

private fun iconForCategory(name: String) =
    when {
        name.contains("sport", ignoreCase = true) -> Icons.Outlined.SportsSoccer
        name.contains("news", ignoreCase = true) -> Icons.AutoMirrored.Outlined.Article
        name.contains("international", ignoreCase = true) -> Icons.Outlined.Public
        name.contains("general", ignoreCase = true) -> Icons.Outlined.Tv
        name.contains("entertainment", ignoreCase = true) -> Icons.Outlined.Movie
        name.contains("kid", ignoreCase = true) -> Icons.Outlined.ChildCare
        else -> Icons.Outlined.GridView
    }
