import 'package:flutter/material.dart';
import 'package:video_player/video_player.dart';
import 'dart:async';
import 'dart:math';
import '../main.dart';
import '../models/channel.dart';
import '../models/stream.dart';
import '../data/channels_data.dart';
import 'home_screen.dart';

class PlayerScreen extends StatefulWidget {
  final ChannelModel channel;
  final VoidCallback onClose;

  const PlayerScreen({super.key, required this.channel, required this.onClose});

  @override
  State<PlayerScreen> createState() => _PlayerScreenState();
}

class _PlayerScreenState extends State<PlayerScreen> {
  VideoPlayerController? _controller;
  bool _isControllerLoading = true;
  bool _hasError = false;
  String _errorText = "";
  bool _showControls = true;
  bool _showStats = false;
  Timer? _hideControlsTimer;
  Timer? _watchdogTimer;

  // Stats for Nerds values (live simulation)
  late String _scpn;
  double _simBuffer = 4.2;
  double _simBitrate = 8.45;
  int _totalFrames = 0;
  int _droppedFrames = 0;
  Timer? _statsTimer;

  // Tabs: 0 = Fan Chat, 1 = Related Channels
  int _playerTabIdx = 0;

  // Chat variables
  final List<Map<String, dynamic>> _chatMessages = [];
  final TextEditingController _chatInputController = TextEditingController();
  final ScrollController _chatScrollController = ScrollController();
  Timer? _chatSimulatorTimer;

  // Stream references
  late List<StreamModel> _streams;

  @override
  void initState() {
    super.initState();
    _streams = widget.channel.streams.isNotEmpty
        ? widget.channel.streams
        : [StreamModel(label: 'Default', url: '')];

    // Generate static session hash for Stats for Nerds
    _scpn = _generateSCPN();

    // Initialize video stream
    _initVideoPlayer();

    // Start stats updates
    _startStatsTimer();

    // Load initial chat simulation
    _initChatMessages();
    _startChatSimulator();
  }

  @override
  void didUpdateWidget(PlayerScreen oldWidget) {
    super.didUpdateWidget(oldWidget);
    // If channel changed, reset player
    if (oldWidget.channel.id != widget.channel.id) {
      _streams = widget.channel.streams.isNotEmpty
          ? widget.channel.streams
          : [StreamModel(label: 'Default', url: '')];
      _initVideoPlayer();
      _initChatMessages();
    }
  }

  @override
  void dispose() {
    _disposePlayer();
    _hideControlsTimer?.cancel();
    _watchdogTimer?.cancel();
    _statsTimer?.cancel();
    _chatSimulatorTimer?.cancel();
    _chatInputController.dispose();
    _chatScrollController.dispose();
    super.dispose();
  }

  void _disposePlayer() {
    _controller?.removeListener(_playerListener);
    _controller?.dispose();
    _controller = null;
  }

  void _initVideoPlayer() {
    final state = AppStateProvider.of(context);
    final activeUrl = _streams[state.activeStreamIdx].url;

    setState(() {
      _isControllerLoading = true;
      _hasError = false;
      _errorText = "";
    });

    _disposePlayer();
    _watchdogTimer?.cancel();

    // Set up watchdog loading timeout (10 seconds)
    _watchdogTimer = Timer(const Duration(seconds: 10), () {
      if (_isControllerLoading && mounted) {
        _triggerStreamFallback("Loading timeout expired.");
      }
    });

    if (activeUrl.isEmpty) {
      setState(() {
        _isControllerLoading = false;
        _hasError = true;
        _errorText = "Invalid empty stream URL.";
      });
      return;
    }

    try {
      _controller = VideoPlayerController.networkUrl(Uri.parse(activeUrl))
        ..initialize()
            .then((_) {
              if (!mounted) return;
              _watchdogTimer?.cancel();
              setState(() {
                _isControllerLoading = false;
              });

              // Apply initial audio settings
              _controller!.setVolume(state.isMutedByDefault ? 0.0 : 1.0);
              _controller!.play();

              // Auto hide controls after 4 seconds
              _startHideControlsTimer();
            })
            .catchError((err) {
              _watchdogTimer?.cancel();
              _triggerStreamFallback(err.toString());
            });

      _controller!.addListener(_playerListener);
    } catch (e) {
      _watchdogTimer?.cancel();
      _triggerStreamFallback(e.toString());
    }
  }

  void _playerListener() {
    if (_controller == null) return;
    if (_controller!.value.hasError) {
      _triggerStreamFallback(
        _controller!.value.errorDescription ?? "Playback error.",
      );
    }
  }

  // Backup Stream Fallback triggers
  void _triggerStreamFallback(String reason) {
    if (!mounted) return;
    final state = AppStateProvider.of(context);

    // Try fallback streams
    if (state.activeStreamIdx + 1 < _streams.length) {
      final nextIdx = state.activeStreamIdx + 1;
      state.selectStreamIdx(nextIdx);

      // Toast notification
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text(
            "Stream Node failed. Reconnecting to Backup Server (${nextIdx + 1})...",
          ),
          backgroundColor: Colors.orange.shade800,
          duration: const Duration(seconds: 3),
        ),
      );

      // Re-init player with next stream index
      _initVideoPlayer();
    } else {
      setState(() {
        _isControllerLoading = false;
        _hasError = true;
        _errorText = "Stream connection lost. End of available servers.";
      });
    }
  }

  void _startHideControlsTimer() {
    _hideControlsTimer?.cancel();
    _hideControlsTimer = Timer(const Duration(seconds: 4), () {
      if (mounted) {
        setState(() {
          _showControls = false;
        });
      }
    });
  }

  void _toggleControls() {
    setState(() {
      _showControls = !_showControls;
    });
    if (_showControls) {
      _startHideControlsTimer();
    } else {
      _hideControlsTimer?.cancel();
    }
  }

  void _startStatsTimer() {
    final rand = Random();
    _statsTimer = Timer.periodic(const Duration(seconds: 2), (timer) {
      if (!mounted || _controller == null || !_controller!.value.isInitialized)
        return;
      setState(() {
        // Simulate fluctuating network stats
        _simBuffer = 2.0 + rand.nextDouble() * 8.0;
        _simBitrate = 5.0 + rand.nextDouble() * 6.0;
        _totalFrames += 60;
        if (rand.nextDouble() < 0.05) {
          _droppedFrames += rand.nextInt(3);
        }
      });
    });
  }

  // Chat lists builder
  void _initChatMessages() {
    _chatMessages.clear();
    final random = Random();
    final names = [
      "AlphaStream",
      "KonaGamer",
      "LiveSeeker",
      "SlickPlayer",
      "BD_Watcher",
      "TechGuru",
    ];
    final colors = [
      Colors.red,
      Colors.blue,
      Colors.green,
      Colors.orange,
      Colors.purple,
      Colors.cyan,
    ];

    // Seed initial messages based on channel category
    final initialTexts = _getCategoryChatTexts(widget.channel.category ?? '');

    for (int i = 0; i < 6; i++) {
      final nameIdx = random.nextInt(names.length);
      final textIdx = random.nextInt(initialTexts.length);
      _chatMessages.add({
        "id": "chat-init-$i",
        "user": names[nameIdx],
        "text": initialTexts[textIdx],
        "color": colors[nameIdx],
        "isSelf": false,
      });
    }
  }

  void _startChatSimulator() {
    final random = Random();
    final names = [
      "ProStreamer",
      "AlexBD",
      "SportsFanatic",
      "NostalgiaKid",
      "GlobalVoice",
      "FaithSeeker",
      "Techy",
    ];
    final colors = [
      Colors.redAccent,
      Colors.blueAccent,
      Colors.lightGreen,
      Colors.orangeAccent,
      Colors.purpleAccent,
      Colors.cyanAccent,
    ];

    _chatSimulatorTimer = Timer.periodic(const Duration(seconds: 6), (timer) {
      if (!mounted) return;
      final nameIdx = random.nextInt(names.length);
      final texts = _getCategoryChatTexts(widget.channel.category ?? '');
      final textIdx = random.nextInt(texts.length);

      setState(() {
        _chatMessages.add({
          "id": "chat-sim-${DateTime.now().millisecondsSinceEpoch}",
          "user": names[nameIdx],
          "text": texts[textIdx],
          "color": colors[nameIdx],
          "isSelf": false,
        });
      });

      _scrollToBottom();
    });
  }

  List<String> _getCategoryChatTexts(String category) {
    final cat = category.toLowerCase();
    if (cat.contains("sport")) {
      return [
        "OMG What a shot! 🏏⚽",
        "Is this full HD? Looks crispy!",
        "Who is winning the match?",
        "This player is in insane form!",
        "Come on team, let's win this!",
        "F1 speeds are getting crazy",
        "Watching live from Dhaka!",
      ];
    } else if (cat.contains("news")) {
      return [
        "What is the current situation?",
        "Thanks for the 24/7 update.",
        "Al Jazeera always delivers quality news.",
        "Clear and transparent coverage.",
        "Watching from Chittagong.",
        "Big breaking news happening.",
      ];
    } else if (cat.contains("kid") || cat.contains("cartoon")) {
      return [
        "Haha Gopal Bhar is the goat! 🍉",
        "Nostalgia hitting hard right now",
        "My children love this channel",
        "Duronto TV has the best kids programs",
        "Goraemon episodes are awesome",
      ];
    } else if (cat.contains("religious") || cat.contains("islamic")) {
      return [
        "Subhan Allah! Beautiful recitation.",
        "Jazak Allah Khair for this live feed.",
        "Listening to the Makkah live stream feels so peaceful.",
        "Very informative lecture.",
        "Assalamu Alaikum everyone.",
      ];
    } else {
      return [
        "Nice playback quality!",
        "The stream is running smooth.",
        "TNTV UI is so clean now! Wow",
        "No buffering at all. Excellent servers.",
        "Hello from Sylhet!",
        "Love this general drama channel.",
      ];
    }
  }

  void _sendChatMessage() {
    final state = AppStateProvider.of(context);
    final text = _chatInputController.text.trim();
    if (text.isEmpty) return;

    setState(() {
      _chatMessages.add({
        "id": "chat-self-${DateTime.now().millisecondsSinceEpoch}",
        "user": state.username,
        "text": text,
        "color": state.currentTheme.primary,
        "isSelf": true,
      });
      _chatInputController.clear();
    });

    _scrollToBottom();
  }

  void _scrollToBottom() {
    WidgetsBinding.instance.addPostFrameCallback((_) {
      if (_chatScrollController.hasClients) {
        _chatScrollController.animateTo(
          _chatScrollController.position.maxScrollExtent,
          duration: const Duration(milliseconds: 300),
          curve: Curves.easeOut,
        );
      }
    });
  }

  String _generateSCPN() {
    const chars =
        'abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789';
    final r = Random();
    return List.generate(16, (index) => chars[r.nextInt(chars.length)]).join();
  }

  @override
  Widget build(BuildContext context) {
    final state = AppStateProvider.of(context);
    final theme = state.currentTheme;

    // Filter related channels (channels in the same category)
    final relatedChannels = ChannelsData.allChannels
        .where(
          (ch) =>
              ch.category == widget.channel.category &&
              ch.id != widget.channel.id,
        )
        .toList();

    return Container(
      color: Colors.black,
      child: SafeArea(
        child: Column(
          children: [
            // 1. Aspect Ratio Live Video Player Frame
            AspectRatio(
              aspectRatio: 16 / 9,
              child: GestureDetector(
                onTap: _toggleControls,
                child: Container(
                  color: Colors.black,
                  child: Stack(
                    alignment: Alignment.center,
                    children: [
                      // Video output node
                      if (_controller != null &&
                          _controller!.value.isInitialized &&
                          !_hasError)
                        Positioned.fill(child: VideoPlayer(_controller!)),

                      // Stream loading status indicator
                      if (_isControllerLoading)
                        CircularProgressIndicator(color: theme.primary),

                      // Playback error overlay
                      if (_hasError)
                        Container(
                          color: Colors.black87,
                          padding: const EdgeInsets.all(20),
                          child: Column(
                            mainAxisAlignment: MainAxisAlignment.center,
                            children: [
                              const Icon(
                                Icons.error_outline_rounded,
                                color: Colors.redAccent,
                                size: 40,
                              ),
                              const SizedBox(height: 12),
                              Text(
                                _errorText,
                                style: const TextStyle(
                                  color: Colors.white,
                                  fontSize: 13,
                                  fontWeight: FontWeight.bold,
                                ),
                                textAlign: TextAlign.center,
                              ),
                              const SizedBox(height: 16),
                              ElevatedButton.icon(
                                onPressed: _initVideoPlayer,
                                icon: const Icon(
                                  Icons.refresh_rounded,
                                  size: 16,
                                ),
                                label: const Text("RETRY NODE"),
                                style: ElevatedButton.styleFrom(
                                  backgroundColor: theme.primary,
                                  foregroundColor: Colors.black,
                                  shape: RoundedRectangleBorder(
                                    borderRadius: BorderRadius.circular(10),
                                  ),
                                ),
                              ),
                            ],
                          ),
                        ),

                      // Player Stats for Nerds Panel Overlay
                      if (_showStats)
                        Positioned(
                          left: 10,
                          top: 10,
                          child: Container(
                            width: 220,
                            padding: const EdgeInsets.all(10),
                            decoration: BoxDecoration(
                              color: Colors.black.withValues(alpha: 0.85),
                              borderRadius: BorderRadius.circular(12),
                              border: Border.all(
                                color: Colors.white.withValues(alpha: 0.12),
                              ),
                            ),
                            child: Column(
                              crossAxisAlignment: CrossAxisAlignment.start,
                              mainAxisSize: MainAxisSize.min,
                              children: [
                                Row(
                                  children: [
                                    Icon(
                                      Icons.analytics_rounded,
                                      color: theme.primary,
                                      size: 14,
                                    ),
                                    const SizedBox(width: 6),
                                    const Text(
                                      "Stats for Nerds",
                                      style: TextStyle(
                                        color: Colors.white,
                                        fontSize: 10,
                                        fontWeight: FontWeight.bold,
                                      ),
                                    ),
                                    const Spacer(),
                                    GestureDetector(
                                      onTap: () =>
                                          setState(() => _showStats = false),
                                      child: const Icon(
                                        Icons.close_rounded,
                                        color: Colors.white54,
                                        size: 14,
                                      ),
                                    ),
                                  ],
                                ),
                                const Divider(
                                  color: Colors.white24,
                                  height: 10,
                                ),
                                _buildStatRow(
                                  "Resolution",
                                  _controller != null &&
                                          _controller!.value.isInitialized
                                      ? "${_controller!.value.size.width.toInt()}x${_controller!.value.size.height.toInt()}"
                                      : "0x0",
                                ),
                                _buildStatRow(
                                  "Buffer Health",
                                  "${_simBuffer.toStringAsFixed(1)}s",
                                ),
                                _buildStatRow(
                                  "Bandwidth Est.",
                                  "${_simBitrate.toStringAsFixed(2)} Mbps",
                                ),
                                _buildStatRow(
                                  "Dropped Frames",
                                  "$_droppedFrames / $_totalFrames",
                                ),
                                _buildStatRow(
                                  "Playback Latency",
                                  "2.4s (Live)",
                                ),
                                _buildStatRow(
                                  "Player Engine",
                                  "ExoPlayer (HLS Live)",
                                ),
                                _buildStatRow("SCPN", _scpn),
                              ],
                            ),
                          ),
                        ),

                      // Premium Custom Controls Overlay layer
                      if (_showControls && !_isControllerLoading)
                        Positioned.fill(
                          child: Container(
                            color: Colors.black45,
                            child: Column(
                              children: [
                                // Top controls bar
                                Padding(
                                  padding: const EdgeInsets.all(10.0),
                                  child: Row(
                                    children: [
                                      IconButton(
                                        icon: const Icon(
                                          Icons.arrow_back_rounded,
                                          color: Colors.white,
                                        ),
                                        onPressed: widget.onClose,
                                      ),
                                      const SizedBox(width: 8),
                                      Container(
                                        width: 28,
                                        height: 28,
                                        padding: const EdgeInsets.all(4),
                                        decoration: theme.glassDecoration(
                                          radius: 6,
                                        ),
                                        child: ChannelLogo(
                                          path: widget.channel.logo,
                                          size: 20,
                                        ),
                                      ),
                                      const SizedBox(width: 8),
                                      Text(
                                        widget.channel.name,
                                        style: const TextStyle(
                                          color: Colors.white,
                                          fontWeight: FontWeight.bold,
                                          fontSize: 14,
                                        ),
                                      ),
                                    ],
                                  ),
                                ),

                                const Spacer(),

                                // Center Play/Pause button
                                if (!_hasError)
                                  IconButton(
                                    icon: Icon(
                                      _controller != null &&
                                              _controller!.value.isPlaying
                                          ? Icons.pause_rounded
                                          : Icons.play_arrow_rounded,
                                      size: 55,
                                      color: Colors.white,
                                    ),
                                    onPressed: () {
                                      if (_controller == null) return;
                                      setState(() {
                                        if (_controller!.value.isPlaying) {
                                          _controller!.pause();
                                        } else {
                                          _controller!.play();
                                        }
                                      });
                                      _startHideControlsTimer();
                                    },
                                  ),

                                const Spacer(),

                                // Bottom controls bar
                                Padding(
                                  padding: const EdgeInsets.symmetric(
                                    horizontal: 14,
                                    vertical: 10,
                                  ),
                                  child: Row(
                                    children: [
                                      // Mute/Volume toggles
                                      IconButton(
                                        icon: Icon(
                                          _controller != null &&
                                                  _controller!.value.volume > 0
                                              ? Icons.volume_up_rounded
                                              : Icons.volume_off_rounded,
                                          color: Colors.white,
                                          size: 20,
                                        ),
                                        onPressed: () {
                                          if (_controller == null) return;
                                          setState(() {
                                            final isMuted =
                                                _controller!.value.volume == 0;
                                            _controller!.setVolume(
                                              isMuted ? 1.0 : 0.0,
                                            );
                                          });
                                          _startHideControlsTimer();
                                        },
                                      ),

                                      // LIVE Indicator with Pulse
                                      const Icon(
                                        Icons.circle,
                                        color: Colors.red,
                                        size: 8,
                                      ),
                                      const SizedBox(width: 4),
                                      const Text(
                                        "LIVE",
                                        style: TextStyle(
                                          color: Colors.white,
                                          fontSize: 10,
                                          fontWeight: FontWeight.w900,
                                          letterSpacing: 0.5,
                                        ),
                                      ),

                                      const Spacer(),

                                      // Settings / Quality options selector
                                      IconButton(
                                        icon: const Icon(
                                          Icons.settings_rounded,
                                          color: Colors.white,
                                          size: 20,
                                        ),
                                        onPressed: () {
                                          _showQualityPicker(context);
                                          _startHideControlsTimer();
                                        },
                                      ),

                                      // Stats for Nerds toggle
                                      IconButton(
                                        icon: Icon(
                                          Icons.analytics_rounded,
                                          color: _showStats
                                              ? theme.primary
                                              : Colors.white,
                                          size: 20,
                                        ),
                                        onPressed: () {
                                          setState(() {
                                            _showStats = !_showStats;
                                          });
                                          _startHideControlsTimer();
                                        },
                                      ),
                                    ],
                                  ),
                                ),
                              ],
                            ),
                          ),
                        ),
                    ],
                  ),
                ),
              ),
            ),

            // 2. Interactive Details Bar (Favorites, Share)
            Container(
              padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
              decoration: BoxDecoration(
                color: theme.surface,
                border: Border(
                  bottom: BorderSide(
                    color: Colors.white.withValues(alpha: 0.05),
                  ),
                ),
              ),
              child: Row(
                children: [
                  Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        widget.channel.name,
                        style: TextStyle(
                          color: theme.textPrimary,
                          fontSize: 16,
                          fontWeight: FontWeight.bold,
                        ),
                      ),
                      const SizedBox(height: 2),
                      Text(
                        "${widget.channel.category} • Quality: ${widget.channel.quality}",
                        style: TextStyle(
                          color: theme.textSecondary.withValues(alpha: 0.7),
                          fontSize: 11,
                        ),
                      ),
                    ],
                  ),
                  const Spacer(),

                  // Favorite toggle button
                  IconButton(
                    icon: Icon(
                      state.favorites.contains(widget.channel.id)
                          ? Icons.star_rounded
                          : Icons.star_border_rounded,
                      color: state.favorites.contains(widget.channel.id)
                          ? theme.primary
                          : theme.textSecondary,
                    ),
                    onPressed: () => state.toggleFavorite(widget.channel.id),
                  ),

                  // Share button
                  IconButton(
                    icon: Icon(Icons.share_rounded, color: theme.textSecondary),
                    onPressed: () {
                      ScaffoldMessenger.of(context).showSnackBar(
                        const SnackBar(
                          content: Text("Link copied to clipboard!"),
                        ),
                      );
                    },
                  ),
                ],
              ),
            ),

            // 3. Tab Switches (Live Chat vs Related Channels)
            Container(
              color: theme.surface,
              child: Row(
                children: [
                  _buildTabButton(context, 0, "Fan Live Chat"),
                  _buildTabButton(context, 1, "Related Nodes"),
                ],
              ),
            ),

            // 4. Tab Views
            Expanded(
              child: Container(
                color: theme.background,
                child: _playerTabIdx == 0
                    ? _buildChatTabView(context)
                    : _buildRelatedTabView(context, relatedChannels),
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildTabButton(BuildContext context, int index, String label) {
    final state = AppStateProvider.of(context);
    final theme = state.currentTheme;
    final isSelected = _playerTabIdx == index;

    return Expanded(
      child: GestureDetector(
        onTap: () => setState(() => _playerTabIdx = index),
        child: Container(
          padding: const EdgeInsets.symmetric(vertical: 14),
          decoration: BoxDecoration(
            border: Border(
              bottom: BorderSide(
                color: isSelected ? theme.primary : Colors.transparent,
                width: 2.0,
              ),
            ),
          ),
          child: Center(
            child: Text(
              label,
              style: TextStyle(
                fontSize: 12.5,
                fontWeight: isSelected ? FontWeight.bold : FontWeight.w600,
                color: isSelected ? theme.primary : theme.textSecondary,
              ),
            ),
          ),
        ),
      ),
    );
  }

  Widget _buildChatTabView(BuildContext context) {
    final state = AppStateProvider.of(context);
    final theme = state.currentTheme;

    return Column(
      children: [
        // Chat messages scrolling view
        Expanded(
          child: ListView.builder(
            controller: _chatScrollController,
            padding: const EdgeInsets.fromLTRB(16, 12, 16, 20),
            itemCount: _chatMessages.length,
            itemBuilder: (context, idx) {
              final msg = _chatMessages[idx];
              final isSelf = msg["isSelf"] as bool? ?? false;

              return Padding(
                padding: const EdgeInsets.symmetric(vertical: 6),
                child: Row(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    // Chat profile letter avatar
                    Container(
                      width: 24,
                      height: 24,
                      decoration: BoxDecoration(
                        shape: BoxShape.circle,
                        color: msg["color"] as Color,
                      ),
                      child: Center(
                        child: Text(
                          msg["user"][0].toUpperCase(),
                          style: const TextStyle(
                            fontSize: 11,
                            fontWeight: FontWeight.bold,
                            color: Colors.white,
                          ),
                        ),
                      ),
                    ),
                    const SizedBox(width: 8),

                    // User Message bubbles
                    Expanded(
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Row(
                            children: [
                              Text(
                                msg["user"],
                                style: TextStyle(
                                  fontSize: 11,
                                  fontWeight: FontWeight.bold,
                                  color: isSelf
                                      ? theme.primary
                                      : Colors.white70,
                                ),
                              ),
                              if (isSelf) ...[
                                const SizedBox(width: 4),
                                Container(
                                  padding: const EdgeInsets.symmetric(
                                    horizontal: 4,
                                    vertical: 1,
                                  ),
                                  decoration: BoxDecoration(
                                    color: theme.primary.withValues(
                                      alpha: 0.15,
                                    ),
                                    borderRadius: BorderRadius.circular(4),
                                  ),
                                  child: Text(
                                    "YOU",
                                    style: TextStyle(
                                      fontSize: 7,
                                      fontWeight: FontWeight.w900,
                                      color: theme.primary,
                                    ),
                                  ),
                                ),
                              ],
                            ],
                          ),
                          const SizedBox(height: 2),
                          Text(
                            msg["text"],
                            style: TextStyle(
                              fontSize: 12,
                              color: theme.textPrimary,
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

        // Chat Input box
        Container(
          padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
          decoration: BoxDecoration(
            color: theme.surface,
            border: Border(
              top: BorderSide(color: Colors.white.withValues(alpha: 0.05)),
            ),
          ),
          child: Row(
            children: [
              Expanded(
                child: Container(
                  decoration: theme.glassDecoration(radius: 12),
                  padding: const EdgeInsets.symmetric(horizontal: 12),
                  child: TextField(
                    controller: _chatInputController,
                    onSubmitted: (_) => _sendChatMessage(),
                    style: TextStyle(color: theme.textPrimary, fontSize: 13),
                    decoration: InputDecoration(
                      hintText: "Say something in fan chat...",
                      hintStyle: TextStyle(
                        color: theme.textSecondary.withValues(alpha: 0.4),
                      ),
                      border: InputBorder.none,
                      contentPadding: const EdgeInsets.symmetric(vertical: 10),
                    ),
                  ),
                ),
              ),
              const SizedBox(width: 8),
              IconButton(
                icon: Icon(Icons.send_rounded, color: theme.primary),
                onPressed: _sendChatMessage,
              ),
            ],
          ),
        ),
      ],
    );
  }

  Widget _buildRelatedTabView(
    BuildContext context,
    List<ChannelModel> related,
  ) {
    final state = AppStateProvider.of(context);
    final theme = state.currentTheme;

    if (related.isEmpty) {
      return Center(
        child: Text(
          "No related channels found.",
          style: TextStyle(
            color: theme.textSecondary.withValues(alpha: 0.7),
            fontSize: 13,
          ),
        ),
      );
    }

    return ListView.builder(
      padding: const EdgeInsets.fromLTRB(16, 12, 16, 30),
      itemCount: related.length,
      itemBuilder: (context, idx) {
        final ch = related[idx];
        return Card(
          margin: const EdgeInsets.symmetric(vertical: 6),
          color: theme.surface.withValues(alpha: 0.5),
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(14),
          ),
          child: ListTile(
            leading: Container(
              width: 38,
              height: 38,
              padding: const EdgeInsets.all(4),
              decoration: theme.glassDecoration(radius: 8),
              child: ChannelLogo(path: ch.logo, size: 28),
            ),
            title: Text(
              ch.name,
              style: TextStyle(
                color: theme.textPrimary,
                fontSize: 13,
                fontWeight: FontWeight.bold,
              ),
            ),
            subtitle: Text(
              ch.category ?? '',
              style: TextStyle(color: theme.textSecondary, fontSize: 10),
            ),
            trailing: Container(
              padding: const EdgeInsets.symmetric(horizontal: 6, vertical: 2),
              decoration: BoxDecoration(
                color: theme.primary.withValues(alpha: 0.1),
                borderRadius: BorderRadius.circular(6),
              ),
              child: Text(
                ch.quality,
                style: TextStyle(
                  fontSize: 8,
                  fontWeight: FontWeight.bold,
                  color: theme.primary,
                ),
              ),
            ),
            onTap: () {
              // Select related channel
              state.selectChannel(ch);
            },
          ),
        );
      },
    );
  }

  void _showQualityPicker(BuildContext context) {
    final state = AppStateProvider.of(context);
    final theme = state.currentTheme;

    showModalBottomSheet(
      context: context,
      backgroundColor: theme.surface,
      shape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.vertical(top: Radius.circular(20)),
      ),
      builder: (context) {
        return SafeArea(
          child: Padding(
            padding: const EdgeInsets.symmetric(vertical: 16),
            child: Column(
              mainAxisSize: MainAxisSize.min,
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                const Padding(
                  padding: EdgeInsets.symmetric(horizontal: 20, vertical: 8),
                  child: Text(
                    "Select Broadcast Stream Node",
                    style: TextStyle(
                      color: Colors.white,
                      fontSize: 15,
                      fontWeight: FontWeight.bold,
                    ),
                  ),
                ),
                const Divider(color: Colors.white24),
                for (int i = 0; i < _streams.length; i++)
                  ListTile(
                    leading: Icon(
                      Icons.play_circle_filled_rounded,
                      color: state.activeStreamIdx == i
                          ? theme.primary
                          : Colors.white30,
                    ),
                    title: Text(
                      _streams[i].label,
                      style: TextStyle(
                        color: state.activeStreamIdx == i
                            ? theme.primary
                            : Colors.white.withValues(alpha: 0.9),
                        fontWeight: state.activeStreamIdx == i
                            ? FontWeight.bold
                            : FontWeight.normal,
                        fontSize: 13.5,
                      ),
                    ),
                    trailing: state.activeStreamIdx == i
                        ? Icon(Icons.check_rounded, color: theme.primary)
                        : null,
                    onTap: () {
                      state.selectStreamIdx(i);
                      Navigator.pop(context);
                      _initVideoPlayer();
                    },
                  ),
              ],
            ),
          ),
        );
      },
    );
  }

  Widget _buildStatRow(String label, String value) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 3),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          Text(
            label,
            style: const TextStyle(color: Colors.white54, fontSize: 9.5),
          ),
          Text(
            value,
            style: TextStyle(
              color: Colors.white.withValues(alpha: 0.9),
              fontSize: 9.5,
              fontWeight: FontWeight.bold,
              fontFamily: 'monospace',
            ),
          ),
        ],
      ),
    );
  }
}
