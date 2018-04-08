package mhd.com.btcsmartsteward.DatabaseTool;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Mi HD on 2017/12/18.
 */

public class TradeDetailDatabaseHelper extends SQLiteOpenHelper {
    //建立一张表，存储每一笔交易
    final String CREATE_TABLE_CMD="create table if not exists bit_trade("
            +"TBID integer primary key autoincrement,"
            +"id VARCHAR(25),"
            +"price VARCHAR(30),"
            +"amount VARCHAR(60),"
            +"direction VARCHAR(25),"
            +"ts integer)";//单位是minute
    private static SQLiteDatabase sqdb;


    private Context mContext;
    public TradeDetailDatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version){
        super(context,name,factory,version);
        mContext=context;
    }

    public void createTable(){
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
