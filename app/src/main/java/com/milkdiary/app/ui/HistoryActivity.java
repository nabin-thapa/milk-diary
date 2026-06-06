package com.milkdiary.app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.milkdiary.app.databinding.ActivityHistoryBinding;
import com.milkdiary.app.db.MilkRecordDao;
import com.milkdiary.app.model.MilkRecord;
import com.milkdiary.app.ui.adapter.RecordAdapter;
import com.milkdiary.app.util.RoleManager;

import java.util.List;

public class HistoryActivity extends AppCompatActivity implements RecordAdapter.RecordListener {

    private ActivityHistoryBinding binding;
    private MilkRecordDao recordDao;
    private RecordAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHistoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("History");
        }

        recordDao = new MilkRecordDao(this);

        binding.recyclerHistory.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RecordAdapter(this);
        binding.recyclerHistory.setAdapter(adapter);

        setupSearch();
        loadAllRecords();
    }

    @Override
    protected void onResume() {
        super.onResume();
        String query = binding.etSearch.getText().toString().trim();
        if (query.isEmpty()) loadAllRecords();
        else searchRecords(query);
    }

    private void loadAllRecords() {
        List<MilkRecord> records = recordDao.getAllRecords();
        adapter.setRecords(records);
        binding.tvEmpty.setVisibility(records.isEmpty() ? View.VISIBLE : View.GONE);
        binding.recyclerHistory.setVisibility(records.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private void searchRecords(String query) {
        List<MilkRecord> records = recordDao.searchByDate(query);
        adapter.setRecords(records);
        binding.tvEmpty.setVisibility(records.isEmpty() ? View.VISIBLE : View.GONE);
        binding.recyclerHistory.setVisibility(records.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private void setupSearch() {
        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) {}
            @Override public void afterTextChanged(Editable s) {
                String q = s.toString().trim();
                if (q.isEmpty()) loadAllRecords();
                else searchRecords(q);
            }
        });
    }

    @Override
    public void onEditRecord(MilkRecord record) {
        if (new RoleManager(this).isSalesman()) {
            Toast.makeText(this, "View only - Salesman cannot edit records",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(this, AddRecordActivity.class);
        intent.putExtra(AddRecordActivity.EXTRA_RECORD_ID, record.getId());
        startActivity(intent);
    }

    @Override
    public void onDeleteRecord(MilkRecord record) {
        if (new RoleManager(this).isSalesman()) {
            Toast.makeText(this, "View only - Salesman cannot delete records",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle("Delete Record")
                .setMessage("Delete record for " + record.getDate() + "?")
                .setPositiveButton("Delete", (d, w) -> {
                    recordDao.deleteRecord(record.getId());
                    loadAllRecords();
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
