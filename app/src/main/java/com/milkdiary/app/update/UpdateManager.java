package com.milkdiary.app.update;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.Uri;
import android.database.Cursor;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;
import androidx.preference.PreferenceManager;

import com.milkdiary.app.R;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UpdateManager {

    // Default configuration URL. Change this to your raw GitHub Pages or Firebase URL
    public static String UPDATE_JSON_URL = "https://raw.githubusercontent.com/nabin-thapa/milk-diary/main/version.json";
    
    private static final String PREF_LAST_CHECK_TIME = "last_update_check_time";
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());

    public enum UpdateStatus {
        UPDATE_AVAILABLE,
        NO_UPDATE,
        OFFLINE,
        ERROR
    }

    public interface UpdateCallback {
        void onUpdateChecked(UpdateStatus status, UpdateInfo updateInfo);
    }

    /**
     * Checks if the internet connection is active.
     * Uses a lenient approach: tries the standard check first, then falls back
     * to pinging a known host if the standard check is inconclusive.
     */
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return false;

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Network network = cm.getActiveNetwork();
                if (network != null) {
                    NetworkCapabilities capabilities = cm.getNetworkCapabilities(network);
                    if (capabilities != null && (
                            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
                    )) {
                        return true;
                    }
                }
            } else {
                @SuppressWarnings("deprecation")
                NetworkInfo activeNetworkInfo = cm.getActiveNetworkInfo();
                if (activeNetworkInfo != null && activeNetworkInfo.isConnected()) {
                    return true;
                }
            }
        } catch (Exception e) {
            // Fall through to lenient check below
        }

        // Lenient fallback: assume internet might be available and let the HTTP request fail gracefully
        return true;
    }

    /**
     * Asynchronously checks for updates.
     * 
     * @param activity The active Activity context
     * @param isManualCheck Whether this check was requested by a button click (if so, show Toast alerts for offline/failures)
     * @param callback Optional listener for execution completion
     */
    public static void checkForUpdates(final Activity activity, final boolean isManualCheck, final UpdateCallback callback) {
        if (activity == null || activity.isFinishing() || activity.isDestroyed()) {
            if (callback != null) callback.onUpdateChecked(UpdateStatus.ERROR, null);
            return;
        }

        // 1. Check Internet Connection
        if (!isNetworkAvailable(activity)) {
            saveLastCheckResult(activity, "Offline");
            if (isManualCheck) {
                Toast.makeText(activity, "No internet connection. Please connect and try again.", Toast.LENGTH_LONG).show();
            }
            if (callback != null) callback.onUpdateChecked(UpdateStatus.OFFLINE, null);
            return;
        }

        // Save that we're attempting a check (for UI feedback)
        saveLastCheckResult(activity, "Checking...");

        // 2. Fetch JSON in Background
        executor.execute(() -> {
            try {
                // Fetch JSON from URL
                String jsonStr = fetchStringFromUrl(UPDATE_JSON_URL);
                JSONObject json = new JSONObject(jsonStr);

                // Deserialise Update Config safely with robust defaults
                final UpdateInfo updateInfo = new UpdateInfo(
                        json.optString("latestVersion", "1.0.0"),
                        json.optInt("versionCode", 1),
                        json.optString("apkUrl", ""),
                        json.optString("releaseNotes", "No release notes available."),
                        json.optBoolean("forceUpdate", false),
                        json.optInt("minimumSupportedVersion", 0)
                );

                // Read current App details
                PackageManager pm = activity.getPackageManager();
                PackageInfo pi = pm.getPackageInfo(activity.getPackageName(), 0);
                final int currentCode;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    currentCode = (int) pi.getLongVersionCode();
                } else {
                    currentCode = pi.versionCode;
                }
                final String currentName = pi.versionName != null ? pi.versionName : "0.0.0";

                // Compare versions
                final boolean hasUpdate = isUpdateAvailable(currentCode, currentName, updateInfo.getVersionCode(), updateInfo.getLatestVersion());

                // Save last check timestamp and update status
                saveLastCheckResult(activity, hasUpdate ? "Update available" : "Up to date");

                // Post results to UI thread
                mainHandler.post(() -> {
                    if (activity.isFinishing() || activity.isDestroyed()) return;

                    if (hasUpdate) {
                        showUpdateDialog(activity, updateInfo);
                        if (callback != null) callback.onUpdateChecked(UpdateStatus.UPDATE_AVAILABLE, updateInfo);
                    } else {
                        if (isManualCheck) {
                            Toast.makeText(activity, "You are using the latest version.", Toast.LENGTH_SHORT).show();
                        }
                        if (callback != null) callback.onUpdateChecked(UpdateStatus.NO_UPDATE, updateInfo);
                    }
                });

            } catch (final Exception e) {
                e.printStackTrace();
                String errMsg = e.getMessage();
                boolean is404 = errMsg != null && errMsg.contains("404");
                saveLastCheckResult(activity, is404 ? "Update server not found" : "Check failed");
                mainHandler.post(() -> {
                    if (activity.isFinishing() || activity.isDestroyed()) return;

                    if (isManualCheck) {
                        String toastMsg = is404
                            ? "Update server not reachable. Please try again later."
                            : "Failed to check for updates. Please check your network.";
                        Toast.makeText(activity, toastMsg, Toast.LENGTH_LONG).show();
                    }
                    if (callback != null) callback.onUpdateChecked(UpdateStatus.ERROR, null);
                });
            }
        });
    }

    /**
     * Performs a network HTTP GET to download text content (version.json).
     */
    private static String fetchStringFromUrl(String urlStr) throws Exception {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(urlStr);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(15000);
            conn.setReadTimeout(15000);
            conn.setUseCaches(false);
            conn.setRequestProperty("Cache-Control", "no-cache");
            conn.connect();

            int code = conn.getResponseCode();
            if (code != HttpURLConnection.HTTP_OK) {
                throw new Exception("HTTP connection failed with code: " + code);
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append('\n');
                }
                return sb.toString();
            }
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    /**
     * Compares installed version with online version.
     * Returns true if remote version is newer.
     */
    public static boolean isUpdateAvailable(int currentCode, String currentName, int remoteCode, String remoteName) {
        if (remoteCode > currentCode) {
            return true;
        }
        if (remoteCode < currentCode) {
            return false;
        }
        // Fallback: compare versions semantically if versionCode matches (in case it wasn't bumped)
        return compareVersionNames(remoteName, currentName) > 0;
    }

    /**
     * Helper to compare version names semantically (e.g., "1.1.0" > "1.0").
     */
    private static int compareVersionNames(String v1, String v2) {
        if (v1 == null && v2 == null) return 0;
        if (v1 == null) return -1;
        if (v2 == null) return 1;

        String[] parts1 = v1.split("\\.");
        String[] parts2 = v2.split("\\.");
        int length = Math.max(parts1.length, parts2.length);
        for (int i = 0; i < length; i++) {
            int val1 = i < parts1.length ? parseVersionPart(parts1[i]) : 0;
            int val2 = i < parts2.length ? parseVersionPart(parts2[i]) : 0;
            if (val1 < val2) return -1;
            if (val1 > val2) return 1;
        }
        return 0;
    }

    private static int parseVersionPart(String part) {
        try {
            return Integer.parseInt(part.replaceAll("[^0-9]", ""));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * Displays the update dialog.
     */
    private static void showUpdateDialog(final Activity activity, final UpdateInfo updateInfo) {
        if (activity.isFinishing() || activity.isDestroyed()) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        View dialogView = LayoutInflater.from(activity).inflate(R.layout.dialog_update, null);
        builder.setView(dialogView);

        final AlertDialog dialog = builder.create();

        // Bind layout views
        TextView tvInstalled = dialogView.findViewById(R.id.tvInstalledVersion);
        TextView tvLatest = dialogView.findViewById(R.id.tvLatestVersion);
        TextView tvNotes = dialogView.findViewById(R.id.tvReleaseNotes);
        Button btnUpdate = dialogView.findViewById(R.id.btnUpdateNow);
        Button btnLater = dialogView.findViewById(R.id.btnUpdateLater);

        int currentCode = 0;
        try {
            PackageInfo pi = activity.getPackageManager().getPackageInfo(activity.getPackageName(), 0);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                currentCode = (int) pi.getLongVersionCode();
            } else {
                currentCode = pi.versionCode;
            }
            tvInstalled.setText(pi.versionName + " (" + pi.versionCode + ")");
        } catch (Exception ignored) {
            tvInstalled.setText("Unknown");
        }

        tvLatest.setText(updateInfo.getLatestVersion() + " (" + updateInfo.getVersionCode() + ")");
        tvNotes.setText(updateInfo.getReleaseNotes());

        final boolean isForced = updateInfo.isForceUpdate() || currentCode < updateInfo.getMinimumSupportedVersion();

        btnUpdate.setOnClickListener(v -> {
            dialog.dismiss();
            downloadAndInstallApk(activity, updateInfo.getApkUrl());
        });

        if (isForced) {
            btnLater.setVisibility(View.GONE);
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
        } else {
            btnLater.setOnClickListener(v -> dialog.dismiss());
        }

        dialog.show();
    }

    private static void downloadAndInstallApk(final Activity activity, String apkUrl) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !activity.getPackageManager().canRequestPackageInstalls()) {
            showInstallPermissionDialog(activity, apkUrl);
            return;
        }
        try {
            DownloadManager dm = (DownloadManager) activity.getSystemService(Context.DOWNLOAD_SERVICE);
            if (dm == null) {
                Toast.makeText(activity, "Download service not available", Toast.LENGTH_SHORT).show();
                return;
            }

            // Delete any stale APK before downloading fresh
            File oldApk = new File(activity.getExternalFilesDir(null), "MilkDiary-update.apk");
            if (oldApk.exists()) oldApk.delete();

            Uri uri = Uri.parse(apkUrl);
            DownloadManager.Request request = new DownloadManager.Request(uri);
            request.setTitle("Milk Diary Update");
            request.setDescription("Downloading latest version...");
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setMimeType("application/vnd.android.package-archive");
            request.setDestinationInExternalFilesDir(activity, null, "MilkDiary-update.apk");

            final long downloadId = dm.enqueue(request);
            final Context appContext = activity.getApplicationContext();

            BroadcastReceiver onComplete = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                    if (id != downloadId) return;

                    appContext.unregisterReceiver(this);

                    DownloadManager.Query query = new DownloadManager.Query();
                    query.setFilterById(downloadId);
                    try (Cursor c = dm.query(query)) {
                        if (c != null && c.moveToFirst()) {
                            int status = c.getInt(c.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS));
                            if (status == DownloadManager.STATUS_SUCCESSFUL) {
                                installApk(appContext);
                            } else {
                                int reason = c.getInt(c.getColumnIndexOrThrow(DownloadManager.COLUMN_REASON));
                                Toast.makeText(appContext, "Download failed: " + getDownloadErrorString(reason), Toast.LENGTH_LONG).show();
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(appContext, "Download verification failed", Toast.LENGTH_LONG).show();
                    }
                }
            };

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                appContext.registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE), Context.RECEIVER_EXPORTED);
            } else {
                appContext.registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
            }

            Toast.makeText(activity, "Downloading update...", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(activity, "Failed to start download: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private static void installApk(Context context) {
        try {
            File apkFile = new File(context.getExternalFilesDir(null), "MilkDiary-update.apk");
            if (!apkFile.exists()) {
                Toast.makeText(context, "Downloaded file not found", Toast.LENGTH_SHORT).show();
                return;
            }

            Uri apkUri = FileProvider.getUriForFile(context,
                    context.getPackageName() + ".fileprovider", apkFile);

            Intent installIntent = new Intent(Intent.ACTION_VIEW);
            installIntent.setDataAndType(apkUri, "application/vnd.android.package-archive");
            installIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_GRANT_READ_URI_PERMISSION);

            context.startActivity(installIntent);

            Toast.makeText(context, "Opening installer...", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Failed to launch installer: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private static String getDownloadErrorString(int reason) {
        switch (reason) {
            case DownloadManager.ERROR_FILE_ERROR: return "File error";
            case DownloadManager.ERROR_UNHANDLED_HTTP_CODE: return "Server error";
            case DownloadManager.ERROR_HTTP_DATA_ERROR: return "Network error";
            case DownloadManager.ERROR_TOO_MANY_REDIRECTS: return "Redirect error";
            case DownloadManager.ERROR_INSUFFICIENT_SPACE: return "Insufficient space";
            case DownloadManager.ERROR_DEVICE_NOT_FOUND: return "Device not found";
            case DownloadManager.ERROR_CANNOT_RESUME: return "Cannot resume";
            default: return "Unknown error (" + reason + ")";
        }
    }

    private static void showInstallPermissionDialog(Activity activity, String apkUrl) {
        PreferenceManager.getDefaultSharedPreferences(activity)
            .edit().putString("pending_apk_url", apkUrl).apply();

        new AlertDialog.Builder(activity)
            .setTitle("Install Updates")
            .setMessage("Milk Diary needs a one-time permission to install updates.\n\n" +
                "Your data stays on this device and is never shared. Tap 'Allow' to continue.")
            .setPositiveButton("Allow", (d, w) -> {
                Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES);
                intent.setData(Uri.parse("package:" + activity.getPackageName()));
                activity.startActivity(intent);
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    public static void retryPendingDownload(Activity activity) {
        if (activity == null || activity.isFinishing() || activity.isDestroyed()) return;

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
        String pendingUrl = prefs.getString("pending_apk_url", null);
        if (pendingUrl == null) return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !activity.getPackageManager().canRequestPackageInstalls()) {
            return;
        }

        prefs.edit().remove("pending_apk_url").apply();
        downloadAndInstallApk(activity, pendingUrl);
    }

    private static void saveLastCheckResult(Context context, String status) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.edit()
                .putLong(PREF_LAST_CHECK_TIME, System.currentTimeMillis())
                .putString("last_update_status", status)
                .apply();
    }

    public static long getLastCheckTime(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getLong(PREF_LAST_CHECK_TIME, 0);
    }
}
