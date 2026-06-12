import 'package:flutter/material.dart';

class AppTheme {
  // Premium Palette
  static const Color primaryGreen = Color(0xFF1B7A4A);
  static const Color primaryGreenDark = Color(0xFF0F5C36);
  static const Color primaryGreenLight = Color(0xFFE8F5E9);
  
  static const Color cowBlue = Color(0xFF2563EB);
  static const Color buffaloPurple = Color(0xFF7C3AED);
  static const Color pendingOrange = Color(0xFFF59E0B);
  static const Color earningGreen = Color(0xFF16A34A);
  static const Color expenseRed = Color(0xFFDC2626);

  static const Color bgLight = Color(0xFFF8FAFC);
  static const Color bgDark = Color(0xFF090D16);
  
  static const Color cardBgLight = Colors.white;
  static const Color cardBgDark = Color(0xFF131926);

  static ThemeData get lightTheme {
    return ThemeData(
      useMaterial3: true,
      brightness: Brightness.light,
      primaryColor: primaryGreen,
      colorScheme: const ColorScheme.light(
        primary: primaryGreen,
        secondary: cowBlue,
        tertiary: buffaloPurple,
        error: expenseRed,
        background: bgLight,
        surface: cardBgLight,
        onPrimary: Colors.white,
        onSecondary: Colors.white,
      ),
      scaffoldBackgroundColor: bgLight,
      fontFamily: 'Inter',
      textTheme: const TextTheme(
        displayLarge: TextStyle(fontFamily: 'Poppins', fontSize: 32, fontWeight: FontWeight.bold, color: Color(0xFF0F172A)),
        titleLarge: TextStyle(fontFamily: 'Poppins', fontSize: 20, fontWeight: FontWeight.w600, color: Color(0xFF0F172A)),
        titleMedium: TextStyle(fontFamily: 'Poppins', fontSize: 16, fontWeight: FontWeight.w600, color: Color(0xFF0F172A)),
        bodyLarge: TextStyle(fontFamily: 'Inter', fontSize: 16, fontWeight: FontWeight.normal, color: Color(0xFF0F172A)),
        bodyMedium: TextStyle(fontFamily: 'Inter', fontSize: 14, fontWeight: FontWeight.normal, color: Color(0xFF64748B)),
        labelLarge: TextStyle(fontFamily: 'Poppins', fontSize: 12, fontWeight: FontWeight.bold, color: Color(0xFF94A3B8), letterSpacing: 0.8),
      ),
      cardTheme: CardTheme(
        color: cardBgLight,
        elevation: 1.0,
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(24.0),
          side: const BorderSide(color: Color(0xFFE2E8F0), width: 1.0),
        ),
      ),
      appBarTheme: const AppBarTheme(
        backgroundColor: Colors.transparent,
        elevation: 0,
        iconTheme: IconThemeData(color: Color(0xFF0F172A)),
        titleTextStyle: TextStyle(fontFamily: 'Poppins', fontSize: 20, fontWeight: FontWeight.bold, color: Color(0xFF0F172A)),
      ),
      elevatedButtonTheme: ElevatedButtonThemeData(
        style: ElevatedButton.styleFrom(
          backgroundColor: primaryGreen,
          foregroundColor: Colors.white,
          minimumSize: const Size.fromHeight(56),
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(16.0),
          ),
          textStyle: const TextStyle(fontFamily: 'Poppins', fontSize: 16, fontWeight: FontWeight.bold),
          elevation: 2,
        ),
      ),
    );
  }

  static ThemeData get darkTheme {
    return ThemeData(
      useMaterial3: true,
      brightness: Brightness.dark,
      primaryColor: const Color(0xFF10B981),
      colorScheme: const ColorScheme.dark(
        primary: Color(0xFF10B981),
        secondary: Color(0xFF60A5FA),
        tertiary: Color(0xFFA78BFA),
        error: Color(0xFFEF4444),
        background: bgDark,
        surface: cardBgDark,
        onPrimary: Colors.black,
        onSecondary: Colors.black,
      ),
      scaffoldBackgroundColor: bgDark,
      fontFamily: 'Inter',
      textTheme: const TextTheme(
        displayLarge: TextStyle(fontFamily: 'Poppins', fontSize: 32, fontWeight: FontWeight.bold, color: Color(0xFFF1F5F9)),
        titleLarge: TextStyle(fontFamily: 'Poppins', fontSize: 20, fontWeight: FontWeight.w600, color: Color(0xFFF1F5F9)),
        titleMedium: TextStyle(fontFamily: 'Poppins', fontSize: 16, fontWeight: FontWeight.w600, color: Color(0xFFF1F5F9)),
        bodyLarge: TextStyle(fontFamily: 'Inter', fontSize: 16, fontWeight: FontWeight.normal, color: Color(0xFFF1F5F9)),
        bodyMedium: TextStyle(fontFamily: 'Inter', fontSize: 14, fontWeight: FontWeight.normal, color: Color(0xFF94A3B8)),
        labelLarge: TextStyle(fontFamily: 'Poppins', fontSize: 12, fontWeight: FontWeight.bold, color: Color(0xFF64748B), letterSpacing: 0.8),
      ),
      cardTheme: CardTheme(
        color: cardBgDark,
        elevation: 0.0,
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(24.0),
          side: const BorderSide(color: Color(0xFF1E293B), width: 1.0),
        ),
      ),
      appBarTheme: const AppBarTheme(
        backgroundColor: Colors.transparent,
        elevation: 0,
        iconTheme: IconThemeData(color: Color(0xFFF1F5F9)),
        titleTextStyle: TextStyle(fontFamily: 'Poppins', fontSize: 20, fontWeight: FontWeight.bold, color: Color(0xFFF1F5F9)),
      ),
      elevatedButtonTheme: ElevatedButtonThemeData(
        style: ElevatedButton.styleFrom(
          backgroundColor: const Color(0xFF10B981),
          foregroundColor: Colors.black,
          minimumSize: const Size.fromHeight(56),
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(16.0),
          ),
          textStyle: const TextStyle(fontFamily: 'Poppins', fontSize: 16, fontWeight: FontWeight.bold),
          elevation: 0,
        ),
      ),
    );
  }
}
