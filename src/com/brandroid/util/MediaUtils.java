package com.brandroid.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import com.brandroid.dynapaper.WallChanger;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;
import android.os.Environment;

public class MediaUtils {
	public static String MEDIA_DIRECTORY = "data/com.brandroid.dynapaper";
	public static boolean mExternalStorageAvailable = true;
	public static boolean mExternalStorageWriteable = true;
	public static String mExternalState = Environment.getExternalStorageState();
	private static boolean mChecked = false;
	private static File mCacheDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), MEDIA_DIRECTORY);
	
	public static void init(Context c)
	{
		if(!mCacheDir.exists())
			if(!mCacheDir.mkdirs())
			{
				mCacheDir = new File(c.getExternalCacheDir().getAbsolutePath(), MEDIA_DIRECTORY);
				mCacheDir.mkdirs();
			}
	}
	public static void Check()
	{
		if (Environment.MEDIA_MOUNTED.equals(mExternalState)) {
		    // We can read and write the media
		    mExternalStorageAvailable = mExternalStorageWriteable = true;
		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(mExternalState)) {
		    // We can only read the media
		    mExternalStorageAvailable = true;
		    mExternalStorageWriteable = false;
		} else {
		    // Something else is wrong. It may be one of many other states, but all we need
		    //  to know is we can neither read nor write
		    mExternalStorageAvailable = mExternalStorageWriteable = false;
		}
		mChecked = true;
	}
	
	public static File getBaseDirectory()
	{
		return getBaseDirectory(true);
	}
	public static File getBaseDirectory(Boolean bNeedWrite)
	{
		if(!mChecked) Check();
		File ret = null;
		if(!mExternalStorageAvailable || (bNeedWrite && !mExternalStorageWriteable))
			ret = mCacheDir;
		else
			ret = new File(Environment.getExternalStorageDirectory(), MEDIA_DIRECTORY);
		if(!ret.exists())
		{
			if(ret.mkdir())
				Logger.LogDebug("Base directory created: " + ret.toString());
			else
				Logger.LogError("Couldn't create base directory: " + ret.toString());
		}
		return ret;
	}
	public static File getCacheDirectory()
	{
		return mCacheDir;
	}
	
	public static Boolean fileExists(String filename, Boolean useCache)
	{
		File f = new File(filename);
		if(f.exists()) return true;
		f = new File(useCache ? getCacheDirectory() : getBaseDirectory(), filename);
		if(f.exists()) return true;
		return false;
	}
	
	public static String getFullFilename(String filename, Boolean useCache)
	{
		return new File(useCache ? getCacheDirectory() : getBaseDirectory(), filename).getAbsolutePath();
	}
	
	public static File getFile(String filename, Boolean useCache)
	{
		return new File(useCache ? getCacheDirectory() : getBaseDirectory(), filename);
	}

    public static Boolean writeFile(String filename, byte[] data, Boolean useCache) throws IOException
    {
    	if(data == null) return false;
    	Boolean success = false;
    	OutputStream s = null;
    	File f = new File(useCache ? getCacheDirectory() : getBaseDirectory(), filename);
		try {
    		//Logger.LogInfo("Writing to " + f.toString());
        	//if(!f.createNewFile()) return false;
    		if(f.exists())
    			f.delete();
    		else
    			f.createNewFile();
    		s = new BufferedOutputStream(new FileOutputStream(f));
    		s.write(data);
    		success = true;
    	} catch(IOException ex) {
    		Logger.LogError("Exception saving file - " + f.getAbsolutePath(), ex);
    		mExternalStorageWriteable = false;
    	}
    	finally {
    		if(s != null)
				try {
					s.close();
				} catch (IOException e) {
					Logger.LogError("Exception closing stream for \"" + filename + "\".", e);
				}
    	}
    	return success;
    }
    public static Boolean writeFile(String filename, Bitmap bmp, Boolean useCache)
    {
    	if(bmp == null) return false;
    	Boolean success = false;
    	OutputStream s = null;
    	try {
    		File f = new File(useCache ? getCacheDirectory() : getBaseDirectory(), filename);
    		//Logger.LogInfo("Writing to " + f.toString());
        	//if(!f.createNewFile()) return false;
    		if(f.exists())
    			f.delete();
    		s = new BufferedOutputStream(new FileOutputStream(f));
    		bmp.compress(CompressFormat.JPEG, 100, s);
    		success = true;
    	} catch(IOException ex) {
    		Logger.LogError("Error saving " + filename, ex);
    		mExternalStorageWriteable = false;
    	}
    	finally {
    		if(s != null)
				try {
					s.close();
				} catch (IOException e) {
					Logger.LogError("Exception closing stream for \"" + filename + "\".", e);
				}
    	}
    	return success;
    }
    public static Bitmap readFileBitmap(String filename, Boolean useCache)
    {
    	Bitmap ret = null;
    	BufferedInputStream s = null;
    	try {
    		File f;
    		if(filename.indexOf("/") > -1)
    			f = new File(filename);
    		else f = new File(useCache ? getCacheDirectory() : getBaseDirectory(), filename);
    		if(!f.exists()) return null;
        	//Logger.LogInfo("Reading from " + f.toString());
    		//s = new BufferedInputStream(new FileInputStream(f));
    		//ret = BitmapFactory.decodeStream(s);
    		ret = BitmapFactory.decodeFile(f.getAbsolutePath());
    		if(ret == null) throw new IOException("Null file");
    	} catch(IOException ex) { Logger.LogError("Error reading bitmap \"" + filename + "\"", ex); }
    	finally {
    		if(s != null)
				try {
					s.close();
				} catch (IOException e) {
					Logger.LogError("Exception closing stream.", e);
				}
    	}
    	return ret;
    }
    public static Bitmap getSizedBitmap(Bitmap bmp, int mw, int mh)
	{
		int w = bmp.getWidth();
		int h = bmp.getHeight();
		Logger.LogDebug("Bitmap Size: " + w + "x" + h + "  Max Size: " + mw + "x" + mh);
		if(w > mw && h > mh)
		{
			if((w - mw) < (h - mh))
			{
				double r = (double)h / (double)w;
				w = mw;
				h = (int)Math.floor(r * (double)w);
			} else {
				double r = (double)w / (double)h;
				h = mh;
				w = (int)Math.floor(r * (double)h);
			}
			Logger.LogDebug("Resizing to " + w + "x" + h);
			try {
				bmp = Bitmap.createScaledBitmap(bmp, w, h, true);
				//bmp.recycle();
				//return ret;
			} catch(Exception ex) {
				Logger.LogError("Resizing Failed. Using original.", ex);
			}
		} else Logger.LogDebug("No resizing needed.");
		return bmp;
	}
}
