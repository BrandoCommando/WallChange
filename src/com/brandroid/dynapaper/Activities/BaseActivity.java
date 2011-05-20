package com.brandroid.dynapaper.Activities;

import com.brandroid.dynapaper.Prefs;
import com.brandroid.dynapaper.R;
import com.brandroid.dynapaper.WallChanger;
import com.google.ads.AdRequest;
import com.google.ads.AdSize;
import com.google.ads.AdView;

import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
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

        mResources = getResources();
        prefs = Prefs.getPreferences(getApplicationContext());
        
        if(WallChanger.isPaidMode())
        	addAds();
	}
	
	public void addAds()
    {
    	/*
    	Log.i(LOG_KEY, "IAB_LEADERBOARD: " + AdSize.IAB_LEADERBOARD.getWidth() + "x" + AdSize.IAB_LEADERBOARD.getHeight());
    	Log.i(LOG_KEY, "IAB_MRECT: " + AdSize.IAB_MRECT.getWidth() + "x" + AdSize.IAB_MRECT.getHeight());
    	Log.i(LOG_KEY, "IAB_BANNER: " + AdSize.IAB_BANNER.getWidth() + "x" + AdSize.IAB_BANNER.getHeight());
    	Log.i(LOG_KEY, "BANNER: " + AdSize.BANenabledNER.getWidth() + "x" + AdSize.BANNER.getHeight());
    	*/
    	//AdSize adsize = new AdSize(getWindowSize()[0], AdSize.BANNER.getHeight());
    	
    	try {
	    	// Create the adView
	        AdView adView = new AdView(this, AdSize.BANNER, WallChanger.MY_AD_UNIT_ID);
	        // Lookup your LinearLayout assuming its been given
	        // the attribute android:id="@+id/mainLayout"
	        LinearLayout layout = (LinearLayout)findViewById(R.id.adLayout);
	        if(layout == null) return;
	        // Add the adView to it
	        layout.addView(adView);
	        // Initiate a generic request to load it with an ad
	        AdRequest ad = new AdRequest();
	        ad.setTesting(WallChanger.isTesting());
	        adView.loadAd(ad);
    	} catch(Exception ex) { Log.e(WallChanger.LOG_KEY, "Error adding ads: " + ex.toString()); }    
    }

	public void LogError(String msg) { WallChanger.LogError(msg); }
	public void LogError(String msg, Throwable ex) { WallChanger.LogError(msg, ex); }
	public void LogWarning(String msg) { WallChanger.LogWarning(msg); }
	public void LogInfo(String msg) { WallChanger.LogInfo(msg); }
	public void LogDebug(String msg) { WallChanger.LogDebug(msg); }
	
    protected String getResourceString(int stringResourceID)
    {
    	return mResources.getString(stringResourceID);
    }
	

	
	public int getHomeWidth()
	{
		if(mHomeWidth > 0) return mHomeWidth;
		Display display = getWindow().getWindowManager().getDefaultDisplay();
		mHomeWidth = display.getWidth() * 2;
		return mHomeWidth;
	}
	
	public int getHomeHeight()
	{
		if(mHomeHeight > 0) return mHomeHeight;
		Display display = getWindow().getWindowManager().getDefaultDisplay();
		mHomeHeight = display.getHeight();
		return mHomeHeight;
	}

    /* A helper for a set of "show a toast" methods */
	protected void showToast(final String message)  {
		Log.i(WallChanger.LOG_KEY, "Made Toast: " + message);
        showToast(message, Toast.LENGTH_SHORT);
    }
	protected void showToast(final String message, final int toastLength)  {
        runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(getBaseContext(), message, toastLength).show();
            }
        });
    }

}
