import 'package:flutter/material.dart';
import 'package:fl_chart/fl_chart.dart';
import 'package:milk_diary/core/theme/app_theme.dart';
import 'package:milk_diary/shared/widgets/custom_card.dart';

class ReportsScreen extends StatefulWidget {
  const ReportsScreen({super.key});

  @override
  State<ReportsScreen> createState() => _ReportsScreenState();
}

class _ReportsScreenState extends State<ReportsScreen> {
  int _currentMonth = 5; // June
  int _currentYear = 2026;
  
  final List<String> _months = [
    'January', 'February', 'March', 'April', 'May', 'June',
    'July', 'August', 'September', 'October', 'November', 'December'
  ];

  void _adjustMonth(int step) {
    setState(() {
      _currentMonth += step;
      if (_currentMonth < 0) {
        _currentMonth = 11;
        _currentYear -= 1;
      } else if (_currentMonth > 11) {
        _currentMonth = 0;
        _currentYear += 1;
      }
    });
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final isDark = theme.brightness == Brightness.dark;

    return Scaffold(
      appBar: AppBar(
        title: const Text('Reports & Analytics'),
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(16.0),
        child: Column(
          children: [
            // 1. Month Picker Card
            CustomCard(
              padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
              child: Column(
                children: [
                  Row(
                    mainAxisAlignment: MainAxisAlignment.spaceBetween,
                    children: [
                      IconButton(
                        icon: const Icon(Icons.arrow_back_ios, size: 16),
                        onPressed: () => _adjustMonth(-1),
                      ),
                      Text(
                        '${_months[_currentMonth]} $_currentYear',
                        style: theme.textTheme.titleMedium?.copyWith(fontSize: 16, fontWeight: FontWeight.bold),
                      ),
                      IconButton(
                        icon: const Icon(Icons.arrow_forward_ios, size: 16),
                        onPressed: () => _adjustMonth(1),
                      ),
                    ],
                  ),
                  const SizedBox(height: 12),
                  const Divider(height: 1),
                  const SizedBox(height: 12),
                  Row(
                    mainAxisAlignment: MainAxisAlignment.spaceAround,
                    children: [
                      _ReportStatItem(label: 'Total Liters', value: '448.00 L'),
                      Container(width: 1, height: 36, color: isDark ? const Color(0xFF1E293B) : const Color(0xFFE2E8F0)),
                      _ReportStatItem(label: 'Total Revenue', value: 'Rs. 26,414.00', valueColor: AppTheme.earningGreen),
                    ],
                  )
                ],
              ),
            ),

            const SizedBox(height: 16),

            // 2. Linear Chart Card
            CustomCard(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text('Daily Sales Analytics', style: theme.textTheme.titleMedium),
                  const Text('Volume of Milk Sold (Liters)', style: TextStyle(color: Colors.grey, fontSize: 11)),
                  const SizedBox(height: 24),
                  
                  // Graph implementation using fl_chart
                  SizedBox(
                    height: 160,
                    child: LineChart(
                      LineChartData(
                        gridData: FlGridData(
                          show: true,
                          drawVerticalLine: false,
                          getDrawingHorizontalLine: (value) => FlLine(
                            color: isDark ? const Color(0xFF1E293B) : const Color(0xFFF1F5F9),
                            strokeWidth: 1,
                          ),
                        ),
                        titlesData: const FlTitlesData(show: false),
                        borderData: FlBorderData(show: false),
                        minX: 0,
                        maxX: 7,
                        minY: 0,
                        maxY: 60,
                        lineBarsData: [
                          LineChartBarData(
                            spots: const [
                              FlSpot(0, 30),
                              FlSpot(1, 38),
                              FlSpot(2, 28),
                              FlSpot(3, 45),
                              FlSpot(4, 35),
                              FlSpot(5, 40),
                              FlSpot(6, 50),
                              FlSpot(7, 48),
                            ],
                            isCurved: true,
                            gradient: const LinearGradient(
                              colors: [AppTheme.primaryGreen, Color(0xFF10B981)],
                            ),
                            barWidth: 3,
                            isStrokeCapRound: true,
                            dotData: const FlDotData(show: true),
                            belowBarData: BarAreaData(
                              show: true,
                              gradient: LinearGradient(
                                colors: [
                                  AppTheme.primaryGreen.withOpacity(0.2),
                                  AppTheme.primaryGreen.withOpacity(0.0),
                                ],
                                begin: Alignment.topCenter,
                                end: Alignment.bottomCenter,
                              ),
                            ),
                          ),
                        ],
                      ),
                    ),
                  ),

                  const SizedBox(height: 16),
                  const Divider(height: 1),
                  const SizedBox(height: 16),

                  // Data Details
                  GridView.count(
                    crossAxisCount: 2,
                    shrinkWrap: true,
                    physics: const NeverScrollableScrollPhysics(),
                    childAspectRatio: 3.0,
                    mainAxisSpacing: 10,
                    crossAxisSpacing: 10,
                    children: [
                      _DetailItem(label: 'Daily Avg Sold', value: '38.50 L'),
                      _DetailItem(label: 'Buffalo Milk Sold', value: '210.00 L'),
                      _DetailItem(label: 'Cow Milk Sold', value: '238.00 L'),
                      _DetailItem(label: 'Outstanding Pending', value: 'Rs. 3,750.00', valueColor: AppTheme.pendingOrange),
                    ],
                  )
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }
}

class _ReportStatItem extends StatelessWidget {
  final String label;
  final String value;
  final Color? valueColor;

  const _ReportStatItem({
    required this.label,
    required this.value,
    this.valueColor,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return Column(
      children: [
        Text(label, style: theme.textTheme.bodyMedium?.copyWith(fontSize: 12)),
        const SizedBox(height: 2),
        Text(value, style: theme.textTheme.titleMedium?.copyWith(fontWeight: FontWeight.bold, color: valueColor)),
      ],
    );
  }
}

class _DetailItem extends StatelessWidget {
  final String label;
  final String value;
  final Color? valueColor;

  const _DetailItem({
    required this.label,
    required this.value,
    this.valueColor,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      mainAxisAlignment: MainAxisAlignment.center,
      children: [
        Text(label, style: theme.textTheme.bodyMedium?.copyWith(fontSize: 11)),
        const SizedBox(height: 2),
        Text(value, style: theme.textTheme.titleMedium?.copyWith(fontSize: 13, fontWeight: FontWeight.bold, color: valueColor)),
      ],
    );
  }
}
