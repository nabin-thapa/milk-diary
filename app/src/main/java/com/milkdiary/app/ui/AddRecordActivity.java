package com.milkdiary.app.ui;

import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import com.milkdiary.app.databinding.ActivityAddRecordBinding;
import com.milkdiary.app.db.MilkRecordDao;
import com.milkdiary.app.model.MilkRecord;
import com.milkdiary.app.util.DateUtils;
import com.milkdiary.app.util.FormatUtils;

import java.util.Calendar;

public class AddRecordActivity extends AppCompatActivity {

    public static final String EXTRA_RECORD_ID = "record_id";
    public static final String EXTRA_SESSION_TYPE = "session_type";
    public static final String PREF_COW_RATE   = "default_cow_rate";
    public static final String PREF_BUF_RATE   = "default_buf_rate";

    private ActivityAddRecordBinding binding;
    private MilkRecordDao recordDao;
    private SharedPreferences prefs;
    private String selectedDate;
    private String sessionType = MilkRecord.SESSION_MORNING;
    private long editRecordId = -1;
    private boolean isUpdating = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddRecordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        recordDao = new MilkRecordDao(this);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        selectedDate = DateUtils.todayDb();

        // Get session type from intent
        sessionType = getIntent().getStringExtra(EXTRA_SESSION_TYPE);
        if (sessionType == null) {
            sessionType = MilkRecord.SESSION_MORNING;
        }

        editRecordId = getIntent().getLongExtra(EXTRA_RECORD_ID, -1);

        if (editRecordId != -1) {
            loadRecord(editRecordId);
        } else {
            // Validate no duplicate session
            if (recordDao.getRecordByDateAndSession(selectedDate, sessionType) != null) {
                String msg = sessionType.equals(MilkRecord.SESSION_MORNING)
                        ? "Morning milk already recorded."
                        : "Evening milk already recorded.";
                Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
                finish();
                return;
            }
            setToolbarTitle(false);
            updateDateButton();
            prefillDefaultRates();
        }

        setupDatePicker();
        setupAutoCalculation();
        setupSaveButton();

        binding.etCowLiters.requestFocus();
    }

    private void setToolbarTitle(boolean isEdit) {
        if (getSupportActionBar() == null) return;
        String sessionLabel = sessionType.equals(MilkRecord.SESSION_MORNING) ? "Morning" : "Evening";
        if (isEdit) {
            getSupportActionBar().setTitle("Edit " + sessionLabel);
        } else {
            getSupportActionBar().setTitle("Add " + sessionLabel + " Milk");
        }
    }

    private void prefillDefaultRates() {
        float cowRate = prefs.getFloat(PREF_COW_RATE, 0f);
        float bufRate = prefs.getFloat(PREF_BUF_RATE, 0f);
        isUpdating = true;
        if (cowRate > 0) binding.etCowRate.setText(FormatUtils.rateValue(cowRate));
        if (bufRate > 0) binding.etBufRate.setText(FormatUtils.rateValue(bufRate));
        isUpdating = false;
        recalculate();
    }

    private void loadRecord(long id) {
        MilkRecord record = recordDao.getRecordById(id);
        if (record == null) {
            Toast.makeText(this, "Record not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Check one-time edit rule
        if (!record.canEdit()) {
            Toast.makeText(this, "This record can only be edited once.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        sessionType = record.getSessionType();
        selectedDate = record.getDate();
        setToolbarTitle(true);
        updateDateButton();

        isUpdating = true;
        if (record.getCowLiters() > 0)
            binding.etCowLiters.setText(FormatUtils.inputValue(record.getCowLiters()));
        if (record.getCowRate() > 0)
            binding.etCowRate.setText(FormatUtils.inputValue(record.getCowRate()));
        if (record.getBuffaloLiters() > 0)
            binding.etBufLiters.setText(FormatUtils.inputValue(record.getBuffaloLiters()));
        if (record.getBuffaloRate() > 0)
            binding.etBufRate.setText(FormatUtils.inputValue(record.getBuffaloRate()));
        if (record.getNote() != null)
            binding.etNote.setText(record.getNote());
        isUpdating = false;
        recalculate();
    }

    private void updateDateButton() {
        binding.btnDate.setText(DateUtils.dbToDisplay(selectedDate));
    }

    private void setupDatePicker() {
        binding.btnDate.setOnClickListener(v -> {
            String[] parts = selectedDate.split("-");
            int year  = Integer.parseInt(parts[0]);
            int month = Integer.parseInt(parts[1]) - 1;
            int day   = Integer.parseInt(parts[2]);
            new DatePickerDialog(this, (view, y, m, d) -> {
                Calendar cal = Calendar.getInstance();
                cal.set(y, m, d);
                selectedDate = DateUtils.calendarToDb(cal);
                updateDateButton();
            }, year, month, day).show();
        });
    }

    private void setupAutoCalculation() {
        TextWatcher watcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) {}
            @Override public void afterTextChanged(Editable s) {
                if (!isUpdating) recalculate();
            }
        };
        binding.etCowLiters.addTextChangedListener(watcher);
        binding.etCowRate.addTextChangedListener(watcher);
        binding.etBufLiters.addTextChangedListener(watcher);
        binding.etBufRate.addTextChangedListener(watcher);
    }

    private void recalculate() {
        isUpdating = true;
        double cowL = parseDecimal(binding.etCowLiters.getText().toString());
        double cowR = parseDecimal(binding.etCowRate.getText().toString());
        double bufL = parseDecimal(binding.etBufLiters.getText().toString());
        double bufR = parseDecimal(binding.etBufRate.getText().toString());

        double cowAmt = cowL * cowR;
        double bufAmt = bufL * bufR;
        double grand  = cowAmt + bufAmt;

        binding.tvCowAmount.setText(FormatUtils.money(cowAmt));
        binding.tvBufAmount.setText(FormatUtils.money(bufAmt));
        binding.tvGrandTotal.setText(FormatUtils.money(grand));
        isUpdating = false;
    }

    private void setupSaveButton() {
        binding.btnSave.setOnClickListener(v -> saveRecord());
    }

    private void saveRecord() {
        double cowL = parseDecimal(binding.etCowLiters.getText().toString());
        double cowR = parseDecimal(binding.etCowRate.getText().toString());
        double bufL = parseDecimal(binding.etBufLiters.getText().toString());
        double bufR = parseDecimal(binding.etBufRate.getText().toString());
        String note = binding.etNote.getText().toString().trim();

        if (cowL <= 0 && bufL <= 0) {
            Toast.makeText(this,
                    "Enter at least some milk quantity (Cow or Buffalo)",
                    Toast.LENGTH_SHORT).show();
            binding.etCowLiters.requestFocus();
            return;
        }
        if (cowL > 0 && cowR <= 0) {
            binding.etCowRate.setError("Enter cow milk rate");
            binding.etCowRate.requestFocus();
            return;
        }
        if (bufL > 0 && bufR <= 0) {
            binding.etBufRate.setError("Enter buffalo milk rate");
            binding.etBufRate.requestFocus();
            return;
        }
        if (cowL > 500 || bufL > 500) {
            Toast.makeText(this,
                    "Liters value looks too high. Please check.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (cowR > 0) prefs.edit().putFloat(PREF_COW_RATE, (float) cowR).apply();
        if (bufR > 0) prefs.edit().putFloat(PREF_BUF_RATE, (float) bufR).apply();

        MilkRecord record = new MilkRecord(selectedDate, sessionType, cowL, cowR, bufL, bufR, note);

        if (editRecordId != -1) {
            record.setId(editRecordId);
            // Increment edit count for one-time edit rule
            MilkRecord existing = recordDao.getRecordById(editRecordId);
            if (existing != null) {
                record.setEditCount(existing.getEditCount() + 1);
            }
            int rows = recordDao.updateRecord(record);
            if (rows > 0) {
                Toast.makeText(this, "Record updated ✓", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Update failed — please try again", Toast.LENGTH_SHORT).show();
            }
        } else {
            long id = recordDao.insertRecord(record);
            if (id > 0) {
                Toast.makeText(this, "Record saved ✓", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Save failed — please try again", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private double parseDecimal(String s) {
        if (s == null || s.trim().isEmpty()) return 0.0;
        try {
            return Double.parseDouble(s.trim().replace(',', '.'));
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { finish(); return true; }
        return super.onOptionsItemSelected(item);
    }
}
