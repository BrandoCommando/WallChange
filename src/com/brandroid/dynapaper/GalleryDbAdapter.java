package com.brandroid.dynapaper;

import com.brandroid.GalleryItem;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class GalleryDbAdapter
{
	public static final String KEY_ID = "_id";
    public static final String KEY_TITLE = "title";
    public static final String KEY_URL = "url";
    public static final String KEY_DATA = "data";
    public static final String KEY_RATING = "rating";
    public static final String KEY_DOWNLOADS = "downloads";
    public static final String KEY_WIDTH = "w";
    public static final String KEY_HEIGHT = "h";
    public static final String KEY_TAGS = "tags";
    public static final String KEY_VISIBLE = "visible";
    public static final String KEY_STAMP = "stamp";
    
    private static String mCurrentIDs = null;

    //private static final String TAG = "WallChangeGalleryDbAdapter";
    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;
    
    private static final String DATABASE_NAME = "wallchanger.db";
    private static final String DATABASE_TABLE = "gallery";
    private static final int DATABASE_VERSION = 7;

    private static final String DATABASE_CREATE =
        "create table gallery (" + KEY_ID + " integer primary key, "
        + "title text null, url text not null, "
        + "w int null default 0, h int null default 0, tags text null, "
        + "rating real null, downloads int null, data blob null, "
        + "visible int not null default 1, stamp int null default 0);";

    private final Context mCtx;

    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db)
        {
        	Log.i(Preferences.LOG_KEY, "Creating database");
            db.execSQL(DATABASE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(Preferences.LOG_KEY, "Upgrading database from version " + oldVersion + " to "
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
    
    public long createItem(int id, String title, String url, byte[] data,
    		Integer width, Integer height, String tags,
    		Float rating, Integer downloads, Boolean visible, Integer stamp) {
    	if(!mDb.isOpen()) open();
    	ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_ID, id);
        initialValues.put(KEY_TITLE, title);
        initialValues.put(KEY_URL, url);
        if(rating != null)
        	initialValues.put(KEY_RATING, rating);
        initialValues.put(KEY_DOWNLOADS, downloads);
        if(data != null && data.length > 0)
        	initialValues.put(KEY_DATA, data);
		initialValues.put(KEY_WIDTH, width);
		initialValues.put(KEY_HEIGHT, height);
		initialValues.put(KEY_TAGS, tags);
		initialValues.put(KEY_VISIBLE, visible);
		initialValues.put(KEY_STAMP, stamp);

        long ret = 0;
        try {
        	if(mDb.replace(DATABASE_TABLE, null, initialValues) > -1)
        		ret = 1;
        } catch(Exception ex) { }
        if(ret == -1)
        	ret = mDb.update(DATABASE_TABLE, initialValues, KEY_ID + "=" + id, null) > 0 ? 1 : 0;
        return ret;
    }
    public long createItem(GalleryItem item)
    {
    	return createItem(item.getID(), item.getTitle(), item.getURL(), (byte[])null,
    			item.getWidth(), item.getHeight(), item.getTags(),
    			item.getRating(), item.getDownloadCount(), true, item.getStamp()); 
    }
    
    public boolean updateData(long Id, byte[] data)
    {
    	if(!mDb.isOpen()) open();
    	ContentValues args = new ContentValues();
    	//args.put(KEY_ID, Id);
    	args.put(KEY_DATA, data);
    	return mDb.update(DATABASE_TABLE, args, KEY_ID + "=" + Id, null) > 0;
    }
    
    public boolean deleteItem(long rowId) {
    	if(!mDb.isOpen()) open();
    	return mDb.delete(DATABASE_TABLE, KEY_ID + "=" + rowId, null) > 0;
    }
    
    public Cursor fetchAllItems() {
    	if(!mDb.isOpen()) open();
    	return mDb.query(DATABASE_TABLE,
    			new String[] {KEY_ID, KEY_TITLE, KEY_URL, KEY_DATA, KEY_WIDTH, KEY_HEIGHT, KEY_TAGS, KEY_RATING, KEY_DOWNLOADS, KEY_STAMP},
    			KEY_VISIBLE + " = 1", null, null, null, "downloads DESC, rating DESC");
    }
    
    public String fetchAllIDs()
    {
    	Cursor c = mDb.query(DATABASE_TABLE, new String[] {KEY_ID}, KEY_VISIBLE + " = 1", null, null, null, null);
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
    	Cursor c = mDb.query(DATABASE_TABLE, new String[] {KEY_STAMP}, KEY_VISIBLE + " = 1", null, null, null, "stamp DESC");
    	c.moveToFirst();
    	return c.getInt(0);
    }
    
    public Cursor fetchItem(long Id) throws SQLException {
    	if(!mDb.isOpen()) open();
    	Cursor mCursor =
            mDb.query(true, DATABASE_TABLE,
            		new String[] {KEY_ID, KEY_TITLE, KEY_URL, KEY_DATA, KEY_WIDTH, KEY_HEIGHT, KEY_TAGS, KEY_RATING, KEY_DOWNLOADS, KEY_STAMP},
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
    	args.put(KEY_DATA, (byte[])null);
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
    	return updateItem(item.getID(), item.getTitle(), item.getURL(), (byte[])null,
    			item.getWidth(), item.getHeight(), item.getTags(),
    			(Float)item.getRating(), item.getDownloadCount(), true, item.getStamp());
    }
    public boolean updateItem(long Id, String title, String url, byte[] data,
    		Integer width, Integer height, String tags,
    		Float rating, Integer downloads, Boolean visible, Integer stamp) {
    	if(!mDb.isOpen()) open();
    	ContentValues args = new ContentValues();
        args.put(KEY_TITLE, title);
        args.put(KEY_URL, url);
        args.put(KEY_RATING, rating);
        args.put(KEY_DOWNLOADS, downloads);
		args.put(KEY_DATA, data);
		args.put(KEY_WIDTH, width);
		args.put(KEY_HEIGHT, height);
		args.put(KEY_TAGS, tags);
		args.put(KEY_VISIBLE, visible);
		args.put(KEY_STAMP, stamp);

		return mDb.update(DATABASE_TABLE, args, KEY_ID + "=" + Id, null) > 0;
    }
    
}
