package com.brandroid.dynapaper.Activities;

import java.sql.Date;

import com.brandroid.util.CustomExceptionHandler;
import com.brandroid.util.Logger;
import com.brandroid.dynapaper.Prefs;
import com.brandroid.dynapaper.R;
import com.brandroid.dynapaper.WallChanger;
import com.brandroid.dynapaper.Database.LoggerDbAdapter;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.text.format.Time;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.MenuItem;
import android.view.View;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Toast;

public class BaseActivity extends Activity implements OnClickListener, OnMenuItemClickListener 
{
	protected static ArrayAdapter<String> mPastZips;
	
	private int mHomeWidth = 0;
	private int mHomeHeight = 0;
	protected Resources mResources;
	protected Prefs prefs;
	
	protected Boolean mAdBannerLoaded = false;
	protected Boolean mAdFullLoaded = false;
		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if(mPastZips == null)
			mPastZips = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_dropdown_item_1line);
		
		if(!Logger.hasDb())
			Logger.setDb(new LoggerDbAdapter(this));
		
		try {
			PackageManager pm = getPackageManager();
			PackageInfo pi = pm.getPackageInfo("com.brandroid.dynapaper", PackageManager.GET_META_DATA);
			WallChanger.VERSION_CODE = pi.versionCode;
			Logger.LogInfo("Version Code: " + pi.versionCode);
		} catch (NameNotFoundException e) {
			Logger.LogError("Couldn't read build info", e);
		}
		
		//Thread.setDefaultUncaughtExceptionHandler(new CustomExceptionHandler());
		
		Logger.LogVerbose("onCreate :: " + this.toString());

		mResources = getResources();
        if(WallChanger.Prefs == null)
        {
        	WallChanger.Prefs = Prefs.getPreferences(getApplicationContext());
        	WallChanger.setUser(WallChanger.Prefs.getSetting("user", "user" + ((int)Math.floor(Math.random() * 10000000))));
        }
        prefs = WallChanger.Prefs;
        
        //if(!WallChanger.isPaidMode()) addAds();
        if(findViewById(R.id.adLayout)!=null)
        	findViewById(R.id.adLayout).setVisibility(View.GONE);
        	
	}
	
	/*
	public void addAds() // Mobclix version
	{
		LinearLayout adLayout = (LinearLayout)findViewById(R.id.adLayout);
		if(adLayout == null) return;
		if(mAdBanner == null)
		{
			mAdBanner = new MobclixMMABannerXLAdView(this);
			if(mAdBanner.addMobclixAdViewListener(this))
				Logger.LogDebug("Requesting ad: " + mAdBanner.toString());
			else
				Logger.LogWarning("Could not attach to Mobclix!");
		}
		adLayout.addView(mAdBanner);
		if(!mAdBannerLoaded)
		{
			mAdBanner.setVisibility(View.GONE);
			mAdBanner.getAd();
		}
		if(mAdFullScreen == null)
		{
			mAdFullScreen = new MobclixFullScreenAdView(this);
			mAdFullScreen.addMobclixAdViewListener(this);
			mAdFullLoaded = false;
			mAdFullScreen.requestAd();
		}
	}*/
	
	/*
	public void addAds()
    {	
    	try {
	    	// Create the adView
    		Time t = new Time();
    		int iAdToUse = t.hour % (WallChanger.MY_AD_UNIT_ID.length + 1);
    		String sAdID = WallChanger.MY_AD_UNIT_ID[iAdToUse];
    		Logger.LogInfo("Using Ad ID #" + iAdToUse + " for Admob - " + sAdID);
    		
    		DisplayMetrics dm = getResources().getDisplayMetrics();
    		int density = (int)dm.density;
    		int iShortDimension = Math.min(dm.widthPixels, dm.heightPixels);
    		int iLongDimension = Math.max(dm.widthPixels, dm.heightPixels);
    		AdSize adsize = AdSize.BANNER;
    		if(iShortDimension > 480)
    		{
    			Logger.LogDebug("Using Tablet Ad Size because screen is " + iShortDimension + "x" + iLongDimension);
    			adsize = AdSize.IAB_MRECT;
    		}
    		
    		AdView adView = new AdView(this, adsize, sAdID);
    		
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
		        ad.addTestDevice(AdRequest.TEST_EMULATOR);
	        	ad.addTestDevice("383A6E6B957E2A18C8830E6C431B2AAF");
	        } else ad.setTesting(false);
        	
	        ad.setLocation(WallChanger.getLastLocation());
	        adView.loadAd(ad);
    	} catch(Exception ex) { Logger.LogWarning("Error adding ads.", ex); }    
    }
	*/
    protected String getResourceString(int... resourceIDs)
    {
    	StringBuilder ret = new StringBuilder();
    	for(int i = 0; i < resourceIDs.length; i++)
    	{
    		ret.append(getText(resourceIDs[i]));
    		ret.append(" ");
    	}
    	ret.setLength(ret.length() - 1); // remove last space
    	return ret.toString();
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
		Logger.LogWarning("Low memory!", new Exception());
	}
	
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		Logger.LogVerbose("onRestoreInstanceState :: " + this.toString());
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
	protected void onDestroy() {
		super.onDestroy();
		Logger.LogVerbose("onDestroy :: " + this.toString());
	}

	public boolean onMenuItemClick(MenuItem item) {
		Logger.LogDebug("onMenuItemClick :: " + item.toString());
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Logger.LogDebug("onMenuItemClick :: " + item.toString());
		return super.onOptionsItemSelected(item);
	}

	public void onClick(View v) {
		Logger.LogDebug("onClick :: " + v.toString());
	}

	/*
	@Override
	public void onDismissScreen(Ad ad) {
		Logger.LogDebug("Admob present screen for " + this.toString() + ". " + ad.toString());
	}

	@Override
	public void onFailedToReceiveAd(Ad ad, ErrorCode code) {
		Logger.LogDebug("Admob failed to receive ad for " + this.toString() + ". Code " + code + ". " + ad.toString());
	}

	@Override
	public void onLeaveApplication(Ad ad) {
		Logger.LogDebug("Admob left application for " + this.toString() + ". " + ad.toString());
	}

	@Override
	public void onPresentScreen(Ad ad) {
		Logger.LogDebug("Admob present screen for " + this.toString() + ". " + ad.toString());
	}

	@Override
	public void onReceiveAd(Ad ad) {
		Logger.LogDebug("Admob received ad for " + this.toString() + ". " + ad.toString());
	}
	*/
}
