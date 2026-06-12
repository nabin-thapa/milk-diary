import 'dart:ui';
import 'package:flutter/material.dart';
import 'package:milk_diary/core/animations/app_animations.dart';

class CustomCard extends StatelessWidget {
  final Widget child;
  final EdgeInsetsGeometry? padding;
  final VoidCallback? onTap;
  final List<Color>? gradientColors;
  final bool isGlass;
  final double borderRadius;
  final double elevation;

  const CustomCard({
    super.key,
    required this.child,
    this.padding,
    this.onTap,
    this.gradientColors,
    this.isGlass = false,
    this.borderRadius = 24.0,
    this.elevation = 0.5,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final isDark = theme.brightness == Brightness.dark;

    Widget cardWidget = Container(
      padding: padding ?? const EdgeInsets.all(20.0),
      decoration: BoxDecoration(
        color: gradientColors != null
            ? null
            : isGlass
                ? (isDark ? Colors.black.withOpacity(0.25) : Colors.white.withOpacity(0.45))
                : theme.cardTheme.color,
        gradient: gradientColors != null
            ? LinearGradient(
                colors: gradientColors!,
                begin: Alignment.topLeft,
                end: Alignment.bottomRight,
              )
            : null,
        borderRadius: BorderRadius.circular(borderRadius),
        border: Border.all(
          color: isGlass
              ? (isDark ? Colors.white.withOpacity(0.05) : Colors.white.withOpacity(0.4))
              : (isDark ? const Color(0xFF1E293B) : const Color(0xFFE2E8F0)),
          width: 1.0,
        ),
        boxShadow: gradientColors != null || isGlass
            ? []
            : [
                BoxShadow(
                  color: isDark ? Colors.black26 : const Color(0xFF0F172A).withOpacity(0.04),
                  blurRadius: 16.0,
                  offset: const Offset(0, 8),
                ),
              ],
      ),
      child: child,
    );

    if (isGlass) {
      cardWidget = ClipRRect(
        borderRadius: BorderRadius.circular(borderRadius),
        child: BackdropFilter(
          filter: ImageFilter.blur(sigmaX: 15.0, sigmaY: 15.0),
          child: cardWidget,
        ),
      );
    }

    if (onTap != null) {
      cardWidget = AppAnimations.scaleOnTap(
        onTap: onTap!,
        child: cardWidget,
      );
    }

    return cardWidget;
  }
}
