package com.example.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class VendorDBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "VendorDatabase.db"; // Separate database for vendors
    private static final int DATABASE_VERSION = 2; // Incremented version to trigger onUpgrade
    private static final String TABLE_VENDORS = "Vendors";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_NAME = "vendorname";
    private static final String COLUMN_CNIC = "cnic";
    private static final String COLUMN_PHONE = "phone";
    private static final String COLUMN_PASSWORD = "password";

    public VendorDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_VENDORS + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_NAME + " TEXT NOT NULL, " +
                COLUMN_CNIC + " TEXT NOT NULL, " +
                COLUMN_PHONE + " TEXT NOT NULL, " +
                COLUMN_PASSWORD + " TEXT NOT NULL)";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop the old table and create a new one
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_VENDORS);
        onCreate(db);
    }

    // Insert vendor into the database
    public boolean insertVendor(String vendorname, String cnic, String phone, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_NAME, vendorname);
        contentValues.put(COLUMN_CNIC, cnic);
        contentValues.put(COLUMN_PHONE, phone);
        contentValues.put(COLUMN_PASSWORD, password);

        long result = db.insert(TABLE_VENDORS, null, contentValues);
        db.close();
        return result != -1; // Return true if insert is successful
    }

    // Validate vendor credentials
    public boolean validateVendor(String vendorname, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_VENDORS,
                new String[]{COLUMN_ID},
                COLUMN_NAME + "=? AND " + COLUMN_PASSWORD + "=?",
                new String[]{vendorname, password},
                null, null, null);

        boolean isValid = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return isValid;
    }
}
