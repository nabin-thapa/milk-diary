package com.milkdiary.app.ui;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.milkdiary.app.databinding.ActivityAddSaleBinding;
import com.milkdiary.app.db.CustomerDao;
import com.milkdiary.app.db.SaleDao;
import com.milkdiary.app.model.Customer;
import com.milkdiary.app.model.Sale;
import com.milkdiary.app.util.DateUtils;
import com.milkdiary.app.util.RoleManager;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class AddSaleActivity extends AppCompatActivity {

    private ActivityAddSaleBinding binding;
    private SaleDao saleDao;
    private CustomerDao customerDao;
    private String selectedDate;
    private long editId = -1;
    private long selectedCustomerId = -1;
    private List<Customer> customerList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RoleManager.requireDairy(this);
        binding = ActivityAddSaleBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        saleDao = new SaleDao(this);
        customerDao = new CustomerDao(this);
        selectedDate = DateUtils.todayDb();

        editId = getIntent().getLongExtra("sale_id", -1);

        setupCustomerDropdown();
        setupCustomerTypeSpinner();
        setupMilkTypeSpinner();
        setupSessionSpinner();
        setupDatePicker();
        setupCalculation();

        if (editId != -1) {
            setTitle("Edit Sale");
            loadSale(editId);
        } else {
            setTitle("Add Sale");
        }

        setupSaveButton();
    }

    private void setupCustomerDropdown() {
        customerList = customerDao.getAll();
        List<String> customerNames = new ArrayList<>();
        for (Customer c : customerList) {
            customerNames.add(c.getName());
        }
        customerNames.add("+ Add New Customer");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, customerNames);
        binding.etCustomerName.setAdapter(adapter);
        binding.etCustomerName.setOnItemClickListener((parent, view, position, id) -> {
            if (position < customerList.size()) {
                Customer selected = customerList.get(position);
                selectedCustomerId = selected.getId();
                binding.etCustomerName.setText(selected.getName());
                for (int i = 0; i < CustomerDao.CUSTOMER_TYPE_VALUES.length; i++) {
                    if (CustomerDao.CUSTOMER_TYPE_VALUES[i].equals(selected.getCustomerType())) {
                        binding.spinnerCustomerType.setSelection(i);
                        break;
                    }
                }
            } else {
                selectedCustomerId = -1;
                startActivity(new Intent(AddSaleActivity.this, CustomerFormActivity.class));
            }
        });
    }

    private void setupCustomerTypeSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                new String[]{"Hotel", "Tea Shop", "Home", "Other"});
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerCustomerType.setAdapter(adapter);
    }

    private void setupMilkTypeSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                new String[]{"Cow", "Buffalo"});
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerMilkType.setAdapter(adapter);
    }

    private void setupSessionSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                new String[]{"Morning", "Evening"});
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerSession.setAdapter(adapter);
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
            }, year, month, day).show();
        });
    }

    private void updateDateButton() {
        binding.btnDate.setText(DateUtils.dbToDisplay(selectedDate));
    }

    private void setupCalculation() {
        AdapterView.OnItemSelectedListener calcListener = new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> p, View v, int pos, long id) { calculate(); }
            @Override public void onNothingSelected(AdapterView<?> p) {}
        };
        binding.etQuantity.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) {}
            @Override public void afterTextChanged(android.text.Editable s) { calculate(); }
        });
        binding.etRate.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) {}
            @Override public void afterTextChanged(android.text.Editable s) { calculate(); }
        });
    }

    private void calculate() {
        double qty = parseDecimal(binding.etQuantity.getText().toString());
        double rate = parseDecimal(binding.etRate.getText().toString());
        double amount = qty * rate;
        binding.tvAmount.setText("\u20B9" + String.format("%.2f", amount));
    }

    private void loadSale(long id) {
        Sale s = saleDao.getById(id);
        if (s == null) {
            Toast.makeText(this, "Sale not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        selectedDate = s.getDate();
        updateDateButton();
        binding.etCustomerName.setText(s.getCustomerName());
        selectedCustomerId = s.getCustomerId();
        binding.etQuantity.setText(String.valueOf(s.getQuantity()));
        binding.etRate.setText(String.valueOf(s.getRate()));
        binding.etNote.setText(s.getNote());
        binding.checkPaid.setChecked(s.isPaid());

        setSpinnerSelection(binding.spinnerCustomerType, mapCustomerTypeToIndex(s.getCustomerType()));
        setSpinnerSelection(binding.spinnerMilkType, s.getMilkType().equals(Sale.MILK_COW) ? 0 : 1);
        setSpinnerSelection(binding.spinnerSession, s.getSessionType().equals("morning") ? 0 : 1);
        calculate();
    }

    private int mapCustomerTypeToIndex(String type) {
        switch (type) {
            case Sale.CUSTOMER_HOTEL: return 0;
            case Sale.CUSTOMER_TEA_SHOP: return 1;
            case Sale.CUSTOMER_HOME: return 2;
            default: return 3;
        }
    }

    private void setSpinnerSelection(android.widget.Spinner spinner, int index) {
        if (index >= 0 && index < spinner.getAdapter().getCount()) {
            spinner.setSelection(index);
        }
    }

    private void setupSaveButton() {
        binding.btnSave.setOnClickListener(v -> saveSale());
    }

    private void saveSale() {
        String customerName = binding.etCustomerName.getText().toString().trim();
        String qtyStr = binding.etQuantity.getText().toString().trim();
        String rateStr = binding.etRate.getText().toString().trim();

        if (customerName.isEmpty()) {
            binding.etCustomerName.setError("Customer name required");
            binding.etCustomerName.requestFocus();
            return;
        }
        if (qtyStr.isEmpty()) {
            binding.etQuantity.setError("Enter quantity");
            binding.etQuantity.requestFocus();
            return;
        }
        if (rateStr.isEmpty()) {
            binding.etRate.setError("Enter rate");
            binding.etRate.requestFocus();
            return;
        }

        double qty = parseDecimal(qtyStr);
        double rate = parseDecimal(rateStr);

        String[] customerTypes = {Sale.CUSTOMER_HOTEL, Sale.CUSTOMER_TEA_SHOP, Sale.CUSTOMER_HOME, Sale.CUSTOMER_OTHER};
        String customerType = customerTypes[binding.spinnerCustomerType.getSelectedItemPosition()];
        String milkType = binding.spinnerMilkType.getSelectedItemPosition() == 0 ? Sale.MILK_COW : Sale.MILK_BUFFALO;
        String session = binding.spinnerSession.getSelectedItemPosition() == 0 ? "morning" : "evening";
        boolean isPaid = binding.checkPaid.isChecked();
        String note = binding.etNote.getText().toString().trim();

        if (editId != -1) {
            Sale s = saleDao.getById(editId);
            if (s != null) {
                s.setDate(selectedDate);
                s.setSessionType(session);
                s.setCustomerName(customerName);
                s.setCustomerType(customerType);
                s.setMilkType(milkType);
                s.setQuantity(qty);
                s.setRate(rate);
                s.setAmount(qty * rate);
                s.setPaid(isPaid);
                s.setNote(note);
                s.setCustomerId(selectedCustomerId);
                saleDao.update(s);
                Toast.makeText(this, "Sale updated", Toast.LENGTH_SHORT).show();
            }
        } else {
            Sale s = new Sale(selectedDate, session, customerName, customerType, milkType, qty, rate, isPaid, note);
            s.setCustomerId(selectedCustomerId);
            saleDao.insert(s);
            Toast.makeText(this, "Sale saved", Toast.LENGTH_SHORT).show();
        }
        finish();
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
