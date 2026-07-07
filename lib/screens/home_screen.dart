import 'package:flutter/material.dart';
import 'package:flutter_svg/flutter_svg.dart';
import 'dart:async';
import '../main.dart';
import '../models/channel.dart';
import '../data/channels_data.dart';

// Helper widget to load both PNG/JPG/WebP images and SVG graphics
class ChannelLogo extends StatelessWidget {
  final String path;
  final double size;
  final double? fallbackSize;

  const ChannelLogo({
    super.key,
    required this.path,
    required this.size,
    this.fallbackSize,
  });

  @override
  Widget build(BuildContext context) {
    final state = AppStateProvider.of(context);
    final theme = state.currentTheme;

    if (path.toLowerCase().endsWith('.svg')) {
      return SvgPicture.asset(
        path,
        width: size,
        height: size,
        fit: BoxFit.contain,
        placeholderBuilder: (context) => SizedBox(
          width: size,
          height: size,
          child: CircularProgressIndicator(
            strokeWidth: 1.5,
            color: theme.primary.withValues(alpha: 0.5),
          ),
        ),
      );
    } else {
      return Image.asset(
        path,
        width: size,
        height: size,
        fit: BoxFit.contain,
        errorBuilder: (context, error, stackTrace) {
          return Icon(
            Icons.live_tv_rounded,
            color: theme.primary,
            size: fallbackSize ?? (size * 0.8),
          );
        },
      );
    }
  }
}

class HomeScreen extends StatefulWidget {
  const HomeScreen({super.key});

  @override
  State<HomeScreen> createState() => _HomeScreenState();
}

class _HomeScreenState extends State<HomeScreen> {
  final PageController _carouselController = PageController();
  Timer? _carouselTimer;
  int _currentCarouselIdx = 0;
  String _selectedCategoryChip = "Live TV";

  final List<Map<String, String>> _featuredEvents = [
    {
      "title": "LIVE CRICKET EXTRAVAGANZA",
      "subtitle":
          "Watch qualification battles live from high bitrate FHD stream nodes.",
      "tag": "LIVE SPORTS",
      "channelId": "beinsports1",
    },
    {
      "title": "PREMIUM CINE MANIA",
      "subtitle": "Sit back and enjoy high-action blockbuster movies non-stop.",
      "tag": "MOVIE BONANZA",
      "channelId": "sonymax-hd",
    },
    {
      "title": "24/7 GLOBE REPORTING",
      "subtitle":
          "Transparent updates and real-time coverage from worldwide reporters.",
      "tag": "NEWS REELS",
      "channelId": "al-jazeera",
    },
  ];

  final List<String> _categoryChips = [
    "Live TV",
    "Sports",
    "News",
    "Entertainment",
    "Kids",
    "Infotainment",
    "Religious",
    "Indian",
  ];

  @override
  void initState() {
    super.initState();
    _startCarouselTimer();
  }

  void _startCarouselTimer() {
    _carouselTimer = Timer.periodic(const Duration(seconds: 5), (timer) {
      if (!mounted) return;
      _currentCarouselIdx = (_currentCarouselIdx + 1) % _featuredEvents.length;
      if (_carouselController.hasClients) {
        _carouselController.animateToPage(
          _currentCarouselIdx,
          duration: const Duration(milliseconds: 600),
          curve: Curves.easeInOutCubic,
        );
      }
    });
  }

  @override
  void dispose() {
    _carouselTimer?.cancel();
    _carouselController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final state = AppStateProvider.of(context);
    final theme = state.currentTheme;

    // Filters logic
    List<ChannelModel> displayedChannels;
    if (state.searchQuery.isNotEmpty) {
      displayedChannels = ChannelsData.allChannels.where((ch) {
        return ch.name.toLowerCase().contains(
              state.searchQuery.toLowerCase(),
            ) ||
            ch.shortName.toLowerCase().contains(
              state.searchQuery.toLowerCase(),
            ) ||
            (ch.category?.toLowerCase().contains(
                  state.searchQuery.toLowerCase(),
                ) ??
                false);
      }).toList();
    } else if (_selectedCategoryChip == "Live TV") {
      displayedChannels = ChannelsData.allChannels;
    } else {
      displayedChannels = ChannelsData.allChannels.where((ch) {
        final cat = ch.category?.toLowerCase() ?? '';
        final selected = _selectedCategoryChip.toLowerCase();

        if (selected == "sports") return cat.contains("sport");
        if (selected == "news") return cat.contains("news");
        if (selected == "entertainment") {
          return cat.contains("entertainment") ||
              cat.contains("drama") ||
              cat.contains("general");
        }
        if (selected == "kids") {
          return cat.contains("kid") ||
              cat.contains("cartoon") ||
              cat.contains("family");
        }
        if (selected == "infotainment") {
          return cat.contains("infotainment") ||
              cat.contains("science") ||
              cat.contains("discovery");
        }
        if (selected == "religious") {
          return cat.contains("religious") ||
              cat.contains("islamic") ||
              cat.contains("faith");
        }
        if (selected == "indian") {
          return cat.contains("indian") || cat.contains("hindi");
        }
        return cat == selected;
      }).toList();
    }

    if (state.hideOfflineStreams) {
      // Mock filtering out "offline" streams if any
      // For this app we keep them but can filter based on metadata if desired
    }

    return Container(
      width: double.infinity,
      height: double.infinity,
      decoration: BoxDecoration(gradient: theme.bgGradient),
      child: SafeArea(
        child: Column(
          children: [
            // Custom modern Top bar header
            Padding(
              padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 12),
              child: Row(
                children: [
                  // App Title
                  ShaderMask(
                    shaderCallback: (bounds) =>
                        theme.accentGradient.createShader(bounds),
                    child: const Text(
                      "TNTV PREMIUM",
                      style: TextStyle(
                        fontSize: 22,
                        fontWeight: FontWeight.w900,
                        color: Colors.white,
                        letterSpacing: 1.0,
                      ),
                    ),
                  ),
                  const Spacer(),
                  // User Avatar button
                  GestureDetector(
                    onTap: () => state.selectTab(3),
                    child: Container(
                      width: 40,
                      height: 40,
                      decoration: BoxDecoration(
                        shape: BoxShape.circle,
                        gradient: theme.accentGradient,
                        boxShadow: [
                          BoxShadow(
                            color: theme.primary.withValues(alpha: 0.3),
                            blurRadius: 10,
                            spreadRadius: 1,
                          ),
                        ],
                      ),
                      child: Center(
                        child: Text(
                          state.username.isNotEmpty
                              ? state.username[0].toUpperCase()
                              : 'G',
                          style: const TextStyle(
                            fontSize: 16,
                            fontWeight: FontWeight.bold,
                            color: Colors.white,
                          ),
                        ),
                      ),
                    ),
                  ),
                ],
              ),
            ),

            // Search Bar Textfield
            Padding(
              padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
              child: Container(
                decoration: theme.glassDecoration(radius: 16),
                child: TextField(
                  onChanged: (val) => state.updateSearchQuery(val),
                  style: TextStyle(color: theme.textPrimary, fontSize: 14),
                  decoration: InputDecoration(
                    hintText: "Search categories, channels...",
                    hintStyle: TextStyle(
                      color: theme.textSecondary.withValues(alpha: 0.5),
                    ),
                    prefixIcon: Icon(
                      Icons.search_rounded,
                      color: theme.textSecondary.withValues(alpha: 0.7),
                    ),
                    suffixIcon: state.searchQuery.isNotEmpty
                        ? IconButton(
                            icon: Icon(
                              Icons.clear_rounded,
                              color: theme.textSecondary,
                            ),
                            onPressed: () {
                              state.updateSearchQuery("");
                              FocusManager.instance.primaryFocus?.unfocus();
                            },
                          )
                        : null,
                    border: InputBorder.none,
                    contentPadding: const EdgeInsets.symmetric(vertical: 14),
                  ),
                ),
              ),
            ),

            // Scrollable Content
            Expanded(
              child: ListView(
                padding: const EdgeInsets.only(bottom: 110),
                children: [
                  // Dynamic Search View OR Normal Feed
                  if (state.searchQuery.isNotEmpty) ...[
                    // Search Header Title
                    Padding(
                      padding: const EdgeInsets.fromLTRB(20, 16, 20, 12),
                      child: Text(
                        "Search Results (${displayedChannels.length})",
                        style: TextStyle(
                          fontSize: 16,
                          fontWeight: FontWeight.w800,
                          color: theme.textPrimary,
                        ),
                      ),
                    ),
                    if (displayedChannels.isEmpty)
                      Padding(
                        padding: const EdgeInsets.only(top: 60),
                        child: Column(
                          children: [
                            Icon(
                              Icons.search_off_rounded,
                              size: 60,
                              color: theme.textSecondary.withValues(alpha: 0.3),
                            ),
                            const SizedBox(height: 12),
                            Text(
                              "No Channels Found",
                              style: TextStyle(
                                color: theme.textSecondary,
                                fontWeight: FontWeight.bold,
                              ),
                            ),
                          ],
                        ),
                      )
                    else
                      GridView.builder(
                        shrinkWrap: true,
                        physics: const NeverScrollableScrollPhysics(),
                        padding: const EdgeInsets.symmetric(horizontal: 16),
                        gridDelegate:
                            const SliverGridDelegateWithFixedCrossAxisCount(
                              crossAxisCount: 2,
                              crossAxisSpacing: 12,
                              mainAxisSpacing: 12,
                              childAspectRatio: 1.25,
                            ),
                        itemCount: displayedChannels.length,
                        itemBuilder: (context, idx) {
                          return _buildChannelCard(
                            context,
                            displayedChannels[idx],
                          );
                        },
                      ),
                  ] else ...[
                    // 1. Featured Events Carousel
                    _buildFeaturedCarousel(context),

                    // 2. Category Filter Chips List
                    _buildCategoryChips(context),

                    // If Category chip is NOT "Live TV", we show category grid directly
                    if (_selectedCategoryChip != "Live TV") ...[
                      Padding(
                        padding: const EdgeInsets.fromLTRB(20, 16, 20, 12),
                        child: Text(
                          "Category: $_selectedCategoryChip (${displayedChannels.length})",
                          style: TextStyle(
                            fontSize: 16,
                            fontWeight: FontWeight.w800,
                            color: theme.textPrimary,
                          ),
                        ),
                      ),
                      GridView.builder(
                        shrinkWrap: true,
                        physics: const NeverScrollableScrollPhysics(),
                        padding: const EdgeInsets.symmetric(horizontal: 16),
                        gridDelegate:
                            const SliverGridDelegateWithFixedCrossAxisCount(
                              crossAxisCount: 2,
                              crossAxisSpacing: 12,
                              mainAxisSpacing: 12,
                              childAspectRatio: 1.25,
                            ),
                        itemCount: displayedChannels.length,
                        itemBuilder: (context, idx) {
                          return _buildChannelCard(
                            context,
                            displayedChannels[idx],
                          );
                        },
                      ),
                    ] else ...[
                      // 3. Continue Watching Shelf (History)
                      if (state.watchHistoryChannels.isNotEmpty)
                        _buildHorizontalShelf(
                          context,
                          title: "Continue Watching",
                          channels: state.watchHistoryChannels,
                          showClearHistory: true,
                        ),

                      // 4. Favorites Shelf
                      if (state.favoriteChannels.isNotEmpty)
                        _buildHorizontalShelf(
                          context,
                          title: "Quick Favorites",
                          channels: state.favoriteChannels,
                        ),

                      // 5. Main Channel Sections grouped by Category
                      for (final category in ChannelsData.categories)
                        if (category.channels.isNotEmpty)
                          _buildHorizontalShelf(
                            context,
                            title: category.name,
                            channels: category.channels,
                          ),
                    ],
                  ],
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildFeaturedCarousel(BuildContext context) {
    final state = AppStateProvider.of(context);
    final theme = state.currentTheme;

    return Container(
      height: 175,
      margin: const EdgeInsets.symmetric(vertical: 10),
      child: PageView.builder(
        controller: _carouselController,
        itemCount: _featuredEvents.length,
        onPageChanged: (idx) => setState(() => _currentCarouselIdx = idx),
        itemBuilder: (context, idx) {
          final event = _featuredEvents[idx];
          return GestureDetector(
            onTap: () {
              final targetCh = ChannelsData.allChannels.firstWhere(
                (ch) => ch.id == event["channelId"],
                orElse: () => ChannelModel(
                  id: '',
                  name: '',
                  shortName: '',
                  logo: '',
                  quality: '',
                  streams: [],
                ),
              );
              if (targetCh.id.isNotEmpty) {
                state.selectChannel(targetCh);
              }
            },
            child: Container(
              margin: const EdgeInsets.symmetric(horizontal: 16),
              decoration: BoxDecoration(
                borderRadius: BorderRadius.circular(24),
                gradient: LinearGradient(
                  colors: [
                    theme.primary.withValues(alpha: 0.85),
                    theme.secondary.withValues(alpha: 0.95),
                  ],
                  begin: Alignment.topLeft,
                  end: Alignment.bottomRight,
                ),
                boxShadow: [
                  BoxShadow(
                    color: theme.primary.withValues(alpha: 0.12),
                    blurRadius: 15,
                    offset: const Offset(0, 5),
                  ),
                ],
              ),
              child: Stack(
                children: [
                  // Text and Tag content
                  Padding(
                    padding: const EdgeInsets.all(20.0),
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      mainAxisAlignment: MainAxisAlignment.center,
                      children: [
                        Container(
                          padding: const EdgeInsets.symmetric(
                            horizontal: 10,
                            vertical: 4,
                          ),
                          decoration: BoxDecoration(
                            color: Colors.black.withValues(alpha: 0.3),
                            borderRadius: BorderRadius.circular(8),
                          ),
                          child: Text(
                            event["tag"]!,
                            style: const TextStyle(
                              fontSize: 9,
                              fontWeight: FontWeight.w900,
                              color: Colors.white,
                              letterSpacing: 1.5,
                            ),
                          ),
                        ),
                        const SizedBox(height: 12),
                        Text(
                          event["title"]!,
                          style: const TextStyle(
                            fontSize: 18,
                            fontWeight: FontWeight.w900,
                            color: Colors.white,
                            letterSpacing: 0.5,
                          ),
                        ),
                        const SizedBox(height: 6),
                        Text(
                          event["subtitle"]!,
                          maxLines: 2,
                          overflow: TextOverflow.ellipsis,
                          style: TextStyle(
                            fontSize: 11,
                            color: Colors.white.withValues(alpha: 0.8),
                          ),
                        ),
                      ],
                    ),
                  ),
                  // Watch Now button overlay bottom right
                  Positioned(
                    bottom: 16,
                    right: 16,
                    child: Container(
                      padding: const EdgeInsets.symmetric(
                        horizontal: 16,
                        vertical: 6,
                      ),
                      decoration: BoxDecoration(
                        color: Colors.white,
                        borderRadius: BorderRadius.circular(12),
                        boxShadow: [
                          BoxShadow(
                            color: Colors.black.withValues(alpha: 0.1),
                            blurRadius: 5,
                          ),
                        ],
                      ),
                      child: Row(
                        children: [
                          Icon(
                            Icons.play_arrow_rounded,
                            color: theme.secondary,
                            size: 16,
                          ),
                          const SizedBox(width: 4),
                          Text(
                            "WATCH NOW",
                            style: TextStyle(
                              fontSize: 9,
                              fontWeight: FontWeight.w900,
                              color: theme.secondary,
                              letterSpacing: 0.5,
                            ),
                          ),
                        ],
                      ),
                    ),
                  ),
                ],
              ),
            ),
          );
        },
      ),
    );
  }

  Widget _buildCategoryChips(BuildContext context) {
    final state = AppStateProvider.of(context);
    final theme = state.currentTheme;

    return SizedBox(
      height: 40,
      child: ListView.builder(
        scrollDirection: Axis.horizontal,
        padding: const EdgeInsets.symmetric(horizontal: 12),
        itemCount: _categoryChips.length,
        itemBuilder: (context, idx) {
          final label = _categoryChips[idx];
          final isSelected = _selectedCategoryChip == label;

          return GestureDetector(
            onTap: () {
              setState(() {
                _selectedCategoryChip = label;
              });
            },
            child: Container(
              margin: const EdgeInsets.symmetric(horizontal: 6),
              padding: const EdgeInsets.symmetric(horizontal: 16),
              decoration: BoxDecoration(
                gradient: isSelected ? theme.accentGradient : null,
                color: isSelected ? null : Colors.white.withValues(alpha: 0.04),
                borderRadius: BorderRadius.circular(14),
                border: Border.all(
                  color: isSelected
                      ? Colors.transparent
                      : Colors.white.withValues(alpha: 0.06),
                  width: 1.0,
                ),
              ),
              child: Center(
                child: Text(
                  label,
                  style: TextStyle(
                    fontSize: 12,
                    fontWeight: isSelected ? FontWeight.bold : FontWeight.w600,
                    color: isSelected
                        ? Colors.white
                        : theme.textSecondary.withValues(alpha: 0.8),
                  ),
                ),
              ),
            ),
          );
        },
      ),
    );
  }

  Widget _buildHorizontalShelf(
    BuildContext context, {
    required String title,
    required List<ChannelModel> channels,
    bool showClearHistory = false,
  }) {
    final state = AppStateProvider.of(context);
    final theme = state.currentTheme;

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        // Title banner
        Padding(
          padding: const EdgeInsets.fromLTRB(20, 20, 20, 10),
          child: Row(
            children: [
              Text(
                title,
                style: TextStyle(
                  fontSize: 16,
                  fontWeight: FontWeight.w800,
                  color: theme.textPrimary,
                  letterSpacing: 0.5,
                ),
              ),
              const Spacer(),
              if (showClearHistory)
                GestureDetector(
                  onTap: () => state.clearHistory(),
                  child: Container(
                    padding: const EdgeInsets.symmetric(
                      horizontal: 10,
                      vertical: 4,
                    ),
                    decoration: theme.glassDecoration(radius: 8),
                    child: Row(
                      children: [
                        Icon(
                          Icons.delete_sweep_rounded,
                          color: theme.primary,
                          size: 14,
                        ),
                        const SizedBox(width: 4),
                        Text(
                          "Clear",
                          style: TextStyle(
                            fontSize: 10,
                            fontWeight: FontWeight.bold,
                            color: theme.textPrimary,
                          ),
                        ),
                      ],
                    ),
                  ),
                ),
            ],
          ),
        ),

        // Horizontal scrolling channel cards
        SizedBox(
          height: 130,
          child: ListView.builder(
            scrollDirection: Axis.horizontal,
            padding: const EdgeInsets.symmetric(horizontal: 14),
            itemCount: channels.length,
            itemBuilder: (context, idx) {
              final ch = channels[idx];
              return Container(
                width: 145,
                margin: const EdgeInsets.symmetric(horizontal: 6),
                child: _buildChannelCard(context, ch),
              );
            },
          ),
        ),
      ],
    );
  }

  Widget _buildChannelCard(BuildContext context, ChannelModel ch) {
    final state = AppStateProvider.of(context);
    final theme = state.currentTheme;
    final isFav = state.favorites.contains(ch.id);

    return GestureDetector(
      onTap: () => state.selectChannel(ch),
      child: Container(
        decoration: BoxDecoration(
          color: theme.surface.withValues(alpha: 0.65),
          borderRadius: BorderRadius.circular(18),
          border: Border.all(
            color: Colors.white.withValues(alpha: 0.06),
            width: 1.0,
          ),
          boxShadow: [
            BoxShadow(
              color: Colors.black.withValues(alpha: 0.15),
              blurRadius: 6,
              offset: const Offset(0, 3),
            ),
          ],
        ),
        child: Stack(
          children: [
            // Channel Info content
            Padding(
              padding: const EdgeInsets.all(12.0),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  // Logo container
                  Container(
                    width: 40,
                    height: 40,
                    padding: const EdgeInsets.all(5),
                    decoration: theme.glassDecoration(radius: 10),
                    child: ChannelLogo(path: ch.logo, size: 30),
                  ),
                  const Spacer(),
                  // Name and detail text
                  Text(
                    ch.name,
                    maxLines: 1,
                    overflow: TextOverflow.ellipsis,
                    style: TextStyle(
                      fontSize: 12,
                      fontWeight: FontWeight.bold,
                      color: theme.textPrimary,
                    ),
                  ),
                  const SizedBox(height: 2),
                  Text(
                    ch.category ?? 'Live stream',
                    maxLines: 1,
                    overflow: TextOverflow.ellipsis,
                    style: TextStyle(
                      fontSize: 9,
                      color: theme.textSecondary.withValues(alpha: 0.6),
                    ),
                  ),
                ],
              ),
            ),

            // Top right heart / favorite indicator
            Positioned(
              top: 6,
              right: 6,
              child: GestureDetector(
                onTap: () {
                  state.toggleFavorite(ch.id);
                },
                child: Container(
                  padding: const EdgeInsets.all(5),
                  color: Colors.transparent,
                  child: Icon(
                    isFav ? Icons.star_rounded : Icons.star_border_rounded,
                    size: 18,
                    color: isFav
                        ? theme.primary
                        : theme.textSecondary.withValues(alpha: 0.4),
                  ),
                ),
              ),
            ),

            // Bottom right Quality badge
            Positioned(
              bottom: 12,
              right: 12,
              child: Container(
                padding: const EdgeInsets.symmetric(
                  horizontal: 5,
                  vertical: 1.5,
                ),
                decoration: BoxDecoration(
                  color: theme.primary.withValues(alpha: 0.12),
                  borderRadius: BorderRadius.circular(4),
                ),
                child: Text(
                  ch.quality,
                  style: TextStyle(
                    fontSize: 7.5,
                    fontWeight: FontWeight.w900,
                    color: theme.primary,
                  ),
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }
}
