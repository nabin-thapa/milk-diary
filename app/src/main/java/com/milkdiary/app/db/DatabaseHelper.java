package com.milkdiary.app.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "milk_diary.db";
    public static final int DATABASE_VERSION = 5;

    // Table: milk_records
    public static final String TABLE_RECORDS = "milk_records";
    public static final String COL_ID = "_id";
    public static final String COL_DATE = "date";
    public static final String COL_SESSION_TYPE = "session_type";
    public static final String COL_COW_LITERS = "cow_liters";
    public static final String COL_COW_RATE = "cow_rate";
    public static final String COL_COW_AMOUNT = "cow_amount";
    public static final String COL_BUFFALO_LITERS = "buffalo_liters";
    public static final String COL_BUFFALO_RATE = "buffalo_rate";
    public static final String COL_BUFFALO_AMOUNT = "buffalo_amount";
    public static final String COL_TOTAL = "total";
    public static final String COL_NOTE = "note";
    public static final String COL_EDIT_COUNT = "edit_count";
    public static final String COL_SUPPLIER_ID = "supplier_id";
    public static final String COL_FAT_PERCENTAGE = "fat_percentage";

    // Table: payments
    public static final String TABLE_PAYMENTS = "payments";
    public static final String COL_PAY_ID = "_id";
    public static final String COL_PAY_DATE = "pay_date";
    public static final String COL_PAY_AMOUNT = "pay_amount";
    public static final String COL_PAY_NOTE = "pay_note";
    public static final String COL_PARTY_TYPE = "party_type";
    public static final String COL_PARTY_ID = "party_id";

    // Table: suppliers
    public static final String TABLE_SUPPLIERS = "suppliers";
    public static final String COL_SUP_NAME = "name";
    public static final String COL_SUP_PHONE = "phone";
    public static final String COL_SUP_VILLAGE = "village";
    public static final String COL_SUP_MILK_TYPE = "milk_type";
    public static final String COL_SUP_COW_RATE = "default_cow_rate";
    public static final String COL_SUP_BUF_RATE = "default_buffalo_rate";
    public static final String COL_SUP_ACTIVE = "is_active";
    public static final String COL_SUP_CREATED_AT = "created_at";

    // Table: sales
    public static final String TABLE_SALES = "sales";
    public static final String COL_SALE_DATE = "date";
    public static final String COL_SALE_SESSION = "session_type";
    public static final String COL_SALE_CUSTOMER = "customer_name";
    public static final String COL_SALE_CUSTOMER_TYPE = "customer_type";
    public static final String COL_SALE_MILK_TYPE = "milk_type";
    public static final String COL_SALE_QUANTITY = "quantity";
    public static final String COL_SALE_RATE = "rate";
    public static final String COL_SALE_AMOUNT = "amount";
    public static final String COL_SALE_PAID = "is_paid";
    public static final String COL_SALE_NOTE = "note";
    public static final String COL_SALE_CREATED_AT = "created_at";

    // Table: inventory
    public static final String TABLE_INVENTORY = "inventory";
    public static final String COL_INV_DATE = "date";
    public static final String COL_INV_MORNING = "morning_collected";
    public static final String COL_INV_EVENING = "evening_collected";
    public static final String COL_INV_TOTAL_COLLECTED = "total_collected";
    public static final String COL_INV_SOLD = "sold";
    public static final String COL_INV_WASTE = "waste";
    public static final String COL_INV_CLOSING = "closing_stock";
    public static final String COL_INV_NOTE = "note";

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
            COL_SUPPLIER_ID + " INTEGER DEFAULT 0, " +
            COL_FAT_PERCENTAGE + " REAL DEFAULT 0, " +
            "UNIQUE(" + COL_DATE + ", " + COL_SESSION_TYPE + ")" +
            ");";

    private static final String CREATE_PAYMENTS_TABLE =
            "CREATE TABLE " + TABLE_PAYMENTS + " (" +
            COL_PAY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COL_PAY_DATE + " TEXT NOT NULL, " +
            COL_PAY_AMOUNT + " REAL DEFAULT 0, " +
            COL_PAY_NOTE + " TEXT, " +
            COL_PARTY_TYPE + " TEXT DEFAULT 'general', " +
            COL_PARTY_ID + " INTEGER DEFAULT 0" +
            ");";

    private static final String CREATE_SUPPLIERS_TABLE =
            "CREATE TABLE " + TABLE_SUPPLIERS + " (" +
            COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COL_SUP_NAME + " TEXT NOT NULL, " +
            COL_SUP_PHONE + " TEXT, " +
            COL_SUP_VILLAGE + " TEXT, " +
            COL_SUP_MILK_TYPE + " TEXT DEFAULT 'both', " +
            COL_SUP_COW_RATE + " REAL DEFAULT 0, " +
            COL_SUP_BUF_RATE + " REAL DEFAULT 0, " +
            COL_SUP_ACTIVE + " INTEGER DEFAULT 1, " +
            COL_SUP_CREATED_AT + " TEXT NOT NULL" +
            ");";

    private static final String CREATE_SALES_TABLE =
            "CREATE TABLE " + TABLE_SALES + " (" +
            COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COL_SALE_DATE + " TEXT NOT NULL, " +
            COL_SALE_SESSION + " TEXT NOT NULL DEFAULT 'morning', " +
            COL_SALE_CUSTOMER + " TEXT NOT NULL, " +
            COL_SALE_CUSTOMER_TYPE + " TEXT DEFAULT 'other', " +
            COL_SALE_MILK_TYPE + " TEXT NOT NULL, " +
            COL_SALE_QUANTITY + " REAL DEFAULT 0, " +
            COL_SALE_RATE + " REAL DEFAULT 0, " +
            COL_SALE_AMOUNT + " REAL DEFAULT 0, " +
            COL_SALE_PAID + " INTEGER DEFAULT 0, " +
            COL_SALE_NOTE + " TEXT, " +
            COL_SALE_CREATED_AT + " TEXT NOT NULL" +
            ");";

    private static final String CREATE_INVENTORY_TABLE =
            "CREATE TABLE " + TABLE_INVENTORY + " (" +
            COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COL_INV_DATE + " TEXT NOT NULL UNIQUE, " +
            COL_INV_MORNING + " REAL DEFAULT 0, " +
            COL_INV_EVENING + " REAL DEFAULT 0, " +
            COL_INV_TOTAL_COLLECTED + " REAL DEFAULT 0, " +
            COL_INV_SOLD + " REAL DEFAULT 0, " +
            COL_INV_WASTE + " REAL DEFAULT 0, " +
            COL_INV_CLOSING + " REAL DEFAULT 0, " +
            COL_INV_NOTE + " TEXT" +
            ");";

    // Table: users
    public static final String TABLE_USERS = "users";
    public static final String COL_USER_FULL_NAME = "full_name";
    public static final String COL_USER_PHONE = "phone";
    public static final String COL_USER_PASSWORD_HASH = "password_hash";
    public static final String COL_USER_ROLE = "role";
    public static final String COL_USER_CREATED_AT = "created_at";

    // Table: owner_profiles
    public static final String TABLE_OWNER_PROFILES = "owner_profiles";
    public static final String COL_OP_USER_ID = "user_id";
    public static final String COL_OP_DAIRY_NAME = "dairy_name";
    public static final String COL_OP_ADDRESS = "address";

    // Table: supplier_profiles
    public static final String TABLE_SUPPLIER_PROFILES = "supplier_profiles";
    public static final String COL_SP_USER_ID = "user_id";
    public static final String COL_SP_SUPPLIER_ID = "supplier_id";
    public static final String COL_SP_VILLAGE = "village";
    public static final String COL_SP_MILK_TYPE = "milk_type";

    private static final String CREATE_USERS_TABLE =
            "CREATE TABLE " + TABLE_USERS + " (" +
            COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COL_USER_FULL_NAME + " TEXT NOT NULL, " +
            COL_USER_PHONE + " TEXT NOT NULL UNIQUE, " +
            COL_USER_PASSWORD_HASH + " TEXT NOT NULL, " +
            COL_USER_ROLE + " TEXT NOT NULL, " +
            COL_USER_CREATED_AT + " TEXT NOT NULL" +
            ");";

    private static final String CREATE_OWNER_PROFILES_TABLE =
            "CREATE TABLE " + TABLE_OWNER_PROFILES + " (" +
            COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COL_OP_USER_ID + " INTEGER NOT NULL, " +
            COL_OP_DAIRY_NAME + " TEXT, " +
            COL_OP_ADDRESS + " TEXT" +
            ");";

    private static final String CREATE_SUPPLIER_PROFILES_TABLE =
            "CREATE TABLE " + TABLE_SUPPLIER_PROFILES + " (" +
            COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COL_SP_USER_ID + " INTEGER NOT NULL, " +
            COL_SP_SUPPLIER_ID + " INTEGER DEFAULT 0, " +
            COL_SP_VILLAGE + " TEXT, " +
            COL_SP_MILK_TYPE + " TEXT DEFAULT 'both'" +
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
        db.execSQL(CREATE_SUPPLIERS_TABLE);
        db.execSQL(CREATE_SALES_TABLE);
        db.execSQL(CREATE_INVENTORY_TABLE);
        db.execSQL(CREATE_USERS_TABLE);
        db.execSQL(CREATE_OWNER_PROFILES_TABLE);
        db.execSQL(CREATE_SUPPLIER_PROFILES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 3) {
            String tempTable = TABLE_RECORDS + "_new";
            String createTemp = CREATE_RECORDS_TABLE.replace(TABLE_RECORDS, tempTable);
            db.execSQL(createTemp);
            db.execSQL("INSERT INTO " + tempTable + " (" +
                    COL_ID + ", " + COL_DATE + ", " + COL_SESSION_TYPE + ", " +
                    COL_COW_LITERS + ", " + COL_COW_RATE + ", " + COL_COW_AMOUNT + ", " +
                    COL_BUFFALO_LITERS + ", " + COL_BUFFALO_RATE + ", " + COL_BUFFALO_AMOUNT + ", " +
                    COL_TOTAL + ", " + COL_NOTE + ", " + COL_EDIT_COUNT + ", " +
                    COL_SUPPLIER_ID + ", " + COL_FAT_PERCENTAGE + ") " +
                    "SELECT " +
                    COL_ID + ", " + COL_DATE + ", 'morning', " +
                    COL_COW_LITERS + ", " + COL_COW_RATE + ", " + COL_COW_AMOUNT + ", " +
                    COL_BUFFALO_LITERS + ", " + COL_BUFFALO_RATE + ", " + COL_BUFFALO_AMOUNT + ", " +
                    COL_TOTAL + ", " + COL_NOTE + ", 0, 0, 0 " +
                    "FROM " + TABLE_RECORDS);
            db.execSQL("DROP TABLE " + TABLE_RECORDS);
            db.execSQL("ALTER TABLE " + tempTable + " RENAME TO " + TABLE_RECORDS);
        }
        if (oldVersion < 4) {
            db.execSQL("ALTER TABLE " + TABLE_RECORDS + " ADD COLUMN " + COL_SUPPLIER_ID + " INTEGER DEFAULT 0");
            db.execSQL("ALTER TABLE " + TABLE_RECORDS + " ADD COLUMN " + COL_FAT_PERCENTAGE + " REAL DEFAULT 0");
            db.execSQL("ALTER TABLE " + TABLE_PAYMENTS + " ADD COLUMN " + COL_PARTY_TYPE + " TEXT DEFAULT 'general'");
            db.execSQL("ALTER TABLE " + TABLE_PAYMENTS + " ADD COLUMN " + COL_PARTY_ID + " INTEGER DEFAULT 0");
            db.execSQL(CREATE_SUPPLIERS_TABLE);
            db.execSQL(CREATE_SALES_TABLE);
            db.execSQL(CREATE_INVENTORY_TABLE);
        }
        if (oldVersion < 5) {
            db.execSQL(CREATE_USERS_TABLE);
            db.execSQL(CREATE_OWNER_PROFILES_TABLE);
            db.execSQL(CREATE_SUPPLIER_PROFILES_TABLE);
            try {
                db.execSQL("ALTER TABLE " + TABLE_SUPPLIERS + " ADD COLUMN user_id INTEGER DEFAULT 0");
            } catch (Exception ignored) {
                // Column may already exist
            }
        }
    }
}
