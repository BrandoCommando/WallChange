package com.brandroid.dynapaper.Activities;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;

import com.brandroid.OnlineGalleryItem;
import com.brandroid.dynapaper.Preferences;
import com.brandroid.dynapaper.R;
import com.google.ads.AdRequest;
import com.google.ads.AdSize;
import com.google.ads.AdView;

import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

public class WallChangerNew extends WallChangerActivity implements OnClickListener
{ 
	private final static int REQUEST_CODE_GALLERY_UPDATE = 101;
	private EditText mTxtURL;
	private ProgressBar mProgressBar;
	private ImageView mImgPreview;
	private Intent mIntent;
	private Bitmap mCacheBitmap;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        
        final Intent intent = mIntent = getIntent();
        final String action = intent.getAction();

        if(mIntent == null)
        	mIntent = new Intent();
        
        setContentView(R.layout.layout_new);
		
		findViewById(R.id.btnCurrent).setOnClickListener(this);
		findViewById(R.id.btnGallery).setOnClickListener(this);
		findViewById(R.id.btnOnline).setOnClickListener(this);
		findViewById(R.id.btnSelect).setOnClickListener(this);
		findViewById(R.id.btnStocks).setOnClickListener(this);
		findViewById(R.id.btnTest).setOnClickListener(this);
		findViewById(R.id.btnUndo).setOnClickListener(this);
		findViewById(R.id.btnURL).setOnClickListener(this);
		findViewById(R.id.btnWeather).setOnClickListener(this);
		
		mTxtURL = (EditText)findViewById(R.id.txtURL);
		mProgressBar = (ProgressBar)findViewById(R.id.progressBar1);
		mImgPreview = (ImageView)findViewById(R.id.imageSample);
		findViewById(R.id.btnUndo).setEnabled(false);
		findViewById(R.id.txtURL).setVisibility(View.GONE);
		findViewById(R.id.progressBar1).setVisibility(View.GONE);
		
		findViewById(R.id.btnOnline).setEnabled(false);
		startActivityForResult(new Intent(this, GalleryUpdater.class), REQUEST_CODE_GALLERY_UPDATE);
		
		addAds();
	}

	@Override
	public void onClick(View v)
	{
		switch(v.getId())
		{
			case R.id.btnCurrent:
				mCacheBitmap = ((BitmapDrawable)getWallpaper()).getBitmap();
				mImgPreview.setImageBitmap(mCacheBitmap);
				break;
			case R.id.btnGallery:
				Intent intentGallery = new Intent();
				intentGallery.setType("image/*");
				intentGallery.setAction(Intent.ACTION_GET_CONTENT);
				startActivityForResult(Intent.createChooser(intentGallery, "Select Picture"), SELECT_PICTURE);
				break;
			case R.id.btnOnline:
				Intent intentOnline = new Intent(this, OnlineGalleryPicker.class);
				intentOnline.putExtra("url", ONLINE_GALLERY_URL);
				intentOnline.setAction(Intent.ACTION_GET_CONTENT);
				intentOnline.setType("image/*");
				startActivityForResult(intentOnline, SELECT_ONLINE_PICTURE);
				break;
			case R.id.btnSelect:
				break;
			case R.id.btnWeather:
				break;
			case R.id.btnStocks:
				break;
			case R.id.btnTest:
				break;
			case R.id.btnURL:
				Boolean bURLMode = mTxtURL.getVisibility() == View.GONE;
				mTxtURL.setVisibility(bURLMode ? View.VISIBLE : View.GONE);
				break;
			case R.id.btnUndo:
				try {
					setWallpaper(mCacheBitmap);
				} catch (IOException e) {
					DoLog(e.toString());
				}
				break;
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(resultCode == RESULT_CANCELED) return;
		if(requestCode == SELECT_PICTURE)
		{
			Uri selUri = data.getData();
			String selPath = getMediaPath(selUri);
			mTxtURL.setText(selPath);
			mCacheBitmap = BitmapFactory.decodeFile(selPath);
			mImgPreview.setImageBitmap(mCacheBitmap);
		} else if (requestCode == SELECT_ONLINE_PICTURE)
		{
			String url = data.getStringExtra("url");
			int id = data.getIntExtra("id", -1);
			if(id > -1)
				url = Preferences.MY_ROOT_URL + "/dynapaper/get_image.php?id=" + id;
			Log.i(LOG_KEY, "Selected URL: " + url);
			byte[] bmp = data.getByteArrayExtra("data");
			//OnlineGalleryItem item = (OnlineGalleryItem)data.getSerializableExtra("item");
			mTxtURL.setText(url);
			if(!url.startsWith("http"))
				url = Preferences.MY_ROOT_URL + "/images/full/" + url;
			if(bmp != null && bmp.length > 0)
				mImgPreview.setImageBitmap(BitmapFactory.decodeByteArray(bmp, 0, bmp.length));
			else
				new DownloadToWallpaperTask(true).execute(url);
	    	//new DownloadToWallpaperTask().execute(selURL);
		} else if (requestCode == REQUEST_CODE_GALLERY_UPDATE)
		{
			findViewById(R.id.btnOnline).setEnabled(true);
		}
	}
	
    
    public Boolean setHomeWallpaper(Bitmap bmp)
    {
    	try {
   			setWallpaper(bmp);
    		showToast(getResourceString(R.string.s_updated));
            return true;
        } catch (Exception ex) {
        	Log.e(LOG_KEY, "Wallchanger Exception during update: " + ex.toString());
        	showToast(getResourceString(R.string.s_invalid));
            return false;
        }

    }
    
    protected String getResourceString(int stringResourceID)
    {
    	return mResources.getString(stringResourceID);
    }
    

	private class DownloadToWallpaperTask extends AsyncTask<String, Void, Bitmap> {
		public Boolean Testing = false;
		public DownloadToWallpaperTask() { }
		public DownloadToWallpaperTask(Boolean doTest) { Testing = doTest; }
	        /** The system calls this to perform work in a worker thread and
	      * delivers it the parameters given to AsyncTask.execute() */
	    protected Bitmap doInBackground(String... urls)
	    {
	    	Bitmap ret = null;
	    	InputStream s = null;
	    	try {
	    		String url = urls[0];
	    		URLConnection uc = new URL(url).openConnection();
	    		uc.connect();
	    		s = uc.getInputStream();
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
	    	if(Testing)
	    		mImgPreview.setImageBitmap(result);
	    	else
	    		setHomeWallpaper(result);
	    	mCacheBitmap = result;
	    	findViewById(R.id.btnSelect).setEnabled(true);
	    	findViewById(R.id.btnTest).setEnabled(true);
	    	if(mProgressBar != null)
	    		mProgressBar.setVisibility(View.GONE);
	    }
	}
	
	public String getMediaPath(Uri uri) {
	    String[] projection = { MediaStore.Images.Media.DATA };
	    Cursor cursor = managedQuery(uri, projection, null, null, null);
	    int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
	    cursor.moveToFirst();
	    return cursor.getString(column_index);
	}

	
    private void DoLog(String txt)
    {
    	Log.e(Preferences.LOG_KEY, txt);
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
	        AdView adView = new AdView(this, AdSize.BANNER, com.brandroid.dynapaper.Preferences.MY_AD_UNIT_ID);
	        // Lookup your LinearLayout assuming it’s been given
	        // the attribute android:id="@+id/mainLayout"
	        LinearLayout layout = (LinearLayout)findViewById(R.id.adLayout);
	        // Add the adView to it
	        layout.addView(adView);
	        // Initiate a generic request to load it with an ad
	        AdRequest ad = new AdRequest();
	        ad.setTesting(true);
	        adView.loadAd(ad);
    	} catch(Exception ex) { Log.e(LOG_KEY, "Error adding ads: " + ex.toString()); }    
    }
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	@Override
	protected void onStart() {
		super.onStart();
		// TODO Put your code here
	}
	
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
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
}
