package com.milkdiary.app.update;

public class UpdateInfo {
    private final String latestVersion;
    private final int versionCode;
    private final String apkUrl;
    private final String releaseNotes;
    private final boolean forceUpdate;
    private final int minimumSupportedVersion;

    public UpdateInfo(String latestVersion, int versionCode, String apkUrl, String releaseNotes, boolean forceUpdate, int minimumSupportedVersion) {
        this.latestVersion = latestVersion;
        this.versionCode = versionCode;
        this.apkUrl = apkUrl;
        this.releaseNotes = releaseNotes;
        this.forceUpdate = forceUpdate;
        this.minimumSupportedVersion = minimumSupportedVersion;
    }

    public String getLatestVersion() {
        return latestVersion;
    }

    public int getVersionCode() {
        return versionCode;
    }

    public String getApkUrl() {
        return apkUrl;
    }

    public String getReleaseNotes() {
        return releaseNotes;
    }

    public boolean isForceUpdate() {
        return forceUpdate;
    }

    public int getMinimumSupportedVersion() {
        return minimumSupportedVersion;
    }
}
