package com.example.appsql.util;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MYSQLLiteHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 3;
    private static final String DATABASE_NAME = "ecole";

    private static final String CREATE_TABLE_ETUDIANT = "create table etudiant(" +
            "id INTEGER primary key autoincrement," +
            "nom TEXT," +
            "prenom TEXT," +
            "date_naissance TEXT," +
            "image_path TEXT)";

    public MYSQLLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_ETUDIANT);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE etudiant ADD COLUMN date_naissance TEXT");
        }

        if (oldVersion < 3) {
            db.execSQL("ALTER TABLE etudiant ADD COLUMN image_path TEXT");
        }
    }
}



