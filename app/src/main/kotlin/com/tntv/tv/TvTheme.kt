package com.tntv.tv

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.ImageLoader

internal val Bg = Color(0xFF06070B)
internal val RailBg = Color(0xE6090C11)
internal val PanelBg = Color(0xF5080A0E)
internal val ExpandedMenuBg = Color(0xFF17191E)
internal val Text = Color(0xFFF7F8FB)
internal val Text2 = Color(0xB8F7F8FB)
internal val Text3 = Color(0x7AF7F8FB)
internal val Accent = Color(0xFF35D6A4)
internal val Red = Color(0xFFFF0000)

internal data class TvMetrics(
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

internal val LocalTvMetrics = staticCompositionLocalOf {
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

internal val LocalLogoImageLoader = staticCompositionLocalOf<ImageLoader> {
    error("Logo image loader is not available")
}

@Composable
internal fun rememberTvMetrics(width: Dp): TvMetrics {
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
