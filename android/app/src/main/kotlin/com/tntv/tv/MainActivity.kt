package com.tntv.tv

import android.os.Bundle
import android.view.KeyEvent
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Article
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.outlined.ChildCare
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Dns
import androidx.compose.material.icons.automirrored.outlined.FeaturedPlayList
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.NightsStay
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.SportsSoccer
import androidx.compose.material.icons.outlined.Tv
import androidx.compose.material.icons.rounded.Fullscreen
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.SvgDecoder
import kotlinx.coroutines.delay

private val Bg = Color(0xFF06070B)
private val RailBg = Color(0xE6090C11)
private val PanelBg = Color(0xF5080A0E)
private val ExpandedMenuBg = Color(0xFF17191E)
private val Text = Color(0xFFF7F8FB)
private val Text2 = Color(0xB8F7F8FB)
private val Text3 = Color(0x7AF7F8FB)
private val Accent = Color(0xFF35D6A4)
private val Red = Color(0xFFFF0000)

private data class TvMetrics(
    val railSlotWidth: Dp,
    val railWidth: Dp,
    val expandedGuideWidth: Dp,
    val guidePanelWidth: Dp,
    val railIconSize: Dp,
    val railIconGlyph: Dp,
    val screenPadding: Dp,
    val topPadding: Dp,
    val searchWidth: Dp,
    val cardWidth: Dp,
    val rowGap: Dp,
    val cardGap: Dp,
    val logoSize: Dp,
    val topBrandLogo: Dp,
    val titleText: TextUnit,
    val cardText: TextUnit,
    val guideText: TextUnit,
    val brandText: TextUnit,
)

private val LocalTvMetrics = staticCompositionLocalOf {
    TvMetrics(
        railSlotWidth = 37.dp,
        railWidth = 31.dp,
        expandedGuideWidth = 160.dp,
        guidePanelWidth = 141.dp,
        railIconSize = 23.dp,
        railIconGlyph = 11.dp,
        screenPadding = 17.dp,
        topPadding = 17.dp,
        searchWidth = 220.dp,
        cardWidth = 125.dp,
        rowGap = 17.dp,
        cardGap = 8.dp,
        logoSize = 22.dp,
        topBrandLogo = 15.dp,
        titleText = 15.sp,
        cardText = 8.sp,
        guideText = 8.sp,
        brandText = 10.sp,
    )
}
private val LocalLogoImageLoader = staticCompositionLocalOf<ImageLoader> {
    error("Logo image loader is not available")
}

@Composable
private fun rememberTvMetrics(width: Dp): TvMetrics {
    // Android TV commonly exposes 1920x1080 as 960x540 dp. These ratios map
    // the website's measured CSS geometry back to the same physical pixels.
    val scale = (width.value / 960f).coerceIn(0.78f, 2f)
    return TvMetrics(
        railSlotWidth = 37.dp * scale,
        railWidth = 31.dp * scale,
        expandedGuideWidth = 160.dp * scale,
        guidePanelWidth = 141.dp * scale,
        railIconSize = 23.dp * scale,
        railIconGlyph = 11.dp * scale,
        screenPadding = 17.dp * scale,
        topPadding = 17.dp * scale,
        searchWidth = 220.dp * scale,
        cardWidth = 125.dp * scale,
        rowGap = 17.dp * scale,
        cardGap = 8.dp * scale,
        logoSize = 22.dp * scale,
        topBrandLogo = 15.dp * scale,
        titleText = (15f * scale).sp,
        cardText = (8f * scale).sp,
        guideText = (8f * scale).sp,
        brandText = (10f * scale).sp,
    )
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                TrionineTvApp()
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

private enum class GuideMode {
    Closed,
    Categories,
    Category,
    Search,
}

@Composable
fun TrionineTvApp() {
    val context = LocalContext.current
    val logoImageLoader = remember(context) {
        ImageLoader.Builder(context)
            .components { add(SvgDecoder.Factory()) }
            .build()
    }
    var activeChannel by remember { mutableStateOf<Channel?>(null) }
    var guideMode by remember { mutableStateOf(GuideMode.Closed) }
    var selectedCategory by remember { mutableStateOf<ChannelCategory?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var panelFocusSeed by remember { mutableIntStateOf(0) }
    val firstHomeCardRequester = remember { FocusRequester() }
    val categories = channelCategories

    fun openCategory(category: ChannelCategory) {
        selectedCategory = category
        guideMode = GuideMode.Category
        panelFocusSeed += 1
    }

    BackHandler(enabled = guideMode != GuideMode.Closed) {
        guideMode = when (guideMode) {
            GuideMode.Category -> GuideMode.Categories
            GuideMode.Categories, GuideMode.Search -> GuideMode.Closed
            GuideMode.Closed -> GuideMode.Closed
        }
    }

    Surface(color = Bg, modifier = Modifier.fillMaxSize()) {
        BoxWithConstraints(Modifier.fillMaxSize()) {
            val metrics = rememberTvMetrics(maxWidth)
            CompositionLocalProvider(LocalTvMetrics provides metrics, LocalLogoImageLoader provides logoImageLoader) {
                val guideContentInset = when (guideMode) {
                    GuideMode.Categories -> metrics.expandedGuideWidth
                    GuideMode.Category -> metrics.railSlotWidth + metrics.guidePanelWidth
                    GuideMode.Closed, GuideMode.Search -> metrics.railSlotWidth
                }
                Box(
                    Modifier
                        .fillMaxSize()
                        .drawBehind {
                            drawRect(color = Bg)
                            drawRect(
                                brush = Brush.radialGradient(
                                    colors = listOf(Color(0x3058B7FF), Color.Transparent),
                                    center = Offset(size.width * 0.78f, size.height * 0.06f),
                                    radius = size.width * 0.28f,
                                ),
                            )
                            drawRect(
                                brush = Brush.radialGradient(
                                    colors = listOf(Color(0x1F35D6A4), Color.Transparent),
                                    center = Offset(size.width * 0.04f, size.height * 0.42f),
                                    radius = size.width * 0.22f,
                                ),
                            )
                        },
                ) {
                    if (activeChannel == null) {
                        HomeBrowse(
                            categories = categories,
                            firstCardRequester = firstHomeCardRequester,
                            onChannelSelected = {
                                activeChannel = it
                                searchQuery = ""
                                selectedCategory = categories.firstOrNull { cat -> cat.name == it.category }
                                guideMode = GuideMode.Closed
                            },
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(start = guideContentInset),
                        )
                    } else {
                        PlayerScreen(
                            channel = activeChannel!!,
                            onBack = {
                                activeChannel = null
                                guideMode = GuideMode.Closed
                            },
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(start = guideContentInset),
                        )
                    }

                    TvGuide(
                        categories = categories,
                        mode = guideMode,
                        selectedCategory = selectedCategory,
                        panelFocusSeed = panelFocusSeed,
                        onToggleCategories = {
                            guideMode = if (guideMode == GuideMode.Closed) GuideMode.Categories else GuideMode.Closed
                        },
                        onHome = {
                            activeChannel = null
                            searchQuery = ""
                            guideMode = GuideMode.Closed
                            selectedCategory = null
                        },
                        onSearch = { guideMode = GuideMode.Search },
                        onCategory = ::openCategory,
                        onBackToCategories = { guideMode = GuideMode.Categories },
                        onChannelSelected = {
                            activeChannel = it
                            searchQuery = ""
                            selectedCategory = categories.firstOrNull { cat -> cat.name == it.category }
                            guideMode = GuideMode.Closed
                        },
                        modifier = Modifier.align(Alignment.TopStart).zIndex(20f),
                    )

                    if (guideMode == GuideMode.Search) {
                        SearchOverlay(
                            categories = categories,
                            query = searchQuery,
                            onQueryChange = { searchQuery = it },
                            onClose = { guideMode = GuideMode.Closed },
                            onChannelSelected = {
                                activeChannel = it
                                searchQuery = ""
                                selectedCategory = categories.firstOrNull { cat -> cat.name == it.category }
                                guideMode = GuideMode.Closed
                            },
                            modifier = Modifier.fillMaxSize().zIndex(30f),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TvGuide(
    categories: List<ChannelCategory>,
    mode: GuideMode,
    selectedCategory: ChannelCategory?,
    panelFocusSeed: Int,
    onToggleCategories: () -> Unit,
    onHome: () -> Unit,
    onSearch: () -> Unit,
    onCategory: (ChannelCategory) -> Unit,
    onBackToCategories: () -> Unit,
    onChannelSelected: (Channel) -> Unit,
    modifier: Modifier = Modifier,
) {
    val metrics = LocalTvMetrics.current
    when (mode) {
        GuideMode.Categories -> ExpandedCategoryMenu(
            categories = categories,
            onClose = onToggleCategories,
            onHome = onHome,
            onSearch = onSearch,
            onCategory = onCategory,
            modifier = modifier.width(metrics.expandedGuideWidth),
        )
        else -> Row(modifier = modifier.width(if (mode == GuideMode.Category) metrics.railSlotWidth + metrics.guidePanelWidth else metrics.railSlotWidth)) {
            CollapsedRail(
                categories = categories,
                mode = mode,
                selectedCategory = selectedCategory,
                expanded = mode == GuideMode.Category,
                onToggle = onToggleCategories,
                onHome = onHome,
                onSearch = onSearch,
                onCategory = onCategory,
            )
            if (mode == GuideMode.Category && selectedCategory != null) {
            GuidePanel(
                selectedCategory = selectedCategory,
                onBackToCategories = onBackToCategories,
                onChannelSelected = onChannelSelected,
                panelFocusSeed = panelFocusSeed,
                modifier = Modifier
                    .width(metrics.guidePanelWidth)
                    .fillMaxHeight()
                    .padding(top = metrics.topPadding * 0.42f, end = metrics.cardGap * 0.62f, bottom = metrics.topPadding * 0.42f),
            )
        }
        }
    }
}

@Composable
private fun CollapsedRail(
    categories: List<ChannelCategory>,
    mode: GuideMode,
    selectedCategory: ChannelCategory?,
    expanded: Boolean,
    onToggle: () -> Unit,
    onHome: () -> Unit,
    onSearch: () -> Unit,
    onCategory: (ChannelCategory) -> Unit,
) {
    val metrics = LocalTvMetrics.current
    Column(
        modifier = Modifier
            .width(metrics.railSlotWidth)
            .fillMaxHeight()
            .padding(
                start = metrics.cardGap * 0.31f,
                top = metrics.cardGap * 0.62f,
                end = metrics.cardGap * 0.37f,
                bottom = metrics.cardGap * 0.62f,
            )
            .clip(RoundedCornerShape(metrics.cardGap * 1.62f))
            .border(0.5.dp, Color.White.copy(alpha = 0.11f), RoundedCornerShape(metrics.cardGap * 1.62f))
            .background(RailBg)
            .padding(horizontal = metrics.cardGap * 0.5f, vertical = metrics.cardGap * 0.75f),
        verticalArrangement = Arrangement.spacedBy(metrics.cardGap * 0.62f),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        RailIcon(
            icon = if (expanded) Icons.Outlined.Close else Icons.Outlined.Menu,
            selected = false,
            menu = true,
            onSelected = onToggle,
        )
        RailIcon(icon = Icons.Outlined.Home, selected = false, onSelected = onHome)
        RailIcon(icon = Icons.Outlined.Search, selected = mode == GuideMode.Search, onSelected = onSearch)
        Box(Modifier.width(metrics.railIconSize * 0.74f).height(0.5.dp).background(Color.White.copy(alpha = 0.12f)))
        categories.forEach { category ->
            RailIcon(
                icon = iconForCategory(category.name),
                selected = selectedCategory?.name == category.name,
                onSelected = { onCategory(category) },
            )
        }
        Spacer(Modifier.weight(1f))
        Box(modifier = Modifier.height(metrics.railIconSize * 3.9f), contentAlignment = Alignment.Center) {
            Row(modifier = Modifier.rotate(-90f), verticalAlignment = Alignment.Bottom) {
                Text("TRIONINE", color = Text3, fontSize = metrics.guideText * 0.86f, fontWeight = FontWeight.Black)
                Text(" TV", color = Color(0xC76181FF), fontSize = metrics.guideText * 0.58f, fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
private fun ExpandedCategoryMenu(
    categories: List<ChannelCategory>,
    onClose: () -> Unit,
    onHome: () -> Unit,
    onSearch: () -> Unit,
    onCategory: (ChannelCategory) -> Unit,
    modifier: Modifier = Modifier,
) {
    val metrics = LocalTvMetrics.current
    Column(
        modifier = modifier
            .fillMaxHeight()
            .padding(start = metrics.cardGap * 0.62f, top = metrics.topPadding * 0.42f, end = metrics.cardGap * 0.62f, bottom = metrics.topPadding * 0.42f)
            .clip(RoundedCornerShape(metrics.cardGap * 1.5f))
            .border(0.5.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(metrics.cardGap * 1.5f))
            .background(ExpandedMenuBg)
            .padding(
                start = metrics.cardGap * 0.62f,
                top = metrics.cardGap * 0.75f,
                end = metrics.cardGap * 0.87f,
                bottom = metrics.cardGap * 0.87f,
            ),
        verticalArrangement = Arrangement.spacedBy(metrics.cardGap * 0.5f),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(metrics.cardGap * 0.62f)) {
            RailIcon(icon = Icons.Outlined.Close, selected = false, menu = true, onSelected = onClose)
            AsyncImage(
                model = logoModel("assets/iptv.png"),
                imageLoader = LocalLogoImageLoader.current,
                contentDescription = null,
                modifier = Modifier.size(metrics.topBrandLogo),
                contentScale = ContentScale.Fit,
            )
            Row(verticalAlignment = Alignment.Bottom) {
                Text("TRIONINE", color = Text, fontSize = metrics.brandText, fontWeight = FontWeight.Black)
                Text(" TV", color = Color(0xFF6181FF), fontSize = metrics.brandText * 0.68f, fontWeight = FontWeight.Black)
            }
        }
        ExpandedNavRow(icon = Icons.Outlined.Home, label = "Home", onSelected = onHome)
        ExpandedNavRow(icon = Icons.Outlined.Search, label = "Search", onSelected = onSearch)
        categories.forEach { category ->
            GuideCategoryRow(category = category, onSelected = { onCategory(category) })
        }
    }
}

@Composable
private fun ExpandedNavRow(icon: ImageVector, label: String, onSelected: () -> Unit) {
    val metrics = LocalTvMetrics.current
    var focused by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(metrics.railIconSize * 1.04f)
            .clip(RoundedCornerShape(metrics.cardGap))
            .background(if (focused) Color.White else Color.Transparent)
            .onFocusChanged { focused = it.isFocused }
            .onPreviewKeyEvent {
                if (it.nativeKeyEvent.action == KeyEvent.ACTION_UP &&
                    (it.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_DPAD_CENTER || it.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_ENTER)
                ) {
                    onSelected()
                    true
                } else false
            }
            .focusable()
            .padding(horizontal = metrics.cardGap * 0.5f),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(metrics.cardGap * 0.75f),
    ) {
        Icon(icon, contentDescription = label, tint = if (focused) Color.Black else Text2, modifier = Modifier.size(metrics.railIconGlyph))
        Text(label, color = if (focused) Color.Black else Text2, fontSize = metrics.guideText, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun RailIcon(
    icon: ImageVector,
    selected: Boolean,
    menu: Boolean = false,
    onSelected: () -> Unit,
) {
    val metrics = LocalTvMetrics.current
    var focused by remember { mutableStateOf(false) }
    val bg = when {
        focused -> Color.White
        menu -> Accent.copy(alpha = 0.14f)
        selected -> Accent.copy(alpha = 0.14f)
        else -> Color.Transparent
    }
    val fg = when {
        focused -> Color(0xFF080808)
        menu -> Accent
        selected -> Accent
        else -> Text2
    }

    Box(
        modifier = Modifier
            .size(metrics.railIconSize)
            .clip(RoundedCornerShape(metrics.cardGap * 1.06f))
            .background(bg)
            .border(
                width = if (focused) 2.dp else if (selected) 1.dp else 0.dp,
                color = if (selected || focused) Accent.copy(alpha = if (focused) 0.75f else 0.35f) else Color.Transparent,
                shape = RoundedCornerShape(metrics.cardGap * 1.06f),
            )
            .onFocusChanged { focused = it.isFocused }
            .onPreviewKeyEvent {
                val keyCode = it.nativeKeyEvent.keyCode
                val isAction = it.nativeKeyEvent.action == KeyEvent.ACTION_UP
                if (isAction && (keyCode == KeyEvent.KEYCODE_DPAD_CENTER ||
                        keyCode == KeyEvent.KEYCODE_ENTER)
                ) {
                    onSelected()
                    true
                } else {
                    false
                }
            }
            .focusable(),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = null, tint = fg, modifier = Modifier.size(metrics.railIconGlyph))
    }
}

@Composable
private fun GuidePanel(
    selectedCategory: ChannelCategory?,
    onBackToCategories: () -> Unit,
    onChannelSelected: (Channel) -> Unit,
    panelFocusSeed: Int,
    modifier: Modifier = Modifier,
) {
    val metrics = LocalTvMetrics.current
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(metrics.cardGap * 1.5f))
            .border(width = 0.5.dp, color = Color.White.copy(alpha = 0.12f), RoundedCornerShape(metrics.cardGap * 1.5f))
            .background(PanelBg)
            .padding(horizontal = metrics.cardGap * 0.75f, vertical = metrics.cardGap * 0.75f),
        verticalArrangement = Arrangement.spacedBy(metrics.cardGap * 0.75f),
    ) {
        GuideHeader(
            title = selectedCategory?.name.orEmpty(),
            showBack = true,
            count = selectedCategory?.channels?.size,
            onBack = onBackToCategories,
        )
        selectedCategory?.let {
            ChannelList(channels = it.channels, focusSeed = panelFocusSeed, onChannelSelected = onChannelSelected)
        }
    }
}

@Composable
private fun GuideHeader(
    title: String,
    showBack: Boolean,
    count: Int?,
    onBack: () -> Unit,
) {
    val metrics = LocalTvMetrics.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(metrics.railIconSize * 1.38f)
            .padding(bottom = metrics.cardGap * 0.25f),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (showBack) {
            HeaderButton(icon = Icons.AutoMirrored.Outlined.ArrowBack, onSelected = onBack)
        }
        Text(
            title,
            modifier = Modifier.weight(1f),
            color = Text,
            fontSize = metrics.guideText * 1.2f,
            fontWeight = FontWeight.ExtraBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (count != null) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF1C1F29))
                    .padding(horizontal = metrics.cardGap * 0.62f, vertical = metrics.cardGap * 0.25f),
            ) {
                Text(count.toString(), color = Text2, fontSize = metrics.guideText * 0.75f, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun HeaderButton(icon: ImageVector, onSelected: () -> Unit) {
    val metrics = LocalTvMetrics.current
    var focused by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .size(metrics.railIconSize * 0.87f)
            .clip(RoundedCornerShape(metrics.cardGap * 0.5f))
            .background(if (focused) Color.White else Color.Transparent)
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
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = null, tint = if (focused) Color.Black else Text, modifier = Modifier.size(metrics.railIconGlyph))
    }
}

@Composable
private fun CategoryList(categories: List<ChannelCategory>, onCategory: (ChannelCategory) -> Unit) {
    val metrics = LocalTvMetrics.current
    LazyColumn(verticalArrangement = Arrangement.spacedBy(metrics.cardGap * 0.5f)) {
        items(categories) { category ->
            GuideCategoryRow(category = category, onSelected = { onCategory(category) })
        }
    }
}

@Composable
private fun SearchOverlay(
    categories: List<ChannelCategory>,
    query: String,
    onQueryChange: (String) -> Unit,
    onClose: () -> Unit,
    onChannelSelected: (Channel) -> Unit,
    modifier: Modifier = Modifier,
) {
    val metrics = LocalTvMetrics.current
    Box(
        modifier = modifier
            .background(Color.Black.copy(alpha = 0.68f))
            .onPreviewKeyEvent {
                if (it.nativeKeyEvent.action == KeyEvent.ACTION_UP && it.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_BACK) {
                    onClose()
                    true
                } else false
            },
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = metrics.topPadding * 4.4f)
                .width(metrics.searchWidth)
                .height(metrics.searchWidth * 1.32f),
            verticalArrangement = Arrangement.spacedBy(metrics.cardGap * 0.62f),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = metrics.cardGap * 0.5f),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Search", modifier = Modifier.weight(1f), color = Text, fontSize = metrics.guideText * 1.2f, fontWeight = FontWeight.Black)
                HeaderButton(icon = Icons.Outlined.Close, onSelected = onClose)
            }
            SearchPanel(
                categories = categories,
                query = query,
                onQueryChange = onQueryChange,
                onChannelSelected = onChannelSelected,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun SearchPanel(
    categories: List<ChannelCategory>,
    query: String,
    onQueryChange: (String) -> Unit,
    onChannelSelected: (Channel) -> Unit,
    modifier: Modifier = Modifier,
) {
    val metrics = LocalTvMetrics.current
    val inputRequester = remember { FocusRequester() }
    val channels = remember(categories, query) {
        val normalized = query.trim().lowercase()
        categories
            .flatMap { it.channels }
            .filter { normalized.isBlank() || it.name.lowercase().contains(normalized) || it.category.lowercase().contains(normalized) }
    }

    LaunchedEffect(Unit) {
        delay(120)
        runCatching { inputRequester.requestFocus() }
    }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(metrics.cardGap * 0.62f)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(metrics.railIconSize * 1.04f)
                .clip(RoundedCornerShape(metrics.cardGap * 0.75f))
                .background(Color(0xF5090B0F))
                .border(0.5.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(metrics.cardGap * 0.75f))
                .padding(horizontal = metrics.cardGap, vertical = metrics.cardGap * 0.5f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(metrics.cardGap * 0.62f),
        ) {
            Icon(Icons.Outlined.Search, contentDescription = null, tint = Text2, modifier = Modifier.size(metrics.railIconGlyph))
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(inputRequester),
                singleLine = true,
                textStyle = TextStyle(color = Text, fontSize = metrics.guideText, fontWeight = FontWeight.SemiBold),
                decorationBox = { field ->
                    if (query.isBlank()) Text("Search channels", color = Text3, fontSize = metrics.guideText)
                    field()
                },
            )
        }
        if (channels.isEmpty()) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(metrics.cardGap * 0.75f))
                    .background(PanelBg),
                contentAlignment = Alignment.Center,
            ) {
                Text("No matching channels", color = Text3, fontSize = metrics.guideText)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(metrics.cardGap * 0.75f))
                    .border(0.5.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(metrics.cardGap * 0.75f))
                    .background(PanelBg)
                    .padding(metrics.cardGap * 0.62f),
                verticalArrangement = Arrangement.spacedBy(metrics.cardGap * 0.3f),
            ) {
                items(channels) { channel ->
                    GuideChannelRow(channel = channel, onSelected = { onChannelSelected(channel) })
                }
            }
        }
    }
}

@Composable
private fun GuideCategoryRow(category: ChannelCategory, onSelected: () -> Unit) {
    val metrics = LocalTvMetrics.current
    var focused by remember { mutableStateOf(false) }
    val activeBg = if (focused) Color.White else Color.Transparent
    val activeFg = if (focused) Color(0xFF080808) else Text2
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(metrics.railIconSize * 1.04f)
            .clip(RoundedCornerShape(metrics.cardGap))
            .background(activeBg)
            .border(
                width = if (focused) 2.dp else 0.dp,
                color = if (focused) Accent.copy(alpha = 0.75f) else Color.Transparent,
                shape = RoundedCornerShape(metrics.cardGap),
            )
            .padding(horizontal = metrics.cardGap * 0.5f)
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
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(metrics.cardGap * 0.75f),
    ) {
        Icon(iconForCategory(category.name), contentDescription = null, tint = activeFg, modifier = Modifier.size(metrics.railIconGlyph))
        Text(category.name, modifier = Modifier.weight(1f), color = activeFg, fontSize = metrics.guideText, fontWeight = FontWeight.Bold)
        Text(category.channels.size.toString(), color = activeFg.copy(alpha = 0.72f), fontSize = metrics.guideText * 0.8f)
        Text("›", color = activeFg, fontSize = metrics.guideText * 1.45f, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun ChannelList(
    channels: List<Channel>,
    focusSeed: Int,
    autoFocus: Boolean = true,
    onChannelSelected: (Channel) -> Unit,
) {
    val metrics = LocalTvMetrics.current
    val firstRequester = remember { FocusRequester() }
    LaunchedEffect(channels, focusSeed, autoFocus) {
        if (autoFocus && channels.isNotEmpty()) {
            delay(180)
            runCatching { firstRequester.requestFocus() }
        }
    }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(metrics.cardGap * 0.6f),
        contentPadding = PaddingValues(bottom = metrics.rowGap),
    ) {
        itemsIndexed(channels) { index, channel ->
            GuideChannelRow(
                channel = channel,
                modifier = if (index == 0) Modifier.focusRequester(firstRequester) else Modifier,
                onSelected = { onChannelSelected(channel) },
            )
        }
    }
}

@Composable
private fun GuideChannelRow(
    channel: Channel,
    modifier: Modifier = Modifier,
    onSelected: () -> Unit,
) {
    val metrics = LocalTvMetrics.current
    var focused by remember { mutableStateOf(false) }
    val bg = if (focused) Color.White else Color.Transparent
    val fg = if (focused) Color(0xFF080808) else Text2
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(metrics.cardGap * 0.5f))
            .background(bg)
            .border(
                width = if (focused) 1.dp else 0.dp,
                color = if (focused) Accent.copy(alpha = 0.75f) else Color.Transparent,
                shape = RoundedCornerShape(metrics.cardGap * 0.5f),
            )
            .padding(horizontal = metrics.cardGap * 0.65f, vertical = metrics.cardGap * 0.65f)
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
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(metrics.cardGap * 0.75f),
    ) {
        ChannelLogo(channel.logo, channel.shortName, Modifier.size(metrics.logoSize))
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(metrics.cardGap * 0.18f)) {
            Text(channel.name, color = fg, fontSize = metrics.guideText, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Box(Modifier.size(metrics.cardGap * 0.31f).clip(CircleShape).background(if (focused) Accent else Text3))
        }
    }
}

@Composable
private fun HomeBrowse(
    categories: List<ChannelCategory>,
    firstCardRequester: FocusRequester,
    onChannelSelected: (Channel) -> Unit,
    modifier: Modifier = Modifier,
) {
    val metrics = LocalTvMetrics.current
    val rowFocusRequesters = remember(categories, firstCardRequester) {
        categories.mapIndexed { rowIndex, category ->
            category.channels.mapIndexed { columnIndex, _ ->
                if (rowIndex == 0 && columnIndex == 0) firstCardRequester else FocusRequester()
            }
        }
    }
    LaunchedEffect(categories) {
        delay(180)
        runCatching { firstCardRequester.requestFocus() }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize(),
        contentPadding = PaddingValues(
            start = metrics.screenPadding,
            top = metrics.topPadding,
            end = metrics.screenPadding,
            bottom = metrics.rowGap * 1.6f,
        ),
        verticalArrangement = Arrangement.spacedBy(metrics.rowGap),
    ) {
        itemsIndexed(categories) { index, category ->
            ChannelRow(
                category = category,
                rowIndex = index,
                rowFocusRequesters = rowFocusRequesters,
                onChannelSelected = onChannelSelected,
            )
        }
    }
}

@Composable
private fun ChannelRow(
    category: ChannelCategory,
    rowIndex: Int,
    rowFocusRequesters: List<List<FocusRequester>>,
    onChannelSelected: (Channel) -> Unit,
) {
    val metrics = LocalTvMetrics.current
    Column(verticalArrangement = Arrangement.spacedBy(metrics.cardGap * 0.85f)) {
        Text(
            category.name,
            color = Text,
            fontSize = metrics.titleText,
            lineHeight = metrics.titleText * 1.16f,
            fontWeight = FontWeight.Black,
        )
        LazyRow(
            contentPadding = PaddingValues(
                start = metrics.cardGap * 0.75f,
                end = metrics.cardGap * 0.75f,
                bottom = metrics.cardGap * 1.75f,
            ),
            horizontalArrangement = Arrangement.spacedBy(metrics.cardGap),
        ) {
            itemsIndexed(category.channels) { index, channel ->
                ChannelCard(
                    channel = channel,
                    modifier = Modifier.focusRequester(rowFocusRequesters[rowIndex][index]),
                    onMoveUp = {
                        if (rowIndex > 0) {
                            val previousRow = rowFocusRequesters[rowIndex - 1]
                            previousRow[index.coerceAtMost(previousRow.lastIndex)].requestFocus()
                        }
                    },
                    onMoveDown = {
                        val nextRow = rowFocusRequesters.getOrNull(rowIndex + 1)
                        if (nextRow != null) {
                            nextRow[index.coerceAtMost(nextRow.lastIndex)].requestFocus()
                        }
                    },
                    onSelected = { onChannelSelected(channel) },
                )
            }
        }
    }
}

@Composable
private fun ChannelCard(
    channel: Channel,
    modifier: Modifier = Modifier,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onSelected: () -> Unit,
) {
    val metrics = LocalTvMetrics.current
    var focused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (focused) 1.08f else 1f, label = "cardScale")
    val shellColor = if (focused) Color.White else Color.Transparent
    val titleColor = if (focused) Color(0xFF080808) else Text

    Column(
        modifier = modifier
            .width(metrics.cardWidth)
            .scale(scale)
            .clip(RoundedCornerShape(if (focused) metrics.cardGap * 0.87f else metrics.cardGap * 0.5f))
            .background(shellColor)
            .padding(if (focused) metrics.cardGap * 0.6f else 0.dp)
            .onFocusChanged { focused = it.isFocused }
            .onPreviewKeyEvent {
                if (it.nativeKeyEvent.action != KeyEvent.ACTION_DOWN) return@onPreviewKeyEvent false

                when (it.nativeKeyEvent.keyCode) {
                    KeyEvent.KEYCODE_DPAD_UP -> {
                        onMoveUp()
                        true
                    }
                    KeyEvent.KEYCODE_DPAD_DOWN -> {
                        onMoveDown()
                        true
                    }
                    KeyEvent.KEYCODE_DPAD_CENTER,
                    KeyEvent.KEYCODE_ENTER -> {
                        onSelected()
                        true
                    }
                    else -> false
                }
            }
            .focusable(),
        verticalArrangement = Arrangement.spacedBy(metrics.cardGap * 0.6f),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .clip(RoundedCornerShape(metrics.cardGap * 0.5f))
                .background(Color.White)
                .border(
                    0.5.dp,
                    if (focused) Color.Transparent else Color.White.copy(alpha = 0.16f),
                    RoundedCornerShape(metrics.cardGap * 0.5f),
                ),
        ) {
            ChannelLogo(channel.logo, channel.shortName, Modifier.align(Alignment.Center).fillMaxSize())
        }
        Text(
            channel.name,
            color = titleColor,
            fontSize = metrics.cardText,
            lineHeight = metrics.cardText * 1.25f,
            fontWeight = FontWeight.ExtraBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
@androidx.annotation.OptIn(UnstableApi::class)
private fun PlayerScreen(channel: Channel, onBack: () -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var sourceIndex by remember(channel.id) { mutableIntStateOf(0) }
    var isPlaying by remember(channel.id, sourceIndex) { mutableStateOf(true) }
    val source = channel.streams[sourceIndex.coerceIn(channel.streams.indices)]
    val player = remember(channel.id, sourceIndex) {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(source.url))
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
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                    )
                }
            },
        )
        PlayerControls(
            channel = channel,
            sourceLabel = source.label,
            isPlaying = isPlaying,
            onPlayPause = {
                if (isPlaying) player.pause() else player.play()
                isPlaying = !isPlaying
            },
            onSourceChange = {
                if (channel.streams.size > 1) sourceIndex = (sourceIndex + 1) % channel.streams.size
            },
            onAutoQuality = {
                player.trackSelectionParameters = player.trackSelectionParameters
                    .buildUpon()
                    .clearOverridesOfType(androidx.media3.common.C.TRACK_TYPE_VIDEO)
                    .build()
            },
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

@Composable
private fun PlayerControls(
    channel: Channel,
    sourceLabel: String,
    isPlaying: Boolean,
    onPlayPause: () -> Unit,
    onSourceChange: () -> Unit,
    onAutoQuality: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val metrics = LocalTvMetrics.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.3f), Color.Black.copy(alpha = 0.85f)),
                ),
            )
            .padding(start = metrics.cardGap * 1.25f, top = metrics.cardGap * 2.5f, end = metrics.cardGap * 1.25f, bottom = metrics.cardGap * 0.87f),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(metrics.cardGap * 0.5f),
    ) {
        PlayerControlIcon(
            icon = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
            onClick = onPlayPause,
        )
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(Color.Black.copy(alpha = 0.5f))
                .padding(horizontal = metrics.cardGap * 0.75f, vertical = metrics.cardGap * 0.31f),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(metrics.cardGap * 0.37f)) {
                Box(Modifier.size(metrics.cardGap * 0.37f).clip(CircleShape).background(Red))
                Text("LIVE", color = Color.White, fontSize = metrics.guideText * 0.75f, fontWeight = FontWeight.Bold)
            }
        }
        PlayerControlIcon(icon = Icons.AutoMirrored.Rounded.VolumeUp, onClick = {})
        Spacer(Modifier.weight(1f))
        Text(
            channel.name,
            color = Color.White,
            fontSize = metrics.cardText * 0.9f,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.weight(1f))
        PlayerControlPill(icon = Icons.Outlined.Dns, label = sourceLabel.uppercase(), onClick = onSourceChange)
        PlayerControlPill(icon = Icons.Outlined.Settings, label = "AUTO", onClick = onAutoQuality)
        PlayerControlIcon(icon = Icons.Rounded.Fullscreen, onClick = {})
    }
}

@Composable
private fun PlayerControlPill(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
) {
    val metrics = LocalTvMetrics.current
    var focused by remember { mutableStateOf(false) }
    val bg = if (focused) Color.White else Color.Black.copy(alpha = 0.62f)
    val fg = if (focused) Color(0xFF080808) else Color.White
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(metrics.cardGap * 1.25f))
            .background(bg)
            .border(if (focused) 1.dp else 0.5.dp, if (focused) Accent else Color.White.copy(alpha = 0.16f), RoundedCornerShape(metrics.cardGap * 1.25f))
            .padding(horizontal = metrics.cardGap * 0.75f, vertical = metrics.cardGap * 0.5f)
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
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(metrics.cardGap * 0.37f),
    ) {
        Icon(icon, contentDescription = null, tint = fg, modifier = Modifier.size(metrics.railIconGlyph * 0.6f))
        Text(label, color = fg, fontSize = metrics.guideText * 0.68f, fontWeight = FontWeight.ExtraBold, maxLines = 1)
    }
}

@Composable
private fun PlayerControlIcon(
    icon: ImageVector,
    onClick: () -> Unit,
) {
    val metrics = LocalTvMetrics.current
    var focused by remember { mutableStateOf(false) }
    val bg = if (focused) Color.White else Color.Transparent
    val fg = if (focused) Color(0xFF080808) else Color.White

    Box(
        modifier = Modifier
            .size(metrics.railIconSize * 0.87f)
            .clip(CircleShape)
            .background(bg)
            .border(
                if (focused) 1.dp else 0.dp,
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
        Icon(icon, contentDescription = null, tint = fg, modifier = Modifier.size(metrics.railIconGlyph))
    }
}

@Composable
private fun ChannelLogo(rawLogo: String, fallback: String, modifier: Modifier = Modifier) {
    val metrics = LocalTvMetrics.current
    val imageLoader = LocalLogoImageLoader.current
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(metrics.cardGap * 0.5f))
            .background(Color.White),
        contentAlignment = Alignment.Center,
    ) {
        AsyncImage(
            model = logoModel(rawLogo),
            imageLoader = imageLoader,
            contentDescription = fallback,
            modifier = Modifier.fillMaxSize().padding(metrics.cardGap * 0.5f),
            contentScale = ContentScale.Fit,
        )
        if (rawLogo.isBlank()) {
            Text(
                fallback.take(3).uppercase(),
                color = Color(0xFF101010),
                fontSize = metrics.guideText,
                fontWeight = FontWeight.Black,
            )
        }
    }
}

private fun logoModel(rawLogo: String): String {
    val logo = rawLogo.trim()
    if (logo.startsWith("http://") || logo.startsWith("https://")) return logo
    return "file:///android_asset/${logo.removePrefix("assets/")}"
}

private fun iconForCategory(name: String): ImageVector =
    when {
        name.contains("sport", ignoreCase = true) -> Icons.Outlined.SportsSoccer
        name.contains("news", ignoreCase = true) -> Icons.AutoMirrored.Outlined.Article
        name.contains("international", ignoreCase = true) -> Icons.Outlined.Public
        name.contains("general", ignoreCase = true) -> Icons.Outlined.Tv
        name.contains("entertainment", ignoreCase = true) -> Icons.Outlined.GridView
        name.contains("indian", ignoreCase = true) -> Icons.Outlined.Flag
        name.contains("kid", ignoreCase = true) -> Icons.Outlined.ChildCare
        name.contains("infotainment", ignoreCase = true) -> Icons.AutoMirrored.Outlined.FeaturedPlayList
        name.contains("religious", ignoreCase = true) -> Icons.Outlined.NightsStay
        else -> Icons.Outlined.GridView
    }
