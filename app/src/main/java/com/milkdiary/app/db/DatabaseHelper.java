package com.milkdiary.app.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "milk_diary.db";
    public static final int DATABASE_VERSION = 3;

    // Table: milk_records
    public static final String TABLE_RECORDS = "milk_records";
    public static final String COL_ID = "_id";
    public static final String COL_DATE = "date";              // YYYY-MM-DD
    public static final String COL_SESSION_TYPE = "session_type"; // "morning" or "evening"
    public static final String COL_COW_LITERS = "cow_liters";
    public static final String COL_COW_RATE = "cow_rate";
    public static final String COL_COW_AMOUNT = "cow_amount";
    public static final String COL_BUFFALO_LITERS = "buffalo_liters";
    public static final String COL_BUFFALO_RATE = "buffalo_rate";
    public static final String COL_BUFFALO_AMOUNT = "buffalo_amount";
    public static final String COL_TOTAL = "total";
    public static final String COL_NOTE = "note";
    public static final String COL_EDIT_COUNT = "edit_count";

    // Table: payments
    public static final String TABLE_PAYMENTS = "payments";
    public static final String COL_PAY_ID = "_id";
    public static final String COL_PAY_DATE = "pay_date";      // YYYY-MM-DD
    public static final String COL_PAY_AMOUNT = "pay_amount";
    public static final String COL_PAY_NOTE = "pay_note";

    private static final String CREATE_RECORDS_TABLE =
            "CREATE TABLE " + TABLE_RECORDS + " (" +
            COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COL_DATE + " TEXT NOT NULL, " +
            COL_SESSION_TYPE + " TEXT NOT NULL DEFAULT 'morning', " +
            COL_COW_LITERS + " REAL DEFAULT 0, " +
            COL_COW_RATE + " REAL DEFAULT 0, " +
            COL_COW_AMOUNT + " REAL DEFAULT 0, " +
            COL_BUFFALO_LITERS + " REAL DEFAULT 0, " +
            COL_BUFFALO_RATE + " REAL DEFAULT 0, " +
            COL_BUFFALO_AMOUNT + " REAL DEFAULT 0, " +
            COL_TOTAL + " REAL DEFAULT 0, " +
            COL_NOTE + " TEXT, " +
            COL_EDIT_COUNT + " INTEGER DEFAULT 0, " +
            "UNIQUE(" + COL_DATE + ", " + COL_SESSION_TYPE + ")" +
            ");";

    private static final String CREATE_PAYMENTS_TABLE =
            "CREATE TABLE " + TABLE_PAYMENTS + " (" +
            COL_PAY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COL_PAY_DATE + " TEXT NOT NULL, " +
            COL_PAY_AMOUNT + " REAL DEFAULT 0, " +
            COL_PAY_NOTE + " TEXT" +
            ");";

    private static volatile DatabaseHelper instance;

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    public static synchronized void resetInstance() {
        if (instance != null) {
            try { instance.close(); } catch (Exception ignored) {}
            instance = null;
        }
    }

    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_RECORDS_TABLE);
        db.execSQL(CREATE_PAYMENTS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // v2 -> v3: add session_type, edit_count; change UNIQUE(date) to UNIQUE(date, session_type)
        if (oldVersion < 3) {
            // Create new table with composite unique constraint
            String tempTable = TABLE_RECORDS + "_new";
            String createTemp = CREATE_RECORDS_TABLE.replace(TABLE_RECORDS, tempTable);
            db.execSQL(createTemp);

            // Copy existing records as "morning" entries
            db.execSQL("INSERT INTO " + tempTable + " (" +
                    COL_ID + ", " + COL_DATE + ", " + COL_SESSION_TYPE + ", " +
                    COL_COW_LITERS + ", " + COL_COW_RATE + ", " + COL_COW_AMOUNT + ", " +
                    COL_BUFFALO_LITERS + ", " + COL_BUFFALO_RATE + ", " + COL_BUFFALO_AMOUNT + ", " +
                    COL_TOTAL + ", " + COL_NOTE + ", " + COL_EDIT_COUNT + ") " +
                    "SELECT " +
                    COL_ID + ", " + COL_DATE + ", 'morning', " +
                    COL_COW_LITERS + ", " + COL_COW_RATE + ", " + COL_COW_AMOUNT + ", " +
                    COL_BUFFALO_LITERS + ", " + COL_BUFFALO_RATE + ", " + COL_BUFFALO_AMOUNT + ", " +
                    COL_TOTAL + ", " + COL_NOTE + ", 0 " +
                    "FROM " + TABLE_RECORDS);

            // Drop old table, rename new one
            db.execSQL("DROP TABLE " + TABLE_RECORDS);
            db.execSQL("ALTER TABLE " + tempTable + " RENAME TO " + TABLE_RECORDS);
        }
    }
}
