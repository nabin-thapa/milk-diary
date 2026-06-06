package com.milkdiary.app.update;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class UpdateInstallReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // No longer needed — installation is handled by system installer via Intent.ACTION_VIEW
    }
}
