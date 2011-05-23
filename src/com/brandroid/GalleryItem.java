package com.brandroid;

import org.json.JSONException;
import org.json.JSONObject;

import com.brandroid.dynapaper.MediaIO;
import com.brandroid.dynapaper.Prefs;
import com.brandroid.dynapaper.WallChanger;
import com.brandroid.dynapaper.Database.GalleryDbAdapter;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;
import android.util.Log;

public class GalleryItem
{
	private int ID;
	private String mUrl;
	private String mTitle;
	private float mRating = Float.NaN;
	private int mDownloadCount = 0;
	private int mWidth = 0;
	private int mHeight = 0;
	private Bitmap mBitmap;
	private String mTags;
	private int mDays = 0;
	
	public GalleryItem(JSONObject obj)
	{
		try {
			if(obj.has("id"))
				ID = Integer.parseInt(obj.getString("id"));
			if(obj.has("url"))
				mUrl = obj.getString("url");
			if(obj.has("title"))
				mTitle = obj.getString("title");
			else mTitle = mUrl;
			if(obj.has("dim"))
			{
				String[] dims = obj.getString("dims").split("x");
				try {
					mWidth = mHeight = Integer.parseInt(dims[0]);
					if(dims.length > 1)
						mHeight = Integer.parseInt(dims[1]);
				} catch(NumberFormatException nfe) { }
			}
			if(obj.has("tags"))
				mTags = obj.getString("tags");
			try {
				mRating = Float.parseFloat(obj.getString("rating"));
			} catch(NumberFormatException nfe) { mRating = (Float)null; }
			try {
				mDownloadCount = Integer.parseInt(obj.getString("dl"));
				Log.i(Prefs.LOG_KEY, "Downloads: " + mDownloadCount);
			} catch(NumberFormatException nfe) {
				Log.w(Prefs.LOG_KEY, "Couldn't parse Downloads");
				mDownloadCount = 0;
			}
			try {
				mDays = Integer.parseInt(obj.getString("days"));
			} catch(NumberFormatException nfe) {
				Log.w(Prefs.LOG_KEY, "Couldn't parse days", nfe);
				mDays = -1;
			}
			mBitmap = MediaIO.readFileBitmap(getID() + ".jpg", true);
		} catch(JSONException je) { }
	}
	public GalleryItem(Cursor cursor)
	{
		ID = TryGet(cursor, GalleryDbAdapter.KEY_ID, 0);
		mUrl = mTitle = TryGet(cursor, GalleryDbAdapter.KEY_URL, "");
		mTitle = TryGet(cursor, GalleryDbAdapter.KEY_TITLE, mTitle);
		mRating = (float)TryGet(cursor, GalleryDbAdapter.KEY_RATING, 0.0f);
		mDownloadCount = TryGet(cursor, GalleryDbAdapter.KEY_DOWNLOADS, 0);
		mDays = TryGet(cursor, GalleryDbAdapter.KEY_DAYS, 0);
		mBitmap = MediaIO.readFileBitmap(getID() + ".jpg", true);
		/*
		byte[] data = TryGet(cursor, GalleryDbAdapter.KEY_DATA, (byte[])null);
		if(data != null)
		{
			mBitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
			if(mBitmap != null)
				setIsDownloaded();
		}
		*/
	}
	
	public static String TryGet(Cursor c, String sKey, String sDefault)
	{
		int i = c.getColumnIndex(sKey);
		try {
			if (i == -1) return sDefault;
			if (c.getString(i) == null) return sDefault;
		} catch(Exception e) { return sDefault; }
		return c.getString(i);
	}
	public static int TryGet(Cursor c, String sKey, int iDefault)
	{
		int i = c.getColumnIndex(sKey);
		try {
			if (i == -1) return iDefault;
			if (c.getInt(i) == -1) return iDefault;
		} catch(Exception e) { return iDefault; }
		return c.getInt(i);
	}
	public static float TryGet(Cursor c, String sKey, float fDefault)
	{
		int i = c.getColumnIndex(sKey);
		try {
			if (i == -1) return fDefault;
			if (c.getFloat(i) == -1) return fDefault;
		} catch(Exception e) { return fDefault; }
		return c.getFloat(i);
	}
	public static byte[] TryGet(Cursor c, String sKey, byte[] bDefault)
	{
		byte[] ret = bDefault;
		int i = c.getColumnIndex(sKey);
		try {
			if (i == -1) return bDefault;
			ret = c.getBlob(i);
			if(ret == null) return bDefault;
		} catch(Exception e) { return bDefault; }
		return ret;
	}

	public int getID() { return ID; }
	public String getURL() { return mUrl; }
	public String getTitle() { return mTitle; }
	public void setURL(String url) { mUrl = url; }
	public Float getRating() { return mRating; }
	public int getDownloadCount() { return mDownloadCount; }
	public void setBitmap(Bitmap bmp) {
		mBitmap = bmp;
		MediaIO.writeFile(getID() + ".jpg", bmp, true);
	}
	public Bitmap getBitmap() { return mBitmap; }

	public void setWidth(int mWidth) {
		this.mWidth = mWidth;
	}
	public int getWidth() {
		return mWidth;
	}
	public void setHeight(int mHeight) {
		this.mHeight = mHeight;
	}
	public int getHeight() {
		return mHeight;
	}
	public void setTags(String mTags) {
		this.mTags = mTags;
	}
	public String getTags() {
		return mTags;
	}
	public void setDays(int mDays) {
		this.mDays = mDays;
	}
	public int getDays() {
		return mDays;
	}
}
