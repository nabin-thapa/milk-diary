package com.milkdiary.app.ui;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.milkdiary.app.databinding.ActivityPaymentBinding;
import com.milkdiary.app.db.MilkRecordDao;
import com.milkdiary.app.db.PaymentDao;
import com.milkdiary.app.model.Payment;
import com.milkdiary.app.ui.adapter.PaymentAdapter;
import com.milkdiary.app.util.DateUtils;
import com.milkdiary.app.util.FormatUtils;

import java.util.Calendar;
import java.util.List;

public class PaymentActivity extends AppCompatActivity implements PaymentAdapter.PaymentListener {

    private ActivityPaymentBinding binding;
    private PaymentDao paymentDao;
    private MilkRecordDao recordDao;
    private PaymentAdapter adapter;
    private String selectedDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPaymentBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Payment Tracking");
        }

        paymentDao = new PaymentDao(this);
        recordDao = new MilkRecordDao(this);
        selectedDate = DateUtils.todayDb();

        binding.recyclerPayments.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PaymentAdapter(this);
        binding.recyclerPayments.setAdapter(adapter);

        setupDatePicker();
        setupAddButton();
        loadPayments();
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

            Payment payment = new Payment(selectedDate, amount, note);
            long id = paymentDao.insertPayment(payment);
            if (id != -1) {
                Toast.makeText(this, "Payment saved", Toast.LENGTH_SHORT).show();
                binding.etPayAmount.setText("");
                binding.etPayNote.setText("");
                selectedDate = DateUtils.todayDb();
                updateDateButton();
                loadPayments();
            }
        });
    }

    private void loadPayments() {
        List<Payment> payments = paymentDao.getAllPayments();
        adapter.setPayments(payments);
        binding.tvNoPayments.setVisibility(payments.isEmpty() ? View.VISIBLE : View.GONE);

        double totalEarnings = recordDao.getTotalEarningsAllTime();
        double totalPaid = paymentDao.getTotalPaid();
        double pending = totalEarnings - totalPaid;

        binding.tvTotalEarned.setText("Total Earned: " + FormatUtils.money(totalEarnings));
        binding.tvTotalPaid.setText("Total Paid: " + FormatUtils.money(totalPaid));
        binding.tvPendingAmt.setText("Pending: " + FormatUtils.money(pending));
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
