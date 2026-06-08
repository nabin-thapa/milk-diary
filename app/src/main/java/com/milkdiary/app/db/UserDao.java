package com.milkdiary.app.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.milkdiary.app.model.OwnerProfile;
import com.milkdiary.app.model.Supplier;
import com.milkdiary.app.model.SupplierProfile;
import com.milkdiary.app.model.User;
import com.milkdiary.app.util.PasswordUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class UserDao {

    private static final String TABLE_USERS = "users";
    private static final String TABLE_OWNER_PROFILES = "owner_profiles";
    private static final String TABLE_SUPPLIER_PROFILES = "supplier_profiles";

    private static final String COL_ID = "_id";
    private static final String COL_FULL_NAME = "full_name";
    private static final String COL_PHONE = "phone";
    private static final String COL_PASSWORD_HASH = "password_hash";
    private static final String COL_ROLE = "role";
    private static final String COL_CREATED_AT = "created_at";

    private static final String COL_USER_ID = "user_id";
    private static final String COL_DAIRY_NAME = "dairy_name";
    private static final String COL_ADDRESS = "address";

    private static final String COL_SUPPLIER_ID = "supplier_id";
    private static final String COL_VILLAGE = "village";
    private static final String COL_MILK_TYPE = "milk_type";

    private final DatabaseHelper dbHelper;

    public UserDao(Context context) {
        dbHelper = DatabaseHelper.getInstance(context);
    }

    public long insertUser(User user) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put(COL_FULL_NAME, user.getFullName());
        v.put(COL_PHONE, user.getPhone());
        v.put(COL_PASSWORD_HASH, user.getPasswordHash());
        v.put(COL_ROLE, user.getRole());
        v.put(COL_CREATED_AT, user.getCreatedAt());
        return db.insert(TABLE_USERS, null, v);
    }

    public User getByPhone(String phone) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.query(TABLE_USERS, null,
                COL_PHONE + "=?", new String[]{phone},
                null, null, null)) {
            if (c != null && c.moveToFirst()) return fromCursor(c);
        }
        return null;
    }

    public User getById(long id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.query(TABLE_USERS, null,
                COL_ID + "=?", new String[]{String.valueOf(id)},
                null, null, null)) {
            if (c != null && c.moveToFirst()) return fromCursor(c);
        }
        return null;
    }

    public boolean isPhoneExists(String phone) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.query(TABLE_USERS, new String[]{COL_ID},
                COL_PHONE + "=?", new String[]{phone},
                null, null, null)) {
            return c != null && c.getCount() > 0;
        }
    }

    public User verifyLogin(String phone, String password) {
        User user = getByPhone(phone);
        if (user == null) return null;
        if (PasswordUtils.verify(password, user.getPasswordHash())) {
            return user;
        }
        return null;
    }

    public void createOwnerProfile(long userId, String dairyName, String address) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put(COL_USER_ID, userId);
        v.put(COL_DAIRY_NAME, dairyName != null ? dairyName : "");
        v.put(COL_ADDRESS, address != null ? address : "");
        db.insert(TABLE_OWNER_PROFILES, null, v);
    }

    public void createSupplierProfile(long userId, long supplierId, String village, String milkType) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put(COL_USER_ID, userId);
        v.put(COL_SUPPLIER_ID, supplierId);
        v.put(COL_VILLAGE, village != null ? village : "");
        v.put(COL_MILK_TYPE, milkType != null ? milkType : "both");
        db.insert(TABLE_SUPPLIER_PROFILES, null, v);
    }

    public OwnerProfile getOwnerProfile(long userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.query(TABLE_OWNER_PROFILES, null,
                COL_USER_ID + "=?", new String[]{String.valueOf(userId)},
                null, null, null)) {
            if (c != null && c.moveToFirst()) {
                OwnerProfile p = new OwnerProfile();
                p.setId(c.getLong(c.getColumnIndexOrThrow(COL_ID)));
                p.setUserId(c.getLong(c.getColumnIndexOrThrow(COL_USER_ID)));
                p.setDairyName(c.getString(c.getColumnIndexOrThrow(COL_DAIRY_NAME)));
                p.setAddress(c.getString(c.getColumnIndexOrThrow(COL_ADDRESS)));
                return p;
            }
        }
        return null;
    }

    public SupplierProfile getSupplierProfile(long userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.query(TABLE_SUPPLIER_PROFILES, null,
                COL_USER_ID + "=?", new String[]{String.valueOf(userId)},
                null, null, null)) {
            if (c != null && c.moveToFirst()) {
                SupplierProfile p = new SupplierProfile();
                p.setId(c.getLong(c.getColumnIndexOrThrow(COL_ID)));
                p.setUserId(c.getLong(c.getColumnIndexOrThrow(COL_USER_ID)));
                p.setSupplierId(c.getLong(c.getColumnIndexOrThrow(COL_SUPPLIER_ID)));
                p.setVillage(c.getString(c.getColumnIndexOrThrow(COL_VILLAGE)));
                p.setMilkType(c.getString(c.getColumnIndexOrThrow(COL_MILK_TYPE)));
                return p;
            }
        }
        return null;
    }

    public long createSupplierAndLinkUser(String name, String phone, String village,
                                           String milkType, long userId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        String now = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        ContentValues supVals = new ContentValues();
        supVals.put(DatabaseHelper.COL_SUP_NAME, name);
        supVals.put(DatabaseHelper.COL_SUP_PHONE, phone);
        supVals.put(DatabaseHelper.COL_SUP_VILLAGE, village != null ? village : "");
        supVals.put(DatabaseHelper.COL_SUP_MILK_TYPE, milkType != null ? milkType : "both");
        supVals.put(DatabaseHelper.COL_SUP_COW_RATE, 0);
        supVals.put(DatabaseHelper.COL_SUP_BUF_RATE, 0);
        supVals.put(DatabaseHelper.COL_SUP_ACTIVE, 1);
        supVals.put(DatabaseHelper.COL_SUP_CREATED_AT, now);
        supVals.put("user_id", userId);

        long supplierId = db.insert(DatabaseHelper.TABLE_SUPPLIERS, null, supVals);

        createSupplierProfile(userId, supplierId, village, milkType);

        return supplierId;
    }

    private User fromCursor(Cursor c) {
        User u = new User();
        u.setId(c.getLong(c.getColumnIndexOrThrow(COL_ID)));
        u.setFullName(c.getString(c.getColumnIndexOrThrow(COL_FULL_NAME)));
        u.setPhone(c.getString(c.getColumnIndexOrThrow(COL_PHONE)));
        u.setPasswordHash(c.getString(c.getColumnIndexOrThrow(COL_PASSWORD_HASH)));
        u.setRole(c.getString(c.getColumnIndexOrThrow(COL_ROLE)));
        u.setCreatedAt(c.getString(c.getColumnIndexOrThrow(COL_CREATED_AT)));
        return u;
    }
}
