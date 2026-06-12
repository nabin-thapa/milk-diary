package com.milkdiary.app.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.milkdiary.app.model.Sale;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SaleDao {

    private final DatabaseHelper dbHelper;

    public SaleDao(Context context) {
        dbHelper = DatabaseHelper.getInstance(context);
    }

    public long insert(Sale sale) {
        if (sale.getCreatedAt() == null) {
            sale.setCreatedAt(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));
        }
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.insert(DatabaseHelper.TABLE_SALES, null, toValues(sale));
    }

    public int update(Sale sale) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.update(DatabaseHelper.TABLE_SALES, toValues(sale),
                DatabaseHelper.COL_ID + "=?", new String[]{String.valueOf(sale.getId())});
    }

    public int delete(long id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.delete(DatabaseHelper.TABLE_SALES,
                DatabaseHelper.COL_ID + "=?", new String[]{String.valueOf(id)});
    }

    public Sale getById(long id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.query(DatabaseHelper.TABLE_SALES, null,
                DatabaseHelper.COL_ID + "=?", new String[]{String.valueOf(id)},
                null, null, null)) {
            if (c != null && c.moveToFirst()) return fromCursor(c);
        }
        return null;
    }

    public List<Sale> getByDate(String date) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        List<Sale> list = new ArrayList<>();
        try (Cursor c = db.query(DatabaseHelper.TABLE_SALES, null,
                DatabaseHelper.COL_SALE_DATE + "=?", new String[]{date},
                null, null, DatabaseHelper.COL_ID + " DESC")) {
            while (c != null && c.moveToNext()) list.add(fromCursor(c));
        }
        return list;
    }

    public List<Sale> getByMonth(String yearMonth) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        List<Sale> list = new ArrayList<>();
        try (Cursor c = db.query(DatabaseHelper.TABLE_SALES, null,
                DatabaseHelper.COL_SALE_DATE + " LIKE ?", new String[]{yearMonth + "%"},
                null, null, DatabaseHelper.COL_SALE_DATE + " DESC, " + DatabaseHelper.COL_ID + " DESC")) {
            while (c != null && c.moveToNext()) list.add(fromCursor(c));
        }
        return list;
    }

    public List<Sale> getAll() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        List<Sale> list = new ArrayList<>();
        try (Cursor c = db.query(DatabaseHelper.TABLE_SALES, null,
                null, null, null, null,
                DatabaseHelper.COL_SALE_DATE + " DESC, " + DatabaseHelper.COL_ID + " DESC")) {
            while (c != null && c.moveToNext()) list.add(fromCursor(c));
        }
        return list;
    }

    public double getTotalSoldByDate(String date) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.rawQuery(
                "SELECT SUM(" + DatabaseHelper.COL_SALE_QUANTITY + ") FROM " +
                DatabaseHelper.TABLE_SALES + " WHERE " + DatabaseHelper.COL_SALE_DATE + "=?",
                new String[]{date})) {
            if (c != null && c.moveToFirst()) return c.getDouble(0);
        }
        return 0;
    }

    public double getTotalAmountByMonth(String yearMonth) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.rawQuery(
                "SELECT SUM(" + DatabaseHelper.COL_SALE_AMOUNT + ") FROM " +
                DatabaseHelper.TABLE_SALES + " WHERE " + DatabaseHelper.COL_SALE_DATE + " LIKE ?",
                new String[]{yearMonth + "%"})) {
            if (c != null && c.moveToFirst()) return c.getDouble(0);
        }
        return 0;
    }

    public double getTotalSalesAllTime() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.rawQuery("SELECT SUM(" + DatabaseHelper.COL_SALE_AMOUNT + ") FROM " + DatabaseHelper.TABLE_SALES, null)) {
            if (c != null && c.moveToFirst()) return c.getDouble(0);
        }
        return 0;
    }

    public double getCreditBalance() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.rawQuery(
                "SELECT SUM(" + DatabaseHelper.COL_SALE_AMOUNT + ") FROM " +
                DatabaseHelper.TABLE_SALES + " WHERE " + DatabaseHelper.COL_SALE_PAID + "=0",
                null)) {
            if (c != null && c.moveToFirst()) return c.getDouble(0);
        }
        return 0;
    }

    public double getUnpaidAmountByDate(String date) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.rawQuery(
                "SELECT SUM(" + DatabaseHelper.COL_SALE_AMOUNT + ") FROM " +
                DatabaseHelper.TABLE_SALES + " WHERE " + DatabaseHelper.COL_SALE_DATE + "=? AND " +
                DatabaseHelper.COL_SALE_PAID + "=0",
                new String[]{date})) {
            if (c != null && c.moveToFirst()) return c.getDouble(0);
        }
        return 0;
    }

    public boolean hasUnpaidSalesOnDate(String date) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.rawQuery(
                "SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_SALES +
                " WHERE " + DatabaseHelper.COL_SALE_DATE + "=? AND " +
                DatabaseHelper.COL_SALE_PAID + "=0",
                new String[]{date})) {
            if (c != null && c.moveToFirst()) return c.getInt(0) > 0;
        }
        return false;
    }

    private ContentValues toValues(Sale s) {
        ContentValues v = new ContentValues();
        v.put(DatabaseHelper.COL_SALE_DATE, s.getDate());
        v.put(DatabaseHelper.COL_SALE_SESSION, s.getSessionType());
        v.put(DatabaseHelper.COL_SALE_CUSTOMER, s.getCustomerName());
        v.put(DatabaseHelper.COL_SALE_CUSTOMER_TYPE, s.getCustomerType());
        v.put(DatabaseHelper.COL_SALE_MILK_TYPE, s.getMilkType());
        v.put(DatabaseHelper.COL_SALE_QUANTITY, s.getQuantity());
        v.put(DatabaseHelper.COL_SALE_RATE, s.getRate());
        v.put(DatabaseHelper.COL_SALE_AMOUNT, s.getAmount());
        v.put(DatabaseHelper.COL_SALE_PAID, s.isPaid() ? 1 : 0);
        v.put(DatabaseHelper.COL_SALE_NOTE, s.getNote());
        v.put(DatabaseHelper.COL_SALE_CREATED_AT, s.getCreatedAt());
        v.put(DatabaseHelper.COL_SALE_CUSTOMER_ID, s.getCustomerId());
        return v;
    }

    private Sale fromCursor(Cursor c) {
        Sale s = new Sale();
        s.setId(c.getLong(c.getColumnIndexOrThrow(DatabaseHelper.COL_ID)));
        s.setDate(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_SALE_DATE)));
        s.setSessionType(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_SALE_SESSION)));
        s.setCustomerName(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_SALE_CUSTOMER)));
        s.setCustomerType(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_SALE_CUSTOMER_TYPE)));
        s.setMilkType(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_SALE_MILK_TYPE)));
        s.setQuantity(c.getDouble(c.getColumnIndexOrThrow(DatabaseHelper.COL_SALE_QUANTITY)));
        s.setRate(c.getDouble(c.getColumnIndexOrThrow(DatabaseHelper.COL_SALE_RATE)));
        s.setAmount(c.getDouble(c.getColumnIndexOrThrow(DatabaseHelper.COL_SALE_AMOUNT)));
        s.setPaid(c.getInt(c.getColumnIndexOrThrow(DatabaseHelper.COL_SALE_PAID)) == 1);
        s.setNote(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_SALE_NOTE)));
        s.setCreatedAt(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_SALE_CREATED_AT)));
        int customerIdIdx = c.getColumnIndex(DatabaseHelper.COL_SALE_CUSTOMER_ID);
        if (customerIdIdx >= 0) s.setCustomerId(c.getLong(customerIdIdx));
        return s;
    }
}
