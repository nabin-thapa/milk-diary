package com.milkdiary.app.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.milkdiary.app.model.Payment;
import com.milkdiary.app.model.Supplier;

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
        List<Payment> list = new ArrayList<>();
        try (Cursor cursor = db.rawQuery(
                "SELECT p.*, COALESCE(s.name, '') as party_name " +
                "FROM " + DatabaseHelper.TABLE_PAYMENTS + " p " +
                "LEFT JOIN " + DatabaseHelper.TABLE_SUPPLIERS + " s ON p." +
                DatabaseHelper.COL_PARTY_ID + "=s." + DatabaseHelper.COL_ID + " " +
                "ORDER BY p." + DatabaseHelper.COL_PAY_DATE + " DESC",
                null)) {
            while (cursor != null && cursor.moveToNext()) list.add(fromCursor(cursor));
        }
        return list;
    }

    public List<Payment> getPaymentsByParty(String partyType, long partyId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        List<Payment> list = new ArrayList<>();
        try (Cursor cursor = db.query(DatabaseHelper.TABLE_PAYMENTS, null,
                DatabaseHelper.COL_PARTY_TYPE + "=? AND " +
                DatabaseHelper.COL_PARTY_ID + "=?",
                new String[]{partyType, String.valueOf(partyId)},
                null, null,
                DatabaseHelper.COL_PAY_DATE + " DESC")) {
            while (cursor != null && cursor.moveToNext()) list.add(fromCursor(cursor));
        }
        return list;
    }

    public double getTotalPaid() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.rawQuery(
                "SELECT SUM(" + DatabaseHelper.COL_PAY_AMOUNT + ") FROM " +
                DatabaseHelper.TABLE_PAYMENTS + " WHERE " +
                DatabaseHelper.COL_PARTY_TYPE + "=?",
                new String[]{Payment.PARTY_CUSTOMER})) {
            double total = 0;
            if (cursor != null && cursor.moveToFirst() && !cursor.isNull(0)) total = cursor.getDouble(0);
            return total;
        }
    }

    public double getTotalPaidToSuppliers() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.rawQuery(
                "SELECT SUM(" + DatabaseHelper.COL_PAY_AMOUNT + ") FROM " +
                DatabaseHelper.TABLE_PAYMENTS + " WHERE " +
                DatabaseHelper.COL_PARTY_TYPE + "=?",
                new String[]{Payment.PARTY_SUPPLIER})) {
            double total = 0;
            if (cursor != null && cursor.moveToFirst() && !cursor.isNull(0)) total = cursor.getDouble(0);
            return total;
        }
    }

    public List<Payment> getPaymentsByType(String partyType) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        List<Payment> list = new ArrayList<>();
        try (Cursor cursor = db.rawQuery(
                "SELECT p.*, COALESCE(s.name, '') as party_name " +
                "FROM " + DatabaseHelper.TABLE_PAYMENTS + " p " +
                "LEFT JOIN " + DatabaseHelper.TABLE_SUPPLIERS + " s ON p." +
                DatabaseHelper.COL_PARTY_ID + "=s." + DatabaseHelper.COL_ID + " " +
                "WHERE p." + DatabaseHelper.COL_PARTY_TYPE + "=? " +
                "ORDER BY p." + DatabaseHelper.COL_PAY_DATE + " DESC",
                new String[]{partyType})) {
            while (cursor != null && cursor.moveToNext()) list.add(fromCursor(cursor));
        }
        return list;
    }

    private ContentValues toContentValues(Payment p) {
        ContentValues v = new ContentValues();
        v.put(DatabaseHelper.COL_PAY_DATE, p.getDate());
        v.put(DatabaseHelper.COL_PAY_AMOUNT, p.getAmount());
        v.put(DatabaseHelper.COL_PAY_NOTE, p.getNote());
        v.put(DatabaseHelper.COL_PARTY_TYPE, p.getPartyType());
        v.put(DatabaseHelper.COL_PARTY_ID, p.getPartyId());
        return v;
    }

    private Payment fromCursor(Cursor c) {
        Payment p = new Payment();
        p.setId(c.getLong(c.getColumnIndexOrThrow(DatabaseHelper.COL_PAY_ID)));
        p.setDate(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_PAY_DATE)));
        p.setAmount(c.getDouble(c.getColumnIndexOrThrow(DatabaseHelper.COL_PAY_AMOUNT)));
        p.setNote(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_PAY_NOTE)));
        p.setPartyType(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_PARTY_TYPE)));
        p.setPartyId(c.getLong(c.getColumnIndexOrThrow(DatabaseHelper.COL_PARTY_ID)));
        int colIdx = c.getColumnIndex("party_name");
        if (colIdx >= 0) p.setPartyName(c.getString(colIdx));
        return p;
    }
}
