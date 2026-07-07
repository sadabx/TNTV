import 'package:flutter/material.dart';
import 'package:video_player/video_player.dart';
import '../main.dart';

class ShortVideo {
  final String id;
  final String title;
  final String creator;
  final String videoUrl;
  final String likes;
  final int comments;

  ShortVideo({
    required this.id,
    required this.title,
    required this.creator,
    required this.videoUrl,
    required this.likes,
    required this.comments,
  });
}

class ShortsScreen extends StatefulWidget {
  const ShortsScreen({super.key});

  @override
  State<ShortsScreen> createState() => _ShortsScreenState();
}

class _ShortsScreenState extends State<ShortsScreen> {
  final PageController _pageController = PageController();
  int _activeIndex = 0;
  bool _isMuted = true;
  final List<String> _likedIds = [];

  final List<ShortVideo> _shortsData = [
    ShortVideo(
      id: "short-1",
      title: "Unbelievable Goal from Midfield! ⚽🔥 #fifa #sports #goals",
      creator: "@SportsArena_BD",
      videoUrl:
          "https://assets.mixkit.co/videos/preview/mixkit-playing-football-in-the-rain-41582-large.mp4",
      likes: "24.5K",
      comments: 512,
    ),
    ShortVideo(
      id: "short-2",
      title:
          "Childhood memories with the best cartoons Gopal Bhar! 🍉 #nostalgia",
      creator: "@KolkataFun_Reels",
      videoUrl:
          "https://assets.mixkit.co/videos/preview/mixkit-little-cartoon-robot-dancing-40018-large.mp4",
      likes: "18.2K",
      comments: 290,
    ),
    ShortVideo(
      id: "short-3",
      title:
          "A majestic view of the green tea gardens in Sylhet 🍵🍃 #bangladesh #travel",
      creator: "@GreenTrails",
      videoUrl:
          "https://assets.mixkit.co/videos/preview/mixkit-river-surrounded-by-forest-seen-from-above-41578-large.mp4",
      likes: "32.1K",
      comments: 780,
    ),
    ShortVideo(
      id: "short-4",
      title:
          "Dynamic racing action behind the wheel! 🏎️🏁 #formula1 #f1 #fast",
      creator: "@F1_Dynamics",
      videoUrl:
          "https://assets.mixkit.co/videos/preview/mixkit-kart-racing-action-on-a-sunny-day-40292-large.mp4",
      likes: "45.0K",
      comments: 1105,
    ),
  ];

  // Map to hold video controllers per index
  final Map<int, VideoPlayerController> _controllers = {};
  final Map<int, bool> _isInitialized = {};

  @override
  void initState() {
    super.initState();
    // Pre-initialize the first and second videos
    _initController(0);
    _initController(1);
  }

  @override
  void dispose() {
    _pageController.dispose();
    for (var controller in _controllers.values) {
      controller.dispose();
    }
    super.dispose();
  }

  Future<void> _initController(int index) async {
    if (index < 0 || index >= _shortsData.length) return;
    if (_controllers.containsKey(index)) return;

    final video = _shortsData[index];
    final controller = VideoPlayerController.networkUrl(
      Uri.parse(video.videoUrl),
    );
    _controllers[index] = controller;
    _isInitialized[index] = false;

    try {
      await controller.initialize();
      if (!mounted) return;

      setState(() {
        _isInitialized[index] = true;
      });

      controller.setLooping(true);
      controller.setVolume(_isMuted ? 0.0 : 1.0);

      // Play immediately if it's the current page
      if (index == _activeIndex) {
        controller.play();
      }
    } catch (e) {
      debugPrint("Error initializing shorts player $index: $e");
    }
  }

  void _onPageChanged(int index) {
    setState(() {
      _activeIndex = index;
    });

    // Play active index and pause others
    _controllers.forEach((idx, controller) {
      if (idx == index) {
        controller.setVolume(_isMuted ? 0.0 : 1.0);
        controller.play();
      } else {
        controller.pause();
        controller.seekTo(Duration.zero);
      }
    });

    // Load adjacent videos in the background
    _initController(index - 1);
    _initController(index + 1);

    // Dispose controllers that are far away to save memory
    final keysToDispose = _controllers.keys
        .where((k) => (k - index).abs() > 1)
        .toList();
    for (var k in keysToDispose) {
      _controllers[k]?.dispose();
      _controllers.remove(k);
      _isInitialized.remove(k);
    }
  }

  void _toggleMute() {
    setState(() {
      _isMuted = !_isMuted;
    });
    _controllers.forEach((idx, controller) {
      controller.setVolume(_isMuted ? 0.0 : 1.0);
    });
  }

  void _toggleLike(String id) {
    setState(() {
      if (_likedIds.contains(id)) {
        _likedIds.remove(id);
      } else {
        _likedIds.add(id);
      }
    });
  }

  void _scrollNext() {
    if (_activeIndex + 1 < _shortsData.length) {
      _pageController.nextPage(
        duration: const Duration(milliseconds: 400),
        curve: Curves.easeOutCubic,
      );
    }
  }

  void _scrollPrev() {
    if (_activeIndex > 0) {
      _pageController.previousPage(
        duration: const Duration(milliseconds: 400),
        curve: Curves.easeOutCubic,
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    final state = AppStateProvider.of(context);
    final theme = state.currentTheme;

    return Scaffold(
      backgroundColor: Colors.black,
      body: Stack(
        children: [
          // Fullscreen Vertical Swiper PageView
          Positioned.fill(
            child: PageView.builder(
              controller: _pageController,
              scrollDirection: Axis.vertical,
              itemCount: _shortsData.length,
              onPageChanged: _onPageChanged,
              itemBuilder: (context, index) {
                final short = _shortsData[index];
                final controller = _controllers[index];
                final isReady = _isInitialized[index] ?? false;

                return GestureDetector(
                  onTap: () {
                    if (controller == null) return;
                    setState(() {
                      if (controller.value.isPlaying) {
                        controller.pause();
                      } else {
                        controller.play();
                      }
                    });
                  },
                  child: Stack(
                    alignment: Alignment.center,
                    children: [
                      // Video Player output
                      if (controller != null && isReady)
                        Positioned.fill(
                          child: FittedBox(
                            fit: BoxFit.cover,
                            child: SizedBox(
                              width: controller.value.size.width,
                              height: controller.value.size.height,
                              child: VideoPlayer(controller),
                            ),
                          ),
                        )
                      else
                        // Centered spinner
                        const Positioned.fill(
                          child: Center(
                            child: CircularProgressIndicator(
                              color: Colors.white,
                            ),
                          ),
                        ),

                      // Ambient bottom shadow cover
                      Positioned(
                        bottom: 0,
                        left: 0,
                        right: 0,
                        height: 250,
                        child: Container(
                          decoration: BoxDecoration(
                            gradient: LinearGradient(
                              colors: [
                                Colors.black.withValues(alpha: 0.9),
                                Colors.black.withValues(alpha: 0.5),
                                Colors.transparent,
                              ],
                              begin: Alignment.bottomCenter,
                              end: Alignment.topCenter,
                            ),
                          ),
                        ),
                      ),

                      // Centered paused icon overlay
                      if (controller != null &&
                          isReady &&
                          !controller.value.isPlaying)
                        Container(
                          width: 70,
                          height: 70,
                          decoration: BoxDecoration(
                            color: Colors.black.withValues(alpha: 0.5),
                            shape: BoxShape.circle,
                            border: Border.all(
                              color: Colors.white.withValues(alpha: 0.1),
                            ),
                          ),
                          child: const Center(
                            child: Icon(
                              Icons.play_arrow_rounded,
                              size: 40,
                              color: Colors.white,
                            ),
                          ),
                        ),

                      // Bottom and Left text panels (creator, details)
                      Positioned(
                        bottom: 95,
                        left: 16,
                        right: 80,
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          mainAxisSize: MainAxisSize.min,
                          children: [
                            Container(
                              padding: const EdgeInsets.symmetric(
                                horizontal: 10,
                                vertical: 4,
                              ),
                              decoration: BoxDecoration(
                                color: theme.primary,
                                borderRadius: BorderRadius.circular(8),
                              ),
                              child: Text(
                                short.creator,
                                style: const TextStyle(
                                  fontSize: 12,
                                  fontWeight: FontWeight.w900,
                                  color: Colors.black,
                                ),
                              ),
                            ),
                            const SizedBox(height: 8),
                            Text(
                              short.title,
                              maxLines: 3,
                              overflow: TextOverflow.ellipsis,
                              style: const TextStyle(
                                fontSize: 13,
                                fontWeight: FontWeight.bold,
                                color: Colors.white,
                                height: 1.35,
                                shadows: [
                                  Shadow(
                                    color: Colors.black,
                                    blurRadius: 4,
                                    offset: Offset(0, 1.5),
                                  ),
                                ],
                              ),
                            ),
                          ],
                        ),
                      ),
                    ],
                  ),
                );
              },
            ),
          ),

          // Floating Top Left Header: "Shorts Arena"
          Positioned(
            top: 50,
            left: 20,
            child: Container(
              padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
              decoration: BoxDecoration(
                color: Colors.black.withValues(alpha: 0.4),
                borderRadius: BorderRadius.circular(16),
                border: Border.all(color: Colors.white.withValues(alpha: 0.08)),
              ),
              child: Row(
                children: [
                  const Icon(
                    Icons.radio_rounded,
                    color: Colors.redAccent,
                    size: 14,
                  ),
                  const SizedBox(width: 6),
                  Text(
                    "SHORTS ARENA",
                    style: TextStyle(
                      fontSize: 10,
                      fontWeight: FontWeight.w900,
                      letterSpacing: 1.5,
                      color: Colors.white.withValues(alpha: 0.9),
                    ),
                  ),
                ],
              ),
            ),
          ),

          // Floating Top Right Mute control
          Positioned(
            top: 50,
            right: 20,
            child: GestureDetector(
              onTap: _toggleMute,
              child: Container(
                padding: const EdgeInsets.all(10),
                decoration: BoxDecoration(
                  color: Colors.black.withValues(alpha: 0.4),
                  shape: BoxShape.circle,
                  border: Border.all(
                    color: Colors.white.withValues(alpha: 0.08),
                  ),
                ),
                child: Icon(
                  _isMuted ? Icons.volume_off_rounded : Icons.volume_up_rounded,
                  color: _isMuted ? Colors.white70 : theme.primary,
                  size: 20,
                ),
              ),
            ),
          ),

          // Floating Right Interaction Panel (Heart, Comment, Share)
          Positioned(
            right: 16,
            bottom: 120,
            child: Column(
              children: [
                // Heart Like
                _buildInteractionItem(
                  icon: Icons.favorite_rounded,
                  color: _likedIds.contains(_shortsData[_activeIndex].id)
                      ? Colors.redAccent
                      : Colors.white,
                  label: _likedIds.contains(_shortsData[_activeIndex].id)
                      ? "LIKED"
                      : _shortsData[_activeIndex].likes,
                  onTap: () => _toggleLike(_shortsData[_activeIndex].id),
                ),
                const SizedBox(height: 18),

                // Comment
                _buildInteractionItem(
                  icon: Icons.message_rounded,
                  color: Colors.white,
                  label: "${_shortsData[_activeIndex].comments}",
                  onTap: () {
                    ScaffoldMessenger.of(context).showSnackBar(
                      const SnackBar(
                        content: Text("Comments opening simulation..."),
                      ),
                    );
                  },
                ),
                const SizedBox(height: 18),

                // Share
                _buildInteractionItem(
                  icon: Icons.share_rounded,
                  color: Colors.white,
                  label: "SHARE",
                  onTap: () {
                    ScaffoldMessenger.of(context).showSnackBar(
                      const SnackBar(content: Text("Share link copied!")),
                    );
                  },
                ),
              ],
            ),
          ),

          // Vertical scroll navigation helper buttons
          Positioned(
            right: 18,
            top: MediaQuery.of(context).size.height * 0.35,
            child: Column(
              children: [
                GestureDetector(
                  onTap: _scrollPrev,
                  child: Opacity(
                    opacity: _activeIndex == 0 ? 0.2 : 0.7,
                    child: Container(
                      padding: const EdgeInsets.all(6),
                      decoration: const BoxDecoration(
                        color: Colors.black45,
                        shape: BoxShape.circle,
                      ),
                      child: const Icon(
                        Icons.keyboard_arrow_up_rounded,
                        color: Colors.white,
                        size: 20,
                      ),
                    ),
                  ),
                ),
                const SizedBox(height: 8),
                GestureDetector(
                  onTap: _scrollNext,
                  child: Opacity(
                    opacity: _activeIndex == _shortsData.length - 1 ? 0.2 : 0.7,
                    child: Container(
                      padding: const EdgeInsets.all(6),
                      decoration: const BoxDecoration(
                        color: Colors.black45,
                        shape: BoxShape.circle,
                      ),
                      child: const Icon(
                        Icons.keyboard_arrow_down_rounded,
                        color: Colors.white,
                        size: 20,
                      ),
                    ),
                  ),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildInteractionItem({
    required IconData icon,
    required Color color,
    required String label,
    required VoidCallback onTap,
  }) {
    return GestureDetector(
      onTap: onTap,
      child: Column(
        children: [
          Container(
            width: 48,
            height: 48,
            decoration: BoxDecoration(
              color: Colors.black.withValues(alpha: 0.45),
              shape: BoxShape.circle,
              border: Border.all(color: Colors.white.withValues(alpha: 0.06)),
            ),
            child: Center(child: Icon(icon, color: color, size: 22)),
          ),
          const SizedBox(height: 4),
          Text(
            label,
            style: const TextStyle(
              fontSize: 9,
              fontWeight: FontWeight.bold,
              color: Colors.white54,
              letterSpacing: 0.5,
            ),
          ),
        ],
      ),
    );
  }
}
