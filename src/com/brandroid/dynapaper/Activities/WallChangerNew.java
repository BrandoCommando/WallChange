package com.brandroid.dynapaper.Activities;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import com.brandroid.dynapaper.Preferences;
import com.brandroid.dynapaper.R;
import com.google.ads.AdRequest;
import com.google.ads.AdSize;
import com.google.ads.AdView;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

public class WallChangerNew extends WallChangerActivity implements OnClickListener
{ 
	private final static int REQUEST_CODE_GALLERY_UPDATE = 101;
	private EditText mTxtURL, mTxtZip;
	private ProgressBar mProgressBar;
	private ImageView mImgPreview;
	private Button mBtnSelect, mBtnTest;
	private Intent mIntent;
	private Bitmap mCacheBitmap;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        
        mIntent = getIntent();
        //final String action = intent.getAction();

        if(mIntent == null)
        	mIntent = new Intent();
        
        setContentView(R.layout.layout_new);
		
        mBtnSelect = (Button)findViewById(R.id.btnSelect);
		mBtnSelect.setOnClickListener(this);
		mBtnTest = (Button)findViewById(R.id.btnTest);
		mBtnTest.setOnClickListener(this);
		
		findViewById(R.id.btnCurrent).setOnClickListener(this);
		findViewById(R.id.btnGallery).setOnClickListener(this);
		findViewById(R.id.btnOnline).setOnClickListener(this);
		findViewById(R.id.btnSelect).setOnClickListener(this);
		//findViewById(R.id.btnStocks).setOnClickListener(this);
		findViewById(R.id.chkGPS).setOnClickListener(this);
		findViewById(R.id.btnTest).setOnClickListener(this);
		findViewById(R.id.btnUndo).setOnClickListener(this);
		findViewById(R.id.btnURL).setOnClickListener(this);
		findViewById(R.id.btnWeather).setOnClickListener(this);
		
		mTxtURL = (EditText)findViewById(R.id.txtURL);
		mProgressBar = (ProgressBar)findViewById(R.id.progressBar1);
		mImgPreview = (ImageView)findViewById(R.id.imageSample);
		mTxtURL.addTextChangedListener(new TextWatcher(){
			@Override
			public void afterTextChanged(Editable s) {}
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				mBtnSelect.setEnabled(true);
				mBtnTest.setEnabled(true);
			} });
		mTxtZip = (EditText)findViewById(R.id.txtZip);
		findViewById(R.id.btnUndo).setEnabled(false);
		mBtnSelect.setEnabled(false);
		mBtnTest.setEnabled(false);
		findViewById(R.id.txtURL).setVisibility(View.GONE);
		findViewById(R.id.progressBar1).setVisibility(View.GONE);
		
		mImgPreview.setVisibility(View.GONE);
		
		findViewById(R.id.btnOnline).setEnabled(false);
		startActivityForResult(new Intent(this, GalleryUpdater.class), REQUEST_CODE_GALLERY_UPDATE);
		
		addAds();
	}
	
	public String getFinalURL()
	{
		String url = "";
		if(mTxtURL != null)
			url = mTxtURL.getText().toString();
		if(url == "")
			url = prefs.getSetting("baseUrl", url);
		if(url == "")
			url = "schema.jpg";
		url = url.replace(MY_ROOT_URL, "");
		url = MY_ROOT_URL + "/images/weather.php?i1=" + URLEncoder.encode(url);
		url += "&source=google&format=image";
		Display display = getWindow().getWindowManager().getDefaultDisplay();
		url += "&x=26&w=" + (display.getWidth() * 2) + "&h=" + display.getHeight();
		Log.i(LOG_KEY, "Final URL: " + url);
		return url;
	}

	@Override
	public void onClick(View v)
	{
		switch(v.getId())
		{
			case R.id.btnCurrent:
				mCacheBitmap = ((BitmapDrawable)getWallpaper()).getBitmap();
				mImgPreview.setImageBitmap(mCacheBitmap);
				new UploadTask().execute(mCacheBitmap);
				break;
			case R.id.btnGallery:
				Intent intentGallery = new Intent();
				intentGallery.setType("image/*");
				intentGallery.setAction(Intent.ACTION_GET_CONTENT);
				startActivityForResult(Intent.createChooser(intentGallery, "Select Picture"), SELECT_PICTURE);
				break;
			case R.id.btnOnline:
				Intent intentOnline = new Intent(this, OnlineGalleryPicker.class);
				intentOnline.setAction(Intent.ACTION_GET_CONTENT);
				intentOnline.setType("image/*");
				startActivityForResult(intentOnline, SELECT_ONLINE_PICTURE);
				break;
			case R.id.chkGPS:
				mTxtZip.setEnabled(((CheckBox)findViewById(R.id.chkGPS)).isChecked());
				break;
			case R.id.btnTest:
				new DownloadToWallpaperTask(true).execute(getFinalURL());
			case R.id.btnSelect:
				new DownloadToWallpaperTask().execute(getFinalURL());
				break;
			case R.id.btnWeather:
				break;
			case R.id.btnStocks:
				break;
			case R.id.btnURL:
				Boolean bURLMode = mTxtURL.getVisibility() == View.GONE;
				mTxtURL.setVisibility(bURLMode ? View.VISIBLE : View.GONE);
				break;
			case R.id.btnUndo:
				try {
					if(mCacheBitmap != null)
					{
						mImgPreview.setImageBitmap(mCacheBitmap);
						setWallpaper(mCacheBitmap);
					}
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
			mCacheBitmap = BitmapFactory.decodeFile(selPath);
			if(mCacheBitmap != null)
			{
				new UploadTask().execute(mCacheBitmap);
				mImgPreview.setImageBitmap(mCacheBitmap);
			}
		} else if (requestCode == SELECT_ONLINE_PICTURE)
		{
			String url = data.getStringExtra("url");
			//int id = data.getIntExtra("id", -1);
			//if(id > -1)
			//	url = Preferences.MY_ROOT_URL + "/dynapaper/get_image.php?id=" + id;
			Log.i(LOG_KEY, "Selected URL: " + url);
			byte[] bmp = data.getByteArrayExtra("data");
			//OnlineGalleryItem item = (OnlineGalleryItem)data.getSerializableExtra("item");
			mTxtURL.setText(url);
			if(!url.startsWith("http"))
				url = getImageFullUrl(url);
			mCacheBitmap = ((BitmapDrawable)getWallpaper()).getBitmap(); 
			if(bmp != null && bmp.length > 0)
			{
				Bitmap mGalleryBitmap = BitmapFactory.decodeByteArray(bmp, 0, bmp.length);
				if(mGalleryBitmap != null)
				{
					new UploadTask().execute(mGalleryBitmap);
					mImgPreview.setImageBitmap(mGalleryBitmap);
				}
			} else
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
				con.setConnectTimeout(20000);
				con.setDoOutput(true);
				out = con.getOutputStream();
				// TODO: Make Quality Configurable
				arg0[0].compress(CompressFormat.JPEG, 100, out);
				out.flush();
				in = con.getInputStream();
				sr = new InputStreamReader(in);
				char[] buf = new char[64];
				while(sr.read(buf) > 0)
				{
					ret.append(buf);
					if(buf.length < 64)
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
		protected void onPreExecute() {
			super.onPreExecute();
			Log.i(LOG_KEY, "Uploading image");
			mProgressBar.setVisibility(View.VISIBLE);
			mBtnSelect.setEnabled(false);
			mBtnTest.setEnabled(false);
			findViewById(R.id.btnCurrent).setEnabled(false);
		}
		
		@Override
		protected void onPostExecute(String result) {
			if(result == null || result == "")
				showToast("Invalid response");
			else {
				Log.i(LOG_KEY, "Response received from upload.");
				String mBaseUrl = result.replaceAll("[^A-Za-z0-9\\.\\/]", "");
				mTxtURL.setText(mBaseUrl);
				prefs.setSetting("baseUrl", mBaseUrl);
				//if(mDynaUrl.indexOf("&i1=") > -1)
				//	mDynaUrl = mDynaUrl.substring(0, mDynaUrl.indexOf("&i1="));
				//mDynaUrl += "&i1=" + mBaseUrl;
				//mTextUrl.setText(mDynaUrl);
				findViewById(R.id.btnUndo).setEnabled(true);
				//mTextUrl.setEnabled(true);
				mBtnSelect.setEnabled(true);
				mBtnTest.setEnabled(true);
				findViewById(R.id.btnCurrent).setEnabled(true);
			}
			mProgressBar.setVisibility(View.GONE);
			//mImgPreview.setVisibility(View.VISIBLE);
			//mImgSample.setImageDrawable(getWallpaper());
			//mImgSample.setVisibility(View.VISIBLE);
		}
	}

	private class DownloadToWallpaperTask extends AsyncTask<String, Void, Bitmap> {
		public Boolean Testing = false;
		public DownloadToWallpaperTask() { Testing = false; }
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
	    
	    @Override
	    protected void onPreExecute() {
	    	super.onPreExecute();
	    	showToast("Downloading image.");
	    	if(mImgPreview != null)
	    		mImgPreview.setVisibility(View.GONE);
	    	if(mProgressBar != null)
	    		mProgressBar.setVisibility(View.VISIBLE);
	    	mBtnSelect.setEnabled(false);
	    	mBtnTest.setEnabled(false);
	    	
	    }
	    
	    /** The system calls this to perform work in the UI thread and delivers
	      * the result from doInBackground() */
	    protected void onPostExecute(Bitmap result) {
	    	if(result == null)
	    		showToast("Invalid image.");
	    	else {
	    		mImgPreview.setVisibility(View.VISIBLE);
	    		mImgPreview.setImageBitmap(result);
	    		if(!Testing)
		    		setHomeWallpaper(result);
		    	//mCacheBitmap = result;
		    	mBtnSelect.setEnabled(true);
		    	mBtnTest.setEnabled(true);
	    	}
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
