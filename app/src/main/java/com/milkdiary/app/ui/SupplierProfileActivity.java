package com.milkdiary.app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.milkdiary.app.R;
import com.milkdiary.app.databinding.ActivitySupplierProfileBinding;
import com.milkdiary.app.db.MilkRecordDao;
import com.milkdiary.app.db.PaymentDao;
import com.milkdiary.app.db.SaleDao;
import com.milkdiary.app.db.SupplierDao;
import com.milkdiary.app.model.MilkRecord;
import com.milkdiary.app.model.Payment;
import com.milkdiary.app.model.Supplier;
import com.milkdiary.app.util.DateUtils;
import com.milkdiary.app.util.FormatUtils;
import com.milkdiary.app.util.RoleManager;

import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SupplierProfileActivity extends AppCompatActivity {

    private ActivitySupplierProfileBinding binding;
    private SupplierDao supplierDao;
    private MilkRecordDao recordDao;
    private PaymentDao paymentDao;
    private SaleDao saleDao;
    private long supplierId = -1;
    private String selectedYearMonth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RoleManager.requireDairy(this);
        binding = ActivitySupplierProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Supplier Profile");
        }

        supplierDao = new SupplierDao(this);
        recordDao = new MilkRecordDao(this);
        paymentDao = new PaymentDao(this);
        saleDao = new SaleDao(this);

        supplierId = getIntent().getLongExtra("supplier_id", -1);
        if (supplierId == -1) {
            finish();
            return;
        }

        selectedYearMonth = DateUtils.currentMonthDb();
        setupMonthPicker();
        setupEntryButtons();
        setupRecordPayment();
        setupHistoryButton();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProfile();
    }

    private void loadProfile() {
        Supplier s = supplierDao.getById(supplierId);
        if (s == null) {
            finish();
            return;
        }

        binding.tvSupplierName.setText(s.getName());
        if (s.getPhone() != null && !s.getPhone().isEmpty())
            binding.tvSupplierPhone.setText(s.getPhone());
        else
            binding.tvSupplierPhone.setVisibility(View.GONE);

        if (s.getVillage() != null && !s.getVillage().isEmpty())
            binding.tvSupplierVillage.setText(s.getVillage());
        else
            binding.tvSupplierVillage.setVisibility(View.GONE);

        StringBuilder rates = new StringBuilder();
        if (s.getDefaultCowRate() > 0)
            rates.append("Cow: Rs.").append(String.format("%.2f", s.getDefaultCowRate())).append("/L");
        if (s.getDefaultBuffaloRate() > 0) {
            if (rates.length() > 0) rates.append("  |  ");
            rates.append("Buffalo: Rs.").append(String.format("%.2f", s.getDefaultBuffaloRate())).append("/L");
        }
        if (rates.length() > 0) binding.tvSupplierRates.setText(rates.toString());
        else binding.tvSupplierRates.setVisibility(View.GONE);

        loadTodayEntries();
        loadMonthlyTable();
        loadPaymentSummary();
        loadPaymentHistory();
    }

    // ---- TODAY'S ENTRIES (Morning + Evening) ----

    private void loadTodayEntries() {
        String today = DateUtils.todayDb();
        List<MilkRecord> todayRecords = recordDao.getRecordsBySupplierAndDate(supplierId, today);

        boolean morningFound = false;
        boolean eveningFound = false;

        for (MilkRecord r : todayRecords) {
            if (r.isMorning()) {
                morningFound = true;
                binding.tvMorningStatus.setText("Morning: Recorded");
                binding.tvMorningStatus.setTextColor(getResources().getColor(R.color.earnings_color));
                String details = formatRecordDetails(r);
                binding.tvMorningDetails.setText(details);
                binding.tvMorningDetails.setVisibility(View.VISIBLE);
            } else {
                eveningFound = true;
                binding.tvEveningStatus.setText("Evening: Recorded");
                binding.tvEveningStatus.setTextColor(getResources().getColor(R.color.earnings_color));
                String details = formatRecordDetails(r);
                binding.tvEveningDetails.setText(details);
                binding.tvEveningDetails.setVisibility(View.VISIBLE);
            }
        }

        if (!morningFound) {
            binding.tvMorningStatus.setText("Morning: No entry");
            binding.tvMorningStatus.setTextColor(getResources().getColor(R.color.text_secondary));
            binding.tvMorningDetails.setVisibility(View.GONE);
        }
        if (!eveningFound) {
            binding.tvEveningStatus.setText("Evening: No entry");
            binding.tvEveningStatus.setTextColor(getResources().getColor(R.color.text_secondary));
            binding.tvEveningDetails.setVisibility(View.GONE);
        }
    }

    private String formatRecordDetails(MilkRecord r) {
        StringBuilder sb = new StringBuilder();
        if (r.getCowLiters() > 0)
            sb.append(String.format("Cow: %.1fL", r.getCowLiters()));
        if (r.getBuffaloLiters() > 0) {
            if (sb.length() > 0) sb.append(" + ");
            sb.append(String.format("Buffalo: %.1fL", r.getBuffaloLiters()));
        }
        sb.append(" = ").append(FormatUtils.money(r.getTotal()));
        if (r.getNote() != null && !r.getNote().isEmpty())
            sb.append("\nNote: ").append(r.getNote());
        return sb.toString();
    }

    // ---- ENTRY BUTTONS ----

    private void setupEntryButtons() {
        binding.btnAddMorning.setOnClickListener(v -> openAddRecord(MilkRecord.SESSION_MORNING));
        binding.btnAddEvening.setOnClickListener(v -> openAddRecord(MilkRecord.SESSION_EVENING));
    }

    private void openAddRecord(String sessionType) {
        String today = DateUtils.todayDb();
        MilkRecord existing = recordDao.getRecordBySupplierDateAndSession(supplierId, today, sessionType);
        if (existing != null) {
            String label = sessionType.equals(MilkRecord.SESSION_MORNING) ? "Morning" : "Evening";
            new AlertDialog.Builder(this)
                    .setTitle("Edit " + label + " Entry?")
                    .setMessage(label + " milk already recorded for today. Edit it?")
                    .setPositiveButton("Edit", (d, w) -> {
                        Intent intent = new Intent(this, AddRecordActivity.class);
                        intent.putExtra("supplier_id", supplierId);
                        intent.putExtra("session_type", sessionType);
                        intent.putExtra(AddRecordActivity.EXTRA_RECORD_ID, existing.getId());
                        startActivity(intent);
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        } else {
            Intent intent = new Intent(this, AddRecordActivity.class);
            intent.putExtra("supplier_id", supplierId);
            intent.putExtra("session_type", sessionType);
            startActivity(intent);
        }
    }

    // ---- MONTHLY TABLE ----

    private void setupMonthPicker() {
        updateMonthLabel();
        binding.btnPrevMonth.setOnClickListener(v -> {
            selectedYearMonth = shiftMonth(selectedYearMonth, -1);
            updateMonthLabel();
            loadMonthlyTable();
        });
        binding.btnNextMonth.setOnClickListener(v -> {
            selectedYearMonth = shiftMonth(selectedYearMonth, +1);
            updateMonthLabel();
            loadMonthlyTable();
        });
    }

    private String shiftMonth(String yearMonth, int delta) {
        int[] ym = DateUtils.getYearMonth(yearMonth);
        Calendar cal = Calendar.getInstance();
        cal.set(ym[0], ym[1] - 1, 1);
        cal.add(Calendar.MONTH, delta);
        return DateUtils.buildYearMonth(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1);
    }

    private void updateMonthLabel() {
        binding.tvSelectedMonth.setText(DateUtils.monthDbToDisplay(selectedYearMonth));
    }

    private void loadMonthlyTable() {
        List<MilkRecord> records = recordDao.getRecordsBySupplierAndMonth(supplierId, selectedYearMonth);
        binding.tableRows.removeAllViews();

        if (records.isEmpty()) {
            binding.tvNoMonthlyData.setVisibility(View.VISIBLE);
            binding.dividerTotal.setVisibility(View.GONE);
            binding.rowMonthlyTotal.setVisibility(View.GONE);
            return;
        }

        binding.tvNoMonthlyData.setVisibility(View.GONE);

        // Group records by date
        LinkedHashMap<String, MilkRecord[]> grouped = new LinkedHashMap<>();
        for (MilkRecord r : records) {
            MilkRecord[] pair = grouped.get(r.getDate());
            if (pair == null) {
                pair = new MilkRecord[]{null, null};
                grouped.put(r.getDate(), pair);
            }
            if (r.isMorning()) {
                pair[0] = r;
            } else {
                pair[1] = r;
            }
        }

        double totalMorningLiters = 0, totalEveningLiters = 0, totalAllLiters = 0, totalAllAmount = 0;
        boolean anyUnpaid = false;

        for (Map.Entry<String, MilkRecord[]> entry : grouped.entrySet()) {
            String date = entry.getKey();
            MilkRecord[] pair = entry.getValue();
            MilkRecord morning = pair[0];
            MilkRecord evening = pair[1];

            double morningL = morning != null ? (morning.getCowLiters() + morning.getBuffaloLiters()) : 0;
            double eveningL = evening != null ? (evening.getCowLiters() + evening.getBuffaloLiters()) : 0;
            double dayTotal = morningL + eveningL;
            double dayAmount = (morning != null ? morning.getTotal() : 0) + (evening != null ? evening.getTotal() : 0);

            totalMorningLiters += morningL;
            totalEveningLiters += eveningL;
            totalAllLiters += dayTotal;
            totalAllAmount += dayAmount;

            boolean hasUnpaid = (morning != null && !morning.isPaid()) || (evening != null && !evening.isPaid());
            if (hasUnpaid) anyUnpaid = true;

            addTableRow(date, morningL, eveningL, dayTotal, dayAmount, hasUnpaid, pair);
        }

        // Monthly total
        binding.dividerTotal.setVisibility(View.VISIBLE);
        binding.rowMonthlyTotal.setVisibility(View.VISIBLE);
        binding.tvTotalMorning.setText(FormatUtils.liters(totalMorningLiters));
        binding.tvTotalEvening.setText(FormatUtils.liters(totalEveningLiters));
        binding.tvTotalLiters.setText(FormatUtils.liters(totalAllLiters));
        binding.tvTotalAmount.setText(FormatUtils.money(totalAllAmount));

        if (anyUnpaid) {
            binding.tvTotalStatus.setText("Has Credit");
            binding.tvTotalStatus.setTextColor(getResources().getColor(R.color.pending_color));
        } else {
            binding.tvTotalStatus.setText("All Paid");
            binding.tvTotalStatus.setTextColor(getResources().getColor(R.color.earnings_color));
        }
    }

    private void addTableRow(String date, double morningL, double eveningL, double totalL, double amount, boolean hasUnpaid, MilkRecord[] pair) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(0, dpToPx(4), 0, dpToPx(4));

        row.addView(createCell(DateUtils.dbToShortDisplay(date), 2.2f, Gravity.START, R.color.text_primary, 12));
        row.addView(createCell(morningL > 0 ? String.format("%.1fL", morningL) : "-", 1.3f, Gravity.END, R.color.text_primary, 12));
        row.addView(createCell(eveningL > 0 ? String.format("%.1fL", eveningL) : "-", 1.3f, Gravity.END, R.color.text_primary, 12));
        row.addView(createCell(String.format("%.1fL", totalL), 1.3f, Gravity.END, R.color.text_primary, 12));
        row.addView(createCell(FormatUtils.money(amount), 1.8f, Gravity.END, R.color.primary, 12));

        // Paid/Unpaid status - clickable
        String statusText = hasUnpaid ? "Unpaid" : "Paid";
        int statusColor = hasUnpaid ? R.color.unpaid_text : R.color.paid_text;
        TextView statusCell = createCell(statusText, 1.3f, Gravity.END, statusColor, 11);
        statusCell.setOnClickListener(v -> toggleDayPaidStatus(date, pair));
        statusCell.setClickable(true);
        statusCell.setFocusable(true);
        statusCell.setBackgroundResource(R.drawable.bg_spinner);
        row.addView(statusCell);

        binding.tableRows.addView(row);

        // Add divider
        View divider = new View(this);
        divider.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dpToPx(1)));
        divider.setBackgroundColor(getResources().getColor(R.color.divider));
        binding.tableRows.addView(divider);
    }

    private void toggleDayPaidStatus(String date, MilkRecord[] pair) {
        boolean anyUnpaid = (pair[0] != null && !pair[0].isPaid()) || (pair[1] != null && !pair[1].isPaid());
        boolean newPaidStatus = anyUnpaid;
        
        if (pair[0] != null) {
            pair[0].setPaid(newPaidStatus);
            recordDao.updateRecord(pair[0]);
        }
        if (pair[1] != null) {
            pair[1].setPaid(newPaidStatus);
            recordDao.updateRecord(pair[1]);
        }
        
        loadMonthlyTable();
    }

    private TextView createCell(String text, float weight, int gravity, int colorRes, int textSizeSp) {
        TextView tv = new TextView(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, weight);
        tv.setLayoutParams(lp);
        tv.setText(text);
        tv.setTextSize(textSizeSp);
        tv.setGravity(gravity);
        tv.setTextColor(getResources().getColor(colorRes));
        return tv;
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

    // ---- PAYMENT ----

    private void loadPaymentSummary() {
        double totalOwed = supplierDao.getTotalEarningsBySupplier(supplierId);
        double totalPaid = supplierDao.getTotalPaidBySupplier(supplierId);
        double remaining = Math.max(0, totalOwed - totalPaid);

        binding.tvTotalOwed.setText(FormatUtils.money(totalOwed));
        binding.tvTotalPaid.setText(FormatUtils.money(totalPaid));
        binding.tvRemaining.setText(FormatUtils.money(remaining));
        binding.tvRemaining.setTextColor(remaining > 0.01
                ? getResources().getColor(R.color.pending_color)
                : getResources().getColor(R.color.earnings_color));
    }

    private void setupRecordPayment() {
        binding.btnRecordPayment.setOnClickListener(v -> {
            String amtStr = binding.etPayAmount.getText().toString().trim();
            String note = binding.etPayNote.getText().toString().trim();

            if (amtStr.isEmpty()) {
                binding.etPayAmount.setError("Enter amount");
                return;
            }

            double amount;
            try {
                amount = Double.parseDouble(amtStr.replace(",", "."));
            } catch (NumberFormatException e) {
                binding.etPayAmount.setError("Invalid amount");
                return;
            }

            if (amount <= 0) {
                binding.etPayAmount.setError("Amount must be > 0");
                return;
            }

            String today = DateUtils.todayDb();
            Payment payment = new Payment(today, amount, note, Payment.PARTY_SUPPLIER, supplierId);
            payment.setPartyName(binding.tvSupplierName.getText().toString());
            paymentDao.insertPayment(payment);

            binding.etPayAmount.setText("");
            binding.etPayNote.setText("");

            Toast.makeText(this, "Payment of " + FormatUtils.money(amount) + " saved", Toast.LENGTH_SHORT).show();

            loadPaymentSummary();
            loadPaymentHistory();
        });
    }

    private void loadPaymentHistory() {
        List<Payment> payments = paymentDao.getPaymentsByParty(Payment.PARTY_SUPPLIER, supplierId);
        binding.paymentHistoryContainer.removeAllViews();

        if (payments.isEmpty()) {
            binding.tvNoPayments.setVisibility(View.VISIBLE);
            return;
        }

        binding.tvNoPayments.setVisibility(View.GONE);

        for (Payment p : payments) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setPadding(dpToPx(14), dpToPx(10), dpToPx(14), dpToPx(10));
            row.setGravity(Gravity.CENTER_VERTICAL);

            LinearLayout leftCol = new LinearLayout(this);
            leftCol.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams leftLp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            leftCol.setLayoutParams(leftLp);

            TextView tvDate = new TextView(this);
            tvDate.setText(DateUtils.dbToDisplay(p.getDate()));
            tvDate.setTextSize(13);
            tvDate.setTextColor(getResources().getColor(R.color.text_primary));
            leftCol.addView(tvDate);

            if (p.getNote() != null && !p.getNote().isEmpty()) {
                TextView tvNote = new TextView(this);
                tvNote.setText(p.getNote());
                tvNote.setTextSize(11);
                tvNote.setTextColor(getResources().getColor(R.color.text_hint));
                leftCol.addView(tvNote);
            }

            row.addView(leftCol);

            TextView tvAmount = new TextView(this);
            tvAmount.setText("-" + FormatUtils.money(p.getAmount()));
            tvAmount.setTextSize(14);
            tvAmount.setTextColor(getResources().getColor(R.color.pending_color));
            row.addView(tvAmount);

            binding.paymentHistoryContainer.addView(row);

            View divider = new View(this);
            divider.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, dpToPx(1)));
            divider.setBackgroundColor(getResources().getColor(R.color.divider));
            binding.paymentHistoryContainer.addView(divider);
        }
    }

    // ---- HISTORY BUTTON ----

    private void setupHistoryButton() {
        binding.btnViewHistory.setOnClickListener(v -> {
            Intent intent = new Intent(this, HistoryActivity.class);
            intent.putExtra("supplier_id", supplierId);
            startActivity(intent);
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { finish(); return true; }
        return super.onOptionsItemSelected(item);
    }
}
