import 'dart:math' as math;
import 'package:flutter/material.dart';

class MilkTankIndicator extends StatefulWidget {
  final double percentage; // 0.0 to 1.0
  final double liters;
  final String label;
  final List<Color> liquidColors;

  const MilkTankIndicator({
    super.key,
    required this.percentage,
    required this.liters,
    required this.label,
    required this.liquidColors,
  });

  @override
  State<MilkTankIndicator> createState() => _MilkTankIndicatorState();
}

class _MilkTankIndicatorState extends State<MilkTankIndicator> with SingleTickerProviderStateMixin {
  late AnimationController _controller;

  @override
  void initState() {
    super.initState();
    _controller = AnimationController(
      vsync: this,
      duration: const Duration(seconds: 2),
    )..repeat();
  }

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final isDark = theme.brightness == Brightness.dark;

    return Column(
      mainAxisSize: MainAxisSize.min,
      children: [
        // Tank Graphic
        Stack(
          alignment: Alignment.center,
          children: [
            Container(
              width: 60,
              height: 100,
              decoration: BoxDecoration(
                color: isDark ? const Color(0xFF1E293B) : const Color(0xFFF1F5F9),
                borderRadius: BorderRadius.circular(16),
                border: Border.all(
                  color: isDark ? const Color(0xFF334155) : const Color(0xFFCBD5E1),
                  width: 3,
                ),
              ),
              child: ClipRRect(
                borderRadius: BorderRadius.circular(12),
                child: AnimatedBuilder(
                  animation: _controller,
                  builder: (context, child) {
                    return CustomPaint(
                      painter: _WavePainter(
                        waveValue: _controller.value,
                        percentage: widget.percentage,
                        colors: widget.liquidColors,
                      ),
                    );
                  },
                ),
              ),
            ),
            // Centered Percentage Text
            Text(
              '${(widget.percentage * 100).toInt()}%',
              style: TextStyle(
                fontSize: 12,
                fontWeight: FontWeight.w800,
                color: isDark ? Colors.white : const Color(0xFF0F172A),
                shadows: [
                  Shadow(
                    color: isDark ? Colors.black45 : Colors.white70,
                    blurRadius: 3,
                  ),
                ],
              ),
            ),
          ],
        ),
        const SizedBox(height: 8),
        // Metadata Labels
        Text(
          widget.label,
          style: theme.textTheme.bodyMedium?.copyWith(
            fontSize: 12,
            fontWeight: FontWeight.w600,
          ),
        ),
        Text(
          '${widget.liters.toStringAsFixed(2)} L',
          style: theme.textTheme.titleMedium?.copyWith(
            fontSize: 14,
            fontWeight: FontWeight.bold,
          ),
        ),
      ],
    );
  }
}

class _WavePainter extends CustomPainter {
  final double waveValue;
  final double percentage;
  final List<Color> colors;

  _WavePainter({
    required this.waveValue,
    required this.percentage,
    required this.colors,
  });

  @override
  void paint(Canvas canvas, Size size) {
    if (percentage <= 0.0) return;

    final paint = Paint()
      ..shader = LinearGradient(
        colors: colors,
        begin: Alignment.bottomCenter,
        end: Alignment.topCenter,
      ).createShader(Rect.fromLTWH(0, 0, size.width, size.height));

    final path = Path();
    final fillHeight = size.height * percentage;
    final baseHeight = size.height - fillHeight;
    
    // Wave Math (Sine curve animation)
    final waveHeight = 5.0; // wave amplitude
    final waveFrequency = 2 * math.pi / size.width;

    path.moveTo(0, size.height);
    for (double x = 0; x <= size.width; x++) {
      final y = baseHeight + math.sin((x * waveFrequency) + (waveValue * 2 * math.pi)) * waveHeight;
      path.lineTo(x, y);
    }
    path.lineTo(size.width, size.height);
    path.close();

    canvas.drawPath(path, paint);
  }

  @override
  bool shouldRepaint(covariant _WavePainter oldDelegate) {
    return oldDelegate.waveValue != waveValue ||
        oldDelegate.percentage != percentage ||
        oldDelegate.colors != colors;
  }
}
