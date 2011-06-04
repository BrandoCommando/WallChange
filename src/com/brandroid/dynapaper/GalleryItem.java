package com.brandroid.dynapaper;

import org.json.JSONException;
import org.json.JSONObject;

import com.brandroid.MediaUtils;
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
	//private Bitmap mThumbnail;
	private String mTags;
	private int mDays = 0;
	
	public GalleryItem(JSONObject obj)
	{
		//try {
			ID = obj.optInt("id");
			mUrl = obj.optString("url", mUrl);
			mTitle = obj.optString("title", mTitle);
			mWidth = obj.optInt("w", mWidth);
			mHeight = obj.optInt("h", mHeight);
			mDownloadCount = obj.optInt("dl", mDownloadCount);
			mDays = obj.optInt("days", mDays);
			mRating = (float)obj.optDouble("rating", mRating);
			mTags = obj.optString("tags", mTags);
			//mThumbnail = MediaUtils.readFileBitmap(getID() + ".jpg", true);
		//} catch(JSONException je) { }
	}
	public GalleryItem(Cursor cursor)
	{
		ID = TryGet(cursor, GalleryDbAdapter.KEY_ID, 0);
		mUrl = mTitle = TryGet(cursor, GalleryDbAdapter.KEY_URL, "");
		mTitle = TryGet(cursor, GalleryDbAdapter.KEY_TITLE, mTitle);
		mRating = (float)TryGet(cursor, GalleryDbAdapter.KEY_RATING, 0.0f);
		mDownloadCount = TryGet(cursor, GalleryDbAdapter.KEY_DOWNLOADS, 0);
		mDays = TryGet(cursor, GalleryDbAdapter.KEY_DAYS, 0);
		//mThumbnail = MediaUtils.readFileBitmap(getID() + ".jpg", true);
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
	
	public void merge(JSONObject obj)
	{
		mUrl = obj.optString("url", mUrl);
		mTitle = obj.optString("title", mTitle);
		mWidth = obj.optInt("w", mWidth);
		mHeight = obj.optInt("h", mHeight);
		mDownloadCount = obj.optInt("dl", mDownloadCount);
		mDays = obj.optInt("days", mDays);
		mRating = (float)obj.optDouble("rating", mRating);
		mTags = obj.optString("tags", mTags);
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
	public void setThumbnail(Bitmap bmp) {
		//mThumbnail = bmp;
		MediaUtils.writeFile(getThumbnailFilename(), bmp, true);
	}
	public Bitmap getThumbnail() {
		//if(mThumbnail == null)
			//mThumbnail = MediaUtils.readFileBitmap(getID() + ".jpg", true);
		return MediaUtils.readFileBitmap(getThumbnailFilename(), true);
	}
	public String getThumbnailFilename() {
		return getThumbnailFilename(false);
	}
	public String getThumbnailFilename(Boolean fullName) {
		/*
		String ret = getURL();
		if(ret.indexOf("/") > -1)
			ret = ret.substring(ret.lastIndexOf("/") + 1);
		ret = ret.replaceAll("[^A-Za-z0-9]", "_").replace("_jpg", ".jpg").replace("_png", ".jpg");
		*/
		String ret = getID() + ".jpg";
		if(fullName)
			ret = MediaUtils.getFullFilename(ret, true);
		return ret;
	}	
	public Boolean hasThumbnail() {
		return MediaUtils.fileExists(getThumbnailFilename(), true);
	}

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
