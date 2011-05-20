package com.brandroid.dynapaper;

import android.app.Activity;
import android.util.Log;

public class WallChanger extends Activity
{
	public static final String LOG_KEY = "WallChanger";
	public static final String MY_AD_UNIT_ID = "a14d9c70f03d5b2";
	public static final String MY_ROOT_URL = "http://android.brandonbowles.com";
	public static final String MY_ROOT_URL_GS = "http://commondatastorage.googleapis.com/data.brandonbowles.com";
	public static final String MY_IMAGE_ROOT_URL = MY_ROOT_URL + "/images/";
	public static final String MY_IMAGE_ROOT_URL_GS = MY_ROOT_URL_GS + "/images/";
	public static final String MY_GALLERY_URL = MY_ROOT_URL + "/dynapaper/gallery2.php?user=%USER%";
	public static final String MY_IMAGE_URL = MY_ROOT_URL + "/dynapaper/get_image.php?user=%USER%&url=%URL%";
	public static final String MY_THUMB_URL = MY_ROOT_URL + "/dynapaper/get_thumb.php?url=%URL%";
	public static final String MY_USER_IMAGE_URL = MY_ROOT_URL + "/dynapaper/get_user_image.php?user=%USER%&md5=%MD5%";
	public static final String MY_UPLOAD_IMAGE_URL = MY_ROOT_URL + "/dynapaper/upload_user_image.php?user=%USER%&md5=%MD5%&UPLOAD_IDENTIFIER=%MD5%&APC_UPLOAD_PROGRESS=%MD5%";
	public static final String MY_UPLOAD_PROGRESS_URL = MY_ROOT_URL + "/dynapaper/upload_progress.php?key=%KEY%";
	public static final String EXTERNAL_ROOT = "/mnt/sdcard/wallchanger/";
	public static final int REQ_SELECT_GALLERY = 1;
	public static final int REQ_SELECT_ONLINE = 2;
	public final static int REQ_UPDATE_GALLERY = 101;
	public final static int DOWNLOAD_CHUNK_SIZE = 512;
	public final static Boolean OPTION_SHOW_GALLERY_INFO = false;
	private static String mUser = "";
	private static int mUploadQuality = 100;
	private static Boolean bPaidMode = false;
	private static Boolean bTesting = true;

	public static void LogError(String msg)
	{
		Log.e(WallChanger.LOG_KEY, msg);
	}
	public static void LogError(String msg, Throwable ex)
	{
		Log.e(WallChanger.LOG_KEY, msg, ex);
	}
	public static void LogWarning(String msg)
	{
		Log.w(WallChanger.LOG_KEY, msg);
	}
	public static void LogInfo(String msg)
	{
		Log.i(WallChanger.LOG_KEY, msg);
	}
	public static void LogDebug(String msg)
	{
		Log.d(WallChanger.LOG_KEY, msg);
	}
	
	public static String getUser() { return getUser("",""); }
	public static String getUser(String prefix) { return getUser(prefix,""); }
	public static String getUser(String prefix, String suffix) {
		if(mUser != null && mUser != "")
			return prefix + mUser + suffix;
		else return "";
	}
	public static void setUser(String user)
	{
		if(user.equalsIgnoreCase(mUser)) return;
		LogInfo("New User: " + user);
		mUser = user;
	}
	
	public final static Boolean isPaidMode() { return bPaidMode; }
	public final static Boolean isTesting() { return bTesting; }
	public static int getUploadQuality() { return mUploadQuality; }
    
    public static String getImageThumbUrl(String sBase)
    {
    	return MY_THUMB_URL.replace("%URL%", sBase.substring(sBase.lastIndexOf("/") + 1).replace("?", "&"));
    }
    
    public static String getImageFullUrl(String sBase)
    {
    	if(sBase.contains("//") && !sBase.startsWith(MY_ROOT_URL))
    		return sBase;
    	return MY_IMAGE_URL.replace("%URL%", sBase.substring(sBase.lastIndexOf("/") + 1));
    }

}
