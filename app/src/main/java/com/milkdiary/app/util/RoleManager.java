package com.milkdiary.app.util;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

public class RoleManager {

    public static final String ROLE_OWNER = "owner";
    public static final String ROLE_SALESMAN = "salesman";

    private static final String KEY_ROLE = "user_role";
    private static final String KEY_ROLE_SET = "role_initialized";

    private final SharedPreferences prefs;

    public RoleManager(Context context) {
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public boolean isRoleSet() {
        return prefs.getBoolean(KEY_ROLE_SET, false);
    }

    public boolean isOwner() {
        return ROLE_OWNER.equals(prefs.getString(KEY_ROLE, ROLE_OWNER));
    }

    public boolean isSalesman() {
        return ROLE_SALESMAN.equals(prefs.getString(KEY_ROLE, ROLE_OWNER));
    }

    public String getRole() {
        return prefs.getString(KEY_ROLE, ROLE_OWNER);
    }

    public void setRole(String role) {
        prefs.edit()
                .putString(KEY_ROLE, role)
                .putBoolean(KEY_ROLE_SET, true)
                .apply();
    }

    public String getRoleDisplayName() {
        if (isOwner()) return "Owner";
        return "Salesman";
    }

    public static void requireOwner(android.content.Context context) {
        RoleManager rm = new RoleManager(context);
        if (rm.isSalesman()) {
            android.widget.Toast.makeText(context,
                    "Only the Owner can access this section.",
                    android.widget.Toast.LENGTH_SHORT).show();
            if (context instanceof android.app.Activity) {
                ((android.app.Activity) context).finish();
            }
        }
    }
}
