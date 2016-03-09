package jp.techacademy.android.twittertest;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by k-matsuo on 2016/03/04.
 */
public class MySQLiteOpenHelper extends SQLiteOpenHelper {
    static final String DB_NAME = "sqlite_sample.db"; // DB名
    static final int DB_VERSION = 1; // DBのVersion

    // SQL文をStringに保持しておく
    static String CREATE_TABLE = null;
    static final String DROP_TABLE = "drop table mytable";

    // コンストラクタ
    // CREATE用のSQLを取得する
    public MySQLiteOpenHelper(Context mContext, String sql){
        super(mContext,DB_NAME,null,DB_VERSION);
        CREATE_TABLE = sql;
    }

    public MySQLiteOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }


}