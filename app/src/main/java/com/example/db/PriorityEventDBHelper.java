package com.example.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class PriorityEventDBHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "PriorityEventDatabase.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_EVENTS = "priority_events";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_NAME = "event_name";
    private static final String COLUMN_LOCATION = "event_location";
    private static final String COLUMN_TYPE = "event_type";
    private static final String COLUMN_NAME_PRIORITY = "name_priority";
    private static final String COLUMN_LOCATION_PRIORITY = "location_priority";
    private static final String COLUMN_TYPE_PRIORITY = "type_priority";

    public PriorityEventDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_EVENTS + " ("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_NAME + " TEXT, "
                + COLUMN_LOCATION + " TEXT, "
                + COLUMN_TYPE + " TEXT, "
                + COLUMN_NAME_PRIORITY + " INTEGER, "
                + COLUMN_LOCATION_PRIORITY + " INTEGER, "
                + COLUMN_TYPE_PRIORITY + " INTEGER)";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EVENTS);
        onCreate(db);
    }

    public void saveEvent(String name, String location, String type) {
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_EVENTS, null);

        boolean found = false;
        while (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID));
            String existingName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME));
            String existingLocation = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LOCATION));
            String existingType = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TYPE));
            int namePriority = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_NAME_PRIORITY));
            int locationPriority = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_LOCATION_PRIORITY));
            int typePriority = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_TYPE_PRIORITY));

            boolean updated = false;
            ContentValues values = new ContentValues();

            if (existingName.equals(name)) {
                values.put(COLUMN_NAME_PRIORITY, namePriority + 1);
                updated = true;
            }

            if (existingLocation.equals(location)) {
                values.put(COLUMN_LOCATION_PRIORITY, locationPriority + 1);
                updated = true;
            }

            if (existingType.equals(type)) {
                values.put(COLUMN_TYPE_PRIORITY, typePriority + 1);
                updated = true;
            }

            if (updated) {
                db.update(TABLE_EVENTS, values, COLUMN_ID + "=?", new String[]{String.valueOf(id)});
                found = true;
            }
        }

        cursor.close();

        if (!found) {
            ContentValues values = new ContentValues();
            values.put(COLUMN_NAME, name);
            values.put(COLUMN_LOCATION, location);
            values.put(COLUMN_TYPE, type);
            values.put(COLUMN_NAME_PRIORITY, 1);
            values.put(COLUMN_LOCATION_PRIORITY, 1);
            values.put(COLUMN_TYPE_PRIORITY, 1);
            db.insert(TABLE_EVENTS, null, values);
        }
    }

    public Cursor getEventsSortedByPriority() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery(
            "SELECT * FROM " + TABLE_EVENTS +
            " ORDER BY (" + COLUMN_NAME_PRIORITY + " + " + COLUMN_LOCATION_PRIORITY + " + " + COLUMN_TYPE_PRIORITY + ") DESC",
            null
        );
    }
}
