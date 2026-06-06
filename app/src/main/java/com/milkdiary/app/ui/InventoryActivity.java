package com.milkdiary.app.ui;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.milkdiary.app.databinding.ActivityInventoryBinding;
import com.milkdiary.app.db.InventoryDao;
import com.milkdiary.app.model.Inventory;
import com.milkdiary.app.util.DateUtils;
import com.milkdiary.app.util.FormatUtils;
import com.milkdiary.app.util.RoleManager;

import java.util.Calendar;

public class InventoryActivity extends AppCompatActivity {

    private ActivityInventoryBinding binding;
    private InventoryDao inventoryDao;
    private String selectedDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RoleManager.requireOwner(this);
        binding = ActivityInventoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Daily Inventory");
        }

        inventoryDao = new InventoryDao(this);
        selectedDate = DateUtils.todayDb();

        setupDatePicker();
        setupAutoCalculate();
        setupSaveButton();
        loadInventory();
    }

    private void setupDatePicker() {
        updateDateButton();
        binding.btnDate.setOnClickListener(v -> {
            String[] parts = selectedDate.split("-");
            int year = Integer.parseInt(parts[0]);
            int month = Integer.parseInt(parts[1]) - 1;
            int day = Integer.parseInt(parts[2]);
            new DatePickerDialog(this, (view, y, m, d) -> {
                Calendar cal = Calendar.getInstance();
                cal.set(y, m, d);
                selectedDate = DateUtils.calendarToDb(cal);
                updateDateButton();
                loadInventory();
            }, year, month, day).show();
        });
    }

    private void updateDateButton() {
        binding.btnDate.setText(DateUtils.dbToDisplay(selectedDate));
    }

    private void setupAutoCalculate() {
        binding.btnAutoCalc.setOnClickListener(v -> {
            inventoryDao.autoCalculate(selectedDate);
            loadInventory();
            Toast.makeText(this, "Auto-calculated from milk records & sales",
                    Toast.LENGTH_SHORT).show();
        });
    }

    private void setupSaveButton() {
        binding.btnSave.setOnClickListener(v -> saveInventory());
    }

    private void loadInventory() {
        Inventory inv = inventoryDao.getByDate(selectedDate);
        if (inv == null) {
            clearFields();
            return;
        }
        binding.etMorning.setText(inv.getMorningCollected() > 0
                ? String.valueOf(inv.getMorningCollected()) : "");
        binding.etEvening.setText(inv.getEveningCollected() > 0
                ? String.valueOf(inv.getEveningCollected()) : "");
        binding.etSold.setText(inv.getSold() > 0
                ? String.valueOf(inv.getSold()) : "");
        binding.etWaste.setText(inv.getWaste() > 0
                ? String.valueOf(inv.getWaste()) : "");
        binding.etNote.setText(inv.getNote() != null ? inv.getNote() : "");

        updateCalculated(inv);
    }

    private void clearFields() {
        binding.etMorning.setText("");
        binding.etEvening.setText("");
        binding.etSold.setText("");
        binding.etWaste.setText("");
        binding.etNote.setText("");
        updateCalculated(null);
    }

    private void updateCalculated(Inventory inv) {
        if (inv != null) {
            binding.tvTotalCollected.setText(FormatUtils.liters(inv.getTotalCollected()));
            binding.tvClosingStock.setText(FormatUtils.liters(inv.getClosingStock()));
        } else {
            binding.tvTotalCollected.setText("0.00 L");
            binding.tvClosingStock.setText("0.00 L");
        }
    }

    private void saveInventory() {
        Inventory inv = inventoryDao.getOrCreateToday(selectedDate);

        double morning = parseDecimal(binding.etMorning.getText().toString());
        double evening = parseDecimal(binding.etEvening.getText().toString());
        double sold = parseDecimal(binding.etSold.getText().toString());
        double waste = parseDecimal(binding.etWaste.getText().toString());
        String note = binding.etNote.getText().toString().trim();

        inv.setMorningCollected(morning);
        inv.setEveningCollected(evening);
        inv.setSold(sold);
        inv.setWaste(waste);
        inv.setNote(note);
        inv.calculateClosing();

        inventoryDao.insert(inv);
        loadInventory();
        Toast.makeText(this, "Inventory saved", Toast.LENGTH_SHORT).show();
    }

    private double parseDecimal(String s) {
        if (s == null || s.trim().isEmpty()) return 0.0;
        try { return Double.parseDouble(s.trim().replace(',', '.')); }
        catch (NumberFormatException e) { return 0.0; }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { finish(); return true; }
        return super.onOptionsItemSelected(item);
    }
}
