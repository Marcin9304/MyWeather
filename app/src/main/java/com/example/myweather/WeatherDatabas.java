package com.example.myweather;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class WeatherDatabase extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "weather.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_NAME = "messages";
    private static final String COL_MIN = "min_temp";
    private static final String COL_MAX = "max_temp";
    private static final String COL_MSG = "message";

    public WeatherDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Tworzenie tabeli
        String createTable = "CREATE TABLE " + TABLE_NAME + " (ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_MIN + " REAL, " + COL_MAX + " REAL, " + COL_MSG + " TEXT)";
        db.execSQL(createTable);

        // Dodanie domyślnych komunikatów do bazy
        insertData(db, -50, 10, "Zimno! Baza radzi: Ubierz kurtkę.");
        insertData(db, 10, 20, "Chłodno! Baza radzi: Weź bluzę.");
        insertData(db, 20, 50, "Ciepło! Baza radzi: Pij wodę, kup se wiatrak.");
    }

    private void insertData(SQLiteDatabase db, double min, double max, String msg) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_MIN, min);
        contentValues.put(COL_MAX, max);
        contentValues.put(COL_MSG, msg);
        db.insert(TABLE_NAME, null, contentValues);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    // Metoda do wyciągania wiadomości na podstawie temperatury
    public String getMessageForTemp(double temp) {
        SQLiteDatabase db = this.getReadableDatabase();
        // Zapytanie SQL: Wybierz wiadomość gdzie temperatura mieści się w zakresie
        Cursor cursor = db.rawQuery("SELECT " + COL_MSG + " FROM " + TABLE_NAME +
                " WHERE ? >= " + COL_MIN + " AND ? < " + COL_MAX, new String[]{String.valueOf(temp), String.valueOf(temp)});

        if (cursor.moveToFirst()) {
            String msg = cursor.getString(0);
            cursor.close();
            return msg;
        } else {
            cursor.close();
            return "Brak danych w bazie dla tej temperatury.";
        }
    }
}