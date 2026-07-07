import 'package:flutter/material.dart';

enum AppThemeMode { toffeeOrange, darkNeon, royalRed, nordicBlue, forestGreen }

class AppThemeData {
  final AppThemeMode mode;
  final String name;
  final Color primary;
  final Color secondary;
  final Color background;
  final Color surface;
  final Color textPrimary;
  final Color textSecondary;
  final Color cardBackground;
  final Color activeGlow;

  AppThemeData({
    required this.mode,
    required this.name,
    required this.primary,
    required this.secondary,
    required this.background,
    required this.surface,
    required this.textPrimary,
    required this.textSecondary,
    required this.cardBackground,
    required this.activeGlow,
  });

  static AppThemeData getTheme(AppThemeMode mode) {
    switch (mode) {
      case AppThemeMode.toffeeOrange:
        return AppThemeData(
          mode: mode,
          name: "Toffee Orange",
          primary: const Color(0xFFFF9800),
          secondary: const Color(0xFFE65100),
          background: const Color(0xFF0F0F0F),
          surface: const Color(0xFF1A1A1A),
          cardBackground: const Color(0xFF222222),
          textPrimary: Colors.white.withValues(alpha: 0.95),
          textSecondary: Colors.white54,
          activeGlow: const Color(0xFFFFB74D).withValues(alpha: 0.15),
        );
      case AppThemeMode.darkNeon:
        return AppThemeData(
          mode: mode,
          name: "Dark Neon",
          primary: const Color(0xFF00FFCC),
          secondary: const Color(0xFF00D2FF),
          background: const Color(0xFF050505),
          surface: const Color(0xFF0E0E0E),
          cardBackground: const Color(0xFF141414),
          textPrimary: Colors.white.withValues(alpha: 0.95),
          textSecondary: Colors.white54,
          activeGlow: const Color(0xFF00FFCC).withValues(alpha: 0.2),
        );
      case AppThemeMode.royalRed:
        return AppThemeData(
          mode: mode,
          name: "Royal Red",
          primary: const Color(0xFFE91E63),
          secondary: const Color(0xFF880E4F),
          background: const Color(0xFF0D0206),
          surface: const Color(0xFF1B0710),
          cardBackground: const Color(0xFF250C19),
          textPrimary: Colors.white.withValues(alpha: 0.95),
          textSecondary: Colors.white54,
          activeGlow: const Color(0xFFE91E63).withValues(alpha: 0.18),
        );
      case AppThemeMode.nordicBlue:
        return AppThemeData(
          mode: mode,
          name: "Nordic Blue",
          primary: const Color(0xFF00E5FF),
          secondary: const Color(0xFF0D47A1),
          background: const Color(0xFF050B14),
          surface: const Color(0xFF0E1A2F),
          cardBackground: const Color(0xFF15263F),
          textPrimary: Colors.white.withValues(alpha: 0.95),
          textSecondary: Colors.white54,
          activeGlow: const Color(0xFF00E5FF).withValues(alpha: 0.2),
        );
      case AppThemeMode.forestGreen:
        return AppThemeData(
          mode: mode,
          name: "Forest Green",
          primary: const Color(0xFF2ECC71),
          secondary: const Color(0xFF1B4D3E),
          background: const Color(0xFF0B140F),
          surface: const Color(0xFF14241B),
          cardBackground: const Color(0xFF1C3326),
          textPrimary: Colors.white.withValues(alpha: 0.95),
          textSecondary: Colors.white54,
          activeGlow: const Color(0xFF2ECC71).withValues(alpha: 0.15),
        );
    }
  }

  ThemeData getMaterialTheme() {
    return ThemeData(
      brightness: Brightness.dark,
      primaryColor: primary,
      scaffoldBackgroundColor: background,
      cardColor: cardBackground,
      colorScheme: ColorScheme.dark(
        primary: primary,
        secondary: secondary,
        surface: surface,
      ),
      appBarTheme: AppBarTheme(
        backgroundColor: background,
        elevation: 0,
        titleTextStyle: TextStyle(
          color: textPrimary,
          fontSize: 20,
          fontWeight: FontWeight.bold,
        ),
      ),
      bottomNavigationBarTheme: BottomNavigationBarThemeData(
        backgroundColor: surface,
        selectedItemColor: primary,
        unselectedItemColor: textSecondary,
      ),
    );
  }

  // Neon or glassmorphic gradients
  LinearGradient get bgGradient => LinearGradient(
    colors: [background, Color.lerp(background, surface, 0.45)!],
    begin: Alignment.topCenter,
    end: Alignment.bottomCenter,
  );

  LinearGradient get accentGradient => LinearGradient(
    colors: [primary, secondary],
    begin: Alignment.topLeft,
    end: Alignment.bottomRight,
  );

  // Box shadow for glowing items
  List<BoxShadow> get glowShadow => [
    BoxShadow(
      color: primary.withValues(alpha: 0.3),
      blurRadius: 10,
      spreadRadius: 1,
    ),
  ];

  // Glassmorphic border
  Border get glassBorder =>
      Border.all(color: Colors.white.withValues(alpha: 0.08), width: 1.0);

  // Glassmorphic background decoration
  BoxDecoration glassDecoration({double radius = 16.0}) {
    return BoxDecoration(
      color: Colors.white.withValues(alpha: 0.04),
      borderRadius: BorderRadius.circular(radius),
      border: glassBorder,
    );
  }
}
