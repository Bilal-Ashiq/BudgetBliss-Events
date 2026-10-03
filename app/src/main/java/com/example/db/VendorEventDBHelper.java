package com.example.db;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

    public class VendorEventDBHelper extends SQLiteOpenHelper {
        private static final String DATABASE_NAME = "VendorDatabase.db";
        private static final int DATABASE_VERSION = 2; // Updated schema version

        // Table for Vendor Events
        private static final String TABLE_EVENTS = "vendor_events";
        private static final String COLUMN_EVENT_ID = "event_id";
        private static final String COLUMN_VENDOR_ID = "vendor_id";
        private static final String COLUMN_EVENT_NAME = "event_name";
        private static final String COLUMN_EVENT_TYPE = "event_type";
        private static final String COLUMN_SERVICES_PROVIDED = "services_provided";
        private static final String COLUMN_DESCRIPTION = "description";
        private static final String COLUMN_BUDGET = "budget";
        private static final String COLUMN_IMAGE_PATH = "image_path";

        public VendorEventDBHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            // Create Event Table
            String createEventTable = "CREATE TABLE IF NOT EXISTS " + TABLE_EVENTS + " ("
                    + COLUMN_EVENT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + COLUMN_VENDOR_ID + " INTEGER, "
                    + COLUMN_EVENT_NAME + " TEXT, "
                    + COLUMN_EVENT_TYPE + " TEXT, "
                    + COLUMN_SERVICES_PROVIDED + " TEXT, "
                    + COLUMN_DESCRIPTION + " TEXT, "
                    + COLUMN_BUDGET + " REAL, "
                    + COLUMN_IMAGE_PATH + " TEXT, "
                    + "FOREIGN KEY (" + COLUMN_VENDOR_ID + ") REFERENCES vendors(vendor_id))";
            db.execSQL(createEventTable);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            if (oldVersion < 2) {
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_EVENTS);
                onCreate(db);
            }
        }

        // Insert Event
        public boolean addVendorEvent(int vendorId, String eventName, String eventType, String services, String description, double budget, String imagePath) {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(COLUMN_VENDOR_ID, vendorId);
            values.put(COLUMN_EVENT_NAME, eventName);
            values.put(COLUMN_EVENT_TYPE, eventType);
            values.put(COLUMN_SERVICES_PROVIDED, services);
            values.put(COLUMN_DESCRIPTION, description);
            values.put(COLUMN_BUDGET, budget);
            values.put(COLUMN_IMAGE_PATH, imagePath);

            long result = db.insert(TABLE_EVENTS, null, values);
            return result != -1;
        }

        // Fetch Events by Vendor ID
        public Cursor getVendorEvents(int vendorId) {
            SQLiteDatabase db = this.getReadableDatabase();
            return db.rawQuery("SELECT * FROM " + TABLE_EVENTS + " WHERE " + COLUMN_VENDOR_ID + "=?", new String[]{String.valueOf(vendorId)});
        }

        // Update Event
        public boolean updateVendorEvent(int eventId, String eventName, String eventType, String services, String description, double budget, String imagePath) {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(COLUMN_EVENT_NAME, eventName);
            values.put(COLUMN_EVENT_TYPE, eventType);
            values.put(COLUMN_SERVICES_PROVIDED, services);
            values.put(COLUMN_DESCRIPTION, description);
            values.put(COLUMN_BUDGET, budget);
            values.put(COLUMN_IMAGE_PATH, imagePath);

            int rowsUpdated = db.update(TABLE_EVENTS, values, COLUMN_EVENT_ID + "=?", new String[]{String.valueOf(eventId)});
            return rowsUpdated > 0;
        }

        // Delete Event
        public boolean deleteVendorEvent(int eventId) {
            SQLiteDatabase db = this.getWritableDatabase();
            int rowsDeleted = db.delete(TABLE_EVENTS, COLUMN_EVENT_ID + "=?", new String[]{String.valueOf(eventId)});
            return rowsDeleted > 0;
        }
    }


