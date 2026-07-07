import 'package:flutter/material.dart';
import 'dart:ui';
import '../main.dart';
import 'home_screen.dart';
import 'shorts_screen.dart';
import 'settings_screen.dart';
import 'player_screen.dart';
import '../models/channel.dart';

class AppLayout extends StatefulWidget {
  const AppLayout({super.key});

  @override
  State<AppLayout> createState() => _AppLayoutState();
}

class _AppLayoutState extends State<AppLayout> {
  // Screens mapped to tab index
  final List<Widget> _screens = [
    const HomeScreen(),
    const ShortsScreen(),
    const FavoritesScreen(),
    const SettingsScreen(),
  ];

  @override
  Widget build(BuildContext context) {
    final state = AppStateProvider.of(context);
    final theme = state.currentTheme;

    return WillPopScope(
      onWillPop: () async {
        // If a channel is actively playing, back button closes it
        if (state.activeChannel != null) {
          state.selectChannel(null);
          return false;
        }
        // If we are not on the Home tab, back button returns to Home
        if (state.activeTabIdx != 0) {
          state.selectTab(0);
          return false;
        }
        return true;
      },
      child: Scaffold(
        resizeToAvoidBottomInset: false,
        body: Stack(
          children: [
            // Current tab screen
            Positioned.fill(
              child: IndexedStack(
                index: state.activeTabIdx,
                children: _screens,
              ),
            ),

            // Custom Glassmorphic Bottom Navigation Bar
            // We hide the bottom bar if a channel is active to let the player take full focus
            if (state.activeChannel == null)
              Positioned(
                left: 16,
                right: 16,
                bottom: 20,
                child: ClipRRect(
                  borderRadius: BorderRadius.circular(24),
                  child: BackdropFilter(
                    filter: ImageFilter.blur(sigmaX: 12, sigmaY: 12),
                    child: Container(
                      height: 70,
                      decoration: BoxDecoration(
                        color: Colors.black.withValues(alpha: 0.45),
                        borderRadius: BorderRadius.circular(24),
                        border: Border.all(
                          color: Colors.white.withValues(alpha: 0.08),
                          width: 1.0,
                        ),
                        boxShadow: [
                          BoxShadow(
                            color: theme.primary.withValues(alpha: 0.04),
                            blurRadius: 20,
                            spreadRadius: 2,
                          ),
                        ],
                      ),
                      child: Row(
                        mainAxisAlignment: MainAxisAlignment.spaceAround,
                        children: [
                          _buildNavItem(context, 0, Icons.home_rounded, "Home"),
                          _buildNavItem(
                            context,
                            1,
                            Icons.play_circle_outline_rounded,
                            "Shorts",
                          ),
                          _buildNavItem(
                            context,
                            2,
                            Icons.favorite_rounded,
                            "Favorites",
                          ),
                          _buildNavItem(
                            context,
                            3,
                            Icons.person_rounded,
                            "Profile",
                          ),
                        ],
                      ),
                    ),
                  ),
                ),
              ),

            // Active Video Player Fullscreen / Overlay Screen
            if (state.activeChannel != null)
              Positioned.fill(
                child: PlayerScreen(
                  channel: state.activeChannel!,
                  onClose: () => state.selectChannel(null),
                ),
              ),
          ],
        ),
      ),
    );
  }

  Widget _buildNavItem(
    BuildContext context,
    int index,
    IconData icon,
    String label,
  ) {
    final state = AppStateProvider.of(context);
    final theme = state.currentTheme;
    final isSelected = state.activeTabIdx == index;

    return GestureDetector(
      onTap: () => state.selectTab(index),
      behavior: HitTestBehavior.opaque,
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          // Nav Icon with glow and scale transitions
          AnimatedContainer(
            duration: const Duration(milliseconds: 250),
            curve: Curves.easeInOut,
            padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 6),
            margin: const EdgeInsets.only(bottom: 2),
            decoration: BoxDecoration(
              color: isSelected
                  ? theme.primary.withValues(alpha: 0.12)
                  : Colors.transparent,
              borderRadius: BorderRadius.circular(16),
            ),
            child: Icon(
              icon,
              color: isSelected
                  ? theme.primary
                  : theme.textSecondary.withValues(alpha: 0.7),
              size: isSelected ? 26 : 22,
            ),
          ),
          // Nav Label text
          AnimatedDefaultTextStyle(
            duration: const Duration(milliseconds: 200),
            style: TextStyle(
              fontSize: 10,
              fontWeight: isSelected ? FontWeight.w900 : FontWeight.w600,
              color: isSelected
                  ? theme.primary
                  : theme.textSecondary.withValues(alpha: 0.7),
              letterSpacing: 0.5,
            ),
            child: Text(label),
          ),
        ],
      ),
    );
  }
}

// Favorites screen implementation
class FavoritesScreen extends StatelessWidget {
  const FavoritesScreen({super.key});

  @override
  Widget build(BuildContext context) {
    final state = AppStateProvider.of(context);
    final theme = state.currentTheme;
    final favorites = state.favoriteChannels;

    return Container(
      width: double.infinity,
      height: double.infinity,
      decoration: BoxDecoration(gradient: theme.bgGradient),
      child: SafeArea(
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            // Screen Header
            Padding(
              padding: const EdgeInsets.fromLTRB(20, 20, 20, 10),
              child: Row(
                children: [
                  ShaderMask(
                    shaderCallback: (bounds) =>
                        theme.accentGradient.createShader(bounds),
                    child: const Text(
                      "My Favorites",
                      style: TextStyle(
                        fontSize: 26,
                        fontWeight: FontWeight.w900,
                        color: Colors.white,
                        letterSpacing: 0.8,
                      ),
                    ),
                  ),
                  const Spacer(),
                  Container(
                    padding: const EdgeInsets.symmetric(
                      horizontal: 12,
                      vertical: 6,
                    ),
                    decoration: theme.glassDecoration(radius: 12),
                    child: Row(
                      children: [
                        Icon(
                          Icons.star_rounded,
                          color: theme.primary,
                          size: 16,
                        ),
                        const SizedBox(width: 4),
                        Text(
                          "${favorites.length} Saved",
                          style: TextStyle(
                            fontSize: 11,
                            fontWeight: FontWeight.bold,
                            color: theme.textPrimary,
                          ),
                        ),
                      ],
                    ),
                  ),
                ],
              ),
            ),

            // Favorites Shelf / Grid view
            Expanded(
              child: favorites.isEmpty
                  ? Center(
                      child: Column(
                        mainAxisAlignment: MainAxisAlignment.center,
                        children: [
                          Icon(
                            Icons.favorite_border_rounded,
                            size: 70,
                            color: theme.textSecondary.withValues(alpha: 0.2),
                          ),
                          const SizedBox(height: 16),
                          Text(
                            "No Favorites Added Yet",
                            style: TextStyle(
                              fontSize: 15,
                              fontWeight: FontWeight.bold,
                              color: theme.textSecondary.withValues(alpha: 0.8),
                            ),
                          ),
                          const SizedBox(height: 8),
                          Text(
                            "Tap the star icon on any channel stream to save it here.",
                            textAlign: TextAlign.center,
                            style: TextStyle(
                              fontSize: 12,
                              color: theme.textSecondary.withValues(alpha: 0.5),
                            ),
                          ),
                        ],
                      ),
                    )
                  : GridView.builder(
                      padding: const EdgeInsets.fromLTRB(16, 12, 16, 110),
                      gridDelegate:
                          const SliverGridDelegateWithFixedCrossAxisCount(
                            crossAxisCount: 2,
                            crossAxisSpacing: 14,
                            mainAxisSpacing: 14,
                            childAspectRatio: 1.15,
                          ),
                      itemCount: favorites.length,
                      itemBuilder: (context, idx) {
                        final ch = favorites[idx];
                        return _buildFavoriteCard(context, ch);
                      },
                    ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildFavoriteCard(BuildContext context, ChannelModel ch) {
    final state = AppStateProvider.of(context);
    final theme = state.currentTheme;

    return GestureDetector(
      onTap: () => state.selectChannel(ch),
      child: Container(
        decoration: BoxDecoration(
          color: theme.surface.withValues(alpha: 0.55),
          borderRadius: BorderRadius.circular(20),
          border: Border.all(
            color: Colors.white.withValues(alpha: 0.06),
            width: 1.0,
          ),
          boxShadow: [
            BoxShadow(
              color: Colors.black.withValues(alpha: 0.2),
              blurRadius: 8,
              offset: const Offset(0, 4),
            ),
          ],
        ),
        child: Stack(
          children: [
            // Content
            Padding(
              padding: const EdgeInsets.all(12.0),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  // Logo container
                  Container(
                    width: 48,
                    height: 48,
                    padding: const EdgeInsets.all(6),
                    decoration: theme.glassDecoration(radius: 12),
                    child: Image.asset(
                      ch.logo,
                      fit: BoxFit.contain,
                      errorBuilder: (context, error, stackTrace) => Icon(
                        Icons.live_tv_rounded,
                        color: theme.primary,
                        size: 20,
                      ),
                    ),
                  ),
                  const Spacer(),
                  // Name and Category
                  Text(
                    ch.name,
                    maxLines: 1,
                    overflow: TextOverflow.ellipsis,
                    style: TextStyle(
                      fontSize: 13,
                      fontWeight: FontWeight.bold,
                      color: theme.textPrimary,
                    ),
                  ),
                  const SizedBox(height: 2),
                  Text(
                    ch.category ?? 'General',
                    style: TextStyle(
                      fontSize: 10,
                      color: theme.textSecondary.withValues(alpha: 0.8),
                    ),
                  ),
                ],
              ),
            ),

            // Un-favorite button top right
            Positioned(
              top: 8,
              right: 8,
              child: IconButton(
                icon: const Icon(Icons.star_rounded, size: 20),
                color: theme.primary,
                visualDensity: VisualDensity.compact,
                onPressed: () => state.toggleFavorite(ch.id),
              ),
            ),

            // Quality indicator bottom right
            Positioned(
              bottom: 12,
              right: 12,
              child: Container(
                padding: const EdgeInsets.symmetric(horizontal: 6, vertical: 2),
                decoration: BoxDecoration(
                  color: theme.primary.withValues(alpha: 0.12),
                  borderRadius: BorderRadius.circular(6),
                ),
                child: Text(
                  ch.quality,
                  style: TextStyle(
                    fontSize: 8,
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
