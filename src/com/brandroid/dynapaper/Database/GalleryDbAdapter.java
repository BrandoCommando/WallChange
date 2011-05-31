package com.brandroid.dynapaper.Database;

import com.brandroid.Logger;
import com.brandroid.dynapaper.GalleryItem;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class GalleryDbAdapter
{
	public static final String KEY_ID = "_id";
    public static final String KEY_TITLE = "title";
    public static final String KEY_URL = "url";
    public static final String KEY_RATING = "rating";
    public static final String KEY_DOWNLOADS = "downloads";
    public static final String KEY_WIDTH = "w";
    public static final String KEY_HEIGHT = "h";
    public static final String KEY_TAGS = "tags";
    public static final String KEY_VISIBLE = "visible";
    public static final String KEY_DAYS = "days";
    
    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;
    
    private static final String DATABASE_NAME = "wallchanger.db";
    private static final String DATABASE_TABLE = "gallery";
    private static final int DATABASE_VERSION = 10;

    private static final String DATABASE_CREATE =
        "create table " + DATABASE_TABLE + " (" + KEY_ID + " integer primary key, "
        + "title text null, url text not null, "
        + "w int null default 0, h int null default 0, tags text null, "
        + "rating real null, downloads int null, "
        + "visible int not null default 1, days int null default 0);";

    private final Context mCtx;

    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db)
        {
        	Logger.LogVerbose("Creating database");
            db.execSQL(DATABASE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Logger.LogVerbose("Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE);
            onCreate(db);
        }
    }

    public GalleryDbAdapter(Context ctx)
    {
    	this.mCtx = ctx;
    }
    
    public GalleryDbAdapter open() throws SQLException
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
        initialValues.put(KEY_TITLE, title);
        initialValues.put(KEY_URL, url);
        if(rating != null)
        	initialValues.put(KEY_RATING, rating);
        initialValues.put(KEY_DOWNLOADS, downloads);
		initialValues.put(KEY_WIDTH, width);
		initialValues.put(KEY_HEIGHT, height);
		initialValues.put(KEY_TAGS, tags);
		initialValues.put(KEY_VISIBLE, visible);
		initialValues.put(KEY_DAYS, days);

        long ret = 0;
        try {
        	ret = mDb.insertOrThrow(DATABASE_TABLE, null, initialValues);
        	//if(mDb.replace(DATABASE_TABLE, null, initialValues) > 0)
        		//ret = 1;
        } catch(Exception ex) { Logger.LogError("Error adding item to gallery.", ex); }
        if(ret == -1)
        	ret = mDb.update(DATABASE_TABLE, initialValues, KEY_ID + "=" + id, null);
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
    	if(!mDb.isOpen()) open();
    	return mDb.delete(DATABASE_TABLE, KEY_ID + "=" + rowId, null) > 0;
    }
    
    public Cursor fetchAllItems() {
    	if(!mDb.isOpen()) open();
    	return mDb.query(DATABASE_TABLE,
    			new String[] {KEY_ID, KEY_TITLE, KEY_URL, KEY_WIDTH, KEY_HEIGHT, KEY_TAGS, KEY_RATING, KEY_DOWNLOADS, KEY_DAYS},
    			KEY_VISIBLE + " = 1", null, null, null, "downloads DESC, rating DESC");
    }
    
    public String fetchAllIDs()
    {
    	Cursor c = mDb.query(DATABASE_TABLE, new String[] {KEY_ID}, KEY_VISIBLE + " = 1", null, null, null, null);
    	if(c.getCount() == 0) return "";
    	StringBuilder sb = new StringBuilder(",");
    	c.moveToFirst();
    	for(int i = 0; i < c.getCount(); i++)
    	{
    		sb.append(c.getInt(0) + ",");
    		if(!c.moveToNext()) break;
    	}
    	return sb.toString();
    }
    
    public int fetchLatestStamp()
    {
    	if(!mDb.isOpen()) open();
    	Cursor c = mDb.query(DATABASE_TABLE, new String[] {KEY_DAYS}, KEY_VISIBLE + " = 1", null, null, null, "days LIMIT 1");
    	if(c.getCount() == 0) return 0;
    	c.moveToFirst();
    	return c.getInt(0);
    }
    
    public Cursor fetchItem(long Id) throws SQLException {
    	if(!mDb.isOpen()) open();
    	Cursor mCursor =
            mDb.query(true, DATABASE_TABLE,
            		new String[] {KEY_ID, KEY_TITLE, KEY_URL, KEY_WIDTH, KEY_HEIGHT, KEY_TAGS, KEY_RATING, KEY_DOWNLOADS, KEY_DAYS},
            		KEY_ID + "=" + Id, null,
                    null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;    	
    }
    public boolean hideItem(long Id)
    {
    	if(!mDb.isOpen()) open();
    	ContentValues args = new ContentValues();
    	args.put(KEY_VISIBLE, false);
    	return mDb.update(DATABASE_TABLE, args, KEY_ID + "=" + Id, null) > 0;
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
        args.put(KEY_TITLE, title);
        args.put(KEY_URL, url);
        args.put(KEY_RATING, rating);
        args.put(KEY_DOWNLOADS, downloads);
		args.put(KEY_WIDTH, width);
		args.put(KEY_HEIGHT, height);
		args.put(KEY_TAGS, tags);
		args.put(KEY_VISIBLE, visible);
		args.put(KEY_DAYS, days);
		
		Boolean good = mDb.update(DATABASE_TABLE, args, KEY_ID + "=" + Id, null) > 0;
		if(!good)
			good = mDb.replace(DATABASE_TABLE, null, args) > 0;
		return good;
    }
    
}
