package com.brandroid.dynapaper.Activities;

import java.sql.Date;

import com.brandroid.CustomExceptionHandler;
import com.brandroid.Logger;
import com.brandroid.dynapaper.Prefs;
import com.brandroid.dynapaper.R;
import com.brandroid.dynapaper.WallChanger;
import com.brandroid.dynapaper.Database.LoggerDbAdapter;
import com.google.ads.AdRequest;
import com.google.ads.AdSize;
import com.google.ads.AdView;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.format.Time;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

public class BaseActivity extends Activity
{
	private int mHomeWidth = 0;
	private int mHomeHeight = 0;
	protected Resources mResources;
	protected Prefs prefs;
		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if(!Logger.hasDb())
			Logger.setDb(new LoggerDbAdapter(this));
		
		Thread.setDefaultUncaughtExceptionHandler(new CustomExceptionHandler());
		
		Logger.LogVerbose("onCreate :: " + this.toString());

        mResources = getResources();
        if(WallChanger.Prefs == null)
        	WallChanger.Prefs = Prefs.getPreferences(getApplicationContext());
        prefs = WallChanger.Prefs;
        
        if(!WallChanger.isPaidMode())
        	addAds();
        else if(findViewById(R.id.adLayout)!=null)
        	findViewById(R.id.adLayout).setVisibility(View.GONE);
        	
	}
	
	public void addAds()
    {	
    	try {
	    	// Create the adView
    		Time t = new Time();
    		int iAdToUse = t.hour % (WallChanger.MY_AD_UNIT_ID.length + 1);
    		String sAdID = WallChanger.MY_AD_UNIT_ID[iAdToUse];
    		Logger.LogInfo("Using Ad ID #" + iAdToUse + " for Admob - " + sAdID);
    		AdView adView = new AdView(this, AdSize.BANNER, sAdID);
	        // Lookup your LinearLayout assuming its been given
	        // the attribute android:id="@+id/mainLayout"
	        LinearLayout layout = (LinearLayout)findViewById(R.id.adLayout);
	        if(layout == null) {
	        	Logger.LogWarning("Unable to add Ads.");
	        	return;
	        }
	        // Add the adView to it
	        layout.addView(adView);
	        // Initiate a generic request to load it with an ad
	        AdRequest ad = new AdRequest();
	        if(WallChanger.isTesting())
	        {
	        	//ad.setTesting(WallChanger.isTesting());
	        } else ad.setTesting(false);
	        
	        ad.addTestDevice(AdRequest.TEST_EMULATOR);
        	//ad.addTestDevice("A0000015CF6B9D");
        	ad.addTestDevice("383A6E6B957E2A18C8830E6C431B2AAF");
        	
	        ad.setLocation(WallChanger.getLastLocation());
	        adView.loadAd(ad);
    	} catch(Exception ex) { Logger.LogWarning("Error adding ads.", ex); }    
    }
	
    protected String getResourceString(int stringResourceID)
    {
    	return mResources.getString(stringResourceID);
    }
	

	
	public int getHomeWidth()
	{
		if(mHomeWidth > 0) return mHomeWidth;
		Display display = getWindow().getWindowManager().getDefaultDisplay();
		Logger.LogInfo("Orientation: " + display.getOrientation());
		mHomeWidth = display.getWidth() * 2;
		mHomeWidth = Math.min(mHomeWidth, getWallpaperDesiredMinimumWidth());
		return mHomeWidth;
	}
	
	public int getHomeHeight()
	{
		if(mHomeHeight > 0) return mHomeHeight;
		Display display = getWindow().getWindowManager().getDefaultDisplay();
		mHomeHeight = display.getHeight();
		mHomeHeight = Math.min(mHomeHeight, getWallpaperDesiredMinimumHeight());
		return mHomeHeight;
	}

    /* A helper for a set of "show a toast" methods */
	protected void showToast(final String message)  {
		Log.i(WallChanger.LOG_KEY, "Made Toast: " + message);
        showToast(message, Toast.LENGTH_SHORT);
    }
	protected void showToast(final int iStringResource) { showToast(getResourceString(iStringResource)); }
	protected void showToast(final String message, final int toastLength)  {
        runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(getBaseContext(), message, toastLength).show();
            }
        });
    }
	
	@Override
	protected void onRestart() {
		super.onRestart();
		Logger.LogVerbose("onRestart :: " + this.toString());
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		Logger.LogVerbose("onResume :: " + this.toString());
	}
	
	@Override
	public void onLowMemory() {
		super.onLowMemory();
		Logger.LogWarning("Low memory!");
	}
	
	@Override
	public Object onRetainNonConfigurationInstance() {
		Logger.LogVerbose("onRetainNonConfigurationInstance :: " + this.toString());
		return super.onRetainNonConfigurationInstance();
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		Logger.LogVerbose("onStart :: " + this.toString());
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		Logger.LogVerbose("onStop :: " + this.toString());
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		Logger.LogVerbose("onPause :: " + this.toString());
	}
	
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		Logger.LogVerbose("onRestoreInstanceState :: " + this.toString());
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		Logger.LogVerbose("onDestroy :: " + this.toString());
	}

}
