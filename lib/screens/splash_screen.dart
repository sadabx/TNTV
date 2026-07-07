import 'package:flutter/material.dart';
import '../main.dart';

class SplashScreen extends StatefulWidget {
  final VoidCallback onFinished;

  const SplashScreen({super.key, required this.onFinished});

  @override
  State<SplashScreen> createState() => _SplashScreenState();
}

class _SplashScreenState extends State<SplashScreen>
    with SingleTickerProviderStateMixin {
  late AnimationController _controller;
  late Animation<double> _scaleAnimation;
  late Animation<double> _opacityAnimation;
  late Animation<double> _rotationAnimation;

  @override
  void initState() {
    super.initState();
    _controller = AnimationController(
      vsync: this,
      duration: const Duration(milliseconds: 2200),
    );

    _scaleAnimation = Tween<double>(begin: 0.7, end: 1.0).animate(
      CurvedAnimation(
        parent: _controller,
        curve: const Interval(0.0, 0.6, curve: Curves.easeOutBack),
      ),
    );

    _opacityAnimation = Tween<double>(begin: 0.0, end: 1.0).animate(
      CurvedAnimation(
        parent: _controller,
        curve: const Interval(0.0, 0.5, curve: Curves.easeIn),
      ),
    );

    _rotationAnimation = Tween<double>(begin: 0.0, end: 2.0).animate(
      CurvedAnimation(
        parent: _controller,
        curve: const Interval(0.2, 0.9, curve: Curves.elasticInOut),
      ),
    );

    _controller.forward();

    // End splash after 3 seconds
    Future.delayed(const Duration(milliseconds: 3200), () {
      if (mounted) {
        widget.onFinished();
      }
    });
  }

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final state = AppStateProvider.of(context);
    final theme = state.currentTheme;

    return Scaffold(
      body: Container(
        width: double.infinity,
        height: double.infinity,
        decoration: BoxDecoration(gradient: theme.bgGradient),
        child: Stack(
          alignment: Alignment.center,
          children: [
            // Ambient glow backdrops
            Positioned(
              top: -100,
              right: -100,
              child: Container(
                width: 300,
                height: 300,
                decoration: BoxDecoration(
                  shape: BoxShape.circle,
                  color: theme.primary.withValues(alpha: 0.08),
                ),
              ),
            ),
            Positioned(
              bottom: -150,
              left: -150,
              child: Container(
                width: 400,
                height: 400,
                decoration: BoxDecoration(
                  shape: BoxShape.circle,
                  color: theme.secondary.withValues(alpha: 0.06),
                ),
              ),
            ),

            // Animated Center Logo
            AnimatedBuilder(
              animation: _controller,
              builder: (context, child) {
                return Opacity(
                  opacity: _opacityAnimation.value,
                  child: Transform.scale(
                    scale: _scaleAnimation.value,
                    child: Column(
                      mainAxisSize: MainAxisSize.min,
                      children: [
                        // Glassmorphic Glowing Logo Card
                        Container(
                          width: 140,
                          height: 140,
                          padding: const EdgeInsets.all(24),
                          decoration: BoxDecoration(
                            gradient: RadialGradient(
                              colors: [
                                theme.primary.withValues(alpha: 0.2),
                                Colors.white.withValues(alpha: 0.03),
                              ],
                              radius: 1.0,
                            ),
                            borderRadius: BorderRadius.circular(32),
                            border: Border.all(
                              color: theme.primary.withValues(alpha: 0.3),
                              width: 1.5,
                            ),
                            boxShadow: [
                              BoxShadow(
                                color: theme.primary.withValues(alpha: 0.12),
                                blurRadius: 30,
                                spreadRadius: 5,
                              ),
                            ],
                          ),
                          child: RotationTransition(
                            turns: _rotationAnimation,
                            child: Image.asset(
                              'assets/iptv.png',
                              fit: BoxFit.contain,
                              errorBuilder: (context, error, stackTrace) {
                                // Fallback icon if asset fails
                                return Icon(
                                  Icons.tv_rounded,
                                  size: 70,
                                  color: theme.primary,
                                );
                              },
                            ),
                          ),
                        ),
                        const SizedBox(height: 24),
                        // App Titles
                        ShaderMask(
                          shaderCallback: (bounds) =>
                              theme.accentGradient.createShader(bounds),
                          child: const Text(
                            "TNTV",
                            style: TextStyle(
                              fontSize: 40,
                              fontWeight: FontWeight.w900,
                              color: Colors.white,
                              letterSpacing: 6.0,
                            ),
                          ),
                        ),
                        const SizedBox(height: 6),
                        Text(
                          "ULTRA STREAMING PLATFORM",
                          style: TextStyle(
                            fontSize: 10,
                            fontWeight: FontWeight.bold,
                            letterSpacing: 3.5,
                            color: theme.textSecondary,
                          ),
                        ),
                      ],
                    ),
                  ),
                );
              },
            ),

            // Loading indicator at bottom
            Positioned(
              bottom: 80,
              child: Column(
                children: [
                  SizedBox(
                    width: 32,
                    height: 32,
                    child: CircularProgressIndicator(
                      color: theme.primary,
                      strokeWidth: 2.5,
                    ),
                  ),
                  const SizedBox(height: 16),
                  Text(
                    "Connecting to Nodes...",
                    style: TextStyle(
                      fontSize: 11,
                      color: theme.textSecondary.withValues(alpha: 0.6),
                      letterSpacing: 1.5,
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
}
