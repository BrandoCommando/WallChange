package com.brandroid.dynapaper.Activities;

import com.brandroid.dynapaper.Preferences;

import android.app.Activity;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.widget.ProgressBar;
import android.widget.Toast;

public class WallChangerActivity extends Activity
{
	public static final String LOG_KEY = "WallChanger";
	public static final String MY_AD_UNIT_ID = "a14d9c70f03d5b2";
	public static final String MY_ROOT_URL = "http://android.brandonbowles.com";
	public static final String MY_IMAGE_ROOT_URL = "http://data.brandonbowles.com/images/";
	public static final String ONLINE_GALLERY_URL = MY_ROOT_URL + "/dynapaper/gallery.php";
	public static final String ONLINE_IMAGE_URL = MY_ROOT_URL + "/dynapaper/get_image.php";
	public static final int REQ_SELECT_GALLERY = 1;
	public static final int REQ_SELECT_ONLINE = 2;
	public final static int REQ_UPDATE_GALLERY = 101;
	protected Resources mResources;
	protected Preferences prefs;
	protected Cursor mGalleryCursor;
	protected String mUser = "";
	private int mHomeWidth = 0;
	private int mHomeHeight = 0;
	public final static int mUploadQuality = 100;
	private final static Boolean bPaidMode = false; 
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

        mResources = getResources();
        prefs = Preferences.getPreferences(this);
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
    	return MY_IMAGE_ROOT_URL + "full/" + sBase.substring(sBase.lastIndexOf("/") + 1);
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
				return Bitmap.createScaledBitmap(bmp, w, h, true);
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
}
