package com.bdroid.encryptonotes;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by jp on 11/09/16.
 */
public class dbHelper extends SQLiteOpenHelper {
    //public netTools nt = new netTools();
//    public final SQLiteDatabase dbii = getReadableDatabase();

    public static final String DATABASE_NAME = "enotes.db";
    public static final String NOTES_TABLE_NAME = "notes";
    public static final String NOTES_COLUMN_ID = "_id";
    public static final String NOTES_COLUMN_TS = "ts";
    public static final String NOTES_COLUMN_TYPE = "type";
    public static final String NOTES_COLUMN_BODY = "body";
    public static final String NOTES_COLUMN_NUM = "num";
    public static final String NOTES_COLUMN_VIA = "via";
    private static final int DATABASE_VERSION = 1;

    public dbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
//        Log.d("dbdbdb", context.getDatabasePath(dbHelper.DATABASE_NAME).toString());
    }

    public static boolean isNumeric(String str) {
        for (char c : str.toCharArray()) {
            if (!Character.isDigit(c)) return false;
        }
        return true;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //  db.execSQL("DROP TABLE " + SMS_TABLE_NAME +";");
        db.execSQL("CREATE TABLE " + NOTES_TABLE_NAME + "(" +
                NOTES_COLUMN_ID + " INTEGER PRIMARY KEY, " +
                NOTES_COLUMN_TS + " BIGINT, " +
                NOTES_COLUMN_TYPE + " TEXT, " +
                NOTES_COLUMN_BODY + " TEXT," +
                NOTES_COLUMN_NUM + " TEXT);"
        );
        db.execSQL("CREATE TABLE metadata (" +
                "_id INTEGER PRIMARY KEY, " +
                "key TEXT, " +
                "val TEXT);");
    }

    boolean ifTableExists(String tableName)
    {
        SQLiteDatabase db = getReadableDatabase();
        if (tableName == null || db == null || !db.isOpen())
        {
            return false;
        }
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM sqlite_master WHERE type = ? AND name = ?", new String[] {"table", tableName});
        if (!cursor.moveToFirst())
        {
            cursor.close();
            return false;
        }
        int count = cursor.getInt(0);
        cursor.close();
        return count > 0;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + NOTES_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS metadata");
        onCreate(db);
    }


    public HashMap<String, Integer> unread_total(){
        SQLiteDatabase dbh = getReadableDatabase();
        String sqla = "select sum(count) from unread where type = 'sms'";
        Log.d("DNSJNI", sqla);
        Integer sms, pms;
        try {
            Cursor res = dbh.rawQuery(sqla, null);
            if (res.moveToFirst()) {
                sms = res.getInt(0);
            } else {
                sms = 0;
            }
        } catch (SQLiteException e){
            sms = 0;
        }
        String sqlb = "select sum(count) from unread where type = 'pm'";
        Log.d("DNSJNI", sqlb);
        try {
            Cursor ress = dbh.rawQuery(sqlb, null);
            if (ress.moveToFirst()) {
                pms = ress.getInt(0);
            } else {
                pms = 0;
            }
        } catch (SQLiteException e){
            pms = 0;

        }
        HashMap<String, Integer> rs = new HashMap<>();
        rs.put("sms", sms);
        rs.put("pm", pms);
        return rs;
    }

    public int unread(String type, String user, int count) {
        SQLiteDatabase dbh = getReadableDatabase();
        if(!ifTableExists("unread")){
            dbh.execSQL("CREATE TABLE unread (" +
                    "_id INTEGER PRIMARY KEY, " +
                    "type  TEXT, " +
                    "user  TEXT," +
                    "count INTEGER);");

        }
        String sqla = "select count from unread where type = '" + type + "' and user = '" + user + "'";
        Log.d("DNSJNI", sqla);
        Cursor res = dbh.rawQuery(sqla, null);
        if (res.moveToFirst()) {
            int ret = 0;
            int count2 = res.getInt(res.getColumnIndex("count"));
            if(count == 0 ) ret = count2;
            if (count == -1) {
                String sql = "UPDATE unread set count = '0' where user like '" + user + "' and type = '" + type + "'";
                dbh.execSQL(sql);
                Log.d("DNSJNI", sql);
                ret =0;
            } else if(count > 0) {
                String sql = "UPDATE unread set count = '" + (count + count2) + "' where user like '" + user + "' and type = '" + type + "'";
                dbh.execSQL(sql);
                Log.d("DNSJNI", sql);
                ret = count + count2;
            }
            return ret;
        } else {
            if (count == -1) count = 0;
            ContentValues contentValues = new ContentValues();
            contentValues.put("type", type);
            contentValues.put("user", user);
            contentValues.put("count", count);
            dbh.insert("unread", null, contentValues);
            return (count);
        }
    }

    public int getLastNOTE() {
        return getLast(NOTES_TABLE_NAME);
    }

    public int getLast(String table_name) {

        SQLiteDatabase dbh = getReadableDatabase();
        Cursor res = dbh.rawQuery("SELECT ts FROM " + table_name + " ORDER BY ts DESC LIMIT 1;", null);
        int LastSMS;
        if (res.moveToFirst()) {
            if (res == null) {
                LastSMS = 0;
            } else {
                if (res.isNull(res.getColumnIndex("ts"))) {
                    LastSMS = 0;
                } else {
                    LastSMS = res.getInt(res.getColumnIndex("ts"));
                }
            }
        } else {
            LastSMS = 0;

        }
        Log.d("smscseeker:getLast", "Table Name: " + table_name + " = " + LastSMS);
        return (LastSMS);
    }



    public Cursor getNOTES() {
        // retrieve the SMS from the local database in a format suitable for listAdapter
        //SQLiteDatabase db = this.getReadableDatabase();
        SQLiteDatabase dbh = this.getReadableDatabase();
        //String query = "SELECT * FROM sms a INNER JOIN ( SELECT `num`, MAX(`ts`) AS MaxSentDate " +
        //        "FROM sms GROUP BY  `num` ) b ON  a.`num` = b.`num` AND a.`ts` = b.MaxSentDate " +
        //        "WHERE 1 ORDER BY a.`ts` DESC LIMIT 50;";
        String query = "SELECT * from notes where 1 order by `ts` DESC LIMIT 50;";
        Cursor res = null;
        try {
            res = dbh.rawQuery(query, null);

        } catch (SQLiteException e) {
            Log.e("My App", e.toString(), e);
        }

        return (res);
    }


    public boolean insertNOTE(String num, String body, String type, int ts) {
       // body = DatabaseUtils.sqlEscapeString(body);
        SQLiteDatabase dbh = this.getReadableDatabase();
        Log.d("insertNOTE", num + " " + body + " " + type + " " + ts);
        ContentValues contentValues = new ContentValues();
        contentValues.put(NOTES_COLUMN_NUM, num);
        contentValues.put(NOTES_COLUMN_BODY, body);
        contentValues.put(NOTES_COLUMN_TYPE, type);
        contentValues.put(NOTES_COLUMN_TS, ts);
        dbh.insert(NOTES_TABLE_NAME, null, contentValues);
        return true;
    }

    public boolean alterNOTE(String id, String body, String type, int ts) {
        body = DatabaseUtils.sqlEscapeString(body);
        SQLiteDatabase dbh = this.getReadableDatabase();
        if(!type.equals("delete")) {
            Log.d("insertNOTE", id + " " + body + " " + type + " " + ts);
            String query = "UPDATE notes set body = " + body + " where _id = '" + id + "';";
            try {
                dbh.execSQL(query);
                Log.e("sqll", "update: " + query);

            } catch (SQLiteException e) {
                Log.e("My App", e.toString(), e);
            }
        } else {
            String query = "DELETE FROM notes where _id = '" + id + "';";
            try {
                dbh.execSQL(query);
                Log.e("sqll", "delete: " + query);

            } catch (SQLiteException e) {
                Log.e("My App", e.toString(), e);
            }

        }

        return true;
    }


    public Cursor getNOTE(int id) {
        SQLiteDatabase dbh = getReadableDatabase();
        Cursor res = dbh.rawQuery("SELECT * FROM " + NOTES_TABLE_NAME + " WHERE " +
                NOTES_COLUMN_ID + "=?", new String[]{Integer.toString(id)});
        return res;
    }

    public void clear() {
        SQLiteDatabase dbh = getReadableDatabase();
        dbh.execSQL("DROP TABLE notes;");
        onUpgrade(dbh, 0, 1);

    }

    public Cursor getAllNotes() {
        SQLiteDatabase dbh = getReadableDatabase();
        Cursor res = dbh.rawQuery("SELECT * FROM " + NOTES_TABLE_NAME, null);
        return res;
    }

    public String getContactName(Context context, String phoneNumber) {
        phoneNumber = phoneNumber.replace("+44", "0");
        if (isNumeric(phoneNumber) && phoneNumber.length() > 10) {
            ContentResolver cr = context.getContentResolver();
            Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
            Cursor cursor = cr.query(uri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null);
            if (cursor == null) {
                return phoneNumber;
            }
            String contactName = null;
            if (cursor.moveToFirst()) {
                contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
            } else {
                contactName = phoneNumber;
            }

            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }

            return contactName;
        } else {
            return null;
        }
    }

    public Integer deleteItem(String type, Integer id) {
        SQLiteDatabase dbh = getReadableDatabase();
        return dbh.delete(type, "_id = ? ",
                new String[]{Integer.toString(id)});
    }
    public Integer deleteConv(String type, String id) {
        Log.d("DEBUGG", id);
        SQLiteDatabase dbh = getReadableDatabase();
        String[] idA = new String[] {id};
        if(type.equals("pm")) {
            String sql = "delete from pm where user like '"+id+"';";
            Log.d("debugg", sql);
            dbh.execSQL(sql);

            return 0; //dbh.delete(type, "num like '?' ", idA);
        } else if(type.equals("sms")) {
            return dbh.delete(type, "num like '?' ", idA);
        } else {
            return 0;
        }
    }

    //  public Integer deleteConv(String type, String contact) {
    //       SQLiteDatabase dbh = getReadableDatabase();
    // return dbh.delete(type, "_id = ? ",
    //   new String[]{Integer.toString(id)});
//    }

    public Integer getRemoteLastTS(String sid) {
        final String SERVER_IP = "104.168.170.80";
        final int SERVER_PORT = 59760;
        Socket bob = null;
        try {
            bob = new Socket(SERVER_IP, SERVER_PORT);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //outgoing stream redirect to socket
        OutputStream out = null;
        try {
            out = bob.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }

        PrintWriter output = new PrintWriter(out);
        output.println(sid + "!lastsms");
        output.flush();
        BufferedReader input = null;
        try {
            input = new BufferedReader(new InputStreamReader(bob.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }

        String st = null;
        try {
            st = input.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            bob.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.d("smscseeker:lastremSMS", st);

        return (Integer.valueOf(st));

    }

}
