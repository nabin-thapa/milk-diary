package com.milkdiary.app.ui;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.milkdiary.app.databinding.ActivityCustomerFormBinding;
import com.milkdiary.app.db.CustomerDao;
import com.milkdiary.app.model.Customer;
import com.milkdiary.app.util.RoleManager;

public class CustomerFormActivity extends AppCompatActivity {

    private static final String[] CUSTOMER_TYPES = {"Hotel", "Tea Shop", "Home", "Other"};
    private static final String[] CUSTOMER_TYPE_VALUES = {
            Customer.TYPE_HOTEL, Customer.TYPE_TEA_SHOP, Customer.TYPE_HOME, Customer.TYPE_OTHER
    };

    private ActivityCustomerFormBinding binding;
    private CustomerDao customerDao;
    private long editId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RoleManager.requireDairy(this);
        binding = ActivityCustomerFormBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        customerDao = new CustomerDao(this);
        editId = getIntent().getLongExtra("customer_id", -1);

        if (editId != -1) {
            setTitle("Edit Customer");
            loadCustomer(editId);
        } else {
            setTitle("Add Customer");
        }

        setupCustomerTypeSpinner();
        setupSaveButton();
    }

    private void setupCustomerTypeSpinner() {
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, CUSTOMER_TYPES);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerCustomerType.setAdapter(typeAdapter);
    }

    private void loadCustomer(long id) {
        Customer c = customerDao.getById(id);
        if (c == null) {
            Toast.makeText(this, "Customer not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        binding.etName.setText(c.getName());
        binding.etPhone.setText(c.getPhone());
        binding.etAddress.setText(c.getAddress());

        for (int i = 0; i < CUSTOMER_TYPE_VALUES.length; i++) {
            if (CUSTOMER_TYPE_VALUES[i].equals(c.getCustomerType())) {
                binding.spinnerCustomerType.setSelection(i);
                break;
            }
        }
    }

    private void setupSaveButton() {
        binding.btnSave.setOnClickListener(v -> saveCustomer());
    }

    private void saveCustomer() {
        String name = binding.etName.getText().toString().trim();
        String phone = binding.etPhone.getText().toString().trim();
        String address = binding.etAddress.getText().toString().trim();

        if (name.isEmpty()) {
            binding.etName.setError("Name is required");
            binding.etName.requestFocus();
            return;
        }

        int typeIndex = binding.spinnerCustomerType.getSelectedItemPosition();
        String customerType = CUSTOMER_TYPE_VALUES[typeIndex];

        if (editId != -1) {
            Customer c = customerDao.getById(editId);
            if (c != null) {
                c.setName(name);
                c.setPhone(phone);
                c.setCustomerType(customerType);
                c.setAddress(address);
                customerDao.update(c);
                Toast.makeText(this, "Customer updated", Toast.LENGTH_SHORT).show();
            }
        } else {
            Customer c = new Customer(name, phone, customerType, address);
            customerDao.insert(c);
            Toast.makeText(this, "Customer added", Toast.LENGTH_SHORT).show();
        }
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { finish(); return true; }
        return super.onOptionsItemSelected(item);
    }
}
