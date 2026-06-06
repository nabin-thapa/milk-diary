package com.milkdiary.app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.milkdiary.app.databinding.ActivitySalesBinding;
import com.milkdiary.app.db.SaleDao;
import com.milkdiary.app.model.Sale;
import com.milkdiary.app.ui.adapter.SalesAdapter;
import com.milkdiary.app.util.DateUtils;
import com.milkdiary.app.util.RoleManager;

import java.util.List;

public class SalesActivity extends AppCompatActivity implements SalesAdapter.SaleListener {

    private ActivitySalesBinding binding;
    private SaleDao saleDao;
    private SalesAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RoleManager.requireDairy(this);
        binding = ActivitySalesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Sales");
        }

        saleDao = new SaleDao(this);

        binding.recyclerSales.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SalesAdapter(this);
        binding.recyclerSales.setAdapter(adapter);

        binding.fabAddSale.setOnClickListener(v -> {
            startActivity(new Intent(this, AddSaleActivity.class));
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadSales();
    }

    private void loadSales() {
        List<Sale> sales = saleDao.getByMonth(DateUtils.currentMonthDb());
        adapter.setSales(sales);
        binding.tvEmpty.setVisibility(sales.isEmpty() ? View.VISIBLE : View.GONE);
        binding.recyclerSales.setVisibility(sales.isEmpty() ? View.GONE : View.VISIBLE);

        double totalAmount = saleDao.getTotalAmountByMonth(DateUtils.currentMonthDb());
        double credit = saleDao.getCreditBalance();
        binding.tvTotalSales.setText("Total this month: \u20B9" + String.format("%.2f", totalAmount));

        String creditLabel = credit > 0
                ? "Credit (unpaid): \u20B9" + String.format("%.2f", credit)
                : "Credit: \u20B90.00";
        binding.tvCredit.setText(creditLabel);
    }

    @Override
    public void onEditSale(Sale sale) {
        Intent intent = new Intent(this, AddSaleActivity.class);
        intent.putExtra("sale_id", sale.getId());
        startActivity(intent);
    }

    @Override
    public void onDeleteSale(Sale sale) {
        saleDao.delete(sale.getId());
        loadSales();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { finish(); return true; }
        return super.onOptionsItemSelected(item);
    }
}
