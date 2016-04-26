package com.nairan.batman_launcher.utils;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.nairan.batman_launcher.Batman_Launcher;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by nrzhang on 27/8/2015.
 */
public class DBOpenHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION   = 1;

    public DBOpenHelper(Context ctx) {
        super(ctx, Batman_Launcher.DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String cmd = "CREATE TABLE apps(" +
                "appId INTEGER PRIMARY KEY autoincrement, " +
                "appName varchar(200), " +
                "appGroup varchar(100))";

        db.execSQL(cmd);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub
        db.execSQL("drop table TABLE if exists");
        onCreate(db);
    }
}