package com.brandroid.dynapaper.Activities;

import com.brandroid.Logger;
import com.brandroid.dynapaper.Prefs;
import com.brandroid.dynapaper.R;
import com.brandroid.dynapaper.WallChanger;
import com.mobclix.android.sdk.MobclixAdView;
import com.mobclix.android.sdk.MobclixAdViewListener;
import com.mobclix.android.sdk.MobclixMMABannerXLAdView;

import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;
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
		
		Logger.LogVerbose("onCreate :: " + this.toString());

        mResources = getResources();
        prefs = Prefs.getPreferences(getApplicationContext());
        
        if(!WallChanger.isPaidMode())
        	addAds();
        else if(findViewById(R.id.adLayout)!=null)
        	findViewById(R.id.adLayout).setVisibility(View.GONE);
        	
	}
	
	public void addAds()
	{
		LinearLayout layout = (LinearLayout)findViewById(R.id.adLayout);
        if(layout == null) {
        	Logger.LogWarning("Unable to add Ads.");
        	return;
        }
    	try {
    		Logger.LogVerbose("Making request for Mobclix ads");
    		MobclixAdView adView = new MobclixMMABannerXLAdView(this);
    		adView.addMobclixAdViewListener(new MobclixAdViewListener() {
				
				@Override
				public void onSuccessfulLoad(MobclixAdView adView) {
					Logger.LogVerbose("Mobclix successfully added");
				}
				
				@Override
				public boolean onOpenAllocationLoad(MobclixAdView adView, int arg1) {
					Logger.LogVerbose("Mobclix open allocation requested");
					return false;
				}
				
				@Override
				public void onFailedLoad(MobclixAdView arg0, int arg1) {
					Logger.LogWarning("Mobclix unable to load!");
					
				}
				
				@Override
				public void onCustomAdTouchThrough(MobclixAdView arg0, String arg1) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void onAdClick(MobclixAdView arg0) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public String keywords() {
					// TODO Auto-generated method stub
					return null;
				}

				@Override
				public String query() {
					// TODO Auto-generated method stub
					return null;
				}
			});
    		
	    	// Create the adView
	        //AdView adView = new AdView(this, AdSize.BANNER, WallChanger.MY_AD_UNIT_ID);
	        // Lookup your LinearLayout assuming its been given
	        // the attribute android:id="@+id/mainLayout"
	        
	        // Add the adView to it
	        layout.addView(adView);
	        adView.getAd();
	        // Initiate a generic request to load it with an ad
	        //AdRequest ad = new AdRequest();
	        //ad.setTesting(WallChanger.isTesting());
	        //adView.loadAd(ad);
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
