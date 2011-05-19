package com.brandroid.dynapaper;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.util.Log;

public class Preferences {
	public static final String LOG_KEY = "WallChanger";
	public static final String PREFS_NAME = "WallChangerPrefs";
	public static final String EXTRA_SHORTCUT = "WallChangerShortcut";
	public static final String MY_AD_UNIT_ID = "a14d9c70f03d5b2";
	public static final String MY_ROOT_URL = "http://android.brandonbowles.com";
	public static boolean ExternalStorageAvailable = false;
	public static boolean ExternalStorageWriteable = false;
	
	private static Preferences preferences;
	private SharedPreferences mStorage; 
	
	public Preferences(Context context)
	{
		String state = Environment.getExternalStorageState();

		if (Environment.MEDIA_MOUNTED.equals(state)) {
		    // We can read and write the media
		    ExternalStorageAvailable = ExternalStorageWriteable = true;
		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
		    // We can only read the media
		    ExternalStorageAvailable = true;
		    ExternalStorageWriteable = false;
		} else {
		    // Something else is wrong. It may be one of many other states, but all we need
		    //  to know is we can neither read nor write
		    ExternalStorageAvailable = ExternalStorageWriteable = false;
		}
		
		mStorage = context.getSharedPreferences(PREFS_NAME, 0);
	}
	public static synchronized Preferences getPreferences(Context context)
	{
		if(preferences == null)
			preferences = new Preferences(context);
		return preferences;
	}
	
	public String getSetting(String key, String defValue)
	{
		try {
			return mStorage.getString(key, defValue);
		} catch(ClassCastException cce) { Log.e(LOG_KEY, "Couldn't get string from Prefs.", cce); return defValue; }
	}
	public int getSetting(String key, int defValue)
	{
		return mStorage.getInt(key, defValue);
	}
	public float getSetting(String key, float defValue)
	{
		return mStorage.getFloat(key, defValue);
	}
	public Boolean getSetting(String key, Boolean defValue)
	{
		return mStorage.getBoolean(key, defValue);
	}
	public Long getSetting(String key, Long defValue)
	{
		//try {
			return mStorage.getLong(key, defValue);
		//} catch(Throwable t) { return defValue; }
	}
	public String getString(String key, String defValue) 	{ return getSetting(key, defValue); }
	public int getInt(String key, int defValue) 			{ return getSetting(key, defValue); }
	public float getFloat(String key, float defValue) 		{ return getSetting(key, defValue); }
	public Boolean getBoolean(String key, Boolean defValue) { return getSetting(key, defValue); }
	public Long getLong(String key, Long defValue) 			{ return getSetting(key, defValue); }
	
	public void setSetting(String key, String value)
	{
		SharedPreferences.Editor editor = getPreferences().edit();
		editor.putString(key, value);
		editor.commit();
	}
	public void setSetting(String key, Boolean value)
	{
		SharedPreferences.Editor editor = getPreferences().edit();
		editor.putBoolean(key, value);
		editor.commit();
	}
	public void setSetting(String key, int value)
	{
		SharedPreferences.Editor editor = getPreferences().edit();
		editor.putInt(key, value);
		editor.commit();
	}
	public Boolean hasSetting(String key)
	{
		return mStorage.contains(key);
	}
	
	public SharedPreferences getPreferences() {
        return mStorage;
    }
}
