import 'package:flutter/material.dart';
import 'package:shared_preferences/shared_preferences.dart';
import '../theme/app_theme.dart';
import '../models/channel.dart';
import '../data/channels_data.dart';

class AppState with ChangeNotifier {
  late SharedPreferences _prefs;
  bool _isInitialized = false;

  // Local preferences
  AppThemeMode _currentThemeMode = AppThemeMode.toffeeOrange;
  String _username = "Toffee Guest";
  List<String> _favorites = ["sonymax-hd", "beinsports1", "loli-kids"];
  List<String> _watchHistory = [];
  bool _isMutedByDefault = true;
  bool _hideOfflineStreams = false;

  // Runtime GUI navigation/player state
  int _activeTabIdx = 0;
  ChannelModel? _activeChannel;
  int _activeStreamIdx = 0;
  String _searchQuery = "";

  // Getters
  bool get isInitialized => _isInitialized;
  AppThemeMode get currentThemeMode => _currentThemeMode;
  AppThemeData get currentTheme => AppThemeData.getTheme(_currentThemeMode);
  String get username => _username;
  List<String> get favorites => _favorites;
  List<String> get watchHistory => _watchHistory;
  bool get isMutedByDefault => _isMutedByDefault;
  bool get hideOfflineStreams => _hideOfflineStreams;

  int get activeTabIdx => _activeTabIdx;
  ChannelModel? get activeChannel => _activeChannel;
  int get activeStreamIdx => _activeStreamIdx;
  String get searchQuery => _searchQuery;

  // Helper getters
  List<ChannelModel> get favoriteChannels {
    return ChannelsData.allChannels
        .where((ch) => _favorites.contains(ch.id))
        .toList();
  }

  List<ChannelModel> get watchHistoryChannels {
    // Return watch history items in chronological order (most recent first)
    return _watchHistory
        .map(
          (id) => ChannelsData.allChannels.firstWhere(
            (ch) => ch.id == id,
            orElse: () => ChannelModel(
              id: '',
              name: '',
              shortName: '',
              logo: '',
              quality: '',
              streams: [],
            ),
          ),
        )
        .where((ch) => ch.id.isNotEmpty)
        .toList();
  }

  AppState() {
    _initPrefs();
  }

  Future<void> _initPrefs() async {
    _prefs = await SharedPreferences.getInstance();

    // Load theme
    final themeStr = _prefs.getString('iptv_theme') ?? 'toffeeOrange';
    _currentThemeMode = AppThemeMode.values.firstWhere(
      (e) => e.toString().split('.').last == themeStr,
      orElse: () => AppThemeMode.toffeeOrange,
    );

    // Load username
    _username = _prefs.getString('chat_username') ?? 'Toffee Guest';

    // Load favorites
    _favorites =
        _prefs.getStringList('iptv-favorites') ??
        ["sonymax-hd", "beinsports1", "loli-kids"];

    // Load history
    _watchHistory = _prefs.getStringList('iptv-history') ?? [];

    // Load settings
    _isMutedByDefault = _prefs.getBool('iptv_muted') ?? true;
    _hideOfflineStreams = _prefs.getBool('iptv_hide_offline') ?? false;

    _isInitialized = true;
    notifyListeners();
  }

  // Setters & Actions
  Future<void> setThemeMode(AppThemeMode mode) async {
    _currentThemeMode = mode;
    await _prefs.setString('iptv_theme', mode.toString().split('.').last);
    notifyListeners();
  }

  Future<void> setUsername(String name) async {
    if (name.trim().isEmpty) return;
    _username = name.trim();
    await _prefs.setString('chat_username', _username);
    notifyListeners();
  }

  Future<void> toggleFavorite(String channelId) async {
    if (_favorites.contains(channelId)) {
      _favorites.remove(channelId);
    } else {
      _favorites.add(channelId);
    }
    await _prefs.setStringList('iptv-favorites', _favorites);
    notifyListeners();
  }

  Future<void> setMutedByDefault(bool value) async {
    _isMutedByDefault = value;
    await _prefs.setBool('iptv_muted', value);
    notifyListeners();
  }

  Future<void> setHideOfflineStreams(bool value) async {
    _hideOfflineStreams = value;
    await _prefs.setBool('iptv_hide_offline', value);
    notifyListeners();
  }

  void selectTab(int index) {
    _activeTabIdx = index;
    // Clear search and active channel on bottom nav tab changes to reset navigation stack nicely
    if (index != 0) {
      _searchQuery = "";
    }
    notifyListeners();
  }

  void selectChannel(ChannelModel? channel) {
    _activeChannel = channel;
    _activeStreamIdx = 0;
    if (channel != null) {
      // Append to history
      _watchHistory.remove(channel.id);
      _watchHistory.insert(0, channel.id);
      if (_watchHistory.length > 15) {
        _watchHistory = _watchHistory.sublist(0, 15);
      }
      _prefs.setStringList('iptv-history', _watchHistory);
    }
    notifyListeners();
  }

  void selectStreamIdx(int index) {
    _activeStreamIdx = index;
    notifyListeners();
  }

  void updateSearchQuery(String query) {
    _searchQuery = query;
    notifyListeners();
  }

  void clearHistory() async {
    _watchHistory.clear();
    await _prefs.setStringList('iptv-history', []);
    notifyListeners();
  }
}
