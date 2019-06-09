package com.example.myfeed;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBInterface {
    public static final String _ID = "_id";
    public static final String TITLE = "title";
    public static final String DESCRIPTION = "description";
    public static final String LINK = "link";
    public static final String AUTHOR = "author";
    public static final String DATE = "date";
    public static final String IMAGE = "image";

    public static final String DB_NAME = "myfeeddb";
    public static final String TABLE_NAME = "news";

    public static final String TAG = "DBInterface";
    public static final int VERSION = 1;

    public static final String DB_CREATE =
            "create table " + TABLE_NAME + "( " + _ID + " integer primary key autoincrement, " +
                    TITLE + ", " + DESCRIPTION + ", " + LINK + ", " + AUTHOR + ", " + DATE +
                    ", " + IMAGE + ");";

    private final Context context;
    private DBHelper dbHelper;
    private SQLiteDatabase db;

    public DBInterface(Context con){
        this.context = con;
        dbHelper = new DBHelper(context);
    }

    //Open the DB

    public DBInterface open() throws SQLException {
        db = dbHelper.getWritableDatabase();
        return this;
    }

//Close the DB

    public void close() {
        dbHelper.close();
    }


    public long insert(String title, String description, String link, String author, String date, String image){
        ContentValues initialValues = new ContentValues();
        initialValues.put(TITLE, title);
        initialValues.put(DESCRIPTION, description);
        initialValues.put(LINK, link);
        initialValues.put(AUTHOR, author);
        initialValues.put(DATE, date);
        initialValues.put(IMAGE, image);

        return db.insert(TABLE_NAME ,null, initialValues);
    }

    public Cursor getAll(){
        return db.query(TABLE_NAME, new String[] {TITLE, DESCRIPTION, LINK, AUTHOR, DATE, IMAGE}, null, null, null, null, null);
    }

    //Drop the table and recreate
    public void dropAndRecreateTable(){
        open();
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        db.execSQL(DB_CREATE);
        close();
    }
    private static class DBHelper extends SQLiteOpenHelper {
        DBHelper(Context con) {
            super(con, DB_NAME, null, VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            try {
                db.execSQL(DB_CREATE);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int OldVersion, int NewVersion) {
            Log.w(TAG, "Updating database version" + OldVersion + " with " + NewVersion + ". It will delete all the existing data");
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);

            onCreate(db);
        }
    }
}
