package com.ravensltd.ravens.sqliteDatabase;

import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;

/**
 * Created by jatin on 9/10/17.
 */

public class DatabaseHelper extends SQLiteOpenHelper{

    // Database Name
    private static final String DATABASE_NAME = "RavensLocal";

    // Table Names
    private static final String TABLE_ProfileInfo = "ProfileInfo";

    private static  final String CREATE_TABLE_PROFILE_INFO=
            "create table "+TABLE_ProfileInfo+"(UserName text,Status text,Phone text,Uid text)";//,ProfilePhoto blog

    public DatabaseHelper(Context context) {
        super(context,DATABASE_NAME,null,1);
    }

    public DatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version, DatabaseErrorHandler errorHandler) {
        super(context, name, factory, version, errorHandler);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_PROFILE_INFO);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void exequteQuery(String sql){
        SQLiteDatabase db=getWritableDatabase();
        db.execSQL(sql);
    }

    public void dropTable(){

    }
//, byte[] profilePhotoID
    public void insertProfileInfo(String userName, String status, String phone, String uId){
        SQLiteDatabase db=getWritableDatabase();

        String sql="insert into "+TABLE_ProfileInfo+"values(?,?,?,?)";

        SQLiteStatement sqLiteStatement=db.compileStatement(sql);
        sqLiteStatement.clearBindings();;

        sqLiteStatement.bindString(1,userName);
        sqLiteStatement.bindString(2,status);
        sqLiteStatement.bindString(3,phone);
        sqLiteStatement.bindString(4,uId);
        //sqLiteStatement.bindBlob(5,profilePhotoID);

        sqLiteStatement.executeInsert();

    }

    public Cursor getAllData(){
        SQLiteDatabase db=this.getWritableDatabase();
        Cursor res=db.rawQuery("select * from " + TABLE_ProfileInfo,null);
        return res;
    }

}
