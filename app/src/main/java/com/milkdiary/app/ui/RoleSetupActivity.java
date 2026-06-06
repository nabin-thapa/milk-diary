package com.milkdiary.app.ui;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.milkdiary.app.R;
import com.milkdiary.app.databinding.ActivityRoleSetupBinding;
import com.milkdiary.app.util.RoleManager;

public class RoleSetupActivity extends AppCompatActivity {

    private ActivityRoleSetupBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRoleSetupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        RoleManager roleManager = new RoleManager(this);

        binding.btnDairy.setOnClickListener(v -> {
            roleManager.setRole(RoleManager.ROLE_DAIRY);
            proceedToDashboard();
        });

        binding.btnSupplier.setOnClickListener(v -> {
            roleManager.setRole(RoleManager.ROLE_SUPPLIER);
            proceedToDashboard();
        });
    }

    private void proceedToDashboard() {
        Intent intent = new Intent(this, DashboardActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
