package com.example.myweather;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class WeatherDatabase extends SQLiteOpenHelper {

    // Nazwa bazy i wersja
    private static final String DATABASE_NAME = "weather.db";
    private static final int DATABASE_VERSION = 2; // Wersja 2 (z historią)

    public WeatherDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Tabela 1: Porady
        String createMessages = "CREATE TABLE messages (ID INTEGER PRIMARY KEY AUTOINCREMENT, min_temp REAL, max_temp REAL, message TEXT)";
        db.execSQL(createMessages);

        // Dodanie danych początkowych
        insertData(db, -50, 10, "Zimno! Baza radzi: Ubierz kurtkę.");
        insertData(db, 10, 20, "Chłodno! Baza radzi: Weź bluzę.");
        insertData(db, 20, 50, "Ciepło! Baza radzi: Pij wodę.");

        // Tabela 2: Historia
        String createHistory = "CREATE TABLE history (ID INTEGER PRIMARY KEY AUTOINCREMENT, temp REAL, time TEXT)";
        db.execSQL(createHistory);
    }

    private void insertData(SQLiteDatabase db, double min, double max, String msg) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("min_temp", min);
        contentValues.put("max_temp", max);
        contentValues.put("message", msg);
        db.insert("messages", null, contentValues);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS messages");
        db.execSQL("DROP TABLE IF EXISTS history");
        onCreate(db);
    }

    // --- METODY DO ODCZYTU I ZAPISU ---

    public String getMessageForTemp(double temp) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT message FROM messages WHERE ? >= min_temp AND ? < max_temp", new String[]{String.valueOf(temp), String.valueOf(temp)});

        if (cursor.moveToFirst()) {
            String msg = cursor.getString(0);
            cursor.close();
            return msg;
        }
        cursor.close();
        return "Brak porady w bazie.";
    }

    public void addHistory(double temp) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("temp", temp);
        String time = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
        values.put("time", time);
        db.insert("history", null, values);
    }

    public String getLastReadings() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT time, temp FROM history ORDER BY ID DESC LIMIT 3", null);

        StringBuilder result = new StringBuilder();
        while (cursor.moveToNext()) {
            String time = cursor.getString(0);
            double temp = cursor.getDouble(1);
            result.append(time).append(" -> ").append(temp).append("°C\n");
        }
        cursor.close();

        if (result.length() == 0) return "Brak historii.";
        return result.toString();
    }
}