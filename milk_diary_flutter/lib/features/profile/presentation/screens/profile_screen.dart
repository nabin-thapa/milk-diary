import 'package:flutter/material.dart';
import 'package:milk_diary/core/theme/app_theme.dart';
import 'package:milk_diary/shared/widgets/custom_card.dart';

class ProfileScreen extends StatefulWidget {
  const ProfileScreen({super.key});

  @override
  State<ProfileScreen> createState() => _ProfileScreenState();
}

class _ProfileScreenState extends State<ProfileScreen> {
  bool _darkMode = false;
  double _cowRate = 85.0;
  double _bufRate = 115.0;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final isDark = theme.brightness == Brightness.dark;

    return Scaffold(
      appBar: AppBar(
        title: const Text('Owner Profile'),
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(16.0),
        child: Column(
          children: [
            // 1. User Header
            Column(
              children: [
                Stack(
                  alignment: Alignment.bottomRight,
                  children: [
                    CircleAvatar(
                      radius: 46,
                      backgroundColor: Colors.white,
                      child: ClipRRect(
                        borderRadius: BorderRadius.circular(46),
                        child: Image.network(
                          'https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=200',
                          fit: BoxFit.cover,
                          errorBuilder: (_, __, ___) => const Icon(Icons.person, size: 40),
                        ),
                      ),
                    ),
                    Container(
                      width: 28,
                      height: 28,
                      decoration: BoxDecoration(
                        color: isDark ? const Color(0xFF10B981) : AppTheme.primaryGreen,
                        shape: BoxShape.circle,
                        border: Border.all(color: Colors.white, width: 2),
                      ),
                      child: const Icon(Icons.camera_alt, color: Colors.white, size: 14),
                    )
                  ],
                ),
                const SizedBox(height: 12),
                Text('Nabin Thapa', style: theme.textTheme.titleLarge),
                const Text('Dairy Administrator', style: TextStyle(color: Colors.grey, fontSize: 12)),
              ],
            ),

            const SizedBox(height: 24),

            // 2. Settings Group
            _SettingsGroup(
              title: 'App Settings',
              children: [
                _SettingsItem(
                  title: 'Dark Theme Override',
                  subtitle: 'Switch app looks dynamically',
                  trailing: Switch(
                    value: _darkMode,
                    onChanged: (val) {
                      setState(() {
                        _darkMode = val;
                      });
                      // Normally this updates theme state
                    },
                    activeColor: isDark ? const Color(0xFF10B981) : AppTheme.primaryGreen,
                  ),
                ),
                _SettingsItem(
                  title: 'Default Cow Rate',
                  subtitle: 'Rate per liter (NPR)',
                  trailing: SizedBox(
                    width: 70,
                    child: TextField(
                      keyboardType: TextInputType.number,
                      textAlign: TextAlign.end,
                      decoration: const InputDecoration(border: InputBorder.none),
                      controller: TextEditingController(text: _cowRate.toStringAsFixed(0)),
                      onSubmitted: (val) {
                        _cowRate = double.tryParse(val) ?? 85.0;
                      },
                    ),
                  ),
                ),
                _SettingsItem(
                  title: 'Default Buffalo Rate',
                  subtitle: 'Rate per liter (NPR)',
                  trailing: SizedBox(
                    width: 70,
                    child: TextField(
                      keyboardType: TextInputType.number,
                      textAlign: TextAlign.end,
                      decoration: const InputDecoration(border: InputBorder.none),
                      controller: TextEditingController(text: _bufRate.toStringAsFixed(0)),
                      onSubmitted: (val) {
                        _bufRate = double.tryParse(val) ?? 115.0;
                      },
                    ),
                  ),
                ),
              ],
            ),

            const SizedBox(height: 16),

            // 3. Database Actions Group
            _SettingsGroup(
              title: 'Database & Backup',
              children: [
                _SettingsActionButton(
                  icon: Icons.download,
                  label: 'Export Records to CSV',
                  onTap: () {},
                ),
                _SettingsActionButton(
                  icon: Icons.sync,
                  label: 'Backup / Restore Database',
                  onTap: () {},
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }
}

class _SettingsGroup extends StatelessWidget {
  final String title;
  final List<Widget> children;

  const _SettingsGroup({required this.title, required this.children});

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Padding(
          padding: const EdgeInsets.only(left: 4, bottom: 8),
          child: Text(
            title,
            style: theme.textTheme.labelLarge,
          ),
        ),
        ...children,
      ],
    );
  }
}

class _SettingsItem extends StatelessWidget {
  final String title;
  final String subtitle;
  final Widget trailing;

  const _SettingsItem({
    required this.title,
    required this.subtitle,
    required this.trailing,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return Padding(
      padding: const EdgeInsets.only(bottom: 8.0),
      child: CustomCard(
        padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
        child: Row(
          mainAxisAlignment: MainAxisAlignment.spaceBetween,
          children: [
            Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(title, style: theme.textTheme.titleMedium?.copyWith(fontSize: 14)),
                Text(subtitle, style: theme.textTheme.bodyMedium?.copyWith(fontSize: 11)),
              ],
            ),
            trailing,
          ],
        ),
      ),
    );
  }
}

class _SettingsActionButton extends StatelessWidget {
  final IconData icon;
  final String label;
  final VoidCallback onTap;

  const _SettingsActionButton({
    required this.icon,
    required this.label,
    required this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final isDark = theme.brightness == Brightness.dark;
    return Padding(
      padding: const EdgeInsets.only(bottom: 8.0),
      child: CustomCard(
        padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 14),
        onTap: onTap,
        child: Row(
          children: [
            Icon(icon, size: 20, color: isDark ? const Color(0xFF10B981) : AppTheme.primaryGreen),
            const SizedBox(width: 12),
            Text(label, style: theme.textTheme.titleMedium?.copyWith(fontSize: 14)),
          ],
        ),
      ),
    );
  }
}
