package com.milkdiary.app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.milkdiary.app.databinding.ActivitySignupBinding;
import com.milkdiary.app.db.UserDao;
import com.milkdiary.app.model.Supplier;
import com.milkdiary.app.model.SupplierProfile;
import com.milkdiary.app.model.User;
import com.milkdiary.app.util.PasswordUtils;
import com.milkdiary.app.util.RoleManager;
import com.milkdiary.app.util.SessionManager;

public class SignupActivity extends AppCompatActivity {

    private ActivitySignupBinding binding;
    private UserDao userDao;
    private String selectedRole = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Create Account");
        }

        userDao = new UserDao(this);

        setupRoleSelection();
        setupFormValidation();
        setupSignupButton();
        setupNavigationLinks();
    }

    private void setupRoleSelection() {
        binding.cardDairyOwner.setOnClickListener(v -> selectRole(User.ROLE_DAIRY));
        binding.cardSupplier.setOnClickListener(v -> selectRole(User.ROLE_SUPPLIER));

        binding.tvBackToRole.setOnClickListener(v -> {
            binding.roleSelectionSection.setVisibility(View.VISIBLE);
            binding.formSection.setVisibility(View.GONE);
            selectedRole = null;
        });
    }

    private void selectRole(String role) {
        selectedRole = role;
        binding.roleSelectionSection.setVisibility(View.GONE);
        binding.formSection.setVisibility(View.VISIBLE);

        if (User.ROLE_DAIRY.equals(role)) {
            binding.tvFormTitle.setText("Dairy Owner Details");
            binding.tvFormSubtitle.setText("Fill in your details to create an account.");
            binding.tilDairyName.setVisibility(View.VISIBLE);
            binding.tilAddress.setVisibility(View.VISIBLE);
            binding.tilVillage.setVisibility(View.GONE);
            binding.milkTypeSection.setVisibility(View.GONE);
        } else {
            binding.tvFormTitle.setText("Supplier Details");
            binding.tvFormSubtitle.setText("Fill in your details to create an account.");
            binding.tilDairyName.setVisibility(View.GONE);
            binding.tilAddress.setVisibility(View.GONE);
            binding.tilVillage.setVisibility(View.VISIBLE);
            binding.milkTypeSection.setVisibility(View.VISIBLE);
        }
    }

    private void setupFormValidation() {
        addClearErrorWatcher(binding.etFullName);
        addClearErrorWatcher(binding.etSignupPhone);
        addClearErrorWatcher(binding.etSignupPassword);
        addClearErrorWatcher(binding.etConfirmPassword);
    }

    private void addClearErrorWatcher(com.google.android.material.textfield.TextInputEditText et) {
        et.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) {}
            @Override public void afterTextChanged(Editable s) {
                et.setError(null);
            }
        });
    }

    private void setupSignupButton() {
        binding.btnSignup.setOnClickListener(v -> attemptSignup());
    }

    private void setupNavigationLinks() {
        binding.tvGoToLogin.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }

    private void attemptSignup() {
        String fullName = binding.etFullName.getText().toString().trim();
        String phone = binding.etSignupPhone.getText().toString().trim();
        String password = binding.etSignupPassword.getText().toString().trim();
        String confirmPassword = binding.etConfirmPassword.getText().toString().trim();

        if (fullName.isEmpty()) {
            binding.etFullName.setError("Name is required");
            binding.etFullName.requestFocus();
            return;
        }

        if (phone.isEmpty()) {
            binding.etSignupPhone.setError("Phone number is required");
            binding.etSignupPhone.requestFocus();
            return;
        }

        if (userDao.isPhoneExists(phone)) {
            binding.etSignupPhone.setError("Phone number already registered");
            binding.etSignupPhone.requestFocus();
            return;
        }

        if (password.length() < 6) {
            binding.etSignupPassword.setError("Password must be at least 6 characters");
            binding.etSignupPassword.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            binding.etConfirmPassword.setError("Passwords do not match");
            binding.etConfirmPassword.requestFocus();
            return;
        }

        String hashedPassword = PasswordUtils.hash(password);

        User user = new User(fullName, phone, hashedPassword, selectedRole);
        long userId = userDao.insertUser(user);
        user.setId(userId);

        if (User.ROLE_DAIRY.equals(selectedRole)) {
            String dairyName = binding.etDairyName.getText().toString().trim();
            String address = binding.etAddress.getText().toString().trim();
            userDao.createOwnerProfile(userId, dairyName, address);

        } else {
            String village = binding.etVillage.getText().toString().trim();
            String milkType = Supplier.MILK_BOTH;
            if (binding.chipCow.isChecked()) milkType = Supplier.MILK_COW;
            else if (binding.chipBuffalo.isChecked()) milkType = Supplier.MILK_BUFFALO;

            userDao.createSupplierAndLinkUser(fullName, phone, village, milkType, userId);
        }

        SupplierProfile profile = null;
        if (user.isSupplier()) {
            profile = userDao.getSupplierProfile(userId);
        }
        long supplierId = profile != null ? profile.getSupplierId() : 0;

        SessionManager session = new SessionManager(this);
        session.createSession(user, true, supplierId);

        RoleManager rm = new RoleManager(this);
        rm.setRole(user.getRole());

        Toast.makeText(this, "Account created successfully!", Toast.LENGTH_SHORT).show();

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
