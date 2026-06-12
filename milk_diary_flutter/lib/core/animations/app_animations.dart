import 'package:flutter/material.dart';
import 'package:flutter_animate/flutter_animate.dart';

class AppAnimations {
  // Common Durations
  static const Duration fast = Duration(milliseconds: 150);
  static const Duration normal = Duration(milliseconds: 300);
  static const Duration slow = Duration(milliseconds: 500);

  // Common Curves
  static final Curve standardCurve = Curves.fastOutSlowIn;
  static final Curve slideCurve = Curves.easeOutCubic;

  // Staggered Slide & Fade In widget wrapper
  static Widget slideIn({
    required Widget child,
    int delayMs = 0,
    double beginY = 0.1,
    Duration duration = normal,
  }) {
    return child
        .animate(delay: Duration(milliseconds: delayMs))
        .fadeIn(duration: duration, curve: standardCurve)
        .slideY(begin: beginY, end: 0.0, duration: duration, curve: slideCurve);
  }

  // Scale on Tap Gesture detector
  static Widget scaleOnTap({
    required Widget child,
    required VoidCallback onTap,
  }) {
    return _ScaleOnTapWidget(onTap: onTap, child: child);
  }
}

class _ScaleOnTapWidget extends StatefulWidget {
  final Widget child;
  final VoidCallback onTap;

  const _ScaleOnTapWidget({required this.onTap, required this.child});

  @override
  State<_ScaleOnTapWidget> createState() => _ScaleOnTapWidgetState();
}

class _ScaleOnTapWidgetState extends State<_ScaleOnTapWidget> with SingleTickerProviderStateMixin {
  late AnimationController _controller;
  late Animation<double> _scaleAnimation;

  @override
  void initState() {
    super.initState();
    _controller = AnimationController(
      vsync: this,
      duration: const Duration(milliseconds: 100),
      lowerBound: 0.95,
      upperBound: 1.0,
      value: 1.0,
    );
    _scaleAnimation = _controller;
  }

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return GestureDetector(
      onTapDown: (_) => _controller.reverse(),
      onTapUp: (_) {
        _controller.forward();
        widget.onTap();
      },
      onTapCancel: () => _controller.forward(),
      child: ScaleTransition(
        scale: _scaleAnimation,
        child: widget.child,
      ),
    );
  }
}
