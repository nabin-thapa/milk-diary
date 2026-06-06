package com.milkdiary.app.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.milkdiary.app.model.Inventory;

import java.util.ArrayList;
import java.util.List;

public class InventoryDao {

    private final DatabaseHelper dbHelper;

    public InventoryDao(Context context) {
        dbHelper = DatabaseHelper.getInstance(context);
    }

    public long insert(Inventory inv) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.insertWithOnConflict(DatabaseHelper.TABLE_INVENTORY, null, toValues(inv),
                SQLiteDatabase.CONFLICT_REPLACE);
    }

    public int update(Inventory inv) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.update(DatabaseHelper.TABLE_INVENTORY, toValues(inv),
                DatabaseHelper.COL_ID + "=?", new String[]{String.valueOf(inv.getId())});
    }

    public Inventory getByDate(String date) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.query(DatabaseHelper.TABLE_INVENTORY, null,
                DatabaseHelper.COL_INV_DATE + "=?", new String[]{date},
                null, null, null)) {
            if (c != null && c.moveToFirst()) return fromCursor(c);
        }
        return null;
    }

    public Inventory getOrCreateToday(String date) {
        Inventory inv = getByDate(date);
        if (inv == null) {
            inv = new Inventory(date);
            insert(inv);
        }
        return inv;
    }

    public List<Inventory> getByMonth(String yearMonth) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        List<Inventory> list = new ArrayList<>();
        try (Cursor c = db.query(DatabaseHelper.TABLE_INVENTORY, null,
                DatabaseHelper.COL_INV_DATE + " LIKE ?", new String[]{yearMonth + "%"},
                null, null, DatabaseHelper.COL_INV_DATE + " ASC")) {
            while (c != null && c.moveToNext()) list.add(fromCursor(c));
        }
        return list;
    }

    public void autoCalculate(String date) {
        Inventory inv = getOrCreateToday(date);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // Total collected from milk_records
        try (Cursor c = db.rawQuery(
                "SELECT SUM(" + DatabaseHelper.COL_COW_LITERS + "), SUM(" +
                DatabaseHelper.COL_BUFFALO_LITERS + ") FROM " +
                DatabaseHelper.TABLE_RECORDS + " WHERE " + DatabaseHelper.COL_DATE + "=?",
                new String[]{date})) {
            if (c != null && c.moveToFirst()) {
                double cowTotal = c.getDouble(0);
                double bufTotal = c.getDouble(1);
                inv.setMorningCollected(cowTotal + bufTotal);
            }
        }

        // Total sold from sales
        double soldQty = 0;
        try (Cursor c = db.rawQuery(
                "SELECT SUM(" + DatabaseHelper.COL_SALE_QUANTITY + ") FROM " +
                DatabaseHelper.TABLE_SALES + " WHERE " + DatabaseHelper.COL_SALE_DATE + "=?",
                new String[]{date})) {
            if (c != null && c.moveToFirst()) soldQty = c.getDouble(0);
        }
        inv.setSold(soldQty);
        inv.calculateClosing();
        insert(inv);
    }

    private ContentValues toValues(Inventory inv) {
        ContentValues v = new ContentValues();
        v.put(DatabaseHelper.COL_INV_DATE, inv.getDate());
        v.put(DatabaseHelper.COL_INV_MORNING, inv.getMorningCollected());
        v.put(DatabaseHelper.COL_INV_EVENING, inv.getEveningCollected());
        v.put(DatabaseHelper.COL_INV_TOTAL_COLLECTED, inv.getTotalCollected());
        v.put(DatabaseHelper.COL_INV_SOLD, inv.getSold());
        v.put(DatabaseHelper.COL_INV_WASTE, inv.getWaste());
        v.put(DatabaseHelper.COL_INV_CLOSING, inv.getClosingStock());
        v.put(DatabaseHelper.COL_INV_NOTE, inv.getNote());
        return v;
    }

    private Inventory fromCursor(Cursor c) {
        Inventory inv = new Inventory();
        inv.setId(c.getLong(c.getColumnIndexOrThrow(DatabaseHelper.COL_ID)));
        inv.setDate(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_INV_DATE)));
        inv.setMorningCollected(c.getDouble(c.getColumnIndexOrThrow(DatabaseHelper.COL_INV_MORNING)));
        inv.setEveningCollected(c.getDouble(c.getColumnIndexOrThrow(DatabaseHelper.COL_INV_EVENING)));
        inv.setTotalCollected(c.getDouble(c.getColumnIndexOrThrow(DatabaseHelper.COL_INV_TOTAL_COLLECTED)));
        inv.setSold(c.getDouble(c.getColumnIndexOrThrow(DatabaseHelper.COL_INV_SOLD)));
        inv.setWaste(c.getDouble(c.getColumnIndexOrThrow(DatabaseHelper.COL_INV_WASTE)));
        inv.setClosingStock(c.getDouble(c.getColumnIndexOrThrow(DatabaseHelper.COL_INV_CLOSING)));
        inv.setNote(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_INV_NOTE)));
        return inv;
    }
}
