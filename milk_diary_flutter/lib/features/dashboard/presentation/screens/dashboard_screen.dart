import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:milk_diary/core/theme/app_theme.dart';
import 'package:milk_diary/core/animations/app_animations.dart';
import 'package:milk_diary/shared/widgets/custom_card.dart';
import 'package:milk_diary/shared/widgets/milk_tank_indicator.dart';
import 'package:milk_diary/shared/widgets/animated_counter.dart';
import 'package:milk_diary/features/dashboard/presentation/widgets/add_entry_bottom_sheet.dart';

class DashboardScreen extends ConsumerStatefulWidget {
  const DashboardScreen({super.key});

  @override
  ConsumerState<DashboardScreen> createState() => _DashboardScreenState();
}

class _DashboardScreenState extends ConsumerState<DashboardScreen> {
  // Local state simulation for UI demonstration
  double todayCowMilk = 45.0;
  double todayBufMilk = 35.0;
  double todayEarnings = 7050.0;
  double todayOutstanding = 3750.0;
  double todayProfit = 1653.13;
  
  double stockCow = 15.0;
  double stockBuf = 10.0;

  void _openAddEntry(BuildContext context, String type) {
    showModalBottomSheet(
      context: context,
      isScrollControlled: true,
      backgroundColor: Colors.transparent,
      builder: (context) => AddEntryBottomSheet(
        entryType: type,
        onSave: (milkType, liters, rate, total, isPaid) {
          setState(() {
            if (type == 'purchase') {
              if (milkType == 'cow') {
                todayCowMilk += liters;
                stockCow += liters;
              } else {
                todayBufMilk += liters;
                stockBuf += liters;
              }
            } else {
              // Sale
              todayEarnings += total;
              if (milkType == 'cow') {
                stockCow = (stockCow - liters).clamp(0, double.infinity);
              } else {
                stockBuf = (stockBuf - liters).clamp(0, double.infinity);
              }
              if (!isPaid) {
                todayOutstanding += total;
              }
              // Profit calculation update
              final avgPurchase = (milkType == 'cow' ? 85.0 : 115.0);
              todayProfit += (total - (liters * avgPurchase));
            }
          });
        },
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final isDark = theme.brightness == Brightness.dark;

    return Scaffold(
      body: SingleChildScrollView(
        child: Column(
          children: [
            // 1. TOP APP BAR (Curved Header Gradient)
            Stack(
              children: [
                Container(
                  height: 200,
                  width: double.infinity,
                  decoration: BoxDecoration(
                    gradient: LinearGradient(
                      colors: isDark
                          ? [const Color(0xFF047857), const Color(0xFF064E3B)]
                          : [AppTheme.primaryGreen, AppTheme.primaryGreenDark],
                      begin: Alignment.topLeft,
                      end: Alignment.bottomRight,
                    ),
                    borderRadius: const BorderRadius.only(
                      bottomLeft: Radius.circular(40),
                      bottomRight: Radius.circular(40),
                    ),
                    boxShadow: [
                      BoxShadow(
                        color: AppTheme.primaryGreen.withOpacity(0.15),
                        blurRadius: 20,
                        offset: const Offset(0, 8),
                      )
                    ],
                  ),
                  padding: const EdgeInsets.only(top: 60, left: 24, right: 24),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Row(
                        mainAxisAlignment: MainAxisAlignment.spaceBetween,
                        children: [
                          Row(
                            children: [
                              CircleAvatar(
                                radius: 24,
                                backgroundColor: Colors.white24,
                                child: ClipRRect(
                                  borderRadius: BorderRadius.circular(24),
                                  child: Image.network(
                                    'https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=100',
                                    fit: BoxFit.cover,
                                    errorBuilder: (_, __, ___) => const Icon(Icons.person, color: Colors.white),
                                  ),
                                ),
                              ),
                              const SizedBox(width: 12),
                              Column(
                                crossAxisAlignment: CrossAxisAlignment.start,
                                children: [
                                  Text(
                                    'Welcome Back',
                                    style: TextStyle(
                                      color: Colors.white.withOpacity(0.7),
                                      fontSize: 12,
                                      fontWeight: FontWeight.w500,
                                      letterSpacing: 0.5,
                                    ),
                                  ),
                                  const Text(
                                    'Good Morning, Nabin 👋',
                                    style: TextStyle(
                                      color: Colors.white,
                                      fontSize: 16,
                                      fontWeight: FontWeight.bold,
                                    ),
                                  ),
                                ],
                              ),
                            ],
                          ),
                          Row(
                            children: [
                              _HeaderActionButton(
                                icon: Icons.notifications_none,
                                onTap: () {},
                              ),
                              const SizedBox(width: 8),
                              _HeaderActionButton(
                                icon: Icons.settings_outlined,
                                onTap: () {},
                              ),
                            ],
                          ),
                        ],
                      ),
                      const SizedBox(height: 20),
                      Row(
                        mainAxisAlignment: MainAxisAlignment.spaceBetween,
                        children: [
                          const Text(
                            'June 12, 2026',
                            style: TextStyle(color: Colors.white70, fontSize: 13),
                          ),
                          Container(
                            padding: const EdgeInsets.symmetric(horizontal: 10, py: 4),
                            decoration: BoxDecoration(
                              color: Colors.white10,
                              borderRadius: BorderRadius.circular(10),
                              border: Border.all(color: Colors.white12),
                            ),
                            child: const Text(
                              'Fully Offline 🔒',
                              style: TextStyle(color: Colors.white70, fontSize: 11, fontWeight: FontWeight.w600),
                            ),
                          )
                        ],
                      )
                    ],
                  ),
                ),
              ],
            ),

            const SizedBox(height: 16),

            // 2. HERO SUMMARY CARD (staggered delay)
            AppAnimations.slideIn(
              delayMs: 100,
              child: Padding(
                padding: const EdgeInsets.symmetric(horizontal: 16.0),
                child: CustomCard(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Row(
                        mainAxisAlignment: MainAxisAlignment.spaceBetween,
                        children: [
                          Text(
                            'Today\'s Overview',
                            style: theme.textTheme.titleMedium?.copyWith(color: theme.textTheme.bodyMedium?.color),
                          ),
                          Container(
                            padding: const EdgeInsets.symmetric(horizontal: 8, py: 4),
                            decoration: BoxDecoration(
                              color: AppTheme.earningGreen.withOpacity(0.1),
                              borderRadius: BorderRadius.circular(20),
                            ),
                            child: const Row(
                              children: [
                                Icon(Icons.arrow_upward, size: 12, color: AppTheme.earningGreen),
                                SizedBox(width: 4),
                                Text(
                                  '12.4% vs yesterday',
                                  style: TextStyle(fontSize: 10, color: AppTheme.earningGreen, fontWeight: FontWeight.bold),
                                )
                              ],
                            ),
                          )
                        ],
                      ),
                      const SizedBox(height: 16),
                      Text(
                        'TODAY\'S EARNINGS',
                        style: theme.textTheme.labelLarge,
                      ),
                      const SizedBox(height: 4),
                      AnimatedCounter(
                        value: todayEarnings,
                        style: TextStyle(
                          fontSize: 32,
                          fontWeight: FontWeight.w800,
                          color: isDark ? theme.colorScheme.primary : AppTheme.primaryGreen,
                        ),
                        prefix: 'Rs. ',
                        isMoney: true,
                      ),
                      const SizedBox(height: 16),
                      const Divider(height: 1),
                      const SizedBox(height: 16),
                      
                      // Overview Grid
                      GridView.count(
                        crossAxisCount: 2,
                        shrinkWrap: true,
                        physics: const NeverScrollableScrollPhysics(),
                        childAspectRatio: 2.5,
                        mainAxisSpacing: 12,
                        crossAxisSpacing: 12,
                        children: [
                          _OverviewGridItem(
                            dotColor: AppTheme.cowBlue,
                            label: 'Cow Milk',
                            value: '${todayCowMilk.toStringAsFixed(2)} L',
                          ),
                          _OverviewGridItem(
                            dotColor: AppTheme.buffaloPurple,
                            label: 'Buffalo Milk',
                            value: '${todayBufMilk.toStringAsFixed(2)} L',
                          ),
                          _OverviewGridItem(
                            dotColor: AppTheme.pendingOrange,
                            label: 'Outstanding',
                            value: 'Rs. ${todayOutstanding.toStringAsFixed(2)}',
                            valueColor: AppTheme.pendingOrange,
                          ),
                          _OverviewGridItem(
                            dotColor: AppTheme.earningGreen,
                            label: 'Estimated Profit',
                            value: 'Rs. ${todayProfit.toStringAsFixed(2)}',
                            valueColor: AppTheme.earningGreen,
                          ),
                        ],
                      )
                    ],
                  ),
                ),
              ),
            ),

            const SizedBox(height: 16),

            // 3. QUICK ACTIONS GRID
            Padding(
              padding: const EdgeInsets.symmetric(horizontal: 16.0),
              child: Align(
                alignment: Alignment.centerLeft,
                child: Text('Quick Actions', style: theme.textTheme.titleMedium),
              ),
            ),
            const SizedBox(height: 8),
            AppAnimations.slideIn(
              delayMs: 200,
              child: Padding(
                padding: const EdgeInsets.symmetric(horizontal: 16.0),
                child: GridView.count(
                  crossAxisCount: 3,
                  shrinkWrap: true,
                  physics: const NeverScrollableScrollPhysics(),
                  crossAxisSpacing: 12,
                  mainAxisSpacing: 12,
                  childAspectRatio: 1.0,
                  children: [
                    _ActionCard(
                      icon: Icons.add_circle_outline,
                      label: 'Add Purchase',
                      colors: [const Color(0xFF10B981), const Color(0xFF059669)],
                      onTap: () => _openAddEntry(context, 'purchase'),
                    ),
                    _ActionCard(
                      icon: Icons.monetization_on_outlined,
                      label: 'Add Sale',
                      colors: [const Color(0xFF3B82F6), const Color(0xFF2563EB)],
                      onTap: () => _openAddEntry(context, 'sale'),
                    ),
                    _ActionCard(
                      icon: Icons.people_outline,
                      label: 'Customers',
                      colors: [const Color(0xFFF59E0B), const Color(0xFFD97706)],
                      onTap: () {},
                    ),
                    _ActionCard(
                      icon: Icons.inventory_2_outlined,
                      label: 'Suppliers',
                      colors: [const Color(0xFFEC4899), const Color(0xFFDB2777)],
                      onTap: () {},
                    ),
                    _ActionCard(
                      icon: Icons.history,
                      label: 'History',
                      colors: [const Color(0xFF6366F1), const Color(0xFF4F46E5)],
                      onTap: () {},
                    ),
                    _ActionCard(
                      icon: Icons.bar_chart,
                      label: 'Reports',
                      colors: [const Color(0xFF8B5CF6), const Color(0xFF7C3AED)],
                      onTap: () {},
                    ),
                  ],
                ),
              ),
            ),

            const SizedBox(height: 24),

            // 4. STOCK TANKS
            Padding(
              padding: const EdgeInsets.symmetric(horizontal: 16.0),
              child: Align(
                alignment: Alignment.centerLeft,
                child: Text('Milk Stock Tanks', style: theme.textTheme.titleMedium),
              ),
            ),
            const SizedBox(height: 12),
            AppAnimations.slideIn(
              delayMs: 300,
              child: Padding(
                padding: const EdgeInsets.symmetric(horizontal: 16.0),
                child: Row(
                  children: [
                    Expanded(
                      child: CustomCard(
                        padding: const EdgeInsets.symmetric(vertical: 16, horizontal: 8),
                        child: MilkTankIndicator(
                          percentage: (stockCow / 100).clamp(0, 1),
                          liters: stockCow,
                          label: 'Cow Stock',
                          liquidColors: const [Color(0xFF1D4ED8), Color(0xFF60A5FA)],
                        ),
                      ),
                    ),
                    const SizedBox(width: 12),
                    Expanded(
                      child: CustomCard(
                        padding: const EdgeInsets.symmetric(vertical: 16, horizontal: 8),
                        child: MilkTankIndicator(
                          percentage: (stockBuf / 100).clamp(0, 1),
                          liters: stockBuf,
                          label: 'Buffalo Stock',
                          liquidColors: const [Color(0xFF5B21B6), Color(0xFFA78BFA)],
                        ),
                      ),
                    ),
                    const SizedBox(width: 12),
                    Expanded(
                      child: CustomCard(
                        padding: const EdgeInsets.symmetric(vertical: 16, horizontal: 8),
                        child: MilkTankIndicator(
                          percentage: ((stockCow + stockBuf) / 200).clamp(0, 1),
                          liters: stockCow + stockBuf,
                          label: 'Total Stock',
                          liquidColors: const [Color(0xFF065F46), Color(0xFF34D399)],
                        ),
                      ),
                    ),
                  ],
                ),
              ),
            ),

            const SizedBox(height: 30),
          ],
        ),
      ),
    );
  }
}

class _HeaderActionButton extends StatelessWidget {
  final IconData icon;
  final VoidCallback onTap;

  const _HeaderActionButton({required this.icon, required this.onTap});

  @override
  Widget build(BuildContext context) {
    return AppAnimations.scaleOnTap(
      onTap: onTap,
      child: Container(
        width: 38,
        height: 38,
        decoration: BoxDecoration(
          color: Colors.white.withOpacity(0.15),
          borderRadius: BorderRadius.circular(12),
          border: Border.all(color: Colors.white10),
        ),
        child: Icon(icon, color: Colors.white, size: 20),
      ),
    );
  }
}

class _OverviewGridItem extends StatelessWidget {
  final Color dotColor;
  final String label;
  final String value;
  final Color? valueColor;

  const _OverviewGridItem({
    required this.dotColor,
    required this.label,
    required this.value,
    this.valueColor,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return Row(
      children: [
        Container(
          width: 8,
          height: 8,
          decoration: BoxDecoration(
            color: dotColor,
            shape: BoxShape.circle,
          ),
        ),
        const SizedBox(width: 8),
        Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Text(
              label,
              style: theme.textTheme.bodyMedium?.copyWith(fontSize: 11),
            ),
            Text(
              value,
              style: theme.textTheme.bodyLarge?.copyWith(
                fontWeight: FontWeight.bold,
                fontSize: 13,
                color: valueColor,
              ),
            ),
          ],
        ),
      ],
    );
  }
}

class _ActionCard extends StatelessWidget {
  final IconData icon;
  final String label;
  final List<Color> colors;
  final VoidCallback onTap;

  const _ActionCard({
    required this.icon,
    required this.label,
    required this.colors,
    required this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    return AppAnimations.scaleOnTap(
      onTap: onTap,
      child: Container(
        decoration: BoxDecoration(
          color: Theme.of(context).cardTheme.color,
          borderRadius: BorderRadius.circular(20),
          border: Border.all(color: Theme.of(context).brightness == Brightness.dark ? const Color(0xFF1E293B) : const Color(0xFFE2E8F0)),
          boxShadow: [
            BoxShadow(
              color: Colors.black.withOpacity(0.02),
              blurRadius: 8,
              offset: const Offset(0, 2),
            )
          ],
        ),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Container(
              width: 44,
              height: 44,
              decoration: BoxDecoration(
                gradient: LinearGradient(colors: colors, begin: Alignment.topLeft, end: Alignment.bottomRight),
                borderRadius: BorderRadius.circular(14),
              ),
              child: Icon(icon, color: Colors.white, size: 24),
            ),
            const SizedBox(height: 8),
            Text(
              label,
              style: const TextStyle(fontSize: 11, fontWeight: FontWeight.w600),
            ),
          ],
        ),
      ),
    );
  }
}
