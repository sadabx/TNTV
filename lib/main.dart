import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'providers/app_state.dart';
import 'screens/splash_screen.dart';
import 'screens/app_layout.dart';

void main() {
  WidgetsFlutterBinding.ensureInitialized();

  // Set preferred orientations (portrait only for standard mobile feel)
  SystemChrome.setPreferredOrientations([
    DeviceOrientation.portraitUp,
    DeviceOrientation.portraitDown,
  ]);

  // Set transparent status bar
  SystemChrome.setSystemUIOverlayStyle(
    const SystemUiOverlayStyle(
      statusBarColor: Colors.transparent,
      statusBarIconBrightness: Brightness.light,
    ),
  );

  runApp(const MyApp());
}

// InheritedNotifier to expose AppState to the widget tree
class AppStateProvider extends InheritedNotifier<AppState> {
  const AppStateProvider({
    super.key,
    required AppState super.notifier,
    required super.child,
  });

  static AppState of(BuildContext context) {
    final provider = context
        .dependOnInheritedWidgetOfExactType<AppStateProvider>();
    assert(provider != null, "No AppStateProvider found in context");
    return provider!.notifier!;
  }
}

class MyApp extends StatefulWidget {
  const MyApp({super.key});

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  final AppState _appState = AppState();

  @override
  Widget build(BuildContext context) {
    return AppStateProvider(
      notifier: _appState,
      child: AnimatedBuilder(
        animation: _appState,
        builder: (context, _) {
          if (!_appState.isInitialized) {
            return const MaterialApp(
              home: Scaffold(
                backgroundColor: Color(0xFF0F0F0F),
                body: Center(
                  child: CircularProgressIndicator(color: Color(0xFFFF9800)),
                ),
              ),
            );
          }

          final theme = _appState.currentTheme;

          return MaterialApp(
            title: 'TNTV Premium',
            debugShowCheckedModeBanner: false,
            theme: theme.getMaterialTheme(),
            home: const InitialFlow(),
          );
        },
      ),
    );
  }
}

class InitialFlow extends StatefulWidget {
  const InitialFlow({super.key});

  @override
  State<InitialFlow> createState() => _InitialFlowState();
}

class _InitialFlowState extends State<InitialFlow> {
  bool _showSplash = true;

  @override
  Widget build(BuildContext context) {
    if (_showSplash) {
      return SplashScreen(
        onFinished: () {
          setState(() {
            _showSplash = false;
          });
        },
      );
    }
    return const AppLayout();
  }
}
