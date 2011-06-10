package com.brandroid.dynapaper.Database;

import com.brandroid.Logger;
import com.brandroid.dynapaper.GalleryItem;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class ProfileDbAdapter
{
	public static final String KEY_ID = "_id";
    public static final String KEY_PROFILE_TITLE = "title";
    public static final String KEY_PROFILE_PRIVATE = "private";
    public static final String KEY_BASE_PATH = "path";
    public static final String KEY_BASE_URL = "url";
    public static final String KEY_BASE_ROTATE = "rotation";
    public static final String KEY_BASE_PRIVATE = "hidden";
    public static final String KEY_WIDGET_ID = "widget"; // different than "_id"
    public static final String KEY_WIDGET_LAYER = "layer";
    public static final String KEY_WIDGET_KEY = "key";
    public static final String KEY_WIDGET_VALUE = "value";
    public static final String KEY_TRIGGER_TYPE = "type";
    public static final String KEY_TRIGGER_VALUE = "trigger";
    
    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;
    
    private static final String DATABASE_NAME = "profiles.db";
    private static final String TABLE_PROFILE = "profile";
    private static final String TABLE_BASE = "base";
    private static final String TABLE_WIDGET = "widget";
    private static final String TABLE_TRIGGER = "trigger";
    private static final String TABLE_PROFILE_BASE = "profile_base";
    private static final String TABLE_PROFILE_WIDGET = "profile_widget";
    private static final String TABLE_PROFILE_TRIGGER = "profile_trigger";
    private static final String[] TABLES_ALL = {TABLE_PROFILE, TABLE_BASE, TABLE_WIDGET, TABLE_TRIGGER, TABLE_PROFILE_BASE, TABLE_PROFILE_WIDGET, TABLE_PROFILE_TRIGGER}; 
    
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_CREATE_PROFILE =
        "create table " + TABLE_PROFILE + " (" + KEY_ID + " integer primary key autoincrement, "
        + KEY_PROFILE_TITLE + " text null, "
        + KEY_PROFILE_PRIVATE + " int not null default 1)";
    private static final String TABLE_CREATE_BASE =
        "create table " + TABLE_BASE + " (" + KEY_ID + " integer primary key autoincrement, "
        + KEY_BASE_PATH + " text null, "
        + KEY_BASE_URL + " text null, "
        + KEY_BASE_PRIVATE + " int not null default 1)";
    private static final String TABLE_CREATE_WIDGET =
        "create table " + TABLE_WIDGET + " (" + KEY_ID + " integer primary key autoincrement, "
        + KEY_PROFILE_TITLE + " text null, "
        + KEY_PROFILE_PRIVATE + " int not null default 1)";
    private static final String TABLE_CREATE_TRIGGER =
    	"create table " + TABLE_TRIGGER + " (" + KEY_ID + " integer primary key autoincrement, "
        + KEY_TRIGGER_TYPE + " text not null,"
        + KEY_TRIGGER_VALUE + " text null)";
    private static final String TABLE_CREATE_PROFILE_BASE =
    	"create table " + TABLE_PROFILE_BASE + " (" + KEY_ID + " integer primary key autoincrement, "
    	+ "profile_id int not null, "
    	+ "base_id int not null);";
    private static final String TABLE_CREATE_WIDGET_BASE =
    	"create table " + TABLE_PROFILE_WIDGET + " (" + KEY_ID + " integer primary key autoincrement, "
    	+ "profile_id int not null, "
    	+ "widget_id int not null);";
    private static final String TABLE_CREATE_PROFILE_TRIGGER =
    	"create table " + TABLE_PROFILE_WIDGET + " (" + KEY_ID + " integer primary key autoincrement, "
    	+ "profile_id int not null, "
    	+ "widget_id int not null);";
        
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
            db.execSQL(TABLE_CREATE_BASE);
            db.execSQL(TABLE_CREATE_TRIGGER);
            db.execSQL(TABLE_CREATE_WIDGET);
            db.execSQL(TABLE_CREATE_PROFILE_BASE);
            db.execSQL(TABLE_CREATE_PROFILE_TRIGGER);
            db.execSQL(TABLE_CREATE_WIDGET_BASE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Logger.LogVerbose("Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            //db.execSQL("DROP TABLE IF EXISTS " + TABLE_BASE);
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
    	if(mDb == null)
    		mDb = mDbHelper.getWritableDatabase();
    	return this;
    }
    
    public void close() { 
    	mDbHelper.close();
    }
    
    public long createItem(int id, String title, String url, 
    		Integer width, Integer height, String tags,
    		Float rating, Integer downloads, Boolean visible, Integer days) {
    	open();
    	ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_ID, id);

        long ret = 0;
        try {
        	ret = mDb.insertOrThrow(TABLE_BASE, null, initialValues);
        	//if(mDb.replace(TABLE_BASE, null, initialValues) > 0)
        		//ret = 1;
        } catch(SQLiteConstraintException scex) { // ignore this one
        } catch(Exception ex) { Logger.LogError("Error adding item to gallery.", ex); }
        if(ret == -1)
        	ret = mDb.update(TABLE_BASE, initialValues, KEY_ID + "=" + id, null);
        return ret;
    }
    public long createItem(GalleryItem item)
    {
    	return createItem(item.getID(), item.getTitle(), item.getURL(),
    			item.getWidth(), item.getHeight(), item.getTags(),
    			item.getRating(), item.getDownloadCount(), true, item.getDays()); 
    }
    public int createItems(GalleryItem[] items)
    {
    	int ret = 0;
    	//mDb.beginTransaction();
    	try {
    		for(int i = 0; i < items.length; i++)
    			ret += createItem(items[i]) > 0 ? 1 : 0;
    	} catch(Exception ex) { Logger.LogError("Exception updating items in DB.", ex); }
    	finally {
    		//mDb.endTransaction();
    	}
    	return ret;
    }
    
    public boolean deleteItem(long rowId) {
    	open();
    	return mDb.delete(TABLE_BASE, KEY_ID + "=" + rowId, null) > 0;
    }
    
    public Cursor fetchAllItems() {
    	open();
    	return mDb.query(TABLE_BASE,
    			new String[] {KEY_ID, KEY_BASE_PATH}, null, null, null, null, "downloads DESC, rating DESC");
    }
    
    public Cursor fetchItem(long Id) throws SQLException {
    	open();
    	Cursor mCursor =
            mDb.query(true, TABLE_BASE,
            		new String[] {KEY_ID, KEY_BASE_PATH},
            		KEY_ID + "=" + Id, null,
                    null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;    	
    }
    public boolean exists(long Id)
    {
    	boolean ret = false;
    	//mDb.
    	return ret;
    }
    public boolean updateItem(GalleryItem item)
    {
    	Logger.LogDebug("Adding GalleryItem \"" + item.getTitle() + "\" to db.");
    	return updateItem(item.getID(), item.getTitle(), item.getURL(),
    			item.getWidth(), item.getHeight(), item.getTags(),
    			(Float)item.getRating(), item.getDownloadCount(), true, item.getDays());
    }
    public int updateItems(GalleryItem[] items)
    {
    	int ret = 0;
    	mDb.beginTransaction();
    	try {
    		for(int i = 0; i < items.length; i++)
    			ret += updateItem(items[i]) ? 1 : 0;
    	} catch(Exception ex) { Logger.LogError("Exception updating items in DB.", ex); }
    	finally {
    		mDb.endTransaction();
    	}
    	return ret;
    }
    public boolean updateItem(long Id, String title, String url,
    		Integer width, Integer height, String tags,
    		Float rating, Integer downloads, Boolean visible, Integer days) {
    	open();
    	ContentValues args = new ContentValues();
        args.put(KEY_BASE_PATH, title);
		
		Boolean good = mDb.update(TABLE_BASE, args, KEY_ID + "=" + Id, null) > 0;
		if(!good)
			good = mDb.replace(TABLE_BASE, null, args) > 0;
		return good;
    }
    
}
