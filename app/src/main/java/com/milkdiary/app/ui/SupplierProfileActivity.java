package com.milkdiary.app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.milkdiary.app.databinding.ActivitySupplierProfileBinding;
import com.milkdiary.app.db.MilkRecordDao;
import com.milkdiary.app.db.PaymentDao;
import com.milkdiary.app.db.SupplierDao;
import com.milkdiary.app.model.MilkRecord;
import com.milkdiary.app.model.Supplier;
import com.milkdiary.app.util.DateUtils;
import com.milkdiary.app.util.RoleManager;

import java.util.List;

public class SupplierProfileActivity extends AppCompatActivity {

    private ActivitySupplierProfileBinding binding;
    private SupplierDao supplierDao;
    private MilkRecordDao recordDao;
    private PaymentDao paymentDao;
    private long supplierId = -1;

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

        supplierId = getIntent().getLongExtra("supplier_id", -1);
        if (supplierId == -1) {
            finish();
            return;
        }

        setupButtons();
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

        loadTodayEntry();
        loadMonthlySummary();
        loadPaymentSummary();
    }

    private void loadTodayEntry() {
        String today = DateUtils.todayDb();
        List<MilkRecord> todayRecords = recordDao.getRecordsBySupplierAndDate(supplierId, today);

        if (todayRecords.isEmpty()) {
            binding.tvTodayStatus.setText("No entry today");
            binding.tvTodayDetails.setVisibility(View.GONE);
            binding.btnAddEntry.setVisibility(View.VISIBLE);
        } else {
            binding.tvTodayStatus.setText("Entry recorded");
            StringBuilder details = new StringBuilder();
            for (MilkRecord r : todayRecords) {
                details.append(r.isMorning() ? "Morning: " : "Evening: ");
                if (r.getCowLiters() > 0)
                    details.append(String.format("%.1fL cow", r.getCowLiters()));
                if (r.getBuffaloLiters() > 0) {
                    if (details.length() > 0 && !details.toString().endsWith(": ")) details.append(" + ");
                    details.append(String.format("%.1fL buffalo", r.getBuffaloLiters()));
                }
                details.append(" = Rs.").append(String.format("%.2f", r.getTotal())).append("\n");
            }
            binding.tvTodayDetails.setText(details.toString().trim());
            binding.tvTodayDetails.setVisibility(View.VISIBLE);
            binding.btnAddEntry.setVisibility(View.GONE);
        }
    }

    private void loadMonthlySummary() {
        String thisMonth = DateUtils.currentMonthDb();
        double[] stats = recordDao.getMonthlyStatsBySupplier(supplierId, thisMonth);
        binding.tvMonthCowLiters.setText(String.format("%.2f L", stats[0]));
        binding.tvMonthBufLiters.setText(String.format("%.2f L", stats[1]));
        binding.tvMonthEarnings.setText(String.format("Rs. %.2f", stats[2]));
    }

    private void loadPaymentSummary() {
        double totalPaid = supplierDao.getTotalPaidBySupplier(supplierId);
        double pending = supplierDao.getPendingBalance(supplierId);
        binding.tvTotalPaid.setText(String.format("Rs. %.2f", totalPaid));
        binding.tvPendingBalance.setText(String.format("Rs. %.2f", Math.max(0, pending)));
        if (pending > 0.01) {
            binding.tvPendingBalance.setTextColor(getResources().getColor(com.milkdiary.app.R.color.pending_color));
        } else {
            binding.tvPendingBalance.setTextColor(getResources().getColor(com.milkdiary.app.R.color.primary));
        }
    }

    private void setupButtons() {
        binding.btnAddEntry.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddRecordActivity.class);
            intent.putExtra("supplier_id", supplierId);
            startActivity(intent);
        });

        binding.btnRecordPayment.setOnClickListener(v -> {
            Intent intent = new Intent(this, PaymentActivity.class);
            intent.putExtra("party_type", "supplier");
            intent.putExtra("party_id", supplierId);
            startActivity(intent);
        });

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
