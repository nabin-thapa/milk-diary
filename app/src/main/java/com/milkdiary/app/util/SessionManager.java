package com.milkdiary.app.util;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import com.milkdiary.app.model.User;

public class SessionManager {

    private static final String KEY_USER_ID = "session_user_id";
    private static final String KEY_USER_NAME = "session_user_name";
    private static final String KEY_USER_PHONE = "session_user_phone";
    private static final String KEY_USER_ROLE = "session_user_role";
    private static final String KEY_IS_LOGGED_IN = "session_is_logged_in";
    private static final String KEY_REMEMBER_ME = "session_remember_me";
    private static final String KEY_SUPPLIER_ID = "session_supplier_id";

    private final SharedPreferences prefs;

    public SessionManager(Context context) {
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public void createSession(User user, boolean rememberMe, long supplierId) {
        prefs.edit()
                .putLong(KEY_USER_ID, user.getId())
                .putString(KEY_USER_NAME, user.getFullName())
                .putString(KEY_USER_PHONE, user.getPhone())
                .putString(KEY_USER_ROLE, user.getRole())
                .putBoolean(KEY_IS_LOGGED_IN, true)
                .putBoolean(KEY_REMEMBER_ME, rememberMe)
                .putLong(KEY_SUPPLIER_ID, supplierId)
                .apply();
    }

    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public long getUserId() {
        return prefs.getLong(KEY_USER_ID, -1);
    }

    public String getUserName() {
        return prefs.getString(KEY_USER_NAME, "");
    }

    public String getUserPhone() {
        return prefs.getString(KEY_USER_PHONE, "");
    }

    public String getUserRole() {
        return prefs.getString(KEY_USER_ROLE, User.ROLE_DAIRY);
    }

    public long getSupplierId() {
        return prefs.getLong(KEY_SUPPLIER_ID, 0);
    }

    public boolean isDairyOwner() {
        return User.ROLE_DAIRY.equals(getUserRole());
    }

    public boolean isSupplier() {
        return User.ROLE_SUPPLIER.equals(getUserRole());
    }

    public void logout() {
        prefs.edit()
                .remove(KEY_USER_ID)
                .remove(KEY_USER_NAME)
                .remove(KEY_USER_PHONE)
                .remove(KEY_USER_ROLE)
                .putBoolean(KEY_IS_LOGGED_IN, false)
                .remove(KEY_REMEMBER_ME)
                .remove(KEY_SUPPLIER_ID)
                .apply();
    }
}
