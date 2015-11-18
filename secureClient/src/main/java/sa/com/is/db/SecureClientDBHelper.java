package sa.com.is.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by snouto on 18/11/15.
 */
public class SecureClientDBHelper extends SQLiteOpenHelper {

    public static final int DB_VERSION = 1;
    public static final String ID = "ID";
    public static final String EMAIL_ADDRESS = "Email";
    public static final String EMAIL_CERTIFICATE = "Certificate";
    public static final String TABLE_NAME = "Trustees";

    public static final String DB_CREATE = "create table " + TABLE_NAME + " (" +
            ID + " text primary key,"+EMAIL_ADDRESS + " text unique not null,"+EMAIL_CERTIFICATE + " text not null"+
            ")";

    public SecureClientDBHelper(Context context, String name,
                                SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        sqLiteDatabase.execSQL(DB_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

        sqLiteDatabase.execSQL("drop table if exists " + TABLE_NAME);
        onCreate(sqLiteDatabase);
    }


    public String[] getAllColumns(){

        return new String[]{ID,EMAIL_ADDRESS,EMAIL_CERTIFICATE};
    }
}
