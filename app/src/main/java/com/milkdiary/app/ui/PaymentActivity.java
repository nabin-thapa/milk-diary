package com.milkdiary.app.ui;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.milkdiary.app.R;
import com.milkdiary.app.databinding.ActivityPaymentBinding;
import com.milkdiary.app.db.MilkRecordDao;
import com.milkdiary.app.db.PaymentDao;
import com.milkdiary.app.db.SaleDao;
import com.milkdiary.app.db.SupplierDao;
import com.milkdiary.app.model.Payment;
import com.milkdiary.app.model.Supplier;
import com.milkdiary.app.ui.adapter.PaymentAdapter;
import com.milkdiary.app.util.DateUtils;
import com.milkdiary.app.util.ExportUtils;
import com.milkdiary.app.util.FormatUtils;
import com.milkdiary.app.util.RoleManager;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class PaymentActivity extends AppCompatActivity implements PaymentAdapter.PaymentListener {

    private ActivityPaymentBinding binding;
    private PaymentDao paymentDao;
    private MilkRecordDao recordDao;
    private SaleDao saleDao;
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
        saleDao = new SaleDao(this);
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

        handleIntentExtras();
    }

    private void handleIntentExtras() {
        String partyType = getIntent().getStringExtra("party_type");
        long partyId = getIntent().getLongExtra("party_id", -1);
        if ("supplier".equals(partyType) && partyId > 0) {
            binding.tabSupplierPayments.performClick();
            for (int i = 0; i < supplierList.size(); i++) {
                if (supplierList.get(i).getId() == partyId) {
                    binding.etPaySupplier.setText(supplierList.get(i).getName(), false);
                    selectedSupplier = supplierList.get(i);
                    showSupplierBalance(partyId);
                    break;
                }
            }
        }
    }

    private void setupTabs() {
        binding.tabCustomerPayments.setOnClickListener(v -> {
            showingSupplierPayments = false;
            binding.tabCustomerPayments.setBackgroundColor(
                    ContextCompat.getColor(this, R.color.primary));
            binding.tabCustomerPayments.setTextColor(
                    ContextCompat.getColor(this, R.color.text_on_primary));
            binding.tabSupplierPayments.setBackgroundColor(
                    ContextCompat.getColor(this, R.color.card_bg));
            binding.tabSupplierPayments.setTextColor(
                    ContextCompat.getColor(this, R.color.text_primary));
            binding.supplierSection.setVisibility(View.GONE);
            loadPayments();
        });
        binding.tabSupplierPayments.setOnClickListener(v -> {
            showingSupplierPayments = true;
            binding.tabSupplierPayments.setBackgroundColor(
                    ContextCompat.getColor(this, R.color.primary));
            binding.tabSupplierPayments.setTextColor(
                    ContextCompat.getColor(this, R.color.text_on_primary));
            binding.tabCustomerPayments.setBackgroundColor(
                    ContextCompat.getColor(this, R.color.card_bg));
            binding.tabCustomerPayments.setTextColor(
                    ContextCompat.getColor(this, R.color.text_primary));
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
                binding.cardSupplierBalance.setVisibility(View.GONE);
            } else {
                selectedSupplier = supplierList.get(position - 1);
                showSupplierBalance(selectedSupplier.getId());
            }
        });
    }

    private void showSupplierBalance(long supplierId) {
        double earned = supplierDao.getTotalEarningsBySupplier(supplierId);
        double paid = supplierDao.getTotalPaidBySupplier(supplierId);
        double pending = earned - paid;
        binding.tvSupplierEarned.setText(FormatUtils.money(earned));
        binding.tvSupplierPaid.setText(FormatUtils.money(paid));
        binding.tvSupplierPending.setText(FormatUtils.money(Math.max(0, pending)));
        binding.tvSupplierPending.setTextColor(pending > 0.01
                ? ContextCompat.getColor(this, R.color.pending_color)
                : ContextCompat.getColor(this, R.color.earnings_color));
        binding.cardSupplierBalance.setVisibility(View.VISIBLE);
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

        if (showingSupplierPayments) {
            double totalPurchaseCost = recordDao.getTotalEarningsAllTime();
            double totalPaidToSuppliers = paymentDao.getTotalPaidToSuppliers();
            double pendingToSuppliers = totalPurchaseCost - totalPaidToSuppliers;

            binding.tvTotalEarned.setText("Total Purchases: " + FormatUtils.money(totalPurchaseCost));
            binding.tvTotalPaid.setText("Total Paid: " + FormatUtils.money(totalPaidToSuppliers));
            binding.tvPendingAmt.setText("Pending: " + FormatUtils.money(Math.max(0, pendingToSuppliers)));
            binding.tvPendingAmt.setTextColor(pendingToSuppliers > 0.01 
                    ? ContextCompat.getColor(this, R.color.pending_color) 
                    : ContextCompat.getColor(this, R.color.earnings_color));
        } else {
            double totalSales = saleDao.getTotalSalesAllTime();
            double totalReceived = paymentDao.getTotalPaid();
            double pendingFromCustomers = totalSales - totalReceived;

            binding.tvTotalEarned.setText("Total Sales: " + FormatUtils.money(totalSales));
            binding.tvTotalPaid.setText("Total Received: " + FormatUtils.money(totalReceived));
            binding.tvPendingAmt.setText("Due: " + FormatUtils.money(Math.max(0, pendingFromCustomers)));
            binding.tvPendingAmt.setTextColor(pendingFromCustomers > 0.01 
                    ? ContextCompat.getColor(this, R.color.pending_color) 
                    : ContextCompat.getColor(this, R.color.earnings_color));
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_payment, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { 
            finish(); 
            return true; 
        } else if (item.getItemId() == R.id.action_export_pdf) {
            exportPdf();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void exportPdf() {
        try {
            String title = showingSupplierPayments ? "Purchase Payment Report" : "Sales Payment Report";
            List<Payment> payments = adapter.getPayments();
            File file = ExportUtils.exportPaymentsPdf(this, payments, title);
            Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", file);
            
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("application/pdf");
            intent.putExtra(Intent.EXTRA_STREAM, uri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(intent, "Share Payment Report"));
        } catch (Exception e) {
            Toast.makeText(this, "Failed to export PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
