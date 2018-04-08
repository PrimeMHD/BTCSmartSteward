package mhd.com.btcsmartsteward.DatabaseTool;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Mi HD on 2017/12/20.
 */

public class KLDatabaseHelper extends SQLiteOpenHelper {
    //建立一张表，存储确定的k线（历史K）

    private Context mContext;
    private static SQLiteDatabase sqdb;

    public KLDatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        this.mContext = context;
    }

    public KLDatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version, DatabaseErrorHandler errorHandler) {
        super(context, name, factory, version, errorHandler);
        this.mContext = context;
    }



    public void createTable(String table_name){
        String CREATE_TABLE_CMD="create table if not exists "+table_name
                +" (TBID integer primary key autoincrement,"
                +"id integer," //K线ID
                +"amount VARCHAR(60),"
                +"open VARCHAR(60),"
                +"close VARCHAR(60),"
                +"low VARCHAR(60),"
                +"high VARCHAR(60),"
                +"vol VARCHAR(60),"
                +"count integer)";
        if(sqdb!=null)
        sqdb.execSQL(CREATE_TABLE_CMD);
    }

    @Override
    public void onCreate(SQLiteDatabase db){
        sqdb=db;

    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
        //TODO
    }
}
