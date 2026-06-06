package com.milkdiary.app.ui;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.milkdiary.app.R;
import com.milkdiary.app.databinding.ActivityPaymentBinding;
import com.milkdiary.app.db.MilkRecordDao;
import com.milkdiary.app.db.PaymentDao;
import com.milkdiary.app.db.SupplierDao;
import com.milkdiary.app.model.Payment;
import com.milkdiary.app.model.Supplier;
import com.milkdiary.app.ui.adapter.PaymentAdapter;
import com.milkdiary.app.util.RoleManager;
import com.milkdiary.app.util.DateUtils;
import com.milkdiary.app.util.FormatUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class PaymentActivity extends AppCompatActivity implements PaymentAdapter.PaymentListener {

    private ActivityPaymentBinding binding;
    private PaymentDao paymentDao;
    private MilkRecordDao recordDao;
    private SupplierDao supplierDao;
    private PaymentAdapter adapter;
    private String selectedDate;
    private boolean showingSupplierPayments = false;

    private List<Supplier> supplierList = new ArrayList<>();
    private Supplier selectedSupplier;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPaymentBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Payments");
        }

        paymentDao = new PaymentDao(this);
        recordDao = new MilkRecordDao(this);
        supplierDao = new SupplierDao(this);
        selectedDate = DateUtils.todayDb();

        RoleManager roleManager = new RoleManager(this);
        if (roleManager.isSupplier()) {
            // Supplier view: only supplier payments, read-only
            binding.tabCustomerPayments.setVisibility(View.GONE);
            binding.tabSupplierPayments.performClick();
            binding.labelRecordPayment.setVisibility(View.GONE);
            binding.sectionRecordPayment.setVisibility(View.GONE);
        } else {
            setupTabs();
            setupAddButton();
        }

        setupSupplierDropdown();
        setupDatePicker();

        binding.recyclerPayments.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PaymentAdapter(this);
        binding.recyclerPayments.setAdapter(adapter);

        loadPayments();
    }

    private void setupTabs() {
        binding.tabCustomerPayments.setOnClickListener(v -> {
            showingSupplierPayments = false;
            binding.tabCustomerPayments.setBackgroundColor(
                    getResources().getColor(R.color.primary, getTheme()));
            binding.tabCustomerPayments.setTextColor(
                    getResources().getColor(R.color.text_on_primary, getTheme()));
            binding.tabSupplierPayments.setBackgroundColor(
                    getResources().getColor(R.color.card_bg, getTheme()));
            binding.tabSupplierPayments.setTextColor(
                    getResources().getColor(R.color.text_primary, getTheme()));
            binding.supplierSection.setVisibility(View.GONE);
            loadPayments();
        });
        binding.tabSupplierPayments.setOnClickListener(v -> {
            showingSupplierPayments = true;
            binding.tabSupplierPayments.setBackgroundColor(
                    getResources().getColor(R.color.primary, getTheme()));
            binding.tabSupplierPayments.setTextColor(
                    getResources().getColor(R.color.text_on_primary, getTheme()));
            binding.tabCustomerPayments.setBackgroundColor(
                    getResources().getColor(R.color.card_bg, getTheme()));
            binding.tabCustomerPayments.setTextColor(
                    getResources().getColor(R.color.text_primary, getTheme()));
            binding.supplierSection.setVisibility(View.VISIBLE);
            loadPayments();
        });
        // Default: customer selected
        binding.tabCustomerPayments.performClick();
    }

    private void setupSupplierDropdown() {
        supplierList = supplierDao.getActive();
        List<String> names = new ArrayList<>();
        names.add("-- Select Supplier --");
        for (Supplier s : supplierList) names.add(s.getName());

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, names);
        binding.etPaySupplier.setAdapter(adapter);
        binding.etPaySupplier.setOnItemClickListener((parent, view, position, id) -> {
            if (position == 0) {
                selectedSupplier = null;
            } else {
                selectedSupplier = supplierList.get(position - 1);
            }
        });
    }

    private void setupDatePicker() {
        updateDateButton();
        binding.btnPayDate.setOnClickListener(v -> {
            String[] parts = selectedDate.split("-");
            int year = Integer.parseInt(parts[0]);
            int month = Integer.parseInt(parts[1]) - 1;
            int day = Integer.parseInt(parts[2]);

            new DatePickerDialog(this, (view, y, m, d) -> {
                Calendar cal = Calendar.getInstance();
                cal.set(y, m, d);
                selectedDate = DateUtils.calendarToDb(cal);
                updateDateButton();
            }, year, month, day).show();
        });
    }

    private void updateDateButton() {
        binding.btnPayDate.setText(DateUtils.dbToDisplay(selectedDate));
    }

    private void setupAddButton() {
        binding.btnAddPayment.setOnClickListener(v -> {
            String amtStr = binding.etPayAmount.getText().toString().trim();
            String note = binding.etPayNote.getText().toString().trim();

            if (amtStr.isEmpty()) {
                Toast.makeText(this, "Enter payment amount", Toast.LENGTH_SHORT).show();
                return;
            }
            double amount;
            try { amount = Double.parseDouble(amtStr); }
            catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid amount", Toast.LENGTH_SHORT).show();
                return;
            }

            if (showingSupplierPayments) {
                if (selectedSupplier == null) {
                    Toast.makeText(this, "Select a supplier", Toast.LENGTH_SHORT).show();
                    return;
                }
                Payment payment = new Payment(selectedDate, amount, note,
                        Payment.PARTY_SUPPLIER, selectedSupplier.getId());
                paymentDao.insertPayment(payment);
                Toast.makeText(this, "Payment to " + selectedSupplier.getName() + " saved",
                        Toast.LENGTH_SHORT).show();
            } else {
                Payment payment = new Payment(selectedDate, amount, note,
                        Payment.PARTY_CUSTOMER, 0);
                paymentDao.insertPayment(payment);
                Toast.makeText(this, "Payment saved", Toast.LENGTH_SHORT).show();
            }

            binding.etPayAmount.setText("");
            binding.etPayNote.setText("");
            selectedDate = DateUtils.todayDb();
            updateDateButton();
            loadPayments();
        });
    }

    private void loadPayments() {
        List<Payment> payments;
        if (showingSupplierPayments) {
            payments = paymentDao.getPaymentsByType(Payment.PARTY_SUPPLIER);
        } else {
            payments = paymentDao.getPaymentsByType(Payment.PARTY_CUSTOMER);
        }

        adapter.setPayments(payments);
        binding.tvNoPayments.setVisibility(payments.isEmpty() ? View.VISIBLE : View.GONE);

        double totalEarnings = recordDao.getTotalEarningsAllTime();
        double totalPaid = paymentDao.getTotalPaid();
        double totalPaidToSuppliers = paymentDao.getTotalPaidToSuppliers();
        double pending = totalEarnings - totalPaid;

        if (showingSupplierPayments) {
            binding.tvTotalEarned.setText("Total paid to suppliers: " + FormatUtils.money(totalPaidToSuppliers));
            binding.tvTotalPaid.setText("");
            binding.tvPendingAmt.setText("Paid Out: " + FormatUtils.money(totalPaidToSuppliers));
        } else {
            binding.tvTotalEarned.setText("Total Earned: " + FormatUtils.money(totalEarnings));
            binding.tvTotalPaid.setText("Total Paid: " + FormatUtils.money(totalPaid));
            binding.tvPendingAmt.setText("Pending: " + FormatUtils.money(pending));
        }
    }

    @Override
    public void onDeletePayment(Payment payment) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Payment")
                .setMessage("Delete payment of " + FormatUtils.money(payment.getAmount()) + "?")
                .setPositiveButton("Delete", (d, w) -> {
                    paymentDao.deletePayment(payment.getId());
                    loadPayments();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { finish(); return true; }
        return super.onOptionsItemSelected(item);
    }
}
