package com.milkdiary.app.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.PreferenceManager;

import com.milkdiary.app.R;
import com.milkdiary.app.databinding.ActivityDashboardBinding;
import com.milkdiary.app.db.MilkRecordDao;
import com.milkdiary.app.db.PaymentDao;
import com.milkdiary.app.model.MilkRecord;
import com.milkdiary.app.util.DateUtils;
import com.milkdiary.app.util.FormatUtils;

public class DashboardActivity extends AppCompatActivity {

    private ActivityDashboardBinding binding;
    private MilkRecordDao recordDao;
    private PaymentDao paymentDao;
    private static boolean isAppLaunchCheckDone = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply dark mode before super.onCreate to avoid flicker
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        AppCompatDelegate.setDefaultNightMode(
                prefs.getBoolean("dark_mode", false)
                        ? AppCompatDelegate.MODE_NIGHT_YES
                        : AppCompatDelegate.MODE_NIGHT_NO);

        super.onCreate(savedInstanceState);
        binding = ActivityDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        recordDao = new MilkRecordDao(this);
        paymentDao = new PaymentDao(this);

        setupButtons();

        // Check for updates automatically on app launch (once per session)
        if (!isAppLaunchCheckDone) {
            isAppLaunchCheckDone = true;
            com.milkdiary.app.update.UpdateManager.checkForUpdates(this, false, null);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshDashboard();
    }

    private void refreshDashboard() {
        String today     = DateUtils.todayDb();
        String thisMonth = DateUtils.currentMonthDb();

        // Today's record
        MilkRecord todayRecord = recordDao.getRecordByDate(today);
        double cowLiters = 0, bufLiters = 0, todayEarnings = 0;
        if (todayRecord != null) {
            cowLiters     = todayRecord.getCowLiters();
            bufLiters     = todayRecord.getBuffaloLiters();
            todayEarnings = todayRecord.getTotal();
        }

        // Monthly stats via a single SQL query
        double[] monthly       = recordDao.getMonthlyStats(thisMonth);
        double monthlyLiters   = monthly[0] + monthly[1];
        double monthlyEarnings = monthly[2];

        // Pending = all-time earnings − all-time paid
        double totalEarnings = recordDao.getTotalEarningsAllTime();
        double totalPaid     = paymentDao.getTotalPaid();
        double pending       = totalEarnings - totalPaid;

        // Update UI
        binding.tvTodayDate.setText(DateUtils.dbToDisplay(today));
        binding.tvCowLiters.setText(FormatUtils.liters(cowLiters));
        binding.tvBufLiters.setText(FormatUtils.liters(bufLiters));
        binding.tvTodayEarnings.setText(FormatUtils.money(todayEarnings));
        binding.tvMonthlyLiters.setText(FormatUtils.liters(monthlyLiters));
        binding.tvMonthlyEarnings.setText(FormatUtils.money(monthlyEarnings));
        binding.tvPending.setText(FormatUtils.money(Math.max(0, pending)));

        // Status indicator: did we record today?
        if (todayRecord != null) {
            binding.tvTodayStatus.setText("✓ Today recorded");
            binding.tvTodayStatus.setTextColor(
                    getResources().getColor(R.color.earnings_color, getTheme()));
        } else {
            binding.tvTodayStatus.setText("⚠ No record yet today");
            binding.tvTodayStatus.setTextColor(
                    getResources().getColor(R.color.pending_color, getTheme()));
        }

        // Color pending red if unpaid, green if settled
        binding.tvPending.setTextColor(pending > 0.01
                ? getResources().getColor(R.color.pending_color, getTheme())
                : getResources().getColor(R.color.earnings_color, getTheme()));
    }

    private void setupButtons() {
        binding.btnAddRecord.setOnClickListener(v ->
                startActivity(new Intent(this, AddRecordActivity.class)));
        binding.btnHistory.setOnClickListener(v ->
                startActivity(new Intent(this, HistoryActivity.class)));
        binding.btnMonthlySummary.setOnClickListener(v ->
                startActivity(new Intent(this, MonthlySummaryActivity.class)));
        binding.btnPayments.setOnClickListener(v ->
                startActivity(new Intent(this, PaymentActivity.class)));
        binding.btnSettings.setOnClickListener(v ->
                startActivity(new Intent(this, SettingsActivity.class)));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) { return true; }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }
}
