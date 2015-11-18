package sa.com.is.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import static sa.com.is.db.SecureClientDBHelper.*;
/**
 * Created by snouto on 18/11/15.
 */
public class TrusteeManager {

    private SecureClientDBHelper dbHelper;


    public TrusteeManager(Context context)
    {
        dbHelper = new SecureClientDBHelper(context,SecureClientDBHelper.TABLE_NAME,null,SecureClientDBHelper.DB_VERSION);
    }

    public boolean addTrustee(Trustee trustee)
    {
        //now we are going to insert that trustee
        ContentValues values = new ContentValues();
        values.put(ID,trustee.getID());
        values.put(EMAIL_ADDRESS,trustee.getEmailAddress());
        values.put(EMAIL_CERTIFICATE,trustee.getEmailCertificate());
        //now get a writable Database to insert the trustee
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long affectedRow = db.insert(TABLE_NAME,null,values);
        db.close();

        return (affectedRow > 0) ? true :false;
    }


    public boolean deleteTrustee(String Id){

        String whereClause = ID + " = '" + Id + "'";
        String[] whereArgs = null;
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rows = db.delete(TABLE_NAME, whereClause, whereArgs);
        db.close();
        return (rows > 0 ) ? true : false;
    }


    public boolean deleteTrusteeByEmail(String email){

        String whereClause = EMAIL_ADDRESS + " = '" + email + "'";
        String[] whereArgs = null;
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rows = db.delete(TABLE_NAME,whereClause,whereArgs);
        db.close();
        return (rows > 0 ) ? true : false;
    }


    public Trustee getTrustee(String email){

        String whereClause = EMAIL_ADDRESS + " = '" + email + "'";
        String[] whereArgs = null;
        String groupBy = null;
        String having = null;
        String order = null;
        String[] selectableFields = dbHelper.getAllColumns();
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor data = db.query(TABLE_NAME, selectableFields, whereClause, whereArgs, groupBy, having, order);
        Trustee person = new Trustee();
        while(data.moveToNext()){
            //get the next element
            int columnIndex = data.getColumnIndex(ID);
            person.setID(data.getString(columnIndex));
            columnIndex = data.getColumnIndex(EMAIL_ADDRESS);
            person.setEmailAddress(data.getString(columnIndex));
            columnIndex = data.getColumnIndex(EMAIL_CERTIFICATE);
            person.setEmailCertificate(data.getString(columnIndex));
            //now add the trustee into the trustees
        }
        //close the writable database
        db.close();

        return person;


    }
}
