import 'package:flutter/material.dart';
import 'package:milk_diary/core/theme/app_theme.dart';
import 'package:milk_diary/shared/widgets/custom_card.dart';

class AddEntryBottomSheet extends StatefulWidget {
  final String entryType; // 'purchase' or 'sale'
  final Function(String milkType, double liters, double rate, double total, bool isPaid) onSave;

  const AddEntryBottomSheet({
    super.key,
    required this.entryType,
    required this.onSave,
  });

  @override
  State<AddEntryBottomSheet> createState() => _AddEntryBottomSheetState();
}

class _AddEntryBottomSheetState extends State<AddEntryBottomSheet> {
  final _formKey = GlobalKey<FormState>();
  final _litersController = TextEditingController();
  final _rateController = TextEditingController();

  String _selectedMilkType = 'cow';
  bool _isPaid = true;
  double _liveTotal = 0.0;

  @override
  void initState() {
    super.initState();
    _rateController.text = _selectedMilkType == 'cow' ? '85' : '115';
    _litersController.addListener(_calculateTotal);
    _rateController.addListener(_calculateTotal);
  }

  void _calculateTotal() {
    final liters = double.tryParse(_litersController.text) ?? 0.0;
    final rate = double.tryParse(_rateController.text) ?? 0.0;
    setState(() {
      _liveTotal = liters * rate;
    });
  }

  void _setMilkType(String type) {
    setState(() {
      _selectedMilkType = type;
      _rateController.text = type == 'cow' ? '85' : '115';
    });
  }

  void _submitForm() {
    if (_formKey.currentState!.validate()) {
      final liters = double.parse(_litersController.text);
      final rate = double.parse(_rateController.text);
      widget.onSave(_selectedMilkType, liters, rate, _liveTotal, _isPaid);
      Navigator.of(context).pop();
    }
  }

  @override
  void dispose() {
    _litersController.dispose();
    _rateController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final isDark = theme.brightness == Brightness.dark;
    final isPurchase = widget.entryType == 'purchase';

    return Container(
      decoration: BoxDecoration(
        color: theme.scaffoldBackgroundColor,
        borderRadius: const BorderRadius.only(
          topLeft: Radius.circular(32),
          topRight: Radius.circular(32),
        ),
        border: Border.all(
          color: isDark ? const Color(0xFF1E293B) : const Color(0xFFE2E8F0),
        ),
      ),
      padding: EdgeInsets.only(
        left: 24,
        right: 24,
        top: 8,
        bottom: MediaQuery.of(context).viewInsets.bottom + 24,
      ),
      child: Column(
        mainAxisSize: MainAxisSize.min,
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          // Drag Handle
          Center(
            child: Container(
              width: 36,
              height: 5,
              decoration: BoxDecoration(
                color: isDark ? const Color(0xFF334155) : const Color(0xFFCBD5E1),
                borderRadius: BorderRadius.circular(3),
              ),
            ),
          ),
          const SizedBox(height: 16),
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Text(
                isPurchase ? 'Add Purchased Record' : 'Add Sale Record',
                style: theme.textTheme.titleLarge,
              ),
              IconButton(
                icon: const Icon(Icons.close),
                onPressed: () => Navigator.of(context).pop(),
              )
            ],
          ),
          const SizedBox(height: 16),
          Form(
            key: _formKey,
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                // 1. Milk Type Selection
                Text('Milk Type', style: theme.textTheme.bodyMedium?.copyWith(fontWeight: FontWeight.bold)),
                const SizedBox(height: 8),
                Row(
                  children: [
                    Expanded(
                      child: _SegmentButton(
                        label: 'Cow Milk 🐄',
                        isActive: _selectedMilkType == 'cow',
                        onTap: () => _setMilkType('cow'),
                      ),
                    ),
                    const SizedBox(width: 12),
                    Expanded(
                      child: _SegmentButton(
                        label: 'Buffalo Milk 🦬',
                        isActive: _selectedMilkType == 'buffalo',
                        onTap: () => _setMilkType('buffalo'),
                      ),
                    ),
                  ],
                ),
                const SizedBox(height: 16),

                // 2. Associated Contact
                Text(
                  isPurchase ? 'Supplier Source' : 'Customer Target',
                  style: theme.textTheme.bodyMedium?.copyWith(fontWeight: FontWeight.bold),
                ),
                const SizedBox(height: 8),
                DropdownButtonFormField<String>(
                  value: '1',
                  decoration: InputDecoration(
                    fillColor: isDark ? const Color(0xFF1C2538) : const Color(0xFFF8FAFC),
                    filled: true,
                    contentPadding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
                    border: OutlineInputBorder(
                      borderRadius: BorderRadius.circular(12),
                      borderSide: BorderSide(color: isDark ? const Color(0xFF334155) : const Color(0xFFCBD5E1)),
                    ),
                  ),
                  items: [
                    DropdownMenuItem(
                      value: '1',
                      child: Text(isPurchase ? 'Gopal Dairy Farm' : 'Krishna Prasad'),
                    ),
                    DropdownMenuItem(
                      value: '2',
                      child: Text(isPurchase ? 'Himalayan Cow Ranch' : 'Sita Devi Shrestha'),
                    ),
                  ],
                  onChanged: (val) {},
                ),
                const SizedBox(height: 16),

                // 3. Liters and Rate Fields
                Row(
                  children: [
                    Expanded(
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text('Volume (Liters)', style: theme.textTheme.bodyMedium?.copyWith(fontWeight: FontWeight.bold)),
                          const SizedBox(height: 8),
                          TextFormField(
                            controller: _litersController,
                            keyboardType: const TextInputType.numberWithOptions(decimal: true),
                            decoration: InputDecoration(
                              hintText: '0.00',
                              fillColor: isDark ? const Color(0xFF1C2538) : const Color(0xFFF8FAFC),
                              filled: true,
                              border: OutlineInputBorder(borderRadius: BorderRadius.circular(12)),
                            ),
                            validator: (val) => val == null || double.tryParse(val) == null || double.parse(val) <= 0
                                ? 'Invalid volume'
                                : null,
                          ),
                        ],
                      ),
                    ),
                    const SizedBox(width: 16),
                    Expanded(
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text('Rate per Liter', style: theme.textTheme.bodyMedium?.copyWith(fontWeight: FontWeight.bold)),
                          const SizedBox(height: 8),
                          TextFormField(
                            controller: _rateController,
                            keyboardType: const TextInputType.numberWithOptions(decimal: true),
                            decoration: InputDecoration(
                              hintText: '0.00',
                              fillColor: isDark ? const Color(0xFF1C2538) : const Color(0xFFF8FAFC),
                              filled: true,
                              border: OutlineInputBorder(borderRadius: BorderRadius.circular(12)),
                            ),
                            validator: (val) => val == null || double.tryParse(val) == null || double.parse(val) <= 0
                                ? 'Invalid rate'
                                : null,
                          ),
                        ],
                      ),
                    ),
                  ],
                ),
                const SizedBox(height: 20),

                // 4. Live Total Banner
                CustomCard(
                  padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
                  borderRadius: 16,
                  gradientColors: isDark
                      ? [const Color(0xFF064E3B).withOpacity(0.4), const Color(0xFF065F46).withOpacity(0.4)]
                      : [AppTheme.primaryGreenLight, const Color(0xFFD1FAE5)],
                  child: Row(
                    mainAxisAlignment: MainAxisAlignment.spaceBetween,
                    children: [
                      Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text(
                            'Estimated Amount',
                            style: TextStyle(
                              color: isDark ? const Color(0xFF34D399) : AppTheme.primaryGreen,
                              fontWeight: FontWeight.bold,
                              fontSize: 12,
                            ),
                          ),
                          const Text(
                            'Auto calculates Liters × Rate',
                            style: TextStyle(color: Colors.black38, fontSize: 10),
                          ),
                        ],
                      ),
                      Text(
                        'Rs. ${_liveTotal.toStringAsFixed(2)}',
                        style: TextStyle(
                          fontSize: 20,
                          fontWeight: FontWeight.w800,
                          color: isDark ? const Color(0xFF34D399) : AppTheme.primaryGreen,
                        ),
                      )
                    ],
                  ),
                ),
                const SizedBox(height: 16),

                // 5. Payment details for sales
                if (!isPurchase)
                  Row(
                    mainAxisAlignment: MainAxisAlignment.spaceBetween,
                    children: [
                      Text('Is Amount Fully Paid?', style: theme.textTheme.bodyMedium?.copyWith(fontWeight: FontWeight.bold)),
                      Switch(
                        value: _isPaid,
                        onChanged: (val) => setState(() => _isPaid = val),
                        activeColor: isDark ? const Color(0xFF10B981) : AppTheme.primaryGreen,
                      )
                    ],
                  ),

                const SizedBox(height: 20),
                ElevatedButton(
                  onPressed: _submitForm,
                  child: const Text('Confirm and Save Entry'),
                ),
              ],
            ),
          )
        ],
      ),
    );
  }
}

class _SegmentButton extends StatelessWidget {
  final String label;
  final bool isActive;
  final VoidCallback onTap;

  const _SegmentButton({
    required this.label,
    required this.isActive,
    required this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final isDark = theme.brightness == Brightness.dark;

    return GestureDetector(
      onTap: onTap,
      child: Container(
        padding: const EdgeInsets.symmetric(vertical: 12),
        decoration: BoxDecoration(
          color: isActive
              ? (isDark ? const Color(0xFF1E293B) : Colors.white)
              : (isDark ? const Color(0xFF131926) : const Color(0xFFF1F5F9)),
          borderRadius: BorderRadius.circular(12),
          border: Border.all(
            color: isActive
                ? (isDark ? const Color(0xFF10B981) : AppTheme.primaryGreen)
                : (isDark ? const Color(0xFF1E293B) : const Color(0xFFE2E8F0)),
            width: 1.5,
          ),
          boxShadow: isActive ? [BoxShadow(color: Colors.black.withOpacity(0.04), blurRadius: 4)] : [],
        ),
        alignment: Alignment.center,
        child: Text(
          label,
          style: TextStyle(
            fontSize: 13,
            fontWeight: FontWeight.bold,
            color: isActive
                ? (isDark ? const Color(0xFF10B981) : AppTheme.primaryGreen)
                : theme.textTheme.bodyMedium?.color,
          ),
        ),
      ),
    );
  }
}
