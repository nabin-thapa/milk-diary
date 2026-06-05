package com.milkdiary.app.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.milkdiary.app.model.MilkRecord;
import com.milkdiary.app.model.MonthlySummary;

import java.util.ArrayList;
import java.util.List;

public class MilkRecordDao {

    private final DatabaseHelper dbHelper;

    public MilkRecordDao(Context context) {
        dbHelper = DatabaseHelper.getInstance(context);
    }

    public long insertRecord(MilkRecord record) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = toContentValues(record);
        return db.insertWithOnConflict(DatabaseHelper.TABLE_RECORDS, null, values,
                SQLiteDatabase.CONFLICT_REPLACE);
    }

    public int updateRecord(MilkRecord record) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = toContentValues(record);
        return db.update(DatabaseHelper.TABLE_RECORDS, values,
                DatabaseHelper.COL_ID + "=?",
                new String[]{String.valueOf(record.getId())});
    }

    public int deleteRecord(long id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.delete(DatabaseHelper.TABLE_RECORDS,
                DatabaseHelper.COL_ID + "=?",
                new String[]{String.valueOf(id)});
    }

    public MilkRecord getRecordByDate(String date) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_RECORDS, null,
                DatabaseHelper.COL_DATE + "=?", new String[]{date},
                null, null, null);
        MilkRecord record = null;
        if (cursor.moveToFirst()) {
            record = fromCursor(cursor);
        }
        cursor.close();
        return record;
    }

    public MilkRecord getRecordById(long id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_RECORDS, null,
                DatabaseHelper.COL_ID + "=?", new String[]{String.valueOf(id)},
                null, null, null);
        MilkRecord record = null;
        if (cursor.moveToFirst()) {
            record = fromCursor(cursor);
        }
        cursor.close();
        return record;
    }

    /** All records ordered newest first */
    public List<MilkRecord> getAllRecords() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_RECORDS, null,
                null, null, null, null,
                DatabaseHelper.COL_DATE + " DESC");
        return listFromCursor(cursor);
    }

    /** Records for a given month, e.g. "2024-06" ordered oldest first */
    public List<MilkRecord> getRecordsByMonth(String yearMonth) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_RECORDS, null,
                DatabaseHelper.COL_DATE + " LIKE ?",
                new String[]{yearMonth + "-%"},
                null, null,
                DatabaseHelper.COL_DATE + " ASC");
        return listFromCursor(cursor);
    }

    /** Search by partial date string */
    public List<MilkRecord> searchByDate(String query) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_RECORDS, null,
                DatabaseHelper.COL_DATE + " LIKE ?",
                new String[]{"%" + query + "%"},
                null, null,
                DatabaseHelper.COL_DATE + " DESC");
        return listFromCursor(cursor);
    }

    public MonthlySummary getMonthlySummary(String yearMonth) {
        // Use a single efficient query instead of loading all records
        double[] stats = getMonthlyStats(yearMonth);
        int count = getMonthlyRecordCount(yearMonth);

        MonthlySummary summary = new MonthlySummary();
        summary.setTotalCowLiters(stats[0]);
        summary.setTotalBuffaloLiters(stats[1]);
        summary.setTotalLiters(stats[0] + stats[1]);
        summary.setTotalEarnings(stats[2]);
        summary.setRecordCount(count);
        summary.setAvgDailyEarnings(count == 0 ? 0 : stats[2] / count);
        return summary;
    }

    /**
     * Single-query monthly stats.
     * Returns double[3]: [0]=cowLiters, [1]=bufLiters, [2]=totalEarnings
     */
    public double[] getMonthlyStats(String yearMonth) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT SUM(" + DatabaseHelper.COL_COW_LITERS + "), "
                + "SUM(" + DatabaseHelper.COL_BUFFALO_LITERS + "), "
                + "SUM(" + DatabaseHelper.COL_TOTAL + ") "
                + "FROM " + DatabaseHelper.TABLE_RECORDS
                + " WHERE " + DatabaseHelper.COL_DATE + " LIKE ?",
                new String[]{yearMonth + "-%"});
        double[] stats = {0, 0, 0};
        if (cursor.moveToFirst()) {
            stats[0] = cursor.isNull(0) ? 0 : cursor.getDouble(0);
            stats[1] = cursor.isNull(1) ? 0 : cursor.getDouble(1);
            stats[2] = cursor.isNull(2) ? 0 : cursor.getDouble(2);
        }
        cursor.close();
        return stats;
    }

    private int getMonthlyRecordCount(String yearMonth) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_RECORDS
                + " WHERE " + DatabaseHelper.COL_DATE + " LIKE ?",
                new String[]{yearMonth + "-%"});
        int count = 0;
        if (cursor.moveToFirst()) count = cursor.getInt(0);
        cursor.close();
        return count;
    }

    /** Sum of all totals ever */
    public double getTotalEarningsAllTime() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT SUM(" + DatabaseHelper.COL_TOTAL + ") FROM " + DatabaseHelper.TABLE_RECORDS,
                null);
        double total = 0;
        if (cursor.moveToFirst() && !cursor.isNull(0)) total = cursor.getDouble(0);
        cursor.close();
        return total;
    }

    /** Sum of totals for a specific month */
    public double getMonthlyEarnings(String yearMonth) {
        return getMonthlyStats(yearMonth)[2];
    }

    /** All records ordered oldest first (for CSV export) */
    public List<MilkRecord> getAllRecordsAsc() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_RECORDS, null,
                null, null, null, null,
                DatabaseHelper.COL_DATE + " ASC");
        return listFromCursor(cursor);
    }

    // ---- helpers ----

    private ContentValues toContentValues(MilkRecord r) {
        ContentValues v = new ContentValues();
        v.put(DatabaseHelper.COL_DATE, r.getDate());
        v.put(DatabaseHelper.COL_COW_LITERS, r.getCowLiters());
        v.put(DatabaseHelper.COL_COW_RATE, r.getCowRate());
        v.put(DatabaseHelper.COL_COW_AMOUNT, r.getCowAmount());
        v.put(DatabaseHelper.COL_BUFFALO_LITERS, r.getBuffaloLiters());
        v.put(DatabaseHelper.COL_BUFFALO_RATE, r.getBuffaloRate());
        v.put(DatabaseHelper.COL_BUFFALO_AMOUNT, r.getBuffaloAmount());
        v.put(DatabaseHelper.COL_TOTAL, r.getTotal());
        v.put(DatabaseHelper.COL_NOTE, r.getNote());
        return v;
    }

    private MilkRecord fromCursor(Cursor c) {
        MilkRecord r = new MilkRecord();
        r.setId(c.getLong(c.getColumnIndexOrThrow(DatabaseHelper.COL_ID)));
        r.setDate(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_DATE)));
        r.setCowLiters(c.getDouble(c.getColumnIndexOrThrow(DatabaseHelper.COL_COW_LITERS)));
        r.setCowRate(c.getDouble(c.getColumnIndexOrThrow(DatabaseHelper.COL_COW_RATE)));
        r.setCowAmount(c.getDouble(c.getColumnIndexOrThrow(DatabaseHelper.COL_COW_AMOUNT)));
        r.setBuffaloLiters(c.getDouble(c.getColumnIndexOrThrow(DatabaseHelper.COL_BUFFALO_LITERS)));
        r.setBuffaloRate(c.getDouble(c.getColumnIndexOrThrow(DatabaseHelper.COL_BUFFALO_RATE)));
        r.setBuffaloAmount(c.getDouble(c.getColumnIndexOrThrow(DatabaseHelper.COL_BUFFALO_AMOUNT)));
        r.setTotal(c.getDouble(c.getColumnIndexOrThrow(DatabaseHelper.COL_TOTAL)));
        r.setNote(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_NOTE)));
        return r;
    }

    private List<MilkRecord> listFromCursor(Cursor cursor) {
        List<MilkRecord> list = new ArrayList<>();
        while (cursor.moveToNext()) list.add(fromCursor(cursor));
        cursor.close();
        return list;
    }
}
