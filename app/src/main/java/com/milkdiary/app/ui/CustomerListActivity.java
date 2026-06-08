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

import com.milkdiary.app.databinding.ActivityCustomerListBinding;
import com.milkdiary.app.db.CustomerDao;
import com.milkdiary.app.model.Customer;
import com.milkdiary.app.ui.adapter.CustomerAdapter;
import com.milkdiary.app.util.RoleManager;

import java.util.List;

public class CustomerListActivity extends AppCompatActivity implements CustomerAdapter.CustomerListener {

    private static final int REQUEST_ADD = 2001;
    private static final int REQUEST_EDIT = 2002;

    private ActivityCustomerListBinding binding;
    private CustomerDao customerDao;
    private CustomerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RoleManager.requireDairy(this);
        binding = ActivityCustomerListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Customers");
        }

        customerDao = new CustomerDao(this);

        binding.recyclerCustomers.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CustomerAdapter(this);
        binding.recyclerCustomers.setAdapter(adapter);

        setupSearch();
        setupFab();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCustomers();
    }

    private void loadCustomers() {
        String query = binding.etSearch.getText().toString().trim();
        List<Customer> customers;
        if (query.isEmpty()) {
            customers = customerDao.getAll();
        } else {
            customers = customerDao.search(query);
        }
        adapter.setCustomers(customers);
        binding.tvEmpty.setVisibility(customers.isEmpty() ? View.VISIBLE : View.GONE);
        binding.recyclerCustomers.setVisibility(customers.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private void setupSearch() {
        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) {}
            @Override public void afterTextChanged(Editable s) { loadCustomers(); }
        });
    }

    private void setupFab() {
        binding.fabAddCustomer.setOnClickListener(v -> {
            startActivityForResult(new Intent(this, CustomerFormActivity.class), REQUEST_ADD);
        });
    }

    @Override
    public void onEditCustomer(Customer customer) {
        Intent intent = new Intent(this, CustomerFormActivity.class);
        intent.putExtra("customer_id", customer.getId());
        startActivityForResult(intent, REQUEST_EDIT);
    }

    @Override
    public void onDeleteCustomer(Customer customer) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Customer")
                .setMessage("Delete customer \"" + customer.getName() + "\"?\n\nExisting sales will keep the customer name.")
                .setPositiveButton("Delete", (d, w) -> {
                    customerDao.delete(customer.getId());
                    loadCustomers();
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
