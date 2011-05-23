package com.brandroid.dynapaper;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;
import android.os.Environment;

public class MediaIO {
	public static String MEDIA_DIRECTORY = "brandroid";
	public static boolean mExternalStorageAvailable = false;
	public static boolean mExternalStorageWriteable = false;
	public static String mExternalState = Environment.getExternalStorageState();
	private static boolean mChecked = false;
	private static File mCacheDir = Environment.getDownloadCacheDirectory();
	
	private static void LogError(String s) { WallChanger.LogError(s); }
	private static void LogError(String s, Throwable t) { WallChanger.LogError(s, t); }
	private static void LogInfo(String s) { WallChanger.LogInfo(s); }
	
	public static void init(Context c)
	{
		mCacheDir = c.getCacheDir();
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
		LogInfo("Base directory: " + ret.toString());
		return ret;
	}
	public static File getCacheDirectory()
	{
		return mCacheDir;
	}

    public static Boolean writeFile(String filename, Bitmap bmp, Boolean useCache)
    {
    	Boolean success = false;
    	FileOutputStream s = null;
    	try {
    		File f = new File(useCache ? getCacheDirectory() : getBaseDirectory(), filename);
    		//LogInfo("Writing to " + f.toString());
        	//if(!f.createNewFile()) return false;
    		s = new FileOutputStream(f);
    		bmp.compress(CompressFormat.JPEG, 100, s);
    		success = true;
    	} catch(IOException ex) {
    		LogError("Exception saving file.");
    		mExternalStorageWriteable = false;
    	}
    	finally {
    		if(s != null)
				try {
					s.close();
				} catch (IOException e) {
					LogError("Exception closing stream for \"" + filename + "\".", e);
				}
    	}
    	return success;
    }
    public static Bitmap readFileBitmap(String filename, Boolean useCache)
    {
    	Bitmap ret = null;
    	BufferedInputStream s = null;
    	try {
    		File f = new File(useCache ? getCacheDirectory() : getBaseDirectory(), filename);
    		if(!f.exists()) return null;
        	//LogInfo("Reading from " + f.toString());
    		s = new BufferedInputStream(new FileInputStream(f));
    		ret = BitmapFactory.decodeStream(s);
    	} catch(IOException ex) { LogError("Exception reading bitmap file.", ex); }
    	finally {
    		if(s != null)
				try {
					s.close();
				} catch (IOException e) {
					LogError("Exception closing stream.", e);
				}
    	}
    	return ret;
    }
}
