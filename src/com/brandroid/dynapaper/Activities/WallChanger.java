package com.brandroid.dynapaper.Activities;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.method.KeyListener;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.brandroid.dynapaper.Preferences;
import com.brandroid.dynapaper.R;
import com.google.ads.*;

public class WallChanger extends Activity implements OnClickListener {
	public static final String LOG_KEY = "WallChanger";
	public static final String EXTRA_SHORTCUT = "WallChangerShortcut";
	public static final String MY_AD_UNIT_ID = "a14d9c70f03d5b2";
	public static final String MY_ROOT_URL = "http://android.brandonbowles.com";
	public static final int VISIBLE = android.view.View.VISIBLE;
	public static final int INVISIBLE = android.view.View.INVISIBLE;
	public static final int GONE = android.view.View.GONE;
	public static final int DOWNLOADING = R.string.s_downloading;
	public static final int CREATE = R.string.s_create;
	public static final int SHORTCUT = R.string.s_shortcut; 
	public static final int APP_NAME = R.string.app_name;
	//private LinearLayout mMainLayout;
	private TextView mText1;
	private EditText mTextUrl;
	private Button mBtnSelect, mBtnTest, mBtnCurrent, mBtnUndo;
	private ProgressBar mProgressBar;
	private ImageView mImgSample;
	private Intent mIntent;
	private Boolean mSilent = false;
	private String mDynaUrl = "";
	private String mBaseUrl = "";
	private Resources mResources;
	private Preferences prefs;
	private Bitmap mCacheBitmap;
	private String mCacheUrl;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        final Intent intent = mIntent = getIntent();
        final String action = intent.getAction();

        if(mIntent == null)
        	mIntent = new Intent();
        
        mResources = getResources();

        prefs = Preferences.getPreferences(this);
        mSilent = prefs.getBoolean("silentMode", mSilent);
        mDynaUrl = prefs.getString("dynaUrl", mDynaUrl);
        mBaseUrl = prefs.getSetting("baseUrl", mBaseUrl);
        if(mDynaUrl != "" && mDynaUrl.indexOf("i1=") == -1 && mBaseUrl != "")
        	mDynaUrl += "&i1=" + mBaseUrl;
        
        String sData = mIntent.getStringExtra(EXTRA_SHORTCUT);
        if(sData == null) sData = "";
        
        if(action.equals(Intent.ACTION_CREATE_SHORTCUT) ||
           (action.equals(Intent.ACTION_MAIN) && sData == ""))
        {
        	if(action.equals(Intent.ACTION_CREATE_SHORTCUT))
        		setTitle(getResourceString(R.string.create_shortcut));
            requestWindowFeature(Window.FEATURE_LEFT_ICON);
            getWindow().setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, R.drawable.icon);
            setContentView(R.layout.main);
            //mMainLayout = (LinearLayout)findViewById(R.id.mainLayout);
        	if(mDynaUrl=="")
        	{
        		Display display = getWindow().getWindowManager().getDefaultDisplay();
        		mDynaUrl = MY_ROOT_URL + "/images/weather.php?format=image&source=google" +
        			"&x=26&w=" + (display.getWidth() * 2) + "&h=" + display.getHeight();
        		if(mBaseUrl != "")
        			mDynaUrl += "&i1=" + mBaseUrl;
        	}
        	mText1 = (TextView)findViewById(R.id.lblTitle);
        	mText1.setVisibility(GONE);
        	ImageView mImageIcon = (ImageView)findViewById(R.id.imageIcon);
        	if(mImageIcon != null) mImageIcon.setVisibility(GONE);
        	mTextUrl = (EditText)findViewById(R.id.txtURL);
            mBtnSelect = (Button)findViewById(R.id.btnSelect);
            mBtnCurrent = (Button)findViewById(R.id.btnCurrent);
            mBtnUndo = (Button)findViewById(R.id.btnUndo);
            mBtnTest = (Button)findViewById(R.id.btnTest);
            mBtnUndo.setEnabled(false);
            mImgSample = (ImageView)findViewById(R.id.imageSample);
            mImgSample.setVisibility(GONE);
            mProgressBar = (ProgressBar)findViewById(R.id.progressBar1);
            mProgressBar.setVisibility(GONE);
            mTextUrl.setText(mDynaUrl);
            
            if(mBaseUrl != "" && !mIntent.getAction().equals(Intent.ACTION_CREATE_SHORTCUT))
            	mBtnUndo.setEnabled(true);
           
        	if(action.equals(Intent.ACTION_CREATE_SHORTCUT))
            {
            	mText1.setText(getResourceString(R.string.create_shortcut));
            	mBtnSelect.setText(getResourceString(CREATE));
            } else {
            	mText1.setText(getResourceString(R.string.app_name));
            }
            mBtnSelect.setOnClickListener(this);
            mBtnTest.setOnClickListener(this);
            mBtnCurrent.setOnClickListener(this);
            mBtnUndo.setOnClickListener(this);
        
            addAds();

        	return;
        }
        
        if (mIntent.getDataString() != null) {
        	sData = mIntent.getDataString();
        	sData = java.net.URLDecoder.decode(sData);
        	prefs.setSetting("dynaUrl", sData);
        }
        
    	//setContentView(R.layout.popup);
    	//getWindow().setFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND,
        //        WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
        if(sData.startsWith("http"))
        {
        	new DownloadImageTask().execute(sData);
        	sData = getResourceString(DOWNLOADING) + " " + sData;
        	Toast.makeText(getApplicationContext(), (CharSequence)sData, Toast.LENGTH_SHORT).show();
        }
        else if(sData.startsWith("/") && FileExists(sData))
        {
        	setHomeWallpaper(BitmapFactory.decodeFile(sData));
        }
        finish();
    }
    
    public Boolean setHomeWallpaper(Bitmap bmp)
    {
    	try {
   			setWallpaper(bmp);
    		Toast.makeText(getApplicationContext(), getResourceString(R.string.s_updated), Toast.LENGTH_SHORT).show();
            return true;
        } catch (Exception ex) {
        	Log.e(LOG_KEY, "Wallchanger Exception during update: " + ex.toString());
        	Toast.makeText(getApplicationContext(), getResourceString(R.string.s_invalid), Toast.LENGTH_SHORT).show();
            return false;
        }

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
	        // Add the adView to it
	        layout.addView(adView);
	        // Initiate a generic request to load it with an ad
	        AdRequest ad = new AdRequest();
	        ad.setTesting(true);
	        adView.loadAd(new AdRequest());
    	} catch(Exception ex) { }    
    }
    
    private void DoLog(String txt)
    {
    	Log.e(LOG_KEY, txt);
    }

	@Override
	public void onClick(View v)
	{
		String sUrl = mTextUrl.getText().toString();
		if(v.getId() == R.id.btnSelect)
		{
			if(mIntent.getAction().equals(Intent.ACTION_CREATE_SHORTCUT))
			{
				setupShortcut(sUrl);
				finish();
			} else {
				if(mCacheUrl == null || mCacheBitmap == null || !mCacheUrl.equalsIgnoreCase(sUrl))
				{
					mProgressBar.setVisibility(VISIBLE);
					prefs.setSetting("dynaUrl", sUrl);
					mSilent = true;
		        	Toast.makeText(getApplicationContext(), (CharSequence)getResourceString(R.string.s_downloading) + " " + sUrl, Toast.LENGTH_SHORT).show();
		    		new DownloadImageTask().execute(sUrl);
				} else {
					setHomeWallpaper(mCacheBitmap);
					finish();
				}
			}
		}
		else if (v.getId() == R.id.btnTest)
		{
			if(mCacheUrl == null || !mCacheUrl.equalsIgnoreCase(sUrl))
			{
				mProgressBar.setVisibility(VISIBLE);
				new TestDownloadTask().execute(sUrl);
			}
		} else if (v.getId() == R.id.btnUndo)
		{
			String url = MY_ROOT_URL + "/images/" + mBaseUrl;
			mTextUrl.setText(url);
			mTextUrl.setEnabled(false);
			mProgressBar.setVisibility(VISIBLE);
			mBtnCurrent.setEnabled(false);
			mBtnSelect.setEnabled(false);
			mBtnUndo.setEnabled(false);
			mBtnTest.setEnabled(false);
			prefs.setSetting("baseUrl", null);
			prefs.setSetting("dynaUrl", null);
			mSilent = true;
			new DownloadImageTask().execute(url);
		} else if (v.getId() == R.id.btnCurrent)
		{
			Toast.makeText(getApplicationContext(), getResourceString(R.string.s_creating_base), Toast.LENGTH_LONG).show();
    		mTextUrl.setEnabled(false);
			mBtnSelect.setEnabled(false);
			mBtnTest.setEnabled(false);
			mProgressBar.setVisibility(VISIBLE);
    		Bitmap bmp = ((BitmapDrawable)getWallpaper()).getBitmap();
    		new UploadTask().execute(bmp);
		}
	}
	        
    private void setupShortcut(String sUrl)
    {
        Intent shortcutIntent = new Intent(Intent.ACTION_MAIN);
        shortcutIntent.setClassName(this, this.getClass().getName() + "Hidden");
        shortcutIntent.putExtra(EXTRA_SHORTCUT, sUrl);

        // Then, set up the container intent (the response to the caller)

        Intent intent = new Intent();
        intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
        intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, getResourceString(R.string.app_name));
        intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, 
        		Intent.ShortcutIconResource.fromContext(this, R.drawable.icon));

        // Now, return the result to the launcher

        setResult(RESULT_OK, intent);
    }
    
    private boolean FileExists(String sFilename)
    {
    	boolean ret = false;
    	try {
    		ret = new File(sFilename).exists();
    	} catch(Exception fnfe) { ret = false; }
    	return ret;
    }
    
    private String getResourceString(int stringResourceID)
    {
    	return mResources.getString(stringResourceID);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
    	getMenuInflater().inflate(R.menu.main_menu, menu);
    	return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
    	switch(item.getItemId())
    	{
    	case R.id.menu_select:
    		onClick(mBtnSelect);
    		break;
    	case R.id.menu_test:
    		onClick(mBtnTest);
    		break;
    	case R.id.menu_current:
    		onClick(mBtnCurrent);
    		break;
    	case R.id.menu_undo:
    		onClick(mBtnUndo);
    		break;
    	case R.id.menu_about:
    		startActivity(new Intent(MY_ROOT_URL));
    		break;
    	case R.id.menu_help:
    		
    		break;
    	}
    	return true;
    }
    
	private class UploadTask extends AsyncTask<Bitmap, Void, String>
	{

		@Override
		protected String doInBackground(Bitmap... arg0)
		{
			URL url = null;
			URLConnection con = null;
			OutputStream out = null;
			InputStream in = null;
			StringBuilder ret = new StringBuilder();
			InputStreamReader sr = null;
			try {
				url = new URL(MY_ROOT_URL + "/images/upload.php");
				con = url.openConnection();
				con.setDoOutput(true);
				out = con.getOutputStream();
				arg0[0].compress(CompressFormat.JPEG, 90, out);
				out.flush();
				in = con.getInputStream();
				sr = new InputStreamReader(in);
				char[] buf = new char[512];
				while(sr.read(buf) > 0)
				{
					ret.append(buf);
					if(buf.length < 512)
						break;
				}
			} catch(Exception ex) {
				DoLog("Exception Uploading. " + ex.toString());
			}
			finally {
				try {
					if(out!=null) out.close();
				} catch(IOException ex) { DoLog("Trying to close output. " + ex.toString()); }
				try {
					if(sr!=null) sr.close();
				}catch(IOException ex) { DoLog("Trying to close Stream Reader. " + ex.toString()); }
				try {
					if(in!=null) in.close();
				}catch(IOException ex) { DoLog("Trying to close input. " + ex.toString()); }
				
			}
			return ret.toString();
		}
		@Override
		protected void onPostExecute(String result) {
			mBaseUrl = result.replaceAll("[^A-Za-z0-9\\.\\/]", "");
			prefs.setSetting("baseUrl", mBaseUrl);
			if(mDynaUrl.indexOf("&i1=") > -1)
				mDynaUrl = mDynaUrl.substring(0, mDynaUrl.indexOf("&i1="));
			mDynaUrl += "&i1=" + mBaseUrl;
			mTextUrl.setText(mDynaUrl);
			mBtnUndo.setEnabled(true);
			mTextUrl.setEnabled(true);
			mBtnSelect.setEnabled(true);
			mBtnTest.setEnabled(true);
			mProgressBar.setVisibility(GONE);
			mImgSample.setImageDrawable(getWallpaper());
			mImgSample.setVisibility(VISIBLE);
		}
	}

	private class TestDownloadTask extends AsyncTask<String, Void, Bitmap>
    {

		@Override
		protected Bitmap doInBackground(String... urls)
		{
			Bitmap ret = null;
        	InputStream s = null;
        	try {
        		URL url = new URL(urls[0]);
        		s = url.openStream();
        		ret = BitmapFactory.decodeStream(s);
        		mCacheUrl = urls[0];
        	} catch(Exception ex) {
        		Log.e(LOG_KEY, ex.toString());
        		//Toast.makeText(getApplicationContext(), (CharSequence)ex.toString(), Toast.LENGTH_LONG).show();
        	}
        	finally {
        		try {
        			if(s != null)
        				s.close();
        		} catch(IOException ex) { Log.e(LOG_KEY, ex.toString()); }
        	}
        	return ret;
		}
		
		@Override
		protected void onPostExecute(Bitmap result)
		{
			mProgressBar.setVisibility(GONE);
			if(result == null)
				Toast.makeText(getApplicationContext(), (CharSequence)"Invalid Image!", Toast.LENGTH_SHORT).show();
			else
			{
				mCacheBitmap = result;
				mImgSample.setImageBitmap(result);
				mImgSample.setVisibility(VISIBLE);
			}
		}
		
		@Override
		protected void onProgressUpdate(Void... values) {
			mProgressBar.setVisibility(VISIBLE);
		}
    	
    }
    
    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        /** The system calls this to perform work in a worker thread and
          * delivers it the parameters given to AsyncTask.execute() */
        protected Bitmap doInBackground(String... urls)
        {
        	Bitmap ret = null;
        	InputStream s = null;
        	try {
        		mCacheUrl = urls[0];
        		URL url = new URL(mCacheUrl);
        		s = url.openStream();
        		ret = BitmapFactory.decodeStream(s);
        	} catch(IOException ex) { Log.e(LOG_KEY, ex.toString()); }
        	finally {
        		try {
        			if(s != null)
        				s.close();
        		} catch(IOException ex) { Log.e(LOG_KEY, ex.toString()); }
        	}
        	return ret;
        }
        
        /** The system calls this to perform work in the UI thread and delivers
          * the result from doInBackground() */
        protected void onPostExecute(Bitmap result) {
        	setHomeWallpaper(result);
        	mCacheBitmap = result;
        	if(mProgressBar != null)
        		mProgressBar.setVisibility(GONE);
        	if(mSilent)
        		finish();
        }
    }
    
    @Override
    protected void onPause() {
    	// TODO Auto-generated method stub
    	super.onPause();
    }
    @Override
    protected void onResume() {
    	// TODO Auto-generated method stub
    	super.onResume();
    }
    @Override
    protected void onStart() {
    	// TODO Auto-generated method stub
    	super.onStart();
    }
    @Override
    protected void onStop() {
    	// TODO Auto-generated method stub
    	super.onStop();
    	if(mSilent)
    		finish();
    }
    @Override
    protected void onDestroy() {
    	// TODO Auto-generated method stub
    	super.onDestroy();
    }
}