package com.polito.humantohuman;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;

public class Database extends SQLiteOpenHelper {

  public static final int OWN_ID_KEY = 0;

  public Database(@Nullable Context context) {
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
        + "    rssi        REAL            NOT NULL\n"
        + ");\n");
  }

  @Override
  public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {}

  public void addRow(long id, int power, int rssi) {
    this.getWritableDatabase().execSQL(
        "INSERT INTO sensor_data (time, source, power, rssi)"
            + "VALUES (strftime('%s','now') || substr(strftime('%f','now'),4), "
            + "?, ?, ?)",
        new Object[] {id, power, rssi});
  }

  public ArrayList<Row> popRows() {
    SQLiteDatabase db = this.getWritableDatabase();

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
      Instant timeInstant = Instant.ofEpochMilli(queryResult.getLong(0));
      Date time = Date.from(timeInstant);
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
