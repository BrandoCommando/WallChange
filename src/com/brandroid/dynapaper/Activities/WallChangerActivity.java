package com.brandroid.dynapaper.Activities;

import com.brandroid.dynapaper.Preferences;
import com.brandroid.dynapaper.R;
import com.google.ads.AdRequest;
import com.google.ads.AdSize;
import com.google.ads.AdView;

import android.app.Activity;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

public class WallChangerActivity extends Activity
{
	public static final String LOG_KEY = "WallChanger";
	public static final String MY_AD_UNIT_ID = "a14d9c70f03d5b2";
	public static final String MY_ROOT_URL = "http://android.brandonbowles.com";
	public static final String MY_ROOT_URL_GS = "http://commondatastorage.googleapis.com/data.brandonbowles.com";
	public static final String MY_IMAGE_ROOT_URL = MY_ROOT_URL + "/images/";
	public static final String MY_IMAGE_ROOT_URL_GS = MY_ROOT_URL_GS + "/images/";
	public static final String ONLINE_GALLERY_URL = MY_ROOT_URL + "/dynapaper/gallery.php";
	public static final String ONLINE_IMAGE_URL = MY_ROOT_URL + "/dynapaper/get_image.php";
	public static final String EXTERNAL_ROOT = "/mnt/sdcard/wallchanger/";
	public static final int REQ_SELECT_GALLERY = 1;
	public static final int REQ_SELECT_ONLINE = 2;
	public final static int REQ_UPDATE_GALLERY = 101;
	protected Resources mResources;
	protected Preferences prefs;
	protected Cursor mGalleryCursor;
	private static String mUser = "";
	private int mHomeWidth = 0;
	private int mHomeHeight = 0;
	private final static int mUploadQuality = 100;
	private final static Boolean bPaidMode = false;
	private final static Boolean bTesting = true;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

        mResources = getResources();
        prefs = Preferences.getPreferences(getApplicationContext());
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
	        AdView adView = new AdView(this, AdSize.BANNER, MY_AD_UNIT_ID);
	        // Lookup your LinearLayout assuming it’s been given
	        // the attribute android:id="@+id/mainLayout"
	        LinearLayout layout = (LinearLayout)findViewById(R.id.adLayout);
	        if(layout == null) return;
	        // Add the adView to it
	        layout.addView(adView);
	        // Initiate a generic request to load it with an ad
	        AdRequest ad = new AdRequest();
	        ad.setTesting(bTesting);
	        adView.loadAd(ad);
    	} catch(Exception ex) { Log.e(LOG_KEY, "Error adding ads: " + ex.toString()); }    
    }
	
	public void LogError(String msg)
	{
		Log.e(LOG_KEY, msg);
	}
	
	public final static Boolean isPaidMode() { return bPaidMode; }
    
    protected String getResourceString(int stringResourceID)
    {
    	return mResources.getString(stringResourceID);
    }
    
    protected String getImageThumbUrl(String sBase)
    {
    	return MY_ROOT_URL + "/images/thumb.php?url=" + sBase.substring(sBase.lastIndexOf("/") + 1).replace("?", "&");
    }
    
    protected String getImageFullUrl(String sBase)
    {
    	if(sBase.contains("//") && !sBase.startsWith(MY_ROOT_URL))
    		return sBase;
    	return ONLINE_IMAGE_URL + "?url=" + sBase.substring(sBase.lastIndexOf("/") + 1);
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
	
	public static int getUploadQuality() {
		return mUploadQuality;
	}

	public static Bitmap getSizedBitmap(Bitmap bmp, int mw, int mh)
	{
		int w = bmp.getWidth();
		int h = bmp.getHeight();
		Log.d(LOG_KEY, "Bitmap Size: " + w + "x" + h + "  Max Size: " + mw + "x" + mh);
		if(w > mw && h > mh)
		{
			if((w - mw) < (h - mh))
			{
				double r = (double)h / (double)w;
				w = mw;
				h = (int)Math.floor(r * (double)w);
			} else {
				double r = (double)w / (double)h;
				h = mh;
				w = (int)Math.floor(r * (double)h);
			}
			Log.d(LOG_KEY, "Resizing to " + w + "x" + h);
			try {
				bmp = Bitmap.createScaledBitmap(bmp, w, h, true);
				//bmp.recycle();
				//return ret;
			} catch(Exception ex) {
				Log.e(LOG_KEY, "Resizing Failed. Using original.");
			}
		} else Log.d(LOG_KEY, "No resizing needed.");
		return bmp;
	}

    /* A helper for a set of "show a toast" methods */
	protected void showToast(final String message)  {
		Log.i(LOG_KEY, "Made Toast: " + message);
        showToast(message, Toast.LENGTH_SHORT);
    }
	protected void showToast(final String message, final int toastLength)  {
        runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(getBaseContext(), message, toastLength).show();
            }
        });
    }

	public static void setUser(String mUser) {
		WallChangerActivity.mUser = mUser;
	}

	public static String getUser() { return getUser("",""); }
	public static String getUser(String prefix) { return getUser(prefix,""); }
	public static String getUser(String prefix, String suffix) {
		if(mUser != null && mUser != "")
			return prefix + mUser + suffix;
		else return "";
	}
}
