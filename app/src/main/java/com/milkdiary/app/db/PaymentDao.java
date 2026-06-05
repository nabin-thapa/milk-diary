package com.milkdiary.app.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.milkdiary.app.model.Payment;

import java.util.ArrayList;
import java.util.List;

public class PaymentDao {

    private final DatabaseHelper dbHelper;

    public PaymentDao(Context context) {
        dbHelper = DatabaseHelper.getInstance(context);
    }

    public long insertPayment(Payment payment) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = toContentValues(payment);
        return db.insert(DatabaseHelper.TABLE_PAYMENTS, null, values);
    }

    public int updatePayment(Payment payment) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = toContentValues(payment);
        return db.update(DatabaseHelper.TABLE_PAYMENTS, values,
                DatabaseHelper.COL_PAY_ID + "=?",
                new String[]{String.valueOf(payment.getId())});
    }

    public int deletePayment(long id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.delete(DatabaseHelper.TABLE_PAYMENTS,
                DatabaseHelper.COL_PAY_ID + "=?",
                new String[]{String.valueOf(id)});
    }

    public List<Payment> getAllPayments() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_PAYMENTS, null,
                null, null, null, null,
                DatabaseHelper.COL_PAY_DATE + " DESC");
        return listFromCursor(cursor);
    }

    public double getTotalPaid() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT SUM(" + DatabaseHelper.COL_PAY_AMOUNT + ") FROM " + DatabaseHelper.TABLE_PAYMENTS,
                null);
        double total = 0;
        if (cursor.moveToFirst()) total = cursor.getDouble(0);
        cursor.close();
        return total;
    }

    // ---- helpers ----

    private ContentValues toContentValues(Payment p) {
        ContentValues v = new ContentValues();
        v.put(DatabaseHelper.COL_PAY_DATE, p.getDate());
        v.put(DatabaseHelper.COL_PAY_AMOUNT, p.getAmount());
        v.put(DatabaseHelper.COL_PAY_NOTE, p.getNote());
        return v;
    }

    private Payment fromCursor(Cursor c) {
        Payment p = new Payment();
        p.setId(c.getLong(c.getColumnIndexOrThrow(DatabaseHelper.COL_PAY_ID)));
        p.setDate(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_PAY_DATE)));
        p.setAmount(c.getDouble(c.getColumnIndexOrThrow(DatabaseHelper.COL_PAY_AMOUNT)));
        p.setNote(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_PAY_NOTE)));
        return p;
    }

    private List<Payment> listFromCursor(Cursor cursor) {
        List<Payment> list = new ArrayList<>();
        while (cursor.moveToNext()) list.add(fromCursor(cursor));
        cursor.close();
        return list;
    }
}
