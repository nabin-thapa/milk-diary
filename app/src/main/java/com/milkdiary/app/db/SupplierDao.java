package com.milkdiary.app.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.milkdiary.app.model.Supplier;

import java.util.ArrayList;
import java.util.List;

public class SupplierDao {

    private final DatabaseHelper dbHelper;

    public SupplierDao(Context context) {
        dbHelper = DatabaseHelper.getInstance(context);
    }

    public long insert(Supplier supplier) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.insert(DatabaseHelper.TABLE_SUPPLIERS, null, toValues(supplier));
    }

    public int update(Supplier supplier) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.update(DatabaseHelper.TABLE_SUPPLIERS, toValues(supplier),
                DatabaseHelper.COL_ID + "=?", new String[]{String.valueOf(supplier.getId())});
    }

    public int delete(long id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.delete(DatabaseHelper.TABLE_SUPPLIERS,
                DatabaseHelper.COL_ID + "=?", new String[]{String.valueOf(id)});
    }

    public Supplier getById(long id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.query(DatabaseHelper.TABLE_SUPPLIERS, null,
                DatabaseHelper.COL_ID + "=?", new String[]{String.valueOf(id)},
                null, null, null)) {
            if (c != null && c.moveToFirst()) return fromCursor(c);
        }
        return null;
    }

    public List<Supplier> getAll() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        List<Supplier> list = new ArrayList<>();
        try (Cursor c = db.query(DatabaseHelper.TABLE_SUPPLIERS, null,
                null, null, null, null,
                DatabaseHelper.COL_SUP_NAME + " ASC")) {
            while (c != null && c.moveToNext()) list.add(fromCursor(c));
        }
        return list;
    }

    public List<Supplier> search(String query) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        List<Supplier> list = new ArrayList<>();
        String q = "%" + query + "%";
        try (Cursor c = db.query(DatabaseHelper.TABLE_SUPPLIERS, null,
                DatabaseHelper.COL_SUP_NAME + " LIKE ? OR " +
                DatabaseHelper.COL_SUP_PHONE + " LIKE ? OR " +
                DatabaseHelper.COL_SUP_VILLAGE + " LIKE ?",
                new String[]{q, q, q}, null, null,
                DatabaseHelper.COL_SUP_NAME + " ASC")) {
            while (c != null && c.moveToNext()) list.add(fromCursor(c));
        }
        return list;
    }

    public List<Supplier> getActive() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        List<Supplier> list = new ArrayList<>();
        try (Cursor c = db.query(DatabaseHelper.TABLE_SUPPLIERS, null,
                DatabaseHelper.COL_SUP_ACTIVE + "=1", null, null, null,
                DatabaseHelper.COL_SUP_NAME + " ASC")) {
            while (c != null && c.moveToNext()) list.add(fromCursor(c));
        }
        return list;
    }

    public double getTotalEarningsBySupplier(long supplierId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.rawQuery("SELECT COALESCE(SUM(" + DatabaseHelper.COL_TOTAL + "),0) FROM " +
                DatabaseHelper.TABLE_RECORDS + " WHERE " + DatabaseHelper.COL_SUPPLIER_ID + "=?",
                new String[]{String.valueOf(supplierId)})) {
            if (c != null && c.moveToFirst()) return c.getDouble(0);
        }
        return 0;
    }

    public double getTotalPaidBySupplier(long supplierId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.rawQuery("SELECT COALESCE(SUM(" + DatabaseHelper.COL_PAY_AMOUNT + "),0) FROM " +
                DatabaseHelper.TABLE_PAYMENTS + " WHERE " + DatabaseHelper.COL_PARTY_TYPE + "='supplier' AND " +
                DatabaseHelper.COL_PARTY_ID + "=?",
                new String[]{String.valueOf(supplierId)})) {
            if (c != null && c.moveToFirst()) return c.getDouble(0);
        }
        return 0;
    }

    public double getPendingBalance(long supplierId) {
        return getTotalEarningsBySupplier(supplierId) - getTotalPaidBySupplier(supplierId);
    }

    private ContentValues toValues(Supplier s) {
        ContentValues v = new ContentValues();
        v.put(DatabaseHelper.COL_SUP_NAME, s.getName());
        v.put(DatabaseHelper.COL_SUP_PHONE, s.getPhone());
        v.put(DatabaseHelper.COL_SUP_VILLAGE, s.getVillage());
        v.put(DatabaseHelper.COL_SUP_MILK_TYPE, s.getMilkType());
        v.put(DatabaseHelper.COL_SUP_COW_RATE, s.getDefaultCowRate());
        v.put(DatabaseHelper.COL_SUP_BUF_RATE, s.getDefaultBuffaloRate());
        v.put(DatabaseHelper.COL_SUP_ACTIVE, s.isActive() ? 1 : 0);
        v.put(DatabaseHelper.COL_SUP_CREATED_AT, s.getCreatedAt());
        return v;
    }

    private Supplier fromCursor(Cursor c) {
        Supplier s = new Supplier();
        s.setId(c.getLong(c.getColumnIndexOrThrow(DatabaseHelper.COL_ID)));
        s.setName(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_SUP_NAME)));
        s.setPhone(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_SUP_PHONE)));
        s.setVillage(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_SUP_VILLAGE)));
        s.setMilkType(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_SUP_MILK_TYPE)));
        s.setDefaultCowRate(c.getDouble(c.getColumnIndexOrThrow(DatabaseHelper.COL_SUP_COW_RATE)));
        s.setDefaultBuffaloRate(c.getDouble(c.getColumnIndexOrThrow(DatabaseHelper.COL_SUP_BUF_RATE)));
        s.setActive(c.getInt(c.getColumnIndexOrThrow(DatabaseHelper.COL_SUP_ACTIVE)) == 1);
        s.setCreatedAt(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_SUP_CREATED_AT)));
        return s;
    }
}
