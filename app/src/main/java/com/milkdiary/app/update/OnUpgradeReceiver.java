package com.milkdiary.app.update;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.milkdiary.app.ui.DashboardActivity;

public class OnUpgradeReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_PACKAGE_REPLACED.equals(intent.getAction())) {
            if (intent.getData() != null &&
                    context.getPackageName().equals(intent.getData().getSchemeSpecificPart())) {
                Intent launch = new Intent(context, DashboardActivity.class);
                launch.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                context.startActivity(launch);
            }
        }
    }
}
