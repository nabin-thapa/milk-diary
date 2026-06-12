import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:milk_diary/features/dashboard/presentation/screens/dashboard_screen.dart';
import 'package:milk_diary/features/customers/presentation/screens/customers_screen.dart';
import 'package:milk_diary/features/reports/presentation/screens/reports_screen.dart';
import 'package:milk_diary/features/profile/presentation/screens/profile_screen.dart';

final GlobalKey<NavigatorState> _rootNavigatorKey = GlobalKey<NavigatorState>(debugLabel: 'root');
final GlobalKey<NavigatorState> _shellNavigatorKey = GlobalKey<NavigatorState>(debugLabel: 'shell');

final GoRouter appRouter = GoRouter(
  navigatorKey: _rootNavigatorKey,
  initialLocation: '/home',
  routes: [
    ShellRoute(
      navigatorKey: _shellNavigatorKey,
      builder: (context, state, child) {
        return ScaffoldWithNavBar(child: child);
      },
      routes: [
        GoRoute(
          path: '/home',
          builder: (context, state) => const DashboardScreen(),
        ),
        GoRoute(
          path: '/sales',
          builder: (context, state) => const PlaceholderScreen(title: 'Sales History'),
        ),
        GoRoute(
          path: '/customers',
          builder: (context, state) => const CustomersScreen(),
        ),
        GoRoute(
          path: '/reports',
          builder: (context, state) => const ReportsScreen(),
        ),
        GoRoute(
          path: '/profile',
          builder: (context, state) => const ProfileScreen(),
        ),
      ],
    ),
  ],
);

class ScaffoldWithNavBar extends StatelessWidget {
  final Widget child;

  const ScaffoldWithNavBar({required this.child, super.key});

  @override
  Widget build(BuildContext context) {
    final state = GoRouterState.of(context);
    final location = state.uri.toString();
    
    int getSelectedIndex() {
      if (location.startsWith('/home')) return 0;
      if (location.startsWith('/sales')) return 1;
      if (location.startsWith('/customers')) return 2;
      if (location.startsWith('/reports')) return 3;
      if (location.startsWith('/profile')) return 4;
      return 0;
    }

    void onItemTapped(int index, BuildContext context) {
      switch (index) {
        case 0:
          context.go('/home');
          break;
        case 1:
          context.go('/sales');
          break;
        case 2:
          context.go('/customers');
          break;
        case 3:
          context.go('/reports');
          break;
        case 4:
          context.go('/profile');
          break;
      }
    }

    return Scaffold(
      body: child,
      bottomNavigationBar: BottomNavigationBar(
        currentIndex: getSelectedIndex(),
        onTap: (index) => onItemTapped(index, context),
        type: BottomNavigationBarType.fixed,
        items: const [
          BottomNavigationBarItem(icon: Icon(Icons.home_outlined), activeIcon: Icon(Icons.home), label: 'Home'),
          BottomNavigationBarItem(icon: Icon(Icons.monetization_on_outlined), activeIcon: Icon(Icons.monetization_on), label: 'Sales'),
          BottomNavigationBarItem(icon: Icon(Icons.people_outline), activeIcon: Icon(Icons.people), label: 'Contacts'),
          BottomNavigationBarItem(icon: Icon(Icons.bar_chart_outlined), activeIcon: Icon(Icons.bar_chart), label: 'Reports'),
          BottomNavigationBarItem(icon: Icon(Icons.person_outline), activeIcon: Icon(Icons.person), label: 'Profile'),
        ],
      ),
    );
  }
}

class PlaceholderScreen extends StatelessWidget {
  final String title;
  const PlaceholderScreen({required this.title, super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text(title)),
      body: Center(
        child: Text(
          '$title Screen Content Placeholder',
          style: Theme.of(context).textTheme.bodyLarge,
        ),
      ),
    );
  }
}
