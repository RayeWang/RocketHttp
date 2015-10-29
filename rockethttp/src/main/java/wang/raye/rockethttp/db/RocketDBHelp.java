package wang.raye.rockethttp.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * 数据库相关操作
 * Created by Raye on 2015/10/29.
 */
public class RocketDBHelp extends SQLiteOpenHelper {
    private static final String DBANEM = "rocket.db";
    private static final int VERSION = 1;
    public RocketDBHelp(Context context) {
        super(context, DBANEM, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table if not exists down(url varchar(500),length varchar(20))");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onCreate(db);
    }

    /**
     * 获取当前url的记录位置
     * @param url
     * @return
     */
    public long getDownLength(String url){
        SQLiteDatabase db = getReadableDatabase();
        long length = 0;
        Cursor cursor = db.rawQuery("select length from down where url=?",new String[]{url});

        if(cursor.moveToNext()){
            length = cursor.getLong(0);
        }
        cursor.close();
        db.close();
        return length;
    }

    /**
     * 删除当前URL的记录位置
     * @param url
     */
    public void deleteLog(String url){
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("delete from down where url=?", new String[]{url});
        db.close();
    }

    /**
     * 更新记录的位置
     * @param url
     * @param length
     */
    public void update(String url,long length){
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("update down set length=? where url=?",new Object[]{length,url});
        db.close();
    }

    /**
     * 插入一条新的记录
     * @param url
     */
    public void insert(String url){
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("delete from down where url=?", new String[]{url});
        db.execSQL("insert into down(url,length) values(?,0)",new String[]{url});
        db.close();
    }
}
