package com.milkdiary.app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.milkdiary.app.databinding.ActivitySupplierListBinding;
import com.milkdiary.app.db.SupplierDao;
import com.milkdiary.app.model.Supplier;
import com.milkdiary.app.ui.adapter.SupplierAdapter;
import com.milkdiary.app.util.RoleManager;

import java.util.List;

public class SupplierListActivity extends AppCompatActivity implements SupplierAdapter.SupplierListener {

    private static final int REQUEST_ADD = 1001;
    private static final int REQUEST_EDIT = 1002;

    private ActivitySupplierListBinding binding;
    private SupplierDao supplierDao;
    private SupplierAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RoleManager.requireDairy(this);
        binding = ActivitySupplierListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Suppliers");
        }

        supplierDao = new SupplierDao(this);

        binding.recyclerSuppliers.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SupplierAdapter(this);
        binding.recyclerSuppliers.setAdapter(adapter);

        setupSearch();
        setupFab();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadSuppliers();
    }

    private void loadSuppliers() {
        String query = binding.etSearch.getText().toString().trim();
        List<Supplier> suppliers;
        if (query.isEmpty()) {
            suppliers = supplierDao.getAll();
        } else {
            suppliers = supplierDao.search(query);
        }
        adapter.setSuppliers(suppliers);
        binding.tvEmpty.setVisibility(suppliers.isEmpty() ? View.VISIBLE : View.GONE);
        binding.recyclerSuppliers.setVisibility(suppliers.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private void setupSearch() {
        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) {}
            @Override public void afterTextChanged(Editable s) { loadSuppliers(); }
        });
    }

    private void setupFab() {
        binding.fabAddSupplier.setOnClickListener(v -> {
            startActivityForResult(new Intent(this, SupplierFormActivity.class), REQUEST_ADD);
        });
    }

    @Override
    public void onEditSupplier(Supplier supplier) {
        Intent intent = new Intent(this, SupplierProfileActivity.class);
        intent.putExtra("supplier_id", supplier.getId());
        startActivity(intent);
    }

    @Override
    public void onDeleteSupplier(Supplier supplier) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Supplier")
                .setMessage("Delete supplier \"" + supplier.getName() + "\"?\n\nThis will NOT delete their milk records, but they will be marked inactive.")
                .setPositiveButton("Delete", (d, w) -> {
                    supplier.setActive(false);
                    supplierDao.update(supplier);
                    loadSuppliers();
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
