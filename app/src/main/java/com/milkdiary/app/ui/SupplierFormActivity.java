package com.milkdiary.app.ui;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.milkdiary.app.databinding.ActivitySupplierFormBinding;
import com.milkdiary.app.db.SupplierDao;
import com.milkdiary.app.model.Supplier;
import com.milkdiary.app.util.RoleManager;

public class SupplierFormActivity extends AppCompatActivity {

    private ActivitySupplierFormBinding binding;
    private SupplierDao supplierDao;
    private long editId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RoleManager.requireDairy(this);
        binding = ActivitySupplierFormBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        supplierDao = new SupplierDao(this);
        editId = getIntent().getLongExtra("supplier_id", -1);

        if (editId != -1) {
            setTitle("Edit Supplier");
            loadSupplier(editId);
        } else {
            setTitle("Add Supplier");
        }

        setupMilkTypeSelector();
        setupSaveButton();
    }

    private void setupMilkTypeSelector() {
        binding.chipCow.setOnClickListener(v -> {
            binding.chipCow.setChecked(true);
            binding.chipBuffalo.setChecked(false);
            binding.chipBoth.setChecked(false);
        });
        binding.chipBuffalo.setOnClickListener(v -> {
            binding.chipCow.setChecked(false);
            binding.chipBuffalo.setChecked(true);
            binding.chipBoth.setChecked(false);
        });
        binding.chipBoth.setOnClickListener(v -> {
            binding.chipCow.setChecked(false);
            binding.chipBuffalo.setChecked(false);
            binding.chipBoth.setChecked(true);
        });
    }

    private void loadSupplier(long id) {
        Supplier s = supplierDao.getById(id);
        if (s == null) {
            Toast.makeText(this, "Supplier not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        binding.etName.setText(s.getName());
        binding.etPhone.setText(s.getPhone());
        binding.etVillage.setText(s.getVillage());
        if (s.getDefaultCowRate() > 0)
            binding.etCowRate.setText(String.valueOf(s.getDefaultCowRate()));
        if (s.getDefaultBuffaloRate() > 0)
            binding.etBufRate.setText(String.valueOf(s.getDefaultBuffaloRate()));

        switch (s.getMilkType()) {
            case Supplier.MILK_COW:
                binding.chipCow.setChecked(true);
                break;
            case Supplier.MILK_BUFFALO:
                binding.chipBuffalo.setChecked(true);
                break;
            default:
                binding.chipBoth.setChecked(true);
                break;
        }
    }

    private void setupSaveButton() {
        binding.btnSave.setOnClickListener(v -> saveSupplier());
    }

    private void saveSupplier() {
        String name = binding.etName.getText().toString().trim();
        String phone = binding.etPhone.getText().toString().trim();
        String village = binding.etVillage.getText().toString().trim();

        if (name.isEmpty()) {
            binding.etName.setError("Name is required");
            binding.etName.requestFocus();
            return;
        }

        String milkType = Supplier.MILK_BOTH;
        if (binding.chipCow.isChecked()) milkType = Supplier.MILK_COW;
        else if (binding.chipBuffalo.isChecked()) milkType = Supplier.MILK_BUFFALO;

        double cowRate = 0;
        double bufRate = 0;
        try {
            if (!binding.etCowRate.getText().toString().trim().isEmpty())
                cowRate = Double.parseDouble(binding.etCowRate.getText().toString().trim().replace(',', '.'));
            if (!binding.etBufRate.getText().toString().trim().isEmpty())
                bufRate = Double.parseDouble(binding.etBufRate.getText().toString().trim().replace(',', '.'));
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid rate value", Toast.LENGTH_SHORT).show();
            return;
        }

        if (editId != -1) {
            Supplier s = supplierDao.getById(editId);
            if (s != null) {
                s.setName(name);
                s.setPhone(phone);
                s.setVillage(village);
                s.setMilkType(milkType);
                s.setDefaultCowRate(cowRate);
                s.setDefaultBuffaloRate(bufRate);
                supplierDao.update(s);
                Toast.makeText(this, "Supplier updated", Toast.LENGTH_SHORT).show();
            }
        } else {
            Supplier s = new Supplier(name, phone, village, milkType, cowRate, bufRate);
            supplierDao.insert(s);
            Toast.makeText(this, "Supplier added", Toast.LENGTH_SHORT).show();
        }
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { finish(); return true; }
        return super.onOptionsItemSelected(item);
    }
}
