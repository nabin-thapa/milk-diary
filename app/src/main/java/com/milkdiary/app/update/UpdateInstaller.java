package com.milkdiary.app.update;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;
import androidx.preference.PreferenceManager;

import java.io.File;

public class UpdateInstaller {

    private static final String PENDING_INSTALL_PATH = "pending_install_apk_path";
    private static final String PREF_INSTALLED_VERSION_CODE = "installed_version_code_after_update";
    private static final String APK_DIR_NAME = "updates";

    public static void installApk(Context context, File apkFile) {
        if (apkFile == null || !apkFile.exists()) {
            Toast.makeText(context, "APK file not found", Toast.LENGTH_SHORT).show();
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !context.getPackageManager().canRequestPackageInstalls()) {
            showInstallPermissionDialog(context, apkFile);
            return;
        }

        launchInstaller(context, apkFile);
    }

    private static void launchInstaller(Context context, File apkFile) {
        try {
            Uri apkUri = FileProvider.getUriForFile(context,
                    context.getPackageName() + ".fileprovider", apkFile);

            Intent installIntent = new Intent(Intent.ACTION_VIEW);
            installIntent.setDataAndType(apkUri, "application/vnd.android.package-archive");
            installIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_GRANT_READ_URI_PERMISSION);

            context.startActivity(installIntent);

            Toast.makeText(context, "Opening installer...", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(context, "Failed to open installer: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private static void showInstallPermissionDialog(Context context, File apkFile) {
        savePendingInstallPath(context, apkFile.getAbsolutePath());

        new AlertDialog.Builder(context)
            .setTitle("Install Updates")
            .setMessage("Milk Diary needs a one-time permission to install updates.\n\n" +
                "Your data stays on this device and is never shared. Tap 'Allow' to continue.")
            .setPositiveButton("Allow", (d, w) -> {
                Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES);
                intent.setData(Uri.parse("package:" + context.getPackageName()));
                if (context instanceof Activity) {
                    ((Activity) context).startActivity(intent);
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    public static void checkAndResumePendingInstall(Activity activity) {
        if (activity == null || activity.isFinishing() || activity.isDestroyed()) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !activity.getPackageManager().canRequestPackageInstalls()) return;

        String pendingPath = getPendingInstallPath(activity);
        if (pendingPath == null) return;

        clearPendingInstallPath(activity);
        File apkFile = new File(pendingPath);
        if (apkFile.exists()) {
            installApk(activity, apkFile);
        }
    }

    public static String getVersionedApkFileName(String versionName) {
        String safe = versionName.replaceAll("[^a-zA-Z0-9._-]", "_");
        return "MilkDiary-v" + safe + ".apk";
    }

    public static File getVersionedApkFile(Context context, String versionName) {
        File dir = new File(context.getCacheDir(), APK_DIR_NAME);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return new File(dir, getVersionedApkFileName(versionName));
    }

    public static void clearOldApks(Context context) {
        File dir = new File(context.getCacheDir(), APK_DIR_NAME);
        if (dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File f : files) {
                    f.delete();
                }
            }
        }
        File legacy = new File(context.getCacheDir(), "milk_diary_update.apk");
        if (legacy.exists()) {
            legacy.delete();
        }
    }

    public static void cleanupAfterInstall(Context context) {
        clearOldApks(context);
        clearPendingInstallPath(context);
    }

    public static boolean hasDownloadedApk(Context context) {
        File dir = new File(context.getCacheDir(), APK_DIR_NAME);
        if (dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File f : files) {
                    if (f.exists() && f.length() > 0 && f.getName().endsWith(".apk")) {
                        return true;
                    }
                }
            }
        }
        File legacy = new File(context.getCacheDir(), "milk_diary_update.apk");
        return legacy.exists() && legacy.length() > 0;
    }

    public static File getDownloadedApkFile(Context context) {
        File dir = new File(context.getCacheDir(), APK_DIR_NAME);
        if (dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null) {
                File newest = null;
                for (File f : files) {
                    if (f.exists() && f.length() > 0 && f.getName().endsWith(".apk")) {
                        if (newest == null || f.lastModified() > newest.lastModified()) {
                            newest = f;
                        }
                    }
                }
                if (newest != null) return newest;
            }
        }
        File legacy = new File(context.getCacheDir(), "milk_diary_update.apk");
        if (legacy.exists() && legacy.length() > 0) {
            return legacy;
        }
        return null;
    }

    public static void saveExpectedVersionCode(Context context, int versionCode) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.edit().putInt(PREF_INSTALLED_VERSION_CODE, versionCode).apply();
    }

    public static boolean verifyPostUpdate(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        int expectedCode = prefs.getInt(PREF_INSTALLED_VERSION_CODE, 0);
        if (expectedCode == 0) return true;

        int currentCode = 0;
        try {
            PackageInfo pi = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                currentCode = (int) pi.getLongVersionCode();
            } else {
                currentCode = pi.versionCode;
            }
        } catch (Exception ignored) {}

        prefs.edit().remove(PREF_INSTALLED_VERSION_CODE).apply();

        return currentCode >= expectedCode;
    }

    public static String getCurrentVersionName(Context context) {
        try {
            PackageInfo pi = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return pi.versionName;
        } catch (Exception e) {
            return "unknown";
        }
    }

    public static int getCurrentVersionCode(Context context) {
        try {
            PackageInfo pi = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                return (int) pi.getLongVersionCode();
            } else {
                return pi.versionCode;
            }
        } catch (Exception e) {
            return 0;
        }
    }

    private static void savePendingInstallPath(Context context, String path) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.edit().putString(PENDING_INSTALL_PATH, path).apply();
    }

    private static String getPendingInstallPath(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(PENDING_INSTALL_PATH, null);
    }

    private static void clearPendingInstallPath(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.edit().remove(PENDING_INSTALL_PATH).apply();
    }
}
