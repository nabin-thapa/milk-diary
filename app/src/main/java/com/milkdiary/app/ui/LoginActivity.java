package com.milkdiary.app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.milkdiary.app.databinding.ActivityLoginBinding;
import com.milkdiary.app.db.UserDao;
import com.milkdiary.app.model.User;
import com.milkdiary.app.model.SupplierProfile;
import com.milkdiary.app.util.RoleManager;
import com.milkdiary.app.util.SessionManager;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private UserDao userDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Login");
        }

        userDao = new UserDao(this);

        binding.etPhone.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) {}
            @Override public void afterTextChanged(Editable s) {
                binding.tilPhone.setError(null);
            }
        });

        binding.etPassword.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) {}
            @Override public void afterTextChanged(Editable s) {
                binding.tilPassword.setError(null);
            }
        });

        binding.btnLogin.setOnClickListener(v -> attemptLogin());

        binding.tvGoToSignup.setOnClickListener(v -> {
            startActivity(new Intent(this, SignupActivity.class));
            finish();
        });
    }

    private void attemptLogin() {
        String phone = binding.etPhone.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();
        boolean rememberMe = binding.cbRememberMe.isChecked();

        if (phone.isEmpty()) {
            binding.tilPhone.setError("Phone number is required");
            binding.etPhone.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            binding.tilPassword.setError("Password is required");
            binding.etPassword.requestFocus();
            return;
        }

        User user = userDao.verifyLogin(phone, password);

        if (user == null) {
            Toast.makeText(this, "Wrong phone number or password", Toast.LENGTH_SHORT).show();
            return;
        }

        long supplierId = 0;
        if (user.isSupplier()) {
            SupplierProfile profile = userDao.getSupplierProfile(user.getId());
            if (profile != null) {
                supplierId = profile.getSupplierId();
            }
        }

        SessionManager session = new SessionManager(this);
        session.createSession(user, rememberMe, supplierId);

        RoleManager rm = new RoleManager(this);
        rm.setRole(user.getRole());

        Intent intent = new Intent(this, DashboardActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
