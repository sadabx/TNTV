package com.tntv.tv

import android.os.Bundle
import android.view.KeyEvent
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import kotlinx.coroutines.delay

private val Bg = Color(0xFF08090D)
private val RailBg = Color(0xFF090909)
private val PanelBg = Color(0xFF0A0B0F)
private val Text = Color(0xFFF5F5F5)
private val Text2 = Color(0xFFB8BBC2)
private val Text3 = Color(0xFF8A8E96)
private val Accent = Color(0xFF35D6A4)
private val Red = Color(0xFFFF0000)

private data class TvMetrics(
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
        railWidth = 72.dp,
        expandedGuideWidth = 320.dp,
        guidePanelWidth = 248.dp,
        railIconSize = 48.dp,
        railIconGlyph = 23.dp,
        screenPadding = 34.dp,
        topPadding = 22.dp,
        searchWidth = 420.dp,
        cardWidth = 250.dp,
        rowGap = 34.dp,
        cardGap = 16.dp,
        logoSize = 44.dp,
        topBrandLogo = 34.dp,
        titleText = 30.sp,
        cardText = 15.sp,
        guideText = 15.sp,
        brandText = 28.sp,
    )
}

@Composable
private fun rememberTvMetrics(width: Dp, height: Dp): TvMetrics {
    val compact = width < 900.dp || height < 520.dp
    val large = width >= 1500.dp
    return when {
        compact -> TvMetrics(
            railWidth = 60.dp,
            expandedGuideWidth = 286.dp,
            guidePanelWidth = 226.dp,
            railIconSize = 42.dp,
            railIconGlyph = 21.dp,
            screenPadding = 24.dp,
            topPadding = 16.dp,
            searchWidth = 340.dp,
            cardWidth = 220.dp,
            rowGap = 26.dp,
            cardGap = 12.dp,
            logoSize = 40.dp,
            topBrandLogo = 30.dp,
            titleText = 26.sp,
            cardText = 14.sp,
            guideText = 14.sp,
            brandText = 24.sp,
        )
        large -> TvMetrics(
            railWidth = 88.dp,
            expandedGuideWidth = 382.dp,
            guidePanelWidth = 294.dp,
            railIconSize = 56.dp,
            railIconGlyph = 27.dp,
            screenPadding = 48.dp,
            topPadding = 30.dp,
            searchWidth = 560.dp,
            cardWidth = 320.dp,
            rowGap = 44.dp,
            cardGap = 22.dp,
            logoSize = 52.dp,
            topBrandLogo = 42.dp,
            titleText = 36.sp,
            cardText = 18.sp,
            guideText = 17.sp,
            brandText = 34.sp,
        )
        else -> LocalTvMetrics.current
    }
}

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

private enum class GuideMode {
    Closed,
    Categories,
    Category,
    Search,
}

@Composable
fun TntvApp() {
    var activeChannel by remember { mutableStateOf<Channel?>(null) }
    var guideMode by remember { mutableStateOf(GuideMode.Closed) }
    var selectedCategory by remember { mutableStateOf<ChannelCategory?>(null) }
    var panelFocusSeed by remember { mutableStateOf(0) }
    val topSearchRequester = remember { FocusRequester() }
    val firstHomeCardRequester = remember { FocusRequester() }
    val categories = channelCategories

    fun openCategory(category: ChannelCategory) {
        selectedCategory = category
        guideMode = GuideMode.Category
        panelFocusSeed += 1
    }

    Surface(color = Bg, modifier = Modifier.fillMaxSize()) {
        BoxWithConstraints(Modifier.fillMaxSize()) {
            val metrics = rememberTvMetrics(maxWidth, maxHeight)
            CompositionLocalProvider(LocalTvMetrics provides metrics) {
                Row(Modifier.fillMaxSize()) {
                    TvGuide(
                        categories = categories,
                        mode = guideMode,
                        selectedCategory = selectedCategory,
                        panelFocusSeed = panelFocusSeed,
                        isWatching = activeChannel != null,
                        onToggleCategories = {
                            guideMode = if (guideMode == GuideMode.Categories) GuideMode.Closed else GuideMode.Categories
                            selectedCategory = null
                        },
                        onHome = {
                            activeChannel = null
                            guideMode = GuideMode.Closed
                            selectedCategory = null
                        },
                        onSearch = {
                            selectedCategory = null
                            guideMode = GuideMode.Search
                        },
                        onCategory = ::openCategory,
                        onBackToCategories = {
                            selectedCategory = null
                            guideMode = GuideMode.Categories
                        },
                        onChannelSelected = { activeChannel = it },
                    )

                    if (activeChannel == null) {
                        MainShell(
                            modifier = Modifier.weight(1f),
                            searchSelected = guideMode == GuideMode.Search,
                            searchFocusRequester = topSearchRequester,
                            searchDownRequester = firstHomeCardRequester,
                        ) {
                            HomeBrowse(
                                categories = categories,
                                firstCardRequester = firstHomeCardRequester,
                                topSearchRequester = topSearchRequester,
                                onChannelSelected = {
                                    activeChannel = it
                                    selectedCategory = categories.firstOrNull { cat -> cat.name == it.category }
                                    guideMode = GuideMode.Category
                                    panelFocusSeed += 1
                                },
                            )
                        }
                    } else {
                        MainShell(
                            modifier = Modifier.weight(1f),
                            searchSelected = guideMode == GuideMode.Search,
                            searchFocusRequester = topSearchRequester,
                            searchDownRequester = FocusRequester.Default,
                        ) {
                            PlayerScreen(
                                channel = activeChannel!!,
                                onBack = { activeChannel = null },
                            )
                        }
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
    isWatching: Boolean,
    onToggleCategories: () -> Unit,
    onHome: () -> Unit,
    onSearch: () -> Unit,
    onCategory: (ChannelCategory) -> Unit,
    onBackToCategories: () -> Unit,
    onChannelSelected: (Channel) -> Unit,
) {
    val expanded = mode != GuideMode.Closed
    val metrics = LocalTvMetrics.current
    Row(
        modifier = Modifier
            .width(if (expanded) metrics.expandedGuideWidth else metrics.railWidth)
            .fillMaxHeight()
            .background(RailBg)
            .animateContentSize(),
    ) {
        RailColumn(
            categories = categories,
            mode = mode,
            selectedCategory = selectedCategory,
            isWatching = isWatching,
            expanded = expanded,
            onToggleCategories = onToggleCategories,
            onHome = onHome,
            onSearch = onSearch,
            onCategory = onCategory,
        )

        if (expanded) {
            GuidePanel(
                categories = categories,
                mode = mode,
                selectedCategory = selectedCategory,
                onBackToCategories = onBackToCategories,
                onCategory = onCategory,
                onChannelSelected = onChannelSelected,
                panelFocusSeed = panelFocusSeed,
                modifier = Modifier
                    .width(metrics.guidePanelWidth)
                    .fillMaxHeight()
                    .background(PanelBg),
            )
        }
    }
}

@Composable
private fun RailColumn(
    categories: List<ChannelCategory>,
    mode: GuideMode,
    selectedCategory: ChannelCategory?,
    isWatching: Boolean,
    expanded: Boolean,
    onToggleCategories: () -> Unit,
    onHome: () -> Unit,
    onSearch: () -> Unit,
    onCategory: (ChannelCategory) -> Unit,
) {
    val metrics = LocalTvMetrics.current
    LazyColumn(
        modifier = Modifier
            .width(metrics.railWidth)
            .fillMaxHeight()
            .background(RailBg),
        contentPadding = PaddingValues(horizontal = metrics.railWidth * 0.11f, vertical = metrics.railWidth * 0.17f),
        verticalArrangement = Arrangement.spacedBy(metrics.railWidth * 0.17f),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        item {
            RailIcon(
                icon = Icons.Outlined.Menu,
                selected = expanded,
                onSelected = onToggleCategories,
            )
        }
        if (isWatching) {
            item {
                RailIcon(
                    icon = Icons.Outlined.Home,
                    selected = false,
                    onSelected = onHome,
                )
            }
        }
        item {
            RailIcon(
                icon = Icons.Outlined.Search,
                selected = mode == GuideMode.Search,
                onSelected = onSearch,
            )
        }
        items(categories) { category ->
            RailIcon(
                icon = iconForCategory(category.name),
                selected = selectedCategory?.name == category.name,
                onSelected = { onCategory(category) },
            )
        }
        item {
            RailIcon(
                icon = Icons.Outlined.Settings,
                selected = false,
                onSelected = {},
            )
        }
    }
}

@Composable
private fun RailIcon(
    icon: ImageVector,
    selected: Boolean,
    onSelected: () -> Unit,
) {
    val metrics = LocalTvMetrics.current
    var focused by remember { mutableStateOf(false) }
    val active = focused || selected
    val bg = if (active) Color.White else Color.Transparent
    val fg = if (active) Color(0xFF080808) else Text2

    Box(
        modifier = Modifier
            .size(metrics.railIconSize)
            .clip(RoundedCornerShape(16.dp))
            .background(bg)
            .border(
                width = if (active) 2.dp else 0.dp,
                color = if (active) Accent.copy(alpha = 0.75f) else Color.Transparent,
                shape = RoundedCornerShape(16.dp),
            )
            .onFocusChanged { focused = it.isFocused }
            .onPreviewKeyEvent {
                val keyCode = it.nativeKeyEvent.keyCode
                val isAction = it.nativeKeyEvent.action == KeyEvent.ACTION_UP
                if (isAction && (keyCode == KeyEvent.KEYCODE_DPAD_CENTER ||
                        keyCode == KeyEvent.KEYCODE_ENTER ||
                        keyCode == KeyEvent.KEYCODE_DPAD_RIGHT)
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
    categories: List<ChannelCategory>,
    mode: GuideMode,
    selectedCategory: ChannelCategory?,
    onBackToCategories: () -> Unit,
    onCategory: (ChannelCategory) -> Unit,
    onChannelSelected: (Channel) -> Unit,
    panelFocusSeed: Int,
    modifier: Modifier = Modifier,
) {
    val metrics = LocalTvMetrics.current
    Column(
        modifier = modifier
            .border(width = 1.dp, color = Color.White.copy(alpha = 0.08f))
            .padding(horizontal = metrics.screenPadding * 0.3f, vertical = metrics.topPadding * 0.55f),
        verticalArrangement = Arrangement.spacedBy(metrics.cardGap * 0.75f),
    ) {
        GuideHeader(
            title = when (mode) {
                GuideMode.Category -> selectedCategory?.name.orEmpty()
                GuideMode.Search -> "Search"
                else -> "Categories"
            },
            showBack = mode == GuideMode.Category,
            count = if (mode == GuideMode.Category) selectedCategory?.channels?.size else categories.sumOf { it.channels.size },
            onBack = onBackToCategories,
        )

        when (mode) {
            GuideMode.Search -> SearchPanel(categories, onChannelSelected)
            GuideMode.Category -> selectedCategory?.let { ChannelList(it.channels, panelFocusSeed, onChannelSelected) }
            else -> CategoryList(categories, onCategory)
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
            .padding(bottom = 6.dp),
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
                    .padding(horizontal = 10.dp, vertical = 4.dp),
            ) {
                Text(count.toString(), color = Text2, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun HeaderButton(icon: ImageVector, onSelected: () -> Unit) {
    var focused by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(12.dp))
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
        Icon(icon, contentDescription = null, tint = if (focused) Color.Black else Text, modifier = Modifier.size(21.dp))
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
private fun SearchPanel(categories: List<ChannelCategory>, onChannelSelected: (Channel) -> Unit) {
    val channels = remember(categories) { categories.flatMap { it.channels } }
    ChannelList(channels = channels, focusSeed = 0, onChannelSelected = onChannelSelected)
}

@Composable
private fun MainShell(
    modifier: Modifier = Modifier,
    searchSelected: Boolean,
    searchFocusRequester: FocusRequester,
    searchDownRequester: FocusRequester,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Bg),
    ) {
        TopBar(
            searchSelected = searchSelected,
            searchFocusRequester = searchFocusRequester,
            searchDownRequester = searchDownRequester,
        )
        content()
    }
}

@Composable
private fun TopBar(
    searchSelected: Boolean,
    searchFocusRequester: FocusRequester,
    searchDownRequester: FocusRequester,
) {
    val metrics = LocalTvMetrics.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = metrics.screenPadding,
                top = metrics.topPadding,
                end = metrics.screenPadding,
                bottom = metrics.topPadding * 0.45f,
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(metrics.cardGap),
    ) {
        TopSearchPill(
            searchSelected = searchSelected,
            focusRequester = searchFocusRequester,
            downRequester = searchDownRequester,
            modifier = Modifier.width(metrics.searchWidth),
        )
        Spacer(Modifier.weight(1f))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(metrics.cardGap * 0.55f),
        ) {
            ChannelLogo("assets/iptv.png", "T9", Modifier.size(metrics.topBrandLogo))
            Text("T9TV", color = Text, fontSize = metrics.brandText, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
private fun TopSearchPill(
    searchSelected: Boolean,
    focusRequester: FocusRequester,
    downRequester: FocusRequester,
    modifier: Modifier = Modifier,
) {
    val metrics = LocalTvMetrics.current
    var focused by remember { mutableStateOf(false) }
    val active = focused || searchSelected
    Row(
        modifier = modifier
            .focusRequester(focusRequester)
            .focusProperties { down = downRequester }
            .clip(RoundedCornerShape(28.dp))
            .background(if (active) Color.White else Color(0xFF242424))
            .border(
                width = if (active) 2.dp else 1.dp,
                color = if (active) Accent.copy(alpha = 0.75f) else Color.White.copy(alpha = 0.10f),
                shape = RoundedCornerShape(28.dp),
            )
            .padding(horizontal = metrics.cardGap * 1.2f, vertical = metrics.topPadding * 0.65f)
            .onFocusChanged { focused = it.isFocused }
            .focusable(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(Icons.Outlined.Search, contentDescription = null, tint = if (active) Color.Black else Text2)
        Text("Search", color = if (active) Color.Black else Text2, fontSize = metrics.brandText * 0.78f, fontWeight = FontWeight.Bold)
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
            .clip(RoundedCornerShape(18.dp))
            .background(activeBg)
            .border(
                width = if (focused) 2.dp else 0.dp,
                color = if (focused) Accent.copy(alpha = 0.75f) else Color.Transparent,
                shape = RoundedCornerShape(18.dp),
            )
            .padding(horizontal = metrics.cardGap * 0.75f, vertical = metrics.cardGap * 0.75f)
            .onFocusChanged { focused = it.isFocused }
            .onPreviewKeyEvent {
                if (it.nativeKeyEvent.action == KeyEvent.ACTION_UP &&
                    (it.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_DPAD_CENTER ||
                        it.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_ENTER ||
                        it.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_DPAD_RIGHT)
                ) {
                    onSelected()
                    true
                } else {
                    false
                }
            }
            .focusable(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(iconForCategory(category.name), contentDescription = null, tint = activeFg, modifier = Modifier.size(metrics.railIconGlyph))
        Text(category.name, modifier = Modifier.weight(1f), color = activeFg, fontSize = metrics.guideText, fontWeight = FontWeight.Bold)
        Text(category.channels.size.toString(), color = activeFg.copy(alpha = 0.72f), fontSize = metrics.guideText * 0.8f)
        Text("›", color = activeFg, fontSize = metrics.guideText * 1.45f, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun ChannelList(channels: List<Channel>, focusSeed: Int, onChannelSelected: (Channel) -> Unit) {
    val metrics = LocalTvMetrics.current
    val firstRequester = remember { FocusRequester() }
    LaunchedEffect(channels, focusSeed) {
        if (channels.isNotEmpty()) {
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
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .border(
                width = if (focused) 2.dp else 0.dp,
                color = if (focused) Accent.copy(alpha = 0.75f) else Color.Transparent,
                shape = RoundedCornerShape(8.dp),
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
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        ChannelLogo(channel.logo, channel.shortName, Modifier.size(metrics.logoSize))
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(channel.name, color = fg, fontSize = metrics.guideText, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Box(Modifier.size(5.dp).clip(CircleShape).background(if (focused) Accent else Text3))
        }
    }
}

@Composable
private fun HomeBrowse(
    categories: List<ChannelCategory>,
    firstCardRequester: FocusRequester,
    topSearchRequester: FocusRequester,
    onChannelSelected: (Channel) -> Unit,
    modifier: Modifier = Modifier,
) {
    val metrics = LocalTvMetrics.current
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Bg),
        contentPadding = PaddingValues(
            start = metrics.screenPadding,
            top = metrics.topPadding * 0.55f,
            end = metrics.screenPadding,
            bottom = metrics.rowGap * 1.6f,
        ),
        verticalArrangement = Arrangement.spacedBy(metrics.rowGap),
    ) {
        itemsIndexed(categories) { index, category ->
            ChannelRow(
                category = category,
                isFirstRow = index == 0,
                firstCardRequester = firstCardRequester,
                topSearchRequester = topSearchRequester,
                onChannelSelected = onChannelSelected,
            )
        }
    }
}

@Composable
private fun ChannelRow(
    category: ChannelCategory,
    isFirstRow: Boolean,
    firstCardRequester: FocusRequester,
    topSearchRequester: FocusRequester,
    onChannelSelected: (Channel) -> Unit,
) {
    val metrics = LocalTvMetrics.current
    Column(verticalArrangement = Arrangement.spacedBy(metrics.cardGap * 0.85f)) {
        Text(
            category.name,
            color = Text,
            fontSize = metrics.titleText,
            fontWeight = FontWeight.Black,
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = metrics.cardGap * 0.75f),
            horizontalArrangement = Arrangement.spacedBy(metrics.cardGap),
        ) {
            itemsIndexed(category.channels) { index, channel ->
                val cardModifier =
                    if (isFirstRow && index == 0) Modifier.focusRequester(firstCardRequester) else Modifier
                ChannelCard(
                    channel = channel,
                    modifier = cardModifier,
                    upRequester = topSearchRequester,
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
    upRequester: FocusRequester,
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
            .focusProperties { up = upRequester }
            .scale(scale)
            .clip(RoundedCornerShape(if (focused) 14.dp else 8.dp))
            .background(shellColor)
            .padding(if (focused) metrics.cardGap * 0.6f else 0.dp)
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
        verticalArrangement = Arrangement.spacedBy(metrics.cardGap * 0.6f),
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
                .padding(metrics.cardGap),
        ) {
            ChannelLogo(channel.logo, channel.shortName, Modifier.align(Alignment.Center).fillMaxSize())
            LiveBadge(Modifier.align(Alignment.TopStart))
        }
        Text(
            channel.name,
            color = titleColor,
            fontSize = metrics.cardText,
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
    icon: ImageVector,
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
private fun ChannelLogo(rawLogo: String, fallback: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color.White),
        contentAlignment = Alignment.Center,
    ) {
        AsyncImage(
            model = logoModel(rawLogo),
            contentDescription = fallback,
            modifier = Modifier.fillMaxSize().padding(4.dp),
            contentScale = ContentScale.Fit,
        )
        if (rawLogo.isBlank()) {
            Text(
                fallback.take(3).uppercase(),
                color = Color(0xFF101010),
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
            )
        }
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
        name.contains("entertainment", ignoreCase = true) -> Icons.Outlined.Movie
        name.contains("indian", ignoreCase = true) -> Icons.Outlined.Public
        name.contains("kid", ignoreCase = true) -> Icons.Outlined.ChildCare
        else -> Icons.Outlined.GridView
    }
