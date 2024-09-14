package com.example.myapplication;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "students.db";
    private static final int DATABASE_VERSION = 2;
    private static final String TABLE_NAME = "students";
    private static final String DELETED_TABLE_NAME = "deleted_students";
    private static final String COL_NAME = "name";
    private static final String COL_EMAIL = "email";
    private static final String COL_GENDER = "gender";
    private static final String COL_COUNTRY = "country";
    private static final String COL_ID = "ID";
    private static final String COL_DELETED_AT = "deleted_at"; // Timestamp for deleted students

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create table for active students
        db.execSQL("CREATE TABLE " + TABLE_NAME + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_NAME + " TEXT, " +
                COL_EMAIL + " TEXT UNIQUE, " +
                COL_GENDER + " TEXT, " +
                COL_COUNTRY + " TEXT)");

        // Create table for deleted students with timestamp
        db.execSQL("CREATE TABLE " + DELETED_TABLE_NAME + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_NAME + " TEXT, " +
                COL_EMAIL + " TEXT UNIQUE, " +
                COL_GENDER + " TEXT, " +
                COL_COUNTRY + " TEXT, " +
                COL_DELETED_AT + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DELETED_TABLE_NAME);
        onCreate(db);
    }

    // Method to insert data into the students table
    public boolean insertData(String name, String email, String gender, String country) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_NAME, name);
        contentValues.put(COL_EMAIL, email);
        contentValues.put(COL_GENDER, gender);
        contentValues.put(COL_COUNTRY, country);
        long result = db.insert(TABLE_NAME, null, contentValues);
        return result != -1;
    }

    // Check if an email already exists in the database
    public boolean isEmailExists(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE " + COL_EMAIL + "=?", new String[]{email});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    // Retrieve all active student data
    public Cursor getAllData() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_NAME, null);
    }

    // Delete student from active table and insert it into deleted_students table
    @SuppressLint("Range")
    public boolean deleteData(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE " + COL_ID + "=?", new String[]{String.valueOf(id)});
        if (cursor.moveToFirst()) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(COL_NAME, cursor.getString(cursor.getColumnIndex(COL_NAME)));
            contentValues.put(COL_EMAIL, cursor.getString(cursor.getColumnIndex(COL_EMAIL)));
            contentValues.put(COL_GENDER, cursor.getString(cursor.getColumnIndex(COL_GENDER)));
            contentValues.put(COL_COUNTRY, cursor.getString(cursor.getColumnIndex(COL_COUNTRY)));
            db.insert(DELETED_TABLE_NAME, null, contentValues);
        }
        cursor.close();
        return db.delete(TABLE_NAME, COL_ID + " = ?", new String[]{String.valueOf(id)}) > 0;
    }

    // Retrieve student data by ID
    public Cursor getStudentData(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE " + COL_ID + "=?", new String[]{String.valueOf(id)});
    }

    // Get all deleted student data
    public Cursor getDeletedData() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + DELETED_TABLE_NAME, null);
    }

    // Restore a student from deleted_students to students
    @SuppressLint("Range")
    public boolean restoreData(int studentId) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + DELETED_TABLE_NAME + " WHERE " + COL_ID + "=?", new String[]{String.valueOf(studentId)});
        if (cursor.moveToFirst()) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(COL_NAME, cursor.getString(cursor.getColumnIndex(COL_NAME)));
            contentValues.put(COL_EMAIL, cursor.getString(cursor.getColumnIndex(COL_EMAIL)));
            contentValues.put(COL_GENDER, cursor.getString(cursor.getColumnIndex(COL_GENDER)));
            contentValues.put(COL_COUNTRY, cursor.getString(cursor.getColumnIndex(COL_COUNTRY)));

            long result = db.insert(TABLE_NAME, null, contentValues);
            if (result != -1) {
                db.delete(DELETED_TABLE_NAME, COL_ID + " = ?", new String[]{String.valueOf(studentId)});
                cursor.close();
                return true;
            }
        }
        cursor.close();
        return false;
    }

    // Permanently delete all data from deleted_students table
    public boolean permanentlyDeleteData() {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(DELETED_TABLE_NAME, null, null) > 0;
    }
}
