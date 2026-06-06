package com.milkdiary.app.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

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
import com.milkdiary.app.util.RoleManager;

import java.util.Calendar;
import java.util.List;

public class DashboardActivity extends AppCompatActivity {

    private ActivityDashboardBinding binding;
    private MilkRecordDao recordDao;
    private PaymentDao paymentDao;
    private static boolean isAppLaunchCheckDone = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        AppCompatDelegate.setDefaultNightMode(
                prefs.getBoolean("dark_mode", false)
                        ? AppCompatDelegate.MODE_NIGHT_YES
                        : AppCompatDelegate.MODE_NIGHT_NO);

        super.onCreate(savedInstanceState);
        binding = ActivityDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        RoleManager roleManager = new RoleManager(this);
        if (!roleManager.isRoleSet()) {
            startActivity(new Intent(this, RoleSetupActivity.class));
            finish();
            return;
        }

        // Hide owner-only sections for Salesman
        if (roleManager.isSalesman()) {
            binding.btnSuppliers.setVisibility(View.GONE);
            binding.btnSales.setVisibility(View.GONE);
            binding.btnInventory.setVisibility(View.GONE);
            binding.btnPayments.setVisibility(View.GONE);
            binding.btnMonthlySummary.setVisibility(View.GONE);
            binding.sectionThisMonth.setVisibility(View.GONE);
        }

        recordDao = new MilkRecordDao(this);
        paymentDao = new PaymentDao(this);

        setupButtons();

        if (!isAppLaunchCheckDone) {
            isAppLaunchCheckDone = true;
            com.milkdiary.app.update.UpdateManager.checkForUpdates(this, false, null);
        }

        checkVersionChanged();
    }

    private void checkVersionChanged() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        int savedVersion = sp.getInt("app_version_code", 0);
        int currentVersion = 0;
        try {
            currentVersion = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
        } catch (Exception ignored) {}
        if (currentVersion > 0 && savedVersion > 0 && currentVersion != savedVersion) {
            Toast.makeText(this, "✅ Updated to v" + currentVersion, Toast.LENGTH_LONG).show();
        }
        sp.edit().putInt("app_version_code", currentVersion).apply();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshDashboard();
    }

    private void refreshDashboard() {
        String today = DateUtils.todayDb();
        String thisMonth = DateUtils.currentMonthDb();

        setGreeting();
        loadProfileImage();

        // Get today's records for both sessions
        List<MilkRecord> todayRecords = recordDao.getRecordsByDate(today);
        MilkRecord morningRecord = null;
        MilkRecord eveningRecord = null;
        double todayCowLiters = 0, todayBufLiters = 0, todayEarnings = 0;

        for (MilkRecord r : todayRecords) {
            if (r.isMorning()) morningRecord = r;
            if (r.isEvening()) eveningRecord = r;
            todayCowLiters += r.getCowLiters();
            todayBufLiters += r.getBuffaloLiters();
            todayEarnings += r.getTotal();
        }

        boolean hasMorning = morningRecord != null;
        boolean hasEvening = eveningRecord != null;
        boolean dayComplete = hasMorning && hasEvening;

        // Monthly stats
        double[] monthly = recordDao.getMonthlyStats(thisMonth);
        double monthlyLiters = monthly[0] + monthly[1];
        double monthlyEarnings = monthly[2];

        // Pending
        double totalEarnings = recordDao.getTotalEarningsAllTime();
        double totalPaid = paymentDao.getTotalPaid();
        double pending = totalEarnings - totalPaid;

        // Update UI
        binding.tvTodayDate.setText(DateUtils.dbToDisplay(today));
        binding.tvCowLiters.setText(FormatUtils.liters(todayCowLiters));
        binding.tvBufLiters.setText(FormatUtils.liters(todayBufLiters));
        binding.tvTodayEarnings.setText(FormatUtils.money(todayEarnings));
        binding.tvMonthlyLiters.setText(FormatUtils.liters(monthlyLiters));
        binding.tvMonthlyEarnings.setText(FormatUtils.money(monthlyEarnings));
        binding.tvPending.setText(FormatUtils.money(Math.max(0, pending)));

        // Status indicator
        if (dayComplete) {
            binding.tvTodayStatus.setText("✓ Today completed");
            binding.tvTodayStatus.setTextColor(
                    getResources().getColor(R.color.earnings_color, getTheme()));
        } else if (hasMorning || hasEvening) {
            binding.tvTodayStatus.setText("⚠ Partial record");
            binding.tvTodayStatus.setTextColor(
                    getResources().getColor(R.color.warning_color, getTheme()));
        } else {
            binding.tvTodayStatus.setText("⚠ No record yet today");
            binding.tvTodayStatus.setTextColor(
                    getResources().getColor(R.color.pending_color, getTheme()));
        }

        // Pending color
        binding.tvPending.setTextColor(pending > 0.01
                ? getResources().getColor(R.color.pending_color, getTheme())
                : getResources().getColor(R.color.earnings_color, getTheme()));

        // --- Morning/Evening Entry UI ---
        updateSessionUI(hasMorning, hasEvening);
    }

    private void updateSessionUI(boolean hasMorning, boolean hasEvening) {
        // Morning
        if (hasMorning) {
            binding.tvMorningStatus.setText("Completed");
            binding.tvMorningStatus.setTextColor(
                    getResources().getColor(R.color.earnings_color, getTheme()));
            binding.btnMorningAdd.setText("✅  Morning Completed");
            binding.btnMorningAdd.setEnabled(false);
            binding.btnMorningAdd.setAlpha(0.7f);
        } else {
            binding.tvMorningStatus.setText("Pending");
            binding.tvMorningStatus.setTextColor(
                    getResources().getColor(R.color.warning_color, getTheme()));
            binding.btnMorningAdd.setText("➕  Add Morning Milk");
            binding.btnMorningAdd.setEnabled(true);
            binding.btnMorningAdd.setAlpha(1f);
        }

        // Evening
        if (hasEvening) {
            binding.tvEveningStatus.setText("Completed");
            binding.tvEveningStatus.setTextColor(
                    getResources().getColor(R.color.earnings_color, getTheme()));
            binding.btnEveningAdd.setText("✅  Evening Completed");
            binding.btnEveningAdd.setEnabled(false);
            binding.btnEveningAdd.setAlpha(0.7f);
        } else {
            binding.tvEveningStatus.setText("Pending");
            binding.tvEveningStatus.setTextColor(
                    getResources().getColor(R.color.warning_color, getTheme()));
            binding.btnEveningAdd.setText("➕  Add Evening Milk");
            binding.btnEveningAdd.setEnabled(true);
            binding.btnEveningAdd.setAlpha(1f);
        }

        // Day complete card
        boolean dayComplete = hasMorning && hasEvening;
        binding.cardDayComplete.setVisibility(dayComplete ? View.VISIBLE : View.GONE);
        binding.cardMorning.setVisibility(dayComplete ? View.GONE : View.VISIBLE);
        binding.cardEvening.setVisibility(dayComplete ? View.GONE : View.VISIBLE);
    }

    private void setGreeting() {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        String greeting;
        if (hour < 12) {
            greeting = getString(R.string.greeting_morning);
        } else if (hour < 17) {
            greeting = getString(R.string.greeting_afternoon);
        } else {
            greeting = getString(R.string.greeting_evening);
        }
        String name = PreferenceManager.getDefaultSharedPreferences(this)
            .getString("profile_name", "").trim();
        if (!name.isEmpty()) {
            greeting += ", " + name;
        }
        binding.tvGreeting.setText(greeting);
    }

    private void loadProfileImage() {
        java.io.File imgFile = new java.io.File(getFilesDir(), "profile_pic.jpg");
        if (imgFile.exists()) {
            Bitmap bm = BitmapFactory.decodeFile(imgFile.getPath());
            if (bm != null) {
                binding.ivProfile.setImageBitmap(bm);
            }
        }
    }

    private void setupButtons() {
        binding.btnMorningAdd.setOnClickListener(v ->
                openAddRecord(MilkRecord.SESSION_MORNING));
        binding.btnEveningAdd.setOnClickListener(v ->
                openAddRecord(MilkRecord.SESSION_EVENING));
        binding.btnHistory.setOnClickListener(v ->
                startActivity(new Intent(this, HistoryActivity.class)));
        binding.btnMonthlySummary.setOnClickListener(v ->
                startActivity(new Intent(this, MonthlySummaryActivity.class)));
        binding.btnPayments.setOnClickListener(v ->
                startActivity(new Intent(this, PaymentActivity.class)));
        binding.btnSuppliers.setOnClickListener(v ->
                startActivity(new Intent(this, SupplierListActivity.class)));
        binding.btnSales.setOnClickListener(v ->
                startActivity(new Intent(this, SalesActivity.class)));
        binding.btnInventory.setOnClickListener(v ->
                startActivity(new Intent(this, InventoryActivity.class)));
        binding.btnSettings.setOnClickListener(v ->
                startActivity(new Intent(this, SettingsActivity.class)));
    }

    private void openAddRecord(String sessionType) {
        Intent intent = new Intent(this, AddRecordActivity.class);
        intent.putExtra(AddRecordActivity.EXTRA_SESSION_TYPE, sessionType);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_dashboard, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
