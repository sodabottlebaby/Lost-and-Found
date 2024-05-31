package com.example.lostfound;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "LostFoundDatabase";
    private static final int DATABASE_VERSION = 2;
    private static final String TABLE_ITEMS = "adverts";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_TYPE = "advertType";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_PHONE = "phone";
    private static final String COLUMN_DESCRIPTION = "description";
    private static final String COLUMN_DATE = "date";
    private static final String COLUMN_LOCATION = "location";
    private static final String COLUMN_LATITUDE = "latitude";
    private static final String COLUMN_LONGITUDE = "longitude";

    // SQL statement to create a new table
    private static final String TABLE_CREATE =
            "CREATE TABLE " + TABLE_ITEMS + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_TYPE + " TEXT, " +
                    COLUMN_NAME + " TEXT, " +
                    COLUMN_PHONE + " TEXT, " +
                    COLUMN_DESCRIPTION + " TEXT, " +
                    COLUMN_DATE + " TEXT, " +
                    COLUMN_LOCATION + " TEXT, " +
                    COLUMN_LATITUDE + " REAL, " +
                    COLUMN_LONGITUDE + " REAL" + ")";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ITEMS);
        onCreate(db);
    }
    public long addAdvert(String advert_type, String name, String phone, String description, String date, String location, double latitude, double longitude) {
        long newRowId = -1;
        try (SQLiteDatabase db = this.getWritableDatabase()) {
            ContentValues values = new ContentValues();
            values.put(COLUMN_TYPE, advert_type);
            values.put(COLUMN_NAME, name);
            values.put(COLUMN_PHONE, phone);
            values.put(COLUMN_DESCRIPTION, description);
            values.put(COLUMN_DATE, date);
            values.put(COLUMN_LOCATION, location);
            values.put(COLUMN_LATITUDE, latitude);
            values.put(COLUMN_LONGITUDE, longitude);

            newRowId = db.insert(TABLE_ITEMS, null, values);
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error adding item", e);
        }
        return newRowId;
    }


    public List<ItemsPreview> getAllItems() {
        List<ItemsPreview> itemsList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_ITEMS, null);

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID));
                String advertType = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TYPE));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME));
                double latitude = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_LATITUDE));
                double longitude = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_LONGITUDE));
                String location = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LOCATION));
                ItemsPreview item = new ItemsPreview(id, advertType, name, latitude, longitude, location);
                itemsList.add(item);
            } while (cursor.moveToNext());
        } else {
            Log.e("DatabaseHelper", "No data found in database.");
        }

        cursor.close();
        db.close();
        return itemsList;
    }


    public LostFoundItems getItemById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + TABLE_ITEMS + " WHERE " + COLUMN_ID + " = ?",
                new String[]{String.valueOf(id)}
        );

        LostFoundItems item = null;
        if (cursor.moveToFirst()) {
            item = new LostFoundItems(
                    cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TYPE)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PHONE)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LOCATION)),
                    cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_LATITUDE)),
                    cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_LONGITUDE))
            );
        }
        cursor.close();
        db.close();
        return item;
    }

    public List<LostFoundItems> getAllItemsWithLocation() {
        List<LostFoundItems> itemsList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_ITEMS, null);

        if (cursor.moveToFirst()) {
            do {
                LostFoundItems item = new LostFoundItems(
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TYPE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PHONE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LOCATION)),
                        cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_LATITUDE)),
                        cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_LONGITUDE))
                );
                itemsList.add(item);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return itemsList;
    }



    public void deleteItem(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_ITEMS, COLUMN_ID + " = ?",
                new String[]{String.valueOf(id)});
        db.close();
    }



}

