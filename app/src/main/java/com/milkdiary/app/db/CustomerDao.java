package com.milkdiary.app.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.milkdiary.app.model.Customer;

import java.util.ArrayList;
import java.util.List;

public class CustomerDao {

    public static final String[] CUSTOMER_TYPE_VALUES = {
            Customer.TYPE_HOTEL, Customer.TYPE_TEA_SHOP, Customer.TYPE_HOME, Customer.TYPE_OTHER
    };

    private final DatabaseHelper dbHelper;

    public CustomerDao(Context context) {
        dbHelper = DatabaseHelper.getInstance(context);
    }

    public long insert(Customer customer) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.insert(DatabaseHelper.TABLE_CUSTOMERS, null, toValues(customer));
    }

    public int update(Customer customer) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.update(DatabaseHelper.TABLE_CUSTOMERS, toValues(customer),
                DatabaseHelper.COL_ID + "=?", new String[]{String.valueOf(customer.getId())});
    }

    public int delete(long id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.delete(DatabaseHelper.TABLE_CUSTOMERS,
                DatabaseHelper.COL_ID + "=?", new String[]{String.valueOf(id)});
    }

    public Customer getById(long id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.query(DatabaseHelper.TABLE_CUSTOMERS, null,
                DatabaseHelper.COL_ID + "=?", new String[]{String.valueOf(id)},
                null, null, null)) {
            if (c != null && c.moveToFirst()) return fromCursor(c);
        }
        return null;
    }

    public List<Customer> getAll() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        List<Customer> list = new ArrayList<>();
        try (Cursor c = db.query(DatabaseHelper.TABLE_CUSTOMERS, null,
                null, null, null, null,
                DatabaseHelper.COL_CUS_NAME + " ASC")) {
            while (c != null && c.moveToNext()) list.add(fromCursor(c));
        }
        return list;
    }

    public List<Customer> search(String query) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        List<Customer> list = new ArrayList<>();
        String q = "%" + query + "%";
        try (Cursor c = db.query(DatabaseHelper.TABLE_CUSTOMERS, null,
                DatabaseHelper.COL_CUS_NAME + " LIKE ? OR " +
                DatabaseHelper.COL_CUS_PHONE + " LIKE ?",
                new String[]{q, q}, null, null,
                DatabaseHelper.COL_CUS_NAME + " ASC")) {
            while (c != null && c.moveToNext()) list.add(fromCursor(c));
        }
        return list;
    }

    public Customer getByName(String name) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.query(DatabaseHelper.TABLE_CUSTOMERS, null,
                DatabaseHelper.COL_CUS_NAME + "=?",
                new String[]{name}, null, null, null)) {
            if (c != null && c.moveToFirst()) return fromCursor(c);
        }
        return null;
    }

    private ContentValues toValues(Customer c) {
        ContentValues v = new ContentValues();
        v.put(DatabaseHelper.COL_CUS_NAME, c.getName());
        v.put(DatabaseHelper.COL_CUS_PHONE, c.getPhone());
        v.put(DatabaseHelper.COL_CUS_TYPE, c.getCustomerType());
        v.put(DatabaseHelper.COL_CUS_ADDRESS, c.getAddress());
        v.put(DatabaseHelper.COL_CUS_CREATED_AT, c.getCreatedAt());
        return v;
    }

    private Customer fromCursor(Cursor c) {
        Customer customer = new Customer();
        customer.setId(c.getLong(c.getColumnIndexOrThrow(DatabaseHelper.COL_ID)));
        customer.setName(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_CUS_NAME)));
        customer.setPhone(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_CUS_PHONE)));
        customer.setCustomerType(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_CUS_TYPE)));
        customer.setAddress(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_CUS_ADDRESS)));
        customer.setCreatedAt(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_CUS_CREATED_AT)));
        return customer;
    }
}
