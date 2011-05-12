package com.brandroid.dynapaper.Activities;

import com.brandroid.dynapaper.Preferences;

import android.app.Activity;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
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
	public static final int SELECT_PICTURE = 1;
	public static final int SELECT_ONLINE_PICTURE = 2;
	protected Resources mResources;
	protected Preferences prefs;
	protected Cursor mGalleryCursor;
	protected String mUser = "";
	
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
    
    protected String getResourceString(int stringResourceID)
    {
    	return mResources.getString(stringResourceID);
    }
    
    protected String getImageThumbUrl(String sBase)
    {
    	return MY_IMAGE_ROOT_URL + "thumbs/" + sBase.substring(sBase.lastIndexOf("/") + 1);
    }
    
    protected String getImageFullUrl(String sBase)
    {
    	if(sBase.contains("//") && !sBase.startsWith(MY_ROOT_URL))
    		return sBase;
    	return MY_IMAGE_ROOT_URL + "full/" + sBase.substring(sBase.lastIndexOf("/") + 1);
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
