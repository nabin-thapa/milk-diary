package com.milkdiary.app.update;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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

    public static void cleanupAfterInstall(Context context) {
        File apkFile = new File(context.getCacheDir(), "milk_diary_update.apk");
        if (apkFile.exists()) {
            apkFile.delete();
        }
        clearPendingInstallPath(context);
    }

    public static boolean hasDownloadedApk(Context context) {
        File apkFile = new File(context.getCacheDir(), "milk_diary_update.apk");
        return apkFile.exists() && apkFile.length() > 0;
    }

    public static File getDownloadedApkFile(Context context) {
        File apkFile = new File(context.getCacheDir(), "milk_diary_update.apk");
        if (apkFile.exists() && apkFile.length() > 0) {
            return apkFile;
        }
        return null;
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
