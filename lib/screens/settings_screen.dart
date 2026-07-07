import 'package:flutter/material.dart';
import '../main.dart';
import '../theme/app_theme.dart';

class SettingsScreen extends StatefulWidget {
  const SettingsScreen({super.key});

  @override
  State<SettingsScreen> createState() => _SettingsScreenState();
}

class _SettingsScreenState extends State<SettingsScreen> {
  late TextEditingController _nameController;
  bool _isEditingName = false;

  @override
  void initState() {
    super.initState();
    _nameController = TextEditingController();
  }

  @override
  void didChangeDependencies() {
    super.didChangeDependencies();
    // Pre-populate name controller on init/dependencies load
    final state = AppStateProvider.of(context);
    _nameController.text = state.username;
  }

  @override
  void dispose() {
    _nameController.dispose();
    super.dispose();
  }

  void _saveUsername() {
    final state = AppStateProvider.of(context);
    if (_nameController.text.trim().isNotEmpty) {
      state.setUsername(_nameController.text.trim());
      setState(() {
        _isEditingName = false;
      });
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text("Username updated to ${_nameController.text.trim()}!"),
          duration: const Duration(seconds: 2),
        ),
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    final state = AppStateProvider.of(context);
    final theme = state.currentTheme;

    return Container(
      width: double.infinity,
      height: double.infinity,
      decoration: BoxDecoration(gradient: theme.bgGradient),
      child: SafeArea(
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            // Screen Title Header
            Padding(
              padding: const EdgeInsets.fromLTRB(20, 20, 20, 10),
              child: ShaderMask(
                shaderCallback: (bounds) =>
                    theme.accentGradient.createShader(bounds),
                child: const Text(
                  "Profile & Settings",
                  style: TextStyle(
                    fontSize: 26,
                    fontWeight: FontWeight.w900,
                    color: Colors.white,
                    letterSpacing: 0.8,
                  ),
                ),
              ),
            ),

            // Scrollable Content Layout
            Expanded(
              child: ListView(
                padding: const EdgeInsets.fromLTRB(16, 12, 16, 110),
                children: [
                  // 1. Profile Username Card
                  _buildSectionHeader("Profile Customization"),
                  Container(
                    padding: const EdgeInsets.all(16),
                    decoration: theme.glassDecoration(radius: 20),
                    child: Row(
                      children: [
                        // Avatar glow
                        Container(
                          width: 50,
                          height: 50,
                          decoration: BoxDecoration(
                            shape: BoxShape.circle,
                            gradient: theme.accentGradient,
                            boxShadow: [
                              BoxShadow(
                                color: theme.primary.withValues(alpha: 0.2),
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
                                fontSize: 20,
                                fontWeight: FontWeight.bold,
                                color: Colors.white,
                              ),
                            ),
                          ),
                        ),
                        const SizedBox(width: 16),

                        // Text input field
                        Expanded(
                          child: _isEditingName
                              ? TextField(
                                  controller: _nameController,
                                  style: TextStyle(
                                    color: theme.textPrimary,
                                    fontSize: 14,
                                  ),
                                  autofocus: true,
                                  decoration: InputDecoration(
                                    hintText: "Enter username...",
                                    hintStyle: const TextStyle(
                                      color: Colors.white24,
                                    ),
                                    border: UnderlineInputBorder(
                                      borderSide: BorderSide(
                                        color: theme.primary,
                                      ),
                                    ),
                                    suffixIcon: IconButton(
                                      icon: const Icon(
                                        Icons.check_rounded,
                                        color: Colors.greenAccent,
                                      ),
                                      onPressed: _saveUsername,
                                    ),
                                  ),
                                  onSubmitted: (_) => _saveUsername(),
                                )
                              : Column(
                                  crossAxisAlignment: CrossAxisAlignment.start,
                                  children: [
                                    Text(
                                      state.username,
                                      style: TextStyle(
                                        color: theme.textPrimary,
                                        fontSize: 16,
                                        fontWeight: FontWeight.bold,
                                      ),
                                    ),
                                    const SizedBox(height: 2),
                                    Text(
                                      "Active chat alias",
                                      style: TextStyle(
                                        color: theme.textSecondary.withValues(
                                          alpha: 0.7,
                                        ),
                                        fontSize: 11,
                                      ),
                                    ),
                                  ],
                                ),
                        ),

                        // Edit Trigger button
                        if (!_isEditingName)
                          IconButton(
                            icon: Icon(
                              Icons.edit_rounded,
                              color: theme.primary,
                              size: 20,
                            ),
                            onPressed: () {
                              setState(() {
                                _isEditingName = true;
                              });
                            },
                          ),
                      ],
                    ),
                  ),
                  const SizedBox(height: 24),

                  // 2. Dynamic Theme Grid Swapper
                  _buildSectionHeader("Select Premium App Theme"),
                  GridView.count(
                    crossAxisCount: 2,
                    shrinkWrap: true,
                    physics: const NeverScrollableScrollPhysics(),
                    crossAxisSpacing: 12,
                    mainAxisSpacing: 12,
                    childAspectRatio: 1.6,
                    children: [
                      _buildThemeCard(AppThemeMode.toffeeOrange),
                      _buildThemeCard(AppThemeMode.darkNeon),
                      _buildThemeCard(AppThemeMode.royalRed),
                      _buildThemeCard(AppThemeMode.nordicBlue),
                      _buildThemeCard(AppThemeMode.forestGreen),
                    ],
                  ),
                  const SizedBox(height: 24),

                  // 3. Playback Preferences Card
                  _buildSectionHeader("Player Preferences"),
                  Container(
                    padding: const EdgeInsets.symmetric(
                      horizontal: 16,
                      vertical: 4,
                    ),
                    decoration: theme.glassDecoration(radius: 20),
                    child: Column(
                      children: [
                        SwitchListTile(
                          value: state.isMutedByDefault,
                          title: const Text(
                            "Muted by Default",
                            style: TextStyle(
                              fontSize: 13.5,
                              fontWeight: FontWeight.bold,
                            ),
                          ),
                          subtitle: const Text(
                            "Always start video streams with volume muted",
                            style: TextStyle(fontSize: 11),
                          ),
                          activeThumbColor: theme.primary,
                          contentPadding: EdgeInsets.zero,
                          onChanged: (val) => state.setMutedByDefault(val),
                        ),
                        Divider(color: Colors.white.withValues(alpha: 0.05)),
                        SwitchListTile(
                          value: state.hideOfflineStreams,
                          title: const Text(
                            "Hide Offline Streams",
                            style: TextStyle(
                              fontSize: 13.5,
                              fontWeight: FontWeight.bold,
                            ),
                          ),
                          subtitle: const Text(
                            "Filter out channels currently reported offline",
                            style: TextStyle(fontSize: 11),
                          ),
                          activeThumbColor: theme.primary,
                          contentPadding: EdgeInsets.zero,
                          onChanged: (val) => state.setHideOfflineStreams(val),
                        ),
                      ],
                    ),
                  ),
                  const SizedBox(height: 24),

                  // 4. System Specs Details Card
                  _buildSectionHeader("App Telemetry & License"),
                  Container(
                    padding: const EdgeInsets.all(16),
                    decoration: theme.glassDecoration(radius: 20),
                    child: Column(
                      children: [
                        _buildSpecRow("Version", "v2.0.0 (Flutter Edition)"),
                        Divider(
                          color: Colors.white.withValues(alpha: 0.05),
                          height: 16,
                        ),
                        _buildSpecRow("Framework", "Flutter 3.44.5 stable"),
                        Divider(
                          color: Colors.white.withValues(alpha: 0.05),
                          height: 16,
                        ),
                        _buildSpecRow("Engine", "DartVM JIT Core"),
                        Divider(
                          color: Colors.white.withValues(alpha: 0.05),
                          height: 16,
                        ),
                        _buildSpecRow(
                          "HLS player",
                          "Native VideoPlayer ExoPlayer",
                        ),
                      ],
                    ),
                  ),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildSectionHeader(String label) {
    return Padding(
      padding: const EdgeInsets.only(left: 4, bottom: 10),
      child: Text(
        label,
        style: const TextStyle(
          fontSize: 14,
          fontWeight: FontWeight.bold,
          color: Colors.white70,
          letterSpacing: 0.5,
        ),
      ),
    );
  }

  Widget _buildThemeCard(AppThemeMode mode) {
    final state = AppStateProvider.of(context);
    final activeTheme = state.currentTheme;
    final itemTheme = AppThemeData.getTheme(mode);
    final isSelected = state.currentThemeMode == mode;

    return GestureDetector(
      onTap: () => state.setThemeMode(mode),
      child: Container(
        decoration: BoxDecoration(
          color: itemTheme.surface.withValues(alpha: 0.7),
          borderRadius: BorderRadius.circular(16),
          border: Border.all(
            color: isSelected
                ? activeTheme.primary
                : Colors.white.withValues(alpha: 0.06),
            width: isSelected ? 2.0 : 1.0,
          ),
          boxShadow: isSelected ? activeTheme.glowShadow : null,
        ),
        child: Stack(
          children: [
            // Preview details (color circles)
            Padding(
              padding: const EdgeInsets.all(12),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  Text(
                    itemTheme.name,
                    style: const TextStyle(
                      fontSize: 12.5,
                      fontWeight: FontWeight.bold,
                      color: Colors.white,
                    ),
                  ),
                  const SizedBox(height: 10),
                  Row(
                    children: [
                      // Accent primary color circle
                      Container(
                        width: 16,
                        height: 16,
                        decoration: BoxDecoration(
                          shape: BoxShape.circle,
                          color: itemTheme.primary,
                        ),
                      ),
                      const SizedBox(width: 6),
                      // Background theme color circle
                      Container(
                        width: 16,
                        height: 16,
                        decoration: BoxDecoration(
                          shape: BoxShape.circle,
                          color: itemTheme.background,
                          border: Border.all(color: Colors.white12),
                        ),
                      ),
                    ],
                  ),
                ],
              ),
            ),

            // Select Indicator top right
            if (isSelected)
              Positioned(
                top: 8,
                right: 8,
                child: Icon(
                  Icons.check_circle_rounded,
                  color: activeTheme.primary,
                  size: 16,
                ),
              ),
          ],
        ),
      ),
    );
  }

  Widget _buildSpecRow(String label, String value) {
    return Row(
      mainAxisAlignment: MainAxisAlignment.spaceBetween,
      children: [
        Text(
          label,
          style: const TextStyle(color: Colors.white60, fontSize: 12),
        ),
        Text(
          value,
          style: const TextStyle(
            color: Color(0xE6FFFFFF),
            fontSize: 12,
            fontWeight: FontWeight.bold,
          ),
        ),
      ],
    );
  }
}
