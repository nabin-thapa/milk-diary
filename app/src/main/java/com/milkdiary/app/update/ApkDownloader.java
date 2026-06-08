package com.milkdiary.app.update;

import android.os.Handler;
import android.os.Looper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ApkDownloader {

    private static final int CONNECT_TIMEOUT = 30_000;
    private static final int READ_TIMEOUT = 60_000;
    private static final int BUFFER_SIZE = 8192;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private volatile boolean isCancelled = false;
    private volatile DownloadCallback pendingCallback;
    private HttpURLConnection connection;

    public interface DownloadCallback {
        void onProgress(int percent);
        void onComplete(File file);
        void onError(String message);
        void onCancelled();
    }

    public void download(String apkUrl, File destination, DownloadCallback callback) {
        isCancelled = false;
        pendingCallback = callback;

        executor.execute(() -> {
            FileOutputStream outputStream = null;
            InputStream inputStream = null;

            try {
                URL url = new URL(apkUrl);
                connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(CONNECT_TIMEOUT);
                connection.setReadTimeout(READ_TIMEOUT);
                connection.setRequestMethod("GET");
                connection.connect();

                int responseCode = connection.getResponseCode();
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    postError("Server error: HTTP " + responseCode);
                    return;
                }

                long totalBytes = connection.getContentLengthLong();
                if (totalBytes <= 0) {
                    totalBytes = connection.getContentLength();
                }

                inputStream = connection.getInputStream();
                outputStream = new FileOutputStream(destination);

                byte[] buffer = new byte[BUFFER_SIZE];
                long downloadedBytes = 0;
                int bytesRead;
                int lastReportedPercent = -1;

                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    if (isCancelled) {
                        closeQuietly(outputStream);
                        closeQuietly(inputStream);
                        destination.delete();
                        postCancelled();
                        return;
                    }

                    outputStream.write(buffer, 0, bytesRead);
                    downloadedBytes += bytesRead;

                    if (totalBytes > 0) {
                        int percent = (int) ((downloadedBytes * 100) / totalBytes);
                        if (percent != lastReportedPercent) {
                            lastReportedPercent = percent;
                            final int p = percent;
                            mainHandler.post(() -> {
                                if (pendingCallback != null) pendingCallback.onProgress(p);
                            });
                        }
                    }
                }

                outputStream.flush();
                closeQuietly(outputStream);
                closeQuietly(inputStream);

                if (isCancelled) {
                    destination.delete();
                    postCancelled();
                    return;
                }

                if (destination.exists() && destination.length() > 0) {
                    final File file = destination;
                    mainHandler.post(() -> {
                        if (pendingCallback != null) pendingCallback.onComplete(file);
                    });
                } else {
                    destination.delete();
                    postError("Downloaded file is empty");
                }

            } catch (Exception e) {
                closeQuietly(outputStream);
                closeQuietly(inputStream);
                if (!isCancelled) {
                    destination.delete();
                    String message = e.getMessage() != null ? e.getMessage() : "Unknown download error";
                    if (message.contains("timeout") || message.contains("SocketTimeout")) {
                        message = "Connection timed out. Check your internet connection.";
                    } else if (message.contains("UnknownHost")) {
                        message = "Cannot reach server. Check your internet connection.";
                    } else if (message.contains("ConnectException")) {
                        message = "Connection failed. Check your internet connection.";
                    }
                    postError(message);
                }
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        });
    }

    public void cancel() {
        isCancelled = true;
        if (connection != null) {
            try {
                connection.disconnect();
            } catch (Exception ignored) {}
        }
    }

    private void postError(String message) {
        mainHandler.post(() -> {
            if (pendingCallback != null) pendingCallback.onError(message);
        });
    }

    private void postCancelled() {
        mainHandler.post(() -> {
            if (pendingCallback != null) pendingCallback.onCancelled();
        });
    }

    private static void closeQuietly(java.io.Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Exception ignored) {}
        }
    }
}
