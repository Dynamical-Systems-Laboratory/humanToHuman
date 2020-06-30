package com.polito.humantohuman;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.joda.time.Instant;

import java.util.ArrayList;
import java.util.Date;

public class Database extends SQLiteOpenHelper {

  public static int KEY_OWN_ID = 0;
  public static int KEY_CURRENT_CURSOR = 1;
  public static int KEY_PRIVACY_POLICY = 3;
  public static int KEY_EXPERIMENT_DESCRIPTION = 6;
  public static int KEY_SERVER_BASE_URL = 7;
  public static int KEY_APPSTATE = 8;
  private static Database database;

  Database(@Nullable Context context) {
    super(context, "database", null, 1);
  }

  public static class Row {
    public final long id;
    public final int power, rssi;
    public final Date date;

    public Row(long id, int power, int rssi, Date date) {
      this.id = id;
      this.power = power;
      this.rssi = rssi;
      this.date = date;
    }
  }

  public static void initializeDatabase(Context context) { database  = new Database(context); }

  @Override
  public void onCreate(SQLiteDatabase sqLiteDatabase) {
    sqLiteDatabase.execSQL(
        "CREATE TABLE IF NOT EXISTS metadata (\n"
        + "    key_        INTEGER         PRIMARY KEY,\n"
        + "    tvalue      TEXT            NOT NULL DEFAULT '',\n"
        + "    nvalue      INTEGER         NOT NULL DEFAULT 0\n"
        + ");");
    sqLiteDatabase.execSQL(
        "CREATE TABLE IF NOT EXISTS sensor_data (\n"
        + "    id          INTEGER         PRIMARY KEY AUTOINCREMENT,\n"
        + "    time        INTEGER         NOT NULL,\n"
        + "    source      INTEGER         NOT NULL,\n"
        + "    power       INTEGER         NOT NULL,\n"
        + "    rssi        REAL            NOT NULL,\n"
        + "    dirty       BOOLEAN         NOT NULL\n"
        + ");");
  }

  @Override
  public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {}

  public static Long getPropNumeric(int prop) {
    Cursor queryResult = database.getReadableDatabase().rawQuery("SELECT nvalue FROM metadata WHERE key_ = " + prop, null);
    if (queryResult.moveToNext()) {
      long result = queryResult.getLong(0);
      queryResult.close();
      return result;
    }
    queryResult.close();
    return null;
  }

  public static void setPropNumeric(int prop, long value) {
    SQLiteDatabase db = database.getWritableDatabase();
    try {
      db.execSQL("INSERT INTO metadata (key_, nvalue) VALUES (?, ?)", new Object[] { prop, value });
    } catch (SQLiteConstraintException e) {
      db.execSQL("UPDATE metadata SET nvalue = ? WHERE key_ = ?", new Object[] { value, prop });
    }
  }

  public static String getPropText(int prop) {
    Cursor queryResult = database.getReadableDatabase().rawQuery("SELECT tvalue FROM metadata WHERE key_ = " + prop, null);
    if (queryResult.moveToNext()) {
      String result = queryResult.getString(0);
      queryResult.close();
      return result;
    }
    queryResult.close();
    return null;
  }

  public static void setPropText(int prop, String value) {
    SQLiteDatabase db = database.getWritableDatabase();
    try {
      db.execSQL("INSERT INTO metadata (key_, tvalue) VALUES (?, ?)", new Object[] { prop, value });
    } catch (SQLiteConstraintException e) {
      db.execSQL("UPDATE metadata SET tvalue = ? WHERE key_ = ?", new Object[] { value, prop });
    }
  }

  public static void addRow(long id, int power, int rssi) {
    database.getWritableDatabase().execSQL(
        "INSERT INTO sensor_data (time, source, power, rssi)"
            + "VALUES (strftime('%s','now') || substr(strftime('%f','now'),4), "
            + "?, ?, ?)",
        new Object[] {id, power, rssi});
  }

  public static ArrayList<Row> popRows() {
    SQLiteDatabase db = database.getWritableDatabase();

    Cursor queryResult =
        db.rawQuery("SELECT MAX(id) as max_id FROM sensor_data LIMIT 1", null);
    ArrayList<Row> rows = new ArrayList<>();
    if (!queryResult.moveToNext()) {
      queryResult.close();
      return rows;
    }
    int rowMax = queryResult.getInt(0);
    queryResult.close();

    queryResult =
        db.rawQuery("SELECT time,source,power,rssi FROM sensor_data WHERE id <= " + rowMax, null);
    while (queryResult.moveToNext()) {
      Date time = Instant.ofEpochMilli(queryResult.getLong(0)).toDate();
      long id = queryResult.getLong(1);
      int power = queryResult.getInt(2);
      int rssi = queryResult.getInt(3);
      rows.add(new Row(id, power, rssi, time));
    }
    queryResult.close();

    db.execSQL("DELETE FROM sensor_data WHERE id <= " + rowMax);
    return rows;
  }
}
