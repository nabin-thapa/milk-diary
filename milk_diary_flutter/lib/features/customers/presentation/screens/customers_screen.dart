import 'package:flutter/material.dart';
import 'package:milk_diary/core/theme/app_theme.dart';
import 'package:milk_diary/shared/widgets/custom_card.dart';

class CustomersScreen extends StatefulWidget {
  const CustomersScreen({super.key});

  @override
  State<CustomersScreen> createState() => _CustomersScreenState();
}

class _CustomersScreenState extends State<CustomersScreen> {
  String _activeTab = 'customers'; // 'customers' or 'suppliers'
  final _searchController = TextEditingController();

  final List<Map<String, dynamic>> _contacts = [
    { 'id': 'c1', 'name': 'Krishna Prasad', 'phone': '9841234567', 'address': 'Baluwatar, KTM', 'role': 'customer', 'volume': 140.0, 'pending': 3300.0 },
    { 'id': 'c2', 'name': 'Sita Devi Shrestha', 'phone': '9803124567', 'address': 'Satdobato, Lalitpur', 'role': 'customer', 'volume': 85.0, 'pending': 3750.0 },
    { 'id': 'c3', 'name': 'Hari Bahadur Thapa', 'phone': '9851023456', 'address': 'Koteshwor, KTM', 'role': 'customer', 'volume': 190.0, 'pending': 0.0 },
    { 'id': 's1', 'name': 'Gopal Dairy Farm', 'phone': '9813567890', 'address': 'Bhaktapur', 'role': 'supplier', 'volume': 410.0, 'pending': 0.0 },
    { 'id': 's2', 'name': 'Organo Buffalo Breed', 'phone': '9845012345', 'address': 'Kirtipur', 'role': 'supplier', 'volume': 280.0, 'pending': 0.0 },
    { 'id': 's3', 'name': 'Himalayan Cow Ranch', 'phone': '9808987654', 'address': 'Godavari', 'role': 'supplier', 'volume': 320.0, 'pending': 0.0 },
  ];

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final isDark = theme.brightness == Brightness.dark;

    final roleFilter = _activeTab == 'customers' ? 'customer' : 'supplier';
    final query = _searchController.text.toLowerCase();
    
    final filteredContacts = _contacts.where((c) {
      final matchesRole = c['role'] == roleFilter;
      final matchesSearch = c['name'].toLowerCase().contains(query) || c['phone'].contains(query);
      return matchesRole && matchesSearch;
    }).toList();

    return Scaffold(
      appBar: AppBar(
        title: Text(_activeTab == 'customers' ? 'Customer Directory' : 'Supplier Directory'),
        bottom: PreferredSize(
          preferredSize: const Size.fromHeight(60.0),
          child: Padding(
            padding: const EdgeInsets.symmetric(horizontal: 16.0, vertical: 8.0),
            child: Container(
              height: 44,
              padding: const EdgeInsets.all(2),
              decoration: BoxDecoration(
                color: isDark ? const Color(0xFF131926) : const Color(0xFFF1F5F9),
                borderRadius: BorderRadius.circular(12),
                border: Border.all(color: isDark ? const Color(0xFF1E293B) : const Color(0xFFE2E8F0)),
              ),
              child: Row(
                children: [
                  Expanded(
                    child: _SegmentTabButton(
                      label: 'Customers',
                      isActive: _activeTab == 'customers',
                      onTap: () => setState(() => _activeTab = 'customers'),
                    ),
                  ),
                  Expanded(
                    child: _SegmentTabButton(
                      label: 'Suppliers',
                      isActive: _activeTab == 'suppliers',
                      onTap: () => setState(() => _activeTab = 'suppliers'),
                    ),
                  ),
                ],
              ),
            ),
          ),
        ),
      ),
      body: Column(
        children: [
          // Search Field
          Padding(
            padding: const EdgeInsets.all(16.0),
            child: Row(
              children: [
                Expanded(
                  child: Container(
                    decoration: BoxDecoration(
                      color: theme.cardTheme.color,
                      borderRadius: BorderRadius.circular(14),
                      border: Border.all(color: isDark ? const Color(0xFF1E293B) : const Color(0xFFE2E8F0)),
                      boxShadow: [
                        BoxShadow(
                          color: Colors.black.withOpacity(0.02),
                          blurRadius: 8,
                          offset: const Offset(0, 2),
                        )
                      ],
                    ),
                    padding: const EdgeInsets.symmetric(horizontal: 14),
                    child: TextField(
                      controller: _searchController,
                      onChanged: (val) => setState(() {}),
                      decoration: const InputDecoration(
                        icon: Icon(Icons.search, color: Colors.grey, size: 20),
                        hintText: 'Search directory...',
                        border: InputBorder.none,
                      ),
                    ),
                  ),
                ),
                const SizedBox(width: 12),
                GestureDetector(
                  onTap: () => _openAddContactDialog(context),
                  child: Container(
                    width: 46,
                    height: 46,
                    decoration: BoxDecoration(
                      color: isDark ? const Color(0xFF10B981) : AppTheme.primaryGreen,
                      borderRadius: BorderRadius.circular(14),
                    ),
                    child: const Icon(Icons.add, color: Colors.white, size: 24),
                  ),
                )
              ],
            ),
          ),
          
          // Contacts List
          Expanded(
            child: filteredContacts.isEmpty
                ? Center(
                    child: Text(
                      'No ${_activeTab} found.',
                      style: theme.textTheme.bodyMedium?.copyWith(color: Colors.grey),
                    ),
                  )
                : ListView.builder(
                    padding: const EdgeInsets.symmetric(horizontal: 16.0),
                    itemCount: filteredContacts.length,
                    itemBuilder: (context, index) {
                      final c = filteredContacts[index];
                      return Padding(
                        padding: const EdgeInsets.only(bottom: 12.0),
                        child: CustomCard(
                          padding: const EdgeInsets.all(16.0),
                          child: Row(
                            children: [
                              CircleAvatar(
                                radius: 20,
                                backgroundColor: AppTheme.primaryGreenLight,
                                child: Text(
                                  c['name'][0],
                                  style: TextStyle(
                                    fontWeight: FontWeight.bold,
                                    color: isDark ? const Color(0xFF10B981) : AppTheme.primaryGreen,
                                  ),
                                ),
                              ),
                              const SizedBox(width: 14),
                              Expanded(
                                child: Column(
                                  crossAxisAlignment: CrossAxisAlignment.start,
                                  children: [
                                    Text(c['name'], style: theme.textTheme.titleMedium?.copyWith(fontSize: 15)),
                                    const SizedBox(height: 2),
                                    Text('📞 ${c['phone']} • ${c['address']}', style: theme.textTheme.bodyMedium?.copyWith(fontSize: 12)),
                                    const SizedBox(height: 4),
                                    Text(
                                      'Total Volume: ${c['volume'].toStringAsFixed(1)} L' +
                                          (_activeTab == 'customers'
                                              ? ' | Pending: Rs. ${c['pending'].toStringAsFixed(1)}'
                                              : ''),
                                      style: TextStyle(
                                        fontSize: 11,
                                        fontWeight: FontWeight.w600,
                                        color: c['pending'] > 0 ? AppTheme.pendingOrange : Colors.grey,
                                      ),
                                    ),
                                  ],
                                ),
                              ),
                              Row(
                                children: [
                                  _ContactActionButton(
                                    icon: Icons.phone,
                                    onTap: () {},
                                  ),
                                  const SizedBox(width: 6),
                                  _ContactActionButton(
                                    icon: Icons.delete_outline,
                                    onTap: () {
                                      setState(() {
                                        _contacts.removeWhere((item) => item['id'] == c['id']);
                                      });
                                    },
                                  ),
                                ],
                              ),
                            ],
                          ),
                        ),
                      );
                    },
                  ),
          ),
        ],
      ),
    );
  }

  void _openAddContactDialog(BuildContext context) {
    final nameCtrl = TextEditingController();
    final phoneCtrl = TextEditingController();
    final addressCtrl = TextEditingController();

    showModalBottomSheet(
      context: context,
      isScrollControlled: true,
      backgroundColor: Colors.transparent,
      builder: (context) {
        final theme = Theme.of(context);
        final isDark = theme.brightness == Brightness.dark;
        return Container(
          decoration: BoxDecoration(
            color: theme.scaffoldBackgroundColor,
            borderRadius: const BorderRadius.only(topLeft: Radius.circular(32), topRight: Radius.circular(32)),
          ),
          padding: EdgeInsets.only(
            left: 24,
            right: 24,
            top: 16,
            bottom: MediaQuery.of(context).viewInsets.bottom + 24,
          ),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text('New Contact', style: theme.textTheme.titleLarge),
              const SizedBox(height: 16),
              TextField(
                controller: nameCtrl,
                decoration: const InputDecoration(labelText: 'Full Name', border: OutlineInputBorder()),
              ),
              const SizedBox(height: 12),
              TextField(
                controller: phoneCtrl,
                keyboardType: TextInputType.phone,
                decoration: const InputDecoration(labelText: 'Phone Number', border: OutlineInputBorder()),
              ),
              const SizedBox(height: 12),
              TextField(
                controller: addressCtrl,
                decoration: const InputDecoration(labelText: 'Address', border: OutlineInputBorder()),
              ),
              const SizedBox(height: 20),
              ElevatedButton(
                onPressed: () {
                  if (nameCtrl.text.isNotEmpty && phoneCtrl.text.isNotEmpty) {
                    setState(() {
                      _contacts.add({
                        'id': 'c_${Date.now()}',
                        'name': nameCtrl.text.trim(),
                        'phone': phoneCtrl.text.trim(),
                        'address': addressCtrl.text.trim(),
                        'role': _activeTab == 'customers' ? 'customer' : 'supplier',
                        'volume': 0.0,
                        'pending': 0.0,
                      });
                    });
                    Navigator.of(context).pop();
                  }
                },
                child: const Text('Add to Directory'),
              ),
            ],
          ),
        );
      },
    );
  }
}

class _SegmentTabButton extends StatelessWidget {
  final String label;
  final bool isActive;
  final VoidCallback onTap;

  const _SegmentTabButton({
    required this.label,
    required this.isActive,
    required this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return GestureDetector(
      onTap: onTap,
      child: Container(
        alignment: Alignment.center,
        decoration: BoxDecoration(
          color: isActive ? theme.cardTheme.color : Colors.transparent,
          borderRadius: BorderRadius.circular(10),
          boxShadow: isActive ? [BoxShadow(color: Colors.black.withOpacity(0.03), blurRadius: 4)] : [],
        ),
        child: Text(
          label,
          style: TextStyle(
            fontWeight: FontWeight.bold,
            fontSize: 13,
            color: isActive
                ? (theme.brightness == Brightness.dark ? const Color(0xFF10B981) : AppTheme.primaryGreen)
                : Colors.grey,
          ),
        ),
      ),
    );
  }
}

class _ContactActionButton extends StatelessWidget {
  final IconData icon;
  final VoidCallback onTap;

  const _ContactActionButton({required this.icon, required this.onTap});

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final isDark = theme.brightness == Brightness.dark;
    return GestureDetector(
      onTap: onTap,
      child: Container(
        width: 32,
        height: 32,
        decoration: BoxDecoration(
          color: isDark ? const Color(0xFF1C2538) : const Color(0xFFF8FAFC),
          borderRadius: BorderRadius.circular(8),
          border: Border.all(color: isDark ? const Color(0xFF1E293B) : const Color(0xFFE2E8F0)),
        ),
        child: Icon(icon, size: 16, color: Colors.grey),
      ),
    );
  }
}
