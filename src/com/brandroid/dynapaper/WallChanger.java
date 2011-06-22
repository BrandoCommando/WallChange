package com.brandroid.dynapaper;

import java.net.URLEncoder;

import com.brandroid.util.Logger;

import android.location.Location;
import android.location.LocationManager;

public class WallChanger
{
	public static final String LOG_KEY = "WallChanger";
	public static final String[] MY_AD_UNIT_ID = new String[] {"a14de00854229f0", "a14d9c70f03d5b2"};
	public static final String MY_MOBCLIX_AD_ID = "851E0632-B1F1-417C-AF71-3850ECF46E00";
	public static final String MY_ROOT_URL = "http://791.b.hostable.me";
	public static final String MY_ROOT_URL_GS = "http://commondatastorage.googleapis.com/data.brandonbowles.com";
	public static final String MY_IMAGE_ROOT_URL = MY_ROOT_URL + "/images/";
	public static final String MY_IMAGE_ROOT_URL_GS = MY_ROOT_URL_GS + "/images/";
	public static final String MY_APP_ROOT_URL = MY_ROOT_URL + "/dynapaper/";
	public static final String MY_WEATHER_URL = MY_APP_ROOT_URL + "widget_weather2.php?user=%USER%";
	public static final String MY_WEATHER_API_URL = MY_APP_ROOT_URL + "widget_weather.php?zip=%ZIP%&format=json";
	public static final String MY_GALLERY_URL = MY_APP_ROOT_URL + "gallery3.php?user=%USER%";
	public static final String MY_FEEDBACK_URL = MY_APP_ROOT_URL + "feedback.php?user=%USER%";
	public static final String MY_IMAGE_URL = MY_APP_ROOT_URL + "get_image.php?user=%USER%&url=%URL%";
	public static final String MY_THUMB_URL = MY_APP_ROOT_URL + "get_thumb.php?user=%USER%&url=%URL%";
	public static final String MY_THUMBS_ZIP_URL = MY_APP_ROOT_URL + "get_thumbs.php";
	public static final String MY_USER_IMAGE_URL = MY_APP_ROOT_URL + "get_user_image.php?user=%USER%&md5=%MD5%";
	public static final String MY_UPLOAD_IMAGE_URL = MY_APP_ROOT_URL + "upload_user_image.php?user=%USER%&md5=%MD5%&UPLOAD_IDENTIFIER=%MD5%&APC_UPLOAD_PROGRESS=%MD5%";
	//public static final String MY_UPLOAD_IMAGE_URL = MY_ROOT_URL + "/images/upload.php?user=%USER%&md5=%MD5%&UPLOAD_IDENTIFIER=%MD5%&APC_UPLOAD_PROGRESS=%MD5%";
	public static final String MY_UPLOAD_PROGRESS_URL = MY_APP_ROOT_URL + "upload_progress.php?key=%KEY%";
	public static int VERSION_CODE = 17; 
	public static Boolean FEATURE_GPS = true;
	public static final String EXTERNAL_ROOT = "/mnt/sdcard/wallchanger/";
	public static final int REQ_SELECT_GALLERY = 1;
	public static final int REQ_SELECT_ONLINE = 2;
	public static final int REQ_SETTINGS = 3;
	public final static int REQ_UPDATE_GALLERY = 101;
	public final static int DOWNLOAD_CHUNK_SIZE = 512;
	public final static Boolean OPTION_SHOW_GALLERY_INFO = true;
	private static String mUser = "";
	private static int mUploadQuality = 90;
	private static int mUploadQualityNoWifi = 60;
	private static final Boolean bPaidMode = false;
	private static final Boolean bTesting = true;
	//private static String mDeviceId;
	private static Location mLastLocation;
	public static Prefs Prefs;
	
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
		if(user.equals("")) return;
		Logger.LogInfo("New User: " + user + " (from " + mUser + ")");
		mUser = user;
	}
	
	public final static Boolean isPaidMode() { return bPaidMode; }
	public final static Boolean isTesting() { return bTesting; }
	public static int getUploadQuality() { return getUploadQuality(false); }
	public static int getUploadQuality(Boolean bWifi) {
		if(bWifi)
			return mUploadQuality;
		else return mUploadQualityNoWifi;
	}
    
	public static String getImageThumbUrl(String sBase, int width, int height) { return getImageThumbUrl(sBase, width, height, false); }
	public static String getImageThumbUrl(String sBase, int width, int height, Boolean bUseGoogleStorage)
    {
		if(bUseGoogleStorage)
			return MY_IMAGE_ROOT_URL + "thumbs/" + sBase + width + "x" + height + ".jpg";
			//return MY_IMAGE_ROOT_URL_GS + "thumbs/" + sBase + width + "x" + height + ".jpg";
		else
			return MY_THUMB_URL.replace("%USER%", getUser()).replace("%URL%", URLEncoder.encode(sBase)) + (width > 0 ? "&w=" + width : "") + (height > 0 ? "&h=" + height : "");
    }
    
    public static String getImageFullUrl(String sBase)
    {
    	if(sBase.contains("//") && !sBase.startsWith(MY_ROOT_URL))
    		return sBase;
    	return MY_IMAGE_URL.replace("%USER%", getUser()).replace("%URL%", URLEncoder.encode(sBase));
    }
    
    /*
    public static String getDeviceId(Context c)
    {
    	String ret = "";
    	try {
    		ret = ((TelephonyManager)c.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
    		Logger.LogVerbose("My Device ID: " + ret);
    	} catch(SecurityException sex) { Logger.LogError("Couldn't get Device ID", sex); }
    	return ret;
    }
    */
    
    public static Boolean setLastLocation(Location l)
    {
    	if(isBetterLocation(l, mLastLocation))
    	{
    		Logger.LogInfo("Location Update: " + l.toString());
    		if(Prefs != null)
    			Prefs.setSettings("latitude", l.getLatitude(), "longitude", l.getLongitude(), "accuracy", l.getAccuracy(), "loctime", l.getTime());
    		return true;
    	} else return false;
    }
    public static Boolean isBetterLocation(Location a, Location b)
    {
    	if(a == null) return false;
    	if(b == null) return true;
    	Long deltaTime = a.getTime() - b.getTime();
    	if(deltaTime > 6000000) // 1 hour
    		return true;
    	else if (deltaTime < -600000)
    		return false;
    	if(a.hasAccuracy())
    	{
    		if(!b.hasAccuracy()) return true;
    		else if(a.getAccuracy() < b.getAccuracy())
    			return true;
    		else return false;
    	} else if(b.hasAccuracy())
    		return false;
    	else
    		return true;
    }
    public static Location getLastLocation()
    {
    	if(mLastLocation != null) return mLastLocation;
    	Location ret = new Location(LocationManager.GPS_PROVIDER);
    	if(Prefs != null)
    	{
    		if(Prefs.hasSetting("latitude"))
    			ret.setLatitude(Prefs.getSetting("latitude", ret.getLatitude()));
    		if(Prefs.hasSetting("longitude"))
    			ret.setLongitude(Prefs.getSetting("longitude", ret.getLongitude()));
    		if(Prefs.hasSetting("accuracy"))
    			ret.setAccuracy(Prefs.getSetting("accuracy", ret.getAccuracy()));
    		if(Prefs.hasSetting("loctime"))
    			ret.setTime(Prefs.getSetting("loctime", ret.getTime()));
    	}
    	return ret;
    }
    
}
