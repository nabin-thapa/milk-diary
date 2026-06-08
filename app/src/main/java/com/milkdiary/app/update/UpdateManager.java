package com.milkdiary.app.update;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.preference.PreferenceManager;

import com.milkdiary.app.R;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UpdateManager {

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

        return true;
    }

    public static void checkForUpdates(final Activity activity, final boolean isManualCheck, final UpdateCallback callback) {
        if (activity == null || activity.isFinishing() || activity.isDestroyed()) {
            if (callback != null) callback.onUpdateChecked(UpdateStatus.ERROR, null);
            return;
        }

        if (!isNetworkAvailable(activity)) {
            saveLastCheckResult(activity, "Offline");
            if (isManualCheck) {
                Toast.makeText(activity, "No internet connection. Please connect and try again.", Toast.LENGTH_LONG).show();
            }
            if (callback != null) callback.onUpdateChecked(UpdateStatus.OFFLINE, null);
            return;
        }

        saveLastCheckResult(activity, "Checking...");

        executor.execute(() -> {
            try {
                String cacheBuster = "?t=" + System.currentTimeMillis();
                String jsonStr = fetchStringFromUrl(UPDATE_JSON_URL + cacheBuster);
                JSONObject json = new JSONObject(jsonStr);

                final UpdateInfo updateInfo = new UpdateInfo(
                        json.optString("latestVersion", "1.0.0"),
                        json.optInt("versionCode", 1),
                        json.optString("apkUrl", ""),
                        json.optString("releaseNotes", "No release notes available."),
                        json.optBoolean("forceUpdate", false),
                        json.optInt("minimumSupportedVersion", 0)
                );

                PackageManager pm = activity.getPackageManager();
                PackageInfo pi = pm.getPackageInfo(activity.getPackageName(), 0);
                final int currentCode;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    currentCode = (int) pi.getLongVersionCode();
                } else {
                    currentCode = pi.versionCode;
                }
                final String currentName = pi.versionName != null ? pi.versionName : "0.0.0";

                final boolean hasUpdate = isUpdateAvailable(currentCode, currentName, updateInfo.getVersionCode(), updateInfo.getLatestVersion());

                saveLastCheckResult(activity, hasUpdate ? "Update available" : "Up to date");

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

    public static boolean isUpdateAvailable(int currentCode, String currentName, int remoteCode, String remoteName) {
        if (remoteCode > currentCode) {
            return true;
        }
        if (remoteCode < currentCode) {
            return false;
        }
        return compareVersionNames(remoteName, currentName) > 0;
    }

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

    private static void showUpdateDialog(final Activity activity, final UpdateInfo updateInfo) {
        if (activity.isFinishing() || activity.isDestroyed()) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        View dialogView = LayoutInflater.from(activity).inflate(R.layout.dialog_update, null);
        builder.setView(dialogView);

        final AlertDialog dialog = builder.create();

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
            showDownloadProgress(activity, updateInfo);
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

    private static void showDownloadProgress(Activity activity, UpdateInfo updateInfo) {
        if (activity.isFinishing() || activity.isDestroyed()) return;
        DownloadProgressDialog progressDialog = new DownloadProgressDialog(activity, updateInfo);
        progressDialog.show();
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

    public static String getDownloadedVersionDisplay(Context context) {
        if (!UpdateInstaller.hasDownloadedApk(context)) {
            return "None";
        }
        try {
            PackageInfo pi = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return pi.versionName + " (cached)";
        } catch (Exception e) {
            return "Downloaded";
        }
    }
}
