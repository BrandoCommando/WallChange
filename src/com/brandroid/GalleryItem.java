package com.brandroid;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.text.StringCharacterIterator;
import java.util.StringTokenizer;

import org.json.JSONException;
import org.json.JSONObject;

import com.brandroid.dynapaper.GalleryDbAdapter;
import com.brandroid.dynapaper.Preferences;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;
import android.util.Log;
import android.view.View;

public class GalleryItem implements Serializable
{
	private static final long serialVersionUID = 5229961124433062997L;
	private int ID;
	private String mUrl;
	private String mTitle;
	private float mRating = Float.NaN;
	private int mDownloadCount = 0;
	private Boolean mDownloaded = false;
	private Boolean mDownloading = false;
	private int mWidth = 0;
	private int mHeight = 0;
	private Bitmap mBitmap;
	private String mTags;
	
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
				Log.i(Preferences.LOG_KEY, "Downloads: " + mDownloadCount);
			} catch(NumberFormatException nfe) {
				Log.w(Preferences.LOG_KEY, "Couldn't find Downloads");
				mDownloadCount = 0;
			}
		} catch(JSONException je) { }
	}
	public GalleryItem(Cursor cursor)
	{
		ID = TryGet(cursor, GalleryDbAdapter.KEY_ID, 0);
		mUrl = mTitle = TryGet(cursor, GalleryDbAdapter.KEY_URL, "");
		mTitle = TryGet(cursor, GalleryDbAdapter.KEY_TITLE, mTitle);
		mRating = (float)TryGet(cursor, GalleryDbAdapter.KEY_RATING, 0.0f);
		mDownloadCount = TryGet(cursor, GalleryDbAdapter.KEY_DOWNLOADS, 0);
		byte[] data = TryGet(cursor, GalleryDbAdapter.KEY_DATA, (byte[])null);
		if(data != null)
		{
			mBitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
			if(mBitmap != null)
				setIsDownloaded();
		}
	}
	
	public static String TryGet(Cursor c, String sKey, String sDefault)
	{
		int i = c.getColumnIndex(sKey);
		if (i == -1) return sDefault;
		if (c.getString(i) == null) return sDefault;
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
	public Boolean isDownloaded() { return mDownloaded; }
	public Boolean isStarted() { return mDownloaded || mDownloading; }
	public void setIsDownloaded() { mDownloaded = true; setIsDownloading(false); }
	public void setIsDownloading(Boolean value) { mDownloading = value; }
	public int getDownloadCount() { return mDownloadCount; }
	public void setBitmap(Bitmap bmp) { mBitmap = bmp; }
	public Bitmap getBitmap() { return mBitmap; }
	
	private void writeObject(ObjectOutputStream out) throws IOException
	{
		out.write(ID);
		out.write(mDownloadCount);
		out.writeFloat((float)mRating);
		out.write(mUrl.length());
		out.writeChars(mUrl);
		out.write(mTitle.length());
		out.writeChars(mTitle);
		if(mBitmap != null)
		{
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			if(mBitmap.compress(CompressFormat.PNG, 100, stream))
			{
				out.write(stream.size());
				out.write(stream.toByteArray());
			}
		} else out.write(0);
		out.close();
	}
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
	{
		ID = in.readInt();
		mDownloadCount = in.readInt();
		mRating = in.readFloat();
		try {
			int len = in.readInt();
			Log.i(Preferences.LOG_KEY, "Url should be " + len + " chars long.");
			mUrl = "";
			for(int i=0; i<len; i++)
				mUrl += in.readChar();
			try {
				len = Math.min(in.readInt(),in.available());
				mTitle = "";
				for(int i=0; i<len; i++)
					mTitle += in.readChar();
				try {
					len = Math.min(in.readInt(),in.available());
					if(len > 0)
					{
						byte[] buffer = new byte[len];
						in.read(buffer);
						mBitmap = BitmapFactory.decodeByteArray(buffer, 0, len);
					}
				} catch(EOFException e3) { Log.e(Preferences.LOG_KEY, "Error getting bitmap: " + e3.toString()); }
			} catch(EOFException e2) { Log.e(Preferences.LOG_KEY, "Error getting title: " + e2.toString()); }
		} catch(EOFException e1) { Log.e(Preferences.LOG_KEY, "Error getting url: " + e1.toString()); }
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
}
