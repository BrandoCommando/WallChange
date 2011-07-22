package com.brandroid.dynapaper.Activities;

import com.brandroid.dynapaper.Prefs;
import com.brandroid.dynapaper.R;
import com.brandroid.dynapaper.WallChanger;
import com.brandroid.util.Logger;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;

public class Settings extends PreferenceActivity implements OnSharedPreferenceChangeListener, OnPreferenceChangeListener
{
	protected Prefs prefs;
	protected Preference pUser, pResize;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Logger.LogVerbose("onCreate :: Settings");
		addPreferencesFromResource(R.xml.preferences);
		pUser = findPreference("user");
		pUser.setSummary(WallChanger.getUser());
		pUser.setOnPreferenceChangeListener(this);
		pResize = findPreference("resize");
		pResize.setSummary(WallChanger.getResizeMode());
		pResize.setDefaultValue(WallChanger.getResizeMode());
		pResize.setOnPreferenceChangeListener(this);
	}

	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
	{
		Logger.LogDebug("onSharedPreferenceChanged - " + key + " [" + sharedPreferences.toString() + "]");
	}

	public boolean onPreferenceChange(Preference preference, Object newValue) {
		if(preference.getKey().equals("user"))
		{
			pUser.setSummary((String)newValue);
			WallChanger.setUser((String)newValue);
		} else if(preference.getKey().equals("resize")) {
			pResize.setSummary((String)newValue);
			WallChanger.setResizeMode((String)newValue);
		}
		Logger.LogDebug("onPreferenceChange - " + preference.getKey() + " = " + newValue.toString());
		return true;
	}
}
