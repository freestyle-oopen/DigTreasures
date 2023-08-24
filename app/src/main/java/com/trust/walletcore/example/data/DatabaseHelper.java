package com.trust.walletcore.example.data;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.math.BigDecimal;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "my_database";
    private static final String TABLE_NAME = "eth_wallet";
    private static final String TABLE_NAME_FAIL_BLOCK = "fail_block";
    private static final String ADDRESS = "address";
    private static final String BLOCK_NUMBER = "block_number";
    private static final int DATABASE_VERSION = 1;
    public static final String INSERT_ERROR = "insert_error";
    SQLiteDatabase db;
    public DatabaseHelper(Context context) {
        super(context, context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) + File.separator + DATABASE_NAME, null, DATABASE_VERSION);
        db = this.getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTableQuery = "CREATE TABLE " + TABLE_NAME + "(" +
                ADDRESS + " TEXT PRIMARY KEY, " +
                BLOCK_NUMBER + " integer)";
        String createTable = "CREATE TABLE " + TABLE_NAME_FAIL_BLOCK + "(" + BLOCK_NUMBER + " integer PRIMARY KEY)";
        db.execSQL(createTableQuery);
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
    public void insertDatas(String datas){
        String sql = "insert OR IGNORE into "+TABLE_NAME+"("+ADDRESS+","+BLOCK_NUMBER+") values"+datas;
        db.execSQL(sql);
    }
    public boolean insertFailBlock(int block_number) {
        ContentValues values = new ContentValues();
        values.put(BLOCK_NUMBER, block_number);
        long insert = db.insertWithOnConflict(TABLE_NAME_FAIL_BLOCK, null, values,SQLiteDatabase.CONFLICT_REPLACE);
        return insert>0;
    }
    public int selectCurrentBlock(){
        String sql = "select distinct "+ BLOCK_NUMBER +" from "+ TABLE_NAME +" ORDER BY "+ BLOCK_NUMBER +" DESC limit 39,40";
        Cursor cursor = db.rawQuery(sql, null);
        int block = 0;
        while (cursor.moveToNext()) {
            block = cursor.getInt(cursor.getColumnIndex(BLOCK_NUMBER));
            if(block != 0){
                break;
            }
        }
        cursor.close();
        return block;
    }
    public void deleteData(int id) {
        String whereClause = ADDRESS + "=?";
        String[] whereArgs = {String.valueOf(id)};
        db.delete(TABLE_NAME, whereClause, whereArgs);
    }

    public void updateData(int id, String newName) {
        ContentValues values = new ContentValues();
        values.put(BLOCK_NUMBER, newName);
        String whereClause = ADDRESS + "=?";
        String[] whereArgs = {String.valueOf(id)};
        db.update(TABLE_NAME, values, whereClause, whereArgs);
    }

    public Cursor getAllData() {
        String query = "SELECT * FROM " + TABLE_NAME;
        return db.rawQuery(query, null);
    }

    public int getDataCount() {
        String query = "SELECT count(*) FROM " + TABLE_NAME;
        Cursor cursor = db.rawQuery(query, null);
        if(cursor!=null&&cursor.moveToFirst()){
            int anInt = cursor.getInt(0);
            cursor.close();
            return anInt;
        }
        return 0;
    }
}
