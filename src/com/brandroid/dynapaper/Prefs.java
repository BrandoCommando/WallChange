package com.brandroid.dynapaper;

import java.util.Set;

import com.brandroid.Logger;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;

public class Prefs {
	//public static final String PREFS_NAME = "WallChangerPrefs";
	
	private static Prefs preferences;
	private SharedPreferences mStorage; 
	
	public Prefs(Context context)
	{
		PreferenceManager.setDefaultValues(context, R.xml.preferences, false);
		mStorage = PreferenceManager.getDefaultSharedPreferences(context);
		//context.getSharedPreferences(PREFS_NAME, 0);
	}
	public static synchronized Prefs getPreferences(Context context)
	{
		if(preferences == null)
			preferences = new Prefs(context);
		Logger.LogVerbose("Getting instance of SharedPreferences");
		return preferences;
	}
	
	public String getSetting(String key, String defValue)
	{
		try {
			return mStorage.getString(key, defValue);
		} catch(ClassCastException cce) { Logger.LogError("Couldn't get string \"" + key + "\" from Prefs.", cce); return defValue; }
	}
	public int getSetting(String key, Integer defValue)
	{
		//return mStorage.getInt(key, defValue);
		try {
			String s = mStorage.getString(key, defValue.toString());
			return Integer.parseInt(s);
		} catch(Exception e) { return defValue; }
	}
	public float getSetting(String key, Float defValue)
	{
		//return mStorage.getFloat(key, defValue);
		try {
			String s = mStorage.getString(key, defValue.toString());
			return Float.parseFloat(s);
		} catch(Exception e) { return defValue; }
	}
	public Double getSetting(String key, Double defValue)
	{
		//return mStorage.getFloat(key, defValue);
		try {
			String s = mStorage.getString(key, defValue.toString());
			return Double.parseDouble(s);
		} catch(Exception e) { return defValue; }
	}
	public Boolean getSetting(String key, Boolean defValue)
	{
		//return mStorage.getBoolean(key, defValue);
		try {
			String s = mStorage.getString(key, defValue.toString());
			return Boolean.parseBoolean(s);
		} catch(Exception e) { return defValue; }
	}
	public Long getSetting(String key, Long defValue)
	{
		//try {
			//return mStorage.getLong(key, defValue);
		//} catch(Throwable t) { return defValue; }
		try {
			String s = mStorage.getString(key, defValue.toString());
			return Long.parseLong(s);
		} catch(Exception e) { return defValue; }
	}
	public String getString(String key, String defValue) 	{ return getSetting(key, defValue); }
	public int getInt(String key, int defValue) 			{ return getSetting(key, defValue); }
	public float getFloat(String key, float defValue) 		{ return getSetting(key, defValue); }
	public Boolean getBoolean(String key, Boolean defValue) { return getSetting(key, defValue); }
	public Long getLong(String key, Long defValue) 			{ return getSetting(key, defValue); }
	
	public void setSetting(String key, String value)
	{
		SharedPreferences.Editor editor = getPreferences().edit();
		editor.putString(key, value.toString());
		//editor.putString(key, value);
		editor.commit();
	}
	public void setSetting(String key, Boolean value)
	{
		SharedPreferences.Editor editor = getPreferences().edit();
		editor.putString(key, value.toString());
		//editor.putBoolean(key, value);
		editor.commit();
	}
	public void setSetting(String key, Integer value)
	{
		SharedPreferences.Editor editor = getPreferences().edit();
		editor.putString(key, value.toString());
		//editor.putInt(key, value);
		editor.commit();
	}
	public void setSettings(Object... vals)
	{
		SharedPreferences.Editor editor = getPreferences().edit();
		for(int i = 0; i < vals.length - 1; i += 2)
		{
			String key = vals[i].toString();
			Object val = vals[i+1];
			if(val == null) return;
			if(Integer.class.equals(val.getClass()))
				editor.putInt(key, (Integer)val);
			else if(Float.class.equals(val.getClass()))
				editor.putFloat(key, (Float)val);
			else if(Long.class.equals(val.getClass()))
				editor.putLong(key, (Long)val);
			else if(Boolean.class.equals(val.getClass()))
				editor.putBoolean(key, (Boolean)val);
			else
				editor.putString(key, val.toString());
		}
		editor.commit();
		//pairs.
	}
	public Boolean hasSetting(String key)
	{
		return mStorage.contains(key);
	}
	
	public SharedPreferences getPreferences() {
        return mStorage;
    }
}
