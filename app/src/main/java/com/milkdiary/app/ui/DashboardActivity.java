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
import com.milkdiary.app.db.SaleDao;
import com.milkdiary.app.db.SupplierDao;
import com.milkdiary.app.model.MilkRecord;
import com.milkdiary.app.model.Sale;
import com.milkdiary.app.util.DateUtils;
import com.milkdiary.app.util.FormatUtils;
import com.milkdiary.app.util.RoleManager;
import com.milkdiary.app.util.SessionManager;

import java.util.Calendar;
import java.util.List;

public class DashboardActivity extends AppCompatActivity {

    private ActivityDashboardBinding binding;
    private MilkRecordDao recordDao;
    private PaymentDao paymentDao;
    private SaleDao saleDao;
    private SupplierDao supplierDao;
    private SessionManager session;
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

        session = new SessionManager(this);
        if (!session.isLoggedIn()) {
            Intent welcomeIntent = new Intent(this, WelcomeActivity.class);
            welcomeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(welcomeIntent);
            finish();
            return;
        }

        RoleManager roleManager = new RoleManager(this);
        if (!roleManager.isRoleSet()) {
            roleManager.setRole(session.getUserRole());
        }

        if (session.isSupplier()) {
            binding.sectionQuickActions.setVisibility(View.GONE);
            binding.sectionThisMonth.setVisibility(View.GONE);
            binding.btnViewMyRecords.setVisibility(View.VISIBLE);
        }

        recordDao = new MilkRecordDao(this);
        paymentDao = new PaymentDao(this);
        saleDao = new SaleDao(this);
        supplierDao = new SupplierDao(this);

        setupButtons();

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
        String today = DateUtils.todayDb();
        String thisMonth = DateUtils.currentMonthDb();

        setGreeting();
        loadProfileImage();

        // ---- TODAY'S PURCHASED MILK ----
        List<MilkRecord> todayRecords = recordDao.getRecordsByDate(today);
        double todayCowLiters = 0, todayBufLiters = 0, todayPurchaseCost = 0;
        for (MilkRecord r : todayRecords) {
            todayCowLiters += r.getCowLiters();
            todayBufLiters += r.getBuffaloLiters();
            todayPurchaseCost += r.getTotal();
        }
        double todayTotalLiters = todayCowLiters + todayBufLiters;

        // ---- TODAY'S SOLD MILK (earnings) ----
        List<Sale> todaySales = saleDao.getByDate(today);
        double todaySoldAmount = 0;
        double todaySoldLiters = 0;
        double todayUnpaidAmount = 0;
        for (Sale s : todaySales) {
            todaySoldAmount += s.getAmount();
            todaySoldLiters += s.getQuantity();
            if (!s.isPaid()) {
                todayUnpaidAmount += s.getAmount();
            }
        }

        // ---- STOCK (purchased - sold, no negative) ----
        double cowSold = getCowSoldToday();
        double bufSold = getBufSoldToday();
        double stockCow = Math.max(0, todayCowLiters - cowSold);
        double stockBuf = Math.max(0, todayBufLiters - bufSold);
        double stockTotal = Math.max(0, todayTotalLiters - todaySoldLiters);

        // ---- PROFIT (sold amount - purchase cost of sold milk) ----
        double avgPurchaseRate = todayTotalLiters > 0 ? todayPurchaseCost / todayTotalLiters : 0;
        double costOfSoldMilk = todaySoldLiters * avgPurchaseRate;
        double profit = todaySoldAmount > 0 ? todaySoldAmount - costOfSoldMilk : 0;

        // ---- MONTHLY STATS ----
        double[] monthly = recordDao.getMonthlyStats(thisMonth);
        double monthlyLiters = monthly[0] + monthly[1];
        double monthlySalesAmount = saleDao.getTotalAmountByMonth(thisMonth);
        double monthlyUnpaid = getMonthlyUnpaid();

        // ---- UPDATE UI ----
        binding.tvTodayDate.setText(DateUtils.dbToDisplay(today));

        // Today's purchased milk
        binding.tvCowLiters.setText(FormatUtils.liters(todayCowLiters));
        binding.tvBufLiters.setText(FormatUtils.liters(todayBufLiters));

        // Earnings + Outstanding
        binding.tvTodayEarnings.setText(FormatUtils.money(todaySoldAmount));
        binding.tvOutstanding.setText(FormatUtils.money(todayUnpaidAmount));
        binding.tvOutstanding.setTextColor(todayUnpaidAmount > 0.01
                ? getResources().getColor(R.color.pending_color, getTheme())
                : getResources().getColor(R.color.earnings_color, getTheme()));

        // Purchase
        binding.tvPurchaseCow.setText(FormatUtils.liters(todayCowLiters));
        binding.tvPurchaseBuf.setText(FormatUtils.liters(todayBufLiters));
        binding.tvPurchaseTotal.setText(FormatUtils.liters(todayTotalLiters));
        binding.tvPurchaseCost.setText(FormatUtils.money(todayPurchaseCost));

        // Stock
        binding.tvStockCow.setText(FormatUtils.liters(stockCow));
        binding.tvStockBuf.setText(FormatUtils.liters(stockBuf));
        binding.tvStockTotal.setText(FormatUtils.liters(stockTotal));

        // Profit
        binding.tvProfitSold.setText(FormatUtils.money(todaySoldAmount));
        binding.tvProfitCost.setText(FormatUtils.money(costOfSoldMilk));
        binding.tvProfit.setText(FormatUtils.money(profit));
        binding.tvProfit.setTextColor(profit >= 0
                ? getResources().getColor(R.color.earnings_color, getTheme())
                : getResources().getColor(R.color.pending_color, getTheme()));

        // Monthly
        binding.tvMonthlyLiters.setText(FormatUtils.liters(monthlyLiters));
        binding.tvMonthlyEarnings.setText(FormatUtils.money(monthlySalesAmount));
        binding.tvMonthlyOutstanding.setText(FormatUtils.money(monthlyUnpaid));
        binding.tvMonthlyOutstanding.setTextColor(monthlyUnpaid > 0.01
                ? getResources().getColor(R.color.pending_color, getTheme())
                : getResources().getColor(R.color.earnings_color, getTheme()));
    }

    private double getMonthlyUnpaid() {
        List<Sale> monthSales = saleDao.getByMonth(DateUtils.currentMonthDb());
        double unpaid = 0;
        for (Sale s : monthSales) {
            if (!s.isPaid()) unpaid += s.getAmount();
        }
        return unpaid;
    }

    private double getCowSoldToday() {
        List<Sale> todaySales = saleDao.getByDate(DateUtils.todayDb());
        double cowSold = 0;
        for (Sale s : todaySales) {
            if (Sale.MILK_COW.equals(s.getMilkType())) cowSold += s.getQuantity();
        }
        return cowSold;
    }

    private double getBufSoldToday() {
        List<Sale> todaySales = saleDao.getByDate(DateUtils.todayDb());
        double bufSold = 0;
        for (Sale s : todaySales) {
            if (Sale.MILK_BUFFALO.equals(s.getMilkType())) bufSold += s.getQuantity();
        }
        return bufSold;
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
        String name = session.getUserName();
        if (name == null || name.isEmpty()) {
            name = PreferenceManager.getDefaultSharedPreferences(this)
                .getString("profile_name", "").trim();
        }
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
        binding.btnViewMyRecords.setOnClickListener(v ->
                startActivity(new Intent(this, HistoryActivity.class)));
        binding.btnSuppliers.setOnClickListener(v ->
                startActivity(new Intent(this, SupplierListActivity.class)));
        binding.btnCustomers.setOnClickListener(v ->
                startActivity(new Intent(this, CustomerListActivity.class)));
        binding.btnSales.setOnClickListener(v ->
                startActivity(new Intent(this, SalesActivity.class)));
        binding.btnHistory.setOnClickListener(v ->
                startActivity(new Intent(this, HistoryActivity.class)));
        binding.btnMonthlySummary.setOnClickListener(v ->
                startActivity(new Intent(this, MonthlySummaryActivity.class)));
        binding.btnSettings.setOnClickListener(v ->
                startActivity(new Intent(this, SettingsActivity.class)));
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
