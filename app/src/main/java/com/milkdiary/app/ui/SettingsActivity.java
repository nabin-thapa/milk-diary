package com.milkdiary.app.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.FileProvider;
import androidx.preference.PreferenceManager;

import com.milkdiary.app.R;
import com.milkdiary.app.databinding.ActivitySettingsBinding;
import com.milkdiary.app.db.MilkRecordDao;
import com.milkdiary.app.db.PaymentDao;
import com.milkdiary.app.util.BackupUtils;
import com.milkdiary.app.util.ExportUtils;
import com.milkdiary.app.util.FormatUtils;
import com.milkdiary.app.util.RoleManager;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

public class SettingsActivity extends AppCompatActivity {

    private ActivitySettingsBinding binding;
    private SharedPreferences prefs;
    private RoleManager roleManager;
    private static final int PICK_BACKUP_FILE = 101;
    private static final int REQUEST_PICK_IMAGE = 202;
    private boolean ratesUpdating = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Settings");
        }

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        roleManager = new RoleManager(this);

        // Role section
        setupRoleSection();

        // Dark mode
        binding.switchDarkMode.setChecked(prefs.getBoolean("dark_mode", false));
        binding.switchDarkMode.setOnCheckedChangeListener((btn, checked) -> {
            prefs.edit().putBoolean("dark_mode", checked).apply();
            AppCompatDelegate.setDefaultNightMode(
                    checked ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
        });

        // Default rates
        loadDefaultRates();
        setupDefaultRateWatchers();

        // Data buttons
        binding.btnBackup.setOnClickListener(v -> doBackup());
        binding.btnRestore.setOnClickListener(v -> doRestore());
        binding.btnExportCsv.setOnClickListener(v -> doExportCsv());

        // Update Section
        initUpdateSection();

        // Profile section
        loadProfile();
        setupProfile();
    }

    @Override
    protected void onResume() {
        super.onResume();
        com.milkdiary.app.update.UpdateInstaller.checkAndResumePendingInstall(this);
        updateDownloadedVersionDisplay();
    }

    private void setupRoleSection() {
        binding.tvRoleValue.setText(roleManager.getRoleDisplayName());
        binding.btnSwitchRole.setOnClickListener(v -> {
            if (!roleManager.isDairy()) {
                Toast.makeText(this, "Only the Dairy owner can change role.", Toast.LENGTH_SHORT).show();
                return;
            }
            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Switch Role")
                    .setItems(new String[]{"Dairy", "Milk Supplier"}, (dialog, which) -> {
                        String newRole = which == 0 ? RoleManager.ROLE_DAIRY : RoleManager.ROLE_SUPPLIER;
                        roleManager.setRole(newRole);
                        binding.tvRoleValue.setText(roleManager.getRoleDisplayName());
                        Toast.makeText(this,
                                "Role changed to " + roleManager.getRoleDisplayName(),
                                Toast.LENGTH_SHORT).show();
                    })
                    .show();
        });
    }

    private void loadDefaultRates() {
        float cowRate = prefs.getFloat(AddRecordActivity.PREF_COW_RATE, 0f);
        float bufRate = prefs.getFloat(AddRecordActivity.PREF_BUF_RATE, 0f);
        ratesUpdating = true;
        if (cowRate > 0) binding.etDefaultCowRate.setText(FormatUtils.rateValue(cowRate));
        if (bufRate > 0) binding.etDefaultBufRate.setText(FormatUtils.rateValue(bufRate));
        ratesUpdating = false;
    }

    private void setupDefaultRateWatchers() {
        binding.etDefaultCowRate.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) {}
            @Override public void afterTextChanged(Editable s) {
                if (ratesUpdating) return;
                try {
                    float rate = Float.parseFloat(s.toString().replace(',', '.'));
                    if (rate > 0) prefs.edit().putFloat(AddRecordActivity.PREF_COW_RATE, rate).apply();
                } catch (NumberFormatException ignored) {}
            }
        });

        binding.etDefaultBufRate.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) {}
            @Override public void afterTextChanged(Editable s) {
                if (ratesUpdating) return;
                try {
                    float rate = Float.parseFloat(s.toString().replace(',', '.'));
                    if (rate > 0) prefs.edit().putFloat(AddRecordActivity.PREF_BUF_RATE, rate).apply();
                } catch (NumberFormatException ignored) {}
            }
        });
    }

    private void doBackup() {
        try {
            File f = BackupUtils.backup(this);
            Toast.makeText(this, "Backup saved:\n" + f.getName(), Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this, "Backup failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void doRestore() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(
                Intent.createChooser(intent, "Select Backup (.db) File"), PICK_BACKUP_FILE);
    }

    private void doExportCsv() {
        try {
            MilkRecordDao recordDao = new MilkRecordDao(this);
            PaymentDao paymentDao   = new PaymentDao(this);

            File recordsFile  = ExportUtils.exportRecordsCsv(this, recordDao.getAllRecordsAsc());
            File paymentsFile = ExportUtils.exportPaymentsCsv(this, paymentDao.getAllPayments());

            List<Uri> uris = new ArrayList<>();
            uris.add(FileProvider.getUriForFile(this,
                    getPackageName() + ".fileprovider", recordsFile));
            uris.add(FileProvider.getUriForFile(this,
                    getPackageName() + ".fileprovider", paymentsFile));

            Intent share = new Intent(Intent.ACTION_SEND_MULTIPLE);
            share.setType("text/csv");
            share.putParcelableArrayListExtra(Intent.EXTRA_STREAM, new ArrayList<>(uris));
            share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(share, "Export CSV files"));

        } catch (Exception e) {
            Toast.makeText(this, "Export failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_BACKUP_FILE && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri == null) return;
            try {
                File tempFile = new File(getCacheDir(), "restore_temp.db");
                try (java.io.InputStream in  = getContentResolver().openInputStream(uri);
                     java.io.FileOutputStream out = new java.io.FileOutputStream(tempFile)) {
                    if (in == null) throw new Exception("Cannot read selected file");
                    byte[] buf = new byte[8192];
                    int len;
                    while ((len = in.read(buf)) > 0) out.write(buf, 0, len);
                }
                BackupUtils.restore(this, tempFile);
                Toast.makeText(this,
                        "Restore successful! Please restart the app.",
                        Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                Toast.makeText(this, "Restore failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        } else if (requestCode == REQUEST_PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            handleProfileImageResult(data);
        }
    }

    private void loadProfile() {
        String name = prefs.getString("profile_name", "");
        if (!name.isEmpty()) {
            binding.etProfileName.setText(name);
        }
        loadProfileImage();
    }

    private void loadProfileImage() {
        File imgFile = new File(getFilesDir(), "profile_pic.jpg");
        if (imgFile.exists()) {
            Bitmap bm = BitmapFactory.decodeFile(imgFile.getPath());
            if (bm != null) {
                binding.ivProfile.setImageBitmap(bm);
            }
        }
    }

    private void setupProfile() {
        binding.etProfileName.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                saveProfileName();
                return true;
            }
            return false;
        });

        binding.etProfileName.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) {}
            @Override public void afterTextChanged(Editable s) {
                saveProfileName();
            }
        });

        binding.btnChangePhoto.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, REQUEST_PICK_IMAGE);
        });
    }

    private void saveProfileName() {
        String name = binding.etProfileName.getText().toString().trim();
        prefs.edit().putString("profile_name", name).apply();
    }

    private void handleProfileImageResult(Intent data) {
        try {
            Uri sourceUri = data.getData();
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), sourceUri);
            Bitmap resized = Bitmap.createScaledBitmap(bitmap, 200, 200, true);
            File outFile = new File(getFilesDir(), "profile_pic.jpg");
            try (FileOutputStream fos = new FileOutputStream(outFile)) {
                resized.compress(Bitmap.CompressFormat.JPEG, 80, fos);
            }
            binding.ivProfile.setImageBitmap(resized);
            bitmap.recycle();
            resized.recycle();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { finish(); return true; }
        return super.onOptionsItemSelected(item);
    }

    private void initUpdateSection() {
        try {
            android.content.pm.PackageInfo pi = getPackageManager().getPackageInfo(getPackageName(), 0);
            binding.tvCurrentVersion.setText(pi.versionName + " (" + pi.versionCode + ")");
        } catch (Exception e) {
            binding.tvCurrentVersion.setText("Unknown");
        }

        updateLastCheckedDisplay();
        updateDownloadedVersionDisplay();

        // Set dynamic footer version
        String footerText = "Milk Diary v" + binding.tvCurrentVersion.getText()
                + "  •  Fully Offline  •  Data stored on this device";
        binding.tvFooterVersion.setText(footerText);

        binding.btnCheckUpdates.setOnClickListener(v -> {
            binding.btnCheckUpdates.setEnabled(false);
            binding.btnCheckUpdates.setText("Checking...");
            com.milkdiary.app.update.UpdateManager.checkForUpdates(this, true, (status, updateInfo) -> {
                binding.btnCheckUpdates.setEnabled(true);
                binding.btnCheckUpdates.setText("🔄  Check for Updates");
                updateLastCheckedDisplay();
            });
        });
    }

    private void updateDownloadedVersionDisplay() {
        String display = com.milkdiary.app.update.UpdateManager.getDownloadedVersionDisplay(this);
        binding.tvDownloadedVersion.setText(display);
        if (!"None".equals(display)) {
            binding.tvDownloadedVersion.setTextColor(getResources().getColor(R.color.primary, getTheme()));
        } else {
            binding.tvDownloadedVersion.setTextColor(getResources().getColor(R.color.text_secondary, getTheme()));
        }
    }

    private void updateLastCheckedDisplay() {
        long lastCheck = com.milkdiary.app.update.UpdateManager.getLastCheckTime(this);
        if (lastCheck == 0) {
            binding.tvLastUpdateCheck.setText("Never");
            binding.tvUpdateStatus.setText("Never checked");
            binding.tvUpdateStatus.setTextColor(getResources().getColor(R.color.text_secondary, getTheme()));
        } else {
            long now = System.currentTimeMillis();
            long diffMs = now - lastCheck;
            long seconds = diffMs / 1000;
            String ago;
            if (seconds < 60) {
                ago = "Just now";
            } else if (seconds < 3600) {
                ago = (seconds / 60) + " minutes ago";
            } else if (seconds < 86400) {
                long hours = seconds / 3600;
                ago = hours + " hour" + (hours > 1 ? "s" : "") + " ago";
            } else {
                long days = seconds / 86400;
                ago = days + " day" + (days > 1 ? "s" : "") + " ago";
            }
            binding.tvLastUpdateCheck.setText(ago);

            String status = prefs.getString("last_update_status", "Up to date");
            binding.tvUpdateStatus.setText(status);
            if ("Update available".equalsIgnoreCase(status)) {
                binding.tvUpdateStatus.setTextColor(getResources().getColor(R.color.pending_color, getTheme()));
            } else if ("Offline".equalsIgnoreCase(status) || "Check failed".equalsIgnoreCase(status)) {
                binding.tvUpdateStatus.setTextColor(getResources().getColor(R.color.text_secondary, getTheme()));
            } else {
                binding.tvUpdateStatus.setTextColor(getResources().getColor(R.color.earnings_color, getTheme()));
            }
        }
    }
}
