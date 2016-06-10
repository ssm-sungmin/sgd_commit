package com.example.example1;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class DBManager extends SQLiteOpenHelper {
	 
    public DBManager(Context context, String name, CursorFactory factory, int version) {
        super(context, name, factory, version);
    }
 
    @Override
    public void onCreate(SQLiteDatabase db) {
    	// 새로운 테이블을 생성한다.
        // create table 테이블명 (컬럼명 타입 옵션);
        db.execSQL("CREATE TABLE number_list( _id INTEGER PRIMARY KEY AUTOINCREMENT, number1 INTEGER, number2 INTEGER, number3 INTEGER, number4 INTEGER, number5 INTEGER"
        		+ ", number6 INTEGER, number7 INTEGER, number8 INTEGER, number9 INTEGER, number10 INTEGER);");
    }
 
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
 
    /* 하나로 통일가능 */
    public void insert(String _query) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(_query);
        db.close();     
    }
     
    public void update(String _query) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(_query);
        db.close();     
    }
     
    public void delete(String _query) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(_query);
        db.close();     
    }
     
    public String PrintData() {
        SQLiteDatabase db = getReadableDatabase();
        String str = "";
         
        Cursor cursor = db.rawQuery("select * from number_list", null);
        while(cursor.moveToNext()) {
            str += cursor.getInt(0)
                + "number1:"
                + cursor.getString(1)
                + ",number2:"
                + cursor.getInt(2)
                + ",number3:"
                + cursor.getInt(3)
                + ",number4:"
                + cursor.getInt(4)
                + ",number5:"
                + cursor.getInt(5)
                + ",number6:"
                + cursor.getInt(6)
                + ",number7:"
                + cursor.getInt(7)
                + ",number8:"
                + cursor.getInt(8)
                + ",number9:"
                + cursor.getInt(9)
                + ",number10:"
                + cursor.getInt(10)
                + ",\n";
        }
        return str;
    }
}