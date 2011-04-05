package com.brandroid.dynapaper;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import android.app.Activity;
import android.app.WallpaperManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class WallChanger extends Activity implements OnClickListener {
	public static final String LOG_KEY = "WallChanger";
	public static final String PREFS_NAME = "WallChangerPrefs";
	public static final String EXTRA_SHORTCUT = "WallChangerShortcut";
	public static final int VISIBLE = android.view.View.VISIBLE;
	public static final int INVISIBLE = android.view.View.INVISIBLE;
	public static final int GONE = android.view.View.GONE;
	private TextView mText1;
	private EditText mTextUrl;
	private Button mBtnSelect, mBtnCancel, mBtnTest;
	private ProgressBar mProgressBar;
	private ImageView mImgSample;
	private Intent mIntent;
	private Boolean mSilent = false;
	private String mDynaUrl = "";
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        final Intent intent = mIntent = getIntent();
        final String action = intent.getAction();

        if(mIntent == null)
        	mIntent = new Intent();
        
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, 0);
        mSilent = prefs.getBoolean("silentMode", mSilent);
        mDynaUrl = prefs.getString("dynaUrl", mDynaUrl);
        
        String sData = mIntent.getStringExtra(EXTRA_SHORTCUT);
        if(sData == null) sData = "";
        
        if(action.equals(Intent.ACTION_CREATE_SHORTCUT) ||
           (action.equals(Intent.ACTION_MAIN) && sData == ""))
        {
            requestWindowFeature(Window.FEATURE_LEFT_ICON);
            getWindow().setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, R.drawable.icon);
            setContentView(R.layout.main);
        	mText1 = (TextView)findViewById(R.id.txtTitle);
        	mText1.setVisibility(GONE);
        	findViewById(R.id.imageIcon).setVisibility(GONE);
        	mTextUrl = (EditText)findViewById(R.id.editText1);
            mBtnSelect = (Button)findViewById(R.id.btnSelect);
            mBtnCancel = (Button)findViewById(R.id.btnCancel);
            mBtnTest = (Button)findViewById(R.id.btnTest);
            mImgSample = (ImageView)findViewById(R.id.imageSample);
            mImgSample.setVisibility(GONE);
            mProgressBar = (ProgressBar)findViewById(R.id.progressBar1);
            mProgressBar.setVisibility(GONE);
            mTextUrl.setText(mDynaUrl);
            if(action.equals(Intent.ACTION_CREATE_SHORTCUT))
            {
            	mText1.setText(getResources().getString(R.string.create_shortcut));
            	mBtnSelect.setText(getResources().getString(R.string.create));
            } else {
            	mText1.setText(getResources().getString(R.string.app_name));
            }
            mBtnSelect.setOnClickListener(this);
            mBtnCancel.setOnClickListener(this);
            mBtnTest.setOnClickListener(this);
        	return;
        }
        
        if (mIntent.getDataString() != null) {
        	sData = mIntent.getDataString();
        	sData = java.net.URLDecoder.decode(sData);
        	SharedPreferences.Editor editor = prefs.edit();
    		editor.putString("dynaUrl", sData);
    		editor.commit();
        }
        
    	//setContentView(R.layout.popup);
    	//getWindow().setFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND,
        //        WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
        if(sData.startsWith("http"))
        {
        	new DownloadImageTask().execute(sData);
        	sData = "Downloading " + sData;
        	Toast.makeText(getApplicationContext(), (CharSequence)sData, Toast.LENGTH_SHORT).show();
        }
        else if(sData.startsWith("/") && FileExists(sData))
        {
        	FileInputStream fis = null;
        	try {
        		fis = new FileInputStream(sData);
        		WallpaperManager.getInstance(getApplicationContext()).setStream(fis);
        	} catch(IOException ex) { Log.e(LOG_KEY, ex.toString()); }
        }
        finish();
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
			} else {
	        	SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, 0).edit();
	    		editor.putString("dynaUrl", sUrl);
	    		editor.commit();
	    		Toast.makeText(getApplicationContext(), (CharSequence)"Downloading " + sUrl, Toast.LENGTH_SHORT);
	    		new DownloadImageTask().execute(sUrl);
			}
			finish();
		}
		else if (v.getId() == R.id.btnCancel)
			finish();
		else if (v.getId() == R.id.btnTest)
		{
			mProgressBar.setVisibility(VISIBLE);
			new TestDownloadTask().execute(sUrl);
		}
	}
    
    private void setupShortcut(String sUrl)
    {
        Intent shortcutIntent = new Intent(Intent.ACTION_MAIN);
        shortcutIntent.setClassName(this, this.getClass().getName());
        shortcutIntent.putExtra(EXTRA_SHORTCUT, sUrl);

        // Then, set up the container intent (the response to the caller)

        Intent intent = new Intent();
        intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
        intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, getString(R.string.app_name));
        Parcelable iconResource = Intent.ShortcutIconResource.fromContext(
                this,  R.drawable.icon);
        intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, iconResource);

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
    
    private class TestDownloadTask extends AsyncTask<String, Void, Bitmap> {

		@Override
		protected Bitmap doInBackground(String... urls)
		{
			Bitmap ret = null;
        	InputStream s = null;
        	try {
        		URL url = new URL(urls[0]);
        		s = url.openStream();
        		ret = BitmapFactory.decodeStream(s);
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
        		URL url = new URL(urls[0]);
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
        	try {
        		if(result != null)
        		{
        			WallpaperManager.getInstance(getApplicationContext()).setBitmap(result);
        			Toast.makeText(getApplicationContext(), (CharSequence)"Wallpaper updated!", Toast.LENGTH_SHORT).show();
        		} else
        			Toast.makeText(getApplicationContext(), (CharSequence)"Invalid image!", Toast.LENGTH_SHORT).show();
        		if(mSilent)
        			finish();
			} catch (Exception e) {
				Log.e(LOG_KEY, e.toString());
			}
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