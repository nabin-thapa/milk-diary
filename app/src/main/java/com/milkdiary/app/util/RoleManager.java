package com.milkdiary.app.util;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

public class RoleManager {

    public static final String ROLE_DAIRY = "dairy";
    public static final String ROLE_SUPPLIER = "supplier";

    private static final String KEY_ROLE = "user_role";
    private static final String KEY_ROLE_SET = "role_initialized";

    private final SharedPreferences prefs;

    public RoleManager(Context context) {
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public boolean isRoleSet() {
        return prefs.getBoolean(KEY_ROLE_SET, false);
    }

    public boolean isDairy() {
        return ROLE_DAIRY.equals(prefs.getString(KEY_ROLE, ROLE_DAIRY));
    }

    public boolean isSupplier() {
        return ROLE_SUPPLIER.equals(prefs.getString(KEY_ROLE, ROLE_DAIRY));
    }

    public String getRole() {
        return prefs.getString(KEY_ROLE, ROLE_DAIRY);
    }

    public void setRole(String role) {
        prefs.edit()
                .putString(KEY_ROLE, role)
                .putBoolean(KEY_ROLE_SET, true)
                .apply();
    }

    public String getRoleDisplayName() {
        if (isDairy()) return "Dairy";
        return "Milk Supplier";
    }

    public static void requireDairy(android.content.Context context) {
        RoleManager rm = new RoleManager(context);
        if (rm.isSupplier()) {
            android.widget.Toast.makeText(context,
                    "Only the Dairy owner can access this section.",
                    android.widget.Toast.LENGTH_SHORT).show();
            if (context instanceof android.app.Activity) {
                ((android.app.Activity) context).finish();
            }
        }
    }
}
