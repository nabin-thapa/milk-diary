package com.milkdiary.app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.milkdiary.app.databinding.ActivityMonthlySummaryBinding;
import com.milkdiary.app.db.MilkRecordDao;
import com.milkdiary.app.model.MilkRecord;
import com.milkdiary.app.model.MonthlySummary;
import com.milkdiary.app.ui.adapter.RecordAdapter;
import com.milkdiary.app.util.DateUtils;
import com.milkdiary.app.util.FormatUtils;

import java.util.Calendar;
import java.util.List;

public class MonthlySummaryActivity extends AppCompatActivity implements RecordAdapter.RecordListener {

    private ActivityMonthlySummaryBinding binding;
    private MilkRecordDao recordDao;
    private RecordAdapter adapter;
    private String selectedYearMonth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMonthlySummaryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Monthly Summary");
        }

        recordDao = new MilkRecordDao(this);
        selectedYearMonth = DateUtils.currentMonthDb();

        adapter = new RecordAdapter(this);
        binding.recyclerMonthRecords.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerMonthRecords.setAdapter(adapter);

        setupMonthPicker();
        loadSummary();
    }

    private void setupMonthPicker() {
        updateMonthLabel();

        binding.btnPrevMonth.setOnClickListener(v -> {
            selectedYearMonth = shiftMonth(selectedYearMonth, -1);
            updateMonthLabel();
            loadSummary();
        });

        binding.btnNextMonth.setOnClickListener(v -> {
            selectedYearMonth = shiftMonth(selectedYearMonth, +1);
            updateMonthLabel();
            loadSummary();
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

    private void loadSummary() {
        MonthlySummary summary = recordDao.getMonthlySummary(selectedYearMonth);
        List<MilkRecord> records = recordDao.getRecordsByMonth(selectedYearMonth);

        binding.tvCowTotal.setText(FormatUtils.liters(summary.getTotalCowLiters()));
        binding.tvBufTotal.setText(FormatUtils.liters(summary.getTotalBuffaloLiters()));
        binding.tvTotalLiters.setText(FormatUtils.liters(summary.getTotalLiters()));
        binding.tvTotalEarnings.setText(FormatUtils.money(summary.getTotalEarnings()));
        binding.tvAvgDaily.setText(FormatUtils.money(summary.getAvgDailyEarnings()));
        binding.tvDaysRecorded.setText(summary.getRecordCount() + " days recorded");

        adapter.setRecords(records);
        binding.tvNoData.setVisibility(records.isEmpty() ? View.VISIBLE : View.GONE);
    }

    // RecordAdapter.RecordListener — open edit screen from summary list
    @Override
    public void onEditRecord(MilkRecord record) {
        Intent intent = new Intent(this, AddRecordActivity.class);
        intent.putExtra(AddRecordActivity.EXTRA_RECORD_ID, record.getId());
        startActivity(intent);
    }

    @Override
    public void onDeleteRecord(MilkRecord record) {
        // Not allowed from summary view — user should use History for deletes
        android.widget.Toast.makeText(this,
                "Use History screen to delete records", android.widget.Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh after returning from edit
        loadSummary();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { finish(); return true; }
        return super.onOptionsItemSelected(item);
    }
}
