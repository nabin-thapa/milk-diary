package com.milkdiary.app.update;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.preference.PreferenceManager;

import com.milkdiary.app.R;

import java.io.File;

public class DownloadProgressDialog {

    private AlertDialog dialog;
    private ApkDownloader downloader;
    private final Activity activity;
    private final UpdateInfo updateInfo;
    private File apkFile;

    private TextView tvTitle;
    private TextView tvSubtitle;
    private TextView tvCurrentVersion;
    private TextView tvNewVersion;
    private ProgressBar progressBar;
    private TextView tvProgressPercent;
    private TextView tvProgressStatus;
    private View progressSection;
    private View errorSection;
    private TextView tvErrorMessage;
    private Button btnCancel;
    private Button btnRetry;
    private Button btnInstallNow;

    public DownloadProgressDialog(Activity activity, UpdateInfo updateInfo) {
        this.activity = activity;
        this.updateInfo = updateInfo;
        this.downloader = new ApkDownloader();
    }

    public void show() {
        if (activity.isFinishing() || activity.isDestroyed()) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        View dialogView = LayoutInflater.from(activity).inflate(R.layout.dialog_download_progress, null);
        builder.setView(dialogView);

        dialog = builder.create();
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        bindViews(dialogView);
        populateVersionInfo();
        setupButtons();

        dialog.show();
        startDownload();
    }

    private void bindViews(View dialogView) {
        tvTitle = dialogView.findViewById(R.id.tvDownloadTitle);
        tvSubtitle = dialogView.findViewById(R.id.tvDownloadSubtitle);
        tvCurrentVersion = dialogView.findViewById(R.id.tvDialogCurrentVersion);
        tvNewVersion = dialogView.findViewById(R.id.tvDialogNewVersion);
        progressBar = dialogView.findViewById(R.id.progressBar);
        tvProgressPercent = dialogView.findViewById(R.id.tvProgressPercent);
        tvProgressStatus = dialogView.findViewById(R.id.tvProgressStatus);
        progressSection = dialogView.findViewById(R.id.progressSection);
        errorSection = dialogView.findViewById(R.id.errorSection);
        tvErrorMessage = dialogView.findViewById(R.id.tvErrorMessage);
        btnCancel = dialogView.findViewById(R.id.btnCancel);
        btnRetry = dialogView.findViewById(R.id.btnRetry);
        btnInstallNow = dialogView.findViewById(R.id.btnInstallNow);
    }

    private void populateVersionInfo() {
        try {
            PackageInfo pi = activity.getPackageManager().getPackageInfo(activity.getPackageName(), 0);
            tvCurrentVersion.setText(pi.versionName + " (" + pi.versionCode + ")");
        } catch (Exception e) {
            tvCurrentVersion.setText("Unknown");
        }
        tvNewVersion.setText(updateInfo.getLatestVersion() + " (" + updateInfo.getVersionCode() + ")");
    }

    private void setupButtons() {
        btnCancel.setOnClickListener(v -> {
            downloader.cancel();
            dismiss();
        });

        btnRetry.setOnClickListener(v -> {
            resetUI();
            startDownload();
        });

        btnInstallNow.setOnClickListener(v -> {
            dismiss();
            UpdateInstaller.installApk(activity, apkFile);
        });
    }

    private void startDownload() {
        apkFile = new File(activity.getCacheDir(), "milk_diary_update.apk");

        downloader.download(updateInfo.getApkUrl(), apkFile, new ApkDownloader.DownloadCallback() {
            @Override
            public void onProgress(int percent) {
                if (percent >= 0) {
                    progressBar.setProgress(percent);
                    tvProgressPercent.setText(percent + "%");
                    tvProgressStatus.setText("Downloading update...");
                }
            }

            @Override
            public void onComplete(File file) {
                showComplete(file);
            }

            @Override
            public void onError(String message) {
                showError(message);
            }

            @Override
            public void onCancelled() {
                if (!activity.isFinishing() && !activity.isDestroyed()) {
                    dismiss();
                }
            }
        });
    }

    private void showComplete(File file) {
        this.apkFile = file;
        tvTitle.setText("Update Ready");
        tvSubtitle.setText("Download complete. Tap Install to update.");
        tvProgressPercent.setText("100%");
        tvProgressStatus.setText("Download complete");
        progressBar.setProgress(100);

        progressSection.setVisibility(View.VISIBLE);
        errorSection.setVisibility(View.GONE);
        btnCancel.setVisibility(View.GONE);
        btnRetry.setVisibility(View.GONE);
        btnInstallNow.setVisibility(View.VISIBLE);
    }

    private void showError(String message) {
        tvTitle.setText("Download Failed");
        tvSubtitle.setText("Something went wrong during the download.");
        progressSection.setVisibility(View.GONE);
        errorSection.setVisibility(View.VISIBLE);
        tvErrorMessage.setText(message);
        btnCancel.setVisibility(View.GONE);
        btnRetry.setVisibility(View.VISIBLE);
        btnInstallNow.setVisibility(View.GONE);
    }

    private void resetUI() {
        tvTitle.setText("Downloading Update...");
        tvSubtitle.setText("Please wait while the update downloads.");
        progressBar.setProgress(0);
        tvProgressPercent.setText("0%");
        tvProgressStatus.setText("Preparing download...");
        progressSection.setVisibility(View.VISIBLE);
        errorSection.setVisibility(View.GONE);
        btnCancel.setVisibility(View.VISIBLE);
        btnRetry.setVisibility(View.GONE);
        btnInstallNow.setVisibility(View.GONE);
    }

    public void dismiss() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    public boolean isShowing() {
        return dialog != null && dialog.isShowing();
    }
}
