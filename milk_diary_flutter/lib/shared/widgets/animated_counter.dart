import 'package:flutter/material.dart';

class AnimatedCounter extends StatelessWidget {
  final double value;
  final TextStyle style;
  final String prefix;
  final String suffix;
  final bool isMoney;

  const AnimatedCounter({
    super.key,
    required this.value,
    required this.style,
    this.prefix = '',
    this.suffix = '',
    this.isMoney = false,
  });

  @override
  Widget build(BuildContext context) {
    return TweenAnimationBuilder<double>(
      tween: Tween<double>(begin: 0, end: value),
      duration: const Duration(milliseconds: 800),
      curve: Curves.fastOutSlowIn,
      builder: (context, animValue, child) {
        String formattedVal;
        if (isMoney) {
          formattedVal = '$prefix${animValue.toStringAsFixed(2)}$suffix';
        } else {
          formattedVal = '$prefix${animValue.toStringAsFixed(2)}$suffix';
        }
        return Text(
          formattedVal,
          style: style,
        );
      },
    );
  }
}
