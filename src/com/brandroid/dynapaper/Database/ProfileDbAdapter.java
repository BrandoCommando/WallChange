package com.brandroid.dynapaper.Database;

import com.brandroid.dynapaper.GalleryItem;
import com.brandroid.dynapaper.WallProfile;
import com.brandroid.dynapaper.widget.Widget;
import com.brandroid.dynapaper.widget.Widgets;
import com.brandroid.util.Logger;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Matrix;

public class ProfileDbAdapter
{
	public static final String KEY_ID = "_id";
    public static final String KEY_TITLE = "title";
    public static final String KEY_ENABLED = "enabled";
    public static final String KEY_BASE_PATH = "path";
    public static final String KEY_BASE_MATRIX = "matrix";
    public static final String KEY_WIDGETS = "widgets"; // use flat file w/ json
    public static final String KEY_TRIGGERS = "triggers";
    
    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;
    
    private static final String DATABASE_NAME = "profiles.db";
    private static final String TABLE_PROFILE = "profile";
    
    private static final int DATABASE_VERSION = 2;

    private static final String TABLE_CREATE_PROFILE =
        "create table " + TABLE_PROFILE + " (" + KEY_ID + " integer primary key autoincrement, "
        + KEY_TITLE + " text null, "
        + KEY_BASE_PATH + " text not null, "
        + KEY_BASE_MATRIX + " text null, "
        + KEY_WIDGETS + " text null, "
        + KEY_TRIGGERS + " text null, "
        + KEY_ENABLED + " int not null default 1)";
    
    private final Context mCtx;

    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db)
        {
        	Logger.LogVerbose("Creating tables for Profiles");
        	db.execSQL(TABLE_CREATE_PROFILE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Logger.LogVerbose("Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_PROFILE);
            onCreate(db);
        }
    }

    public ProfileDbAdapter(Context ctx)
    {
    	this.mCtx = ctx;
    }
    
    public ProfileDbAdapter open() throws SQLException
    {
    	if(mDb != null && mDb.isOpen()) return this;
    	if(mDbHelper == null)
    		mDbHelper = new DatabaseHelper(mCtx);
    	if(mDb == null && mDbHelper != null)
    		mDb = mDbHelper.getWritableDatabase();
    	else if(mDbHelper == null)
    		throw new SQLException("mDbHelper is null");
    	return this;
    }
    
    public void close() { 
    	mDbHelper.close();
    }
    
    public long createItem(String title, String base, Matrix matrix, Widgets widgets) {
    	open();
    	ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_BASE_PATH, base);
        initialValues.put(KEY_BASE_MATRIX, matrix != null ? matrix.toShortString() : "");
        if(widgets != null && widgets.size() > 0)
        	initialValues.put(KEY_WIDGETS, widgets.toString());

        long ret = mDb.insert(TABLE_PROFILE, null, initialValues);
        return ret;
    }
    public long updateItem(int id, String title, String base, Matrix matrix, Widget[] widgets)
    {
    	ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_BASE_PATH, base);
        initialValues.put(KEY_BASE_MATRIX, matrix != null ? matrix.toShortString() : "");
        if(widgets != null && widgets.length > 0)
        {
        	StringBuilder sbWidgets = new StringBuilder();
        	for(Widget w : widgets)
        	{
        		sbWidgets.append(w.toString());
        		sbWidgets.append(",");
        	}
        	initialValues.put(KEY_WIDGETS, sbWidgets.toString());
        }
        
        long ret = mDb.update(TABLE_PROFILE, initialValues, KEY_ID + "=" + id, null);
    	if(ret == 0)
    		ret = mDb.insert(TABLE_PROFILE, null, initialValues);
    	return ret;
    }

    public boolean deleteItem(long rowId) {
    	open();
    	return mDb.delete(TABLE_PROFILE, KEY_ID + "=" + rowId, null) > 0;
    }
    public boolean disableItem(long rowId) {
    	open();
    	ContentValues vals = new ContentValues();
    	vals.put(KEY_ENABLED, false);
    	return mDb.update(TABLE_PROFILE, vals, KEY_ID + "=" + rowId, null) > 0;
    }
    
    public Cursor fetchAllItems() {
    	open();
    	return mDb.query(TABLE_PROFILE,
    			new String[] {KEY_ID, KEY_BASE_PATH, KEY_BASE_MATRIX, KEY_WIDGETS, KEY_TRIGGERS, KEY_ENABLED}, null, null, null, null, null);
    }
    
    public WallProfile fetchItem(long Id) throws SQLException {
    	open();
    	Cursor mCursor =
            mDb.query(true, TABLE_PROFILE,
            		new String[] {KEY_BASE_PATH, KEY_BASE_MATRIX, KEY_WIDGETS, KEY_TRIGGERS, KEY_ENABLED},
            		KEY_ID + "=" + Id, null,
                    null, null, null, null);
    	WallProfile ret = new WallProfile();
        if (mCursor != null) {
            mCursor.moveToFirst();
            ret = new WallProfile((int)Id, mCursor.getString(0), null, new Widgets(mCursor.getString(2)));
            mCursor.close();
        }
        return ret;    	
    }
    public boolean exists(long Id)
    {
    	boolean ret = false;
    	//mDb.
    	return ret;
    }
    
}
