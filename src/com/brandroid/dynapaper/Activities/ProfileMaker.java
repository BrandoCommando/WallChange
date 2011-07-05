package com.brandroid.dynapaper.Activities;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.http.util.ByteArrayBuffer;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.brandroid.util.JSON;
import com.brandroid.util.Logger;
import com.brandroid.util.MediaUtils;
import com.brandroid.util.NetUtils;
import com.brandroid.dynapaper.GalleryItem;
import com.brandroid.dynapaper.R;
import com.brandroid.dynapaper.WallChanger;
import com.brandroid.dynapaper.WallProfile;
import com.brandroid.dynapaper.Database.GalleryDbAdapter;
import com.brandroid.dynapaper.Database.ProfileDbAdapter;
import com.brandroid.dynapaper.widget.Weather;
import com.brandroid.dynapaper.widget.Widget;

import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Criteria;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.net.Uri;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

public class ProfileMaker extends BaseActivity
{ 
	private EditText mTxtURL;
	private Button mBtnSelect, mBtnTest, mBtnOnline, mBtnWeatherPosition;
	private CheckBox mBtnWeather;
	private Intent mIntent;
	private View mProgressPanel;
	private ProgressBar mProgressBar;
	private TextView mProgressLabel;
	private DownloadToWallpaperTask mDownloadTask;
	private AddWallpaperWidgetsTask mAddWidgetTask;
	private GalleryDbAdapter gdb;
	private Boolean mWifiEnabled = true;
	private WallProfile mProfileCurrent;
	private ProfileDbAdapter pdb;
	private Bitmap mSample;
	
	private String mWeatherLocation = "";
	private int mWeatherPosition = 4; // middle center
	
	//private String mGPSLocation = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
        mIntent = getIntent();
        //final String action = intent.getAction();

        if(mIntent == null)
        	mIntent = new Intent();
        
        setContentView(R.layout.profile_maker);
		super.onCreate(savedInstanceState);
		
		MediaUtils.init(this);
		
		pdb = new ProfileDbAdapter(getApplicationContext());
		pdb.open();
		//Cursor pc = gdb.fetchAllItems();
        mProfileCurrent = new WallProfile();
        if(mIntent.hasExtra("profile"))
        {
        	Long pid = mIntent.getLongExtra("profile", -1);
        	if(pid > -1)
        	{
        		mProfileCurrent = pdb.fetchItem(pid);
        		pdb.close();
        	}
        } else {
        	mProfileCurrent.setBasePath("last.jpg"); // current wallpaper
        }
		
		gdb = new GalleryDbAdapter(getApplicationContext());
		gdb.open();
		Cursor gc = gdb.fetchAllItems();
		startManagingCursor(gc);
		
		mBtnSelect = (Button)findViewById(R.id.btnSelect);
		mBtnTest = (Button)findViewById(R.id.btnTest);
		mBtnWeather = (CheckBox)findViewById(R.id.btnWeather);
		mBtnOnline = (Button)findViewById(R.id.btnOnline);
		
		mProgressPanel = findViewById(R.id.progress_layout);
		mProgressBar = (ProgressBar)findViewById(R.id.progress_progress);
		mProgressLabel = (TextView)findViewById(R.id.progress_label);
		
		mBtnTest.setOnClickListener(this);
		mBtnSelect.setOnClickListener(this);
		mBtnWeather.setOnClickListener(this);
		mBtnOnline.setOnClickListener(this);
		
		findViewById(R.id.btnCurrent).setOnClickListener(this);
		findViewById(R.id.btnGallery).setOnClickListener(this);
		findViewById(R.id.btnSelect).setOnClickListener(this);
		//findViewById(R.id.btnStocks).setOnClickListener(this);
		findViewById(R.id.btnTest).setOnClickListener(this);
		findViewById(R.id.btnURL).setOnClickListener(this);
		findViewById(R.id.progress_cancel).setOnClickListener(this);
		findViewById(R.id.btnRotate).setOnClickListener(this);
		findViewById(R.id.btnWeatherPosition).setOnClickListener(this);
		findViewById(R.id.btnWeatherLocation).setOnClickListener(this);
		
		mProgressPanel.setVisibility(View.GONE);
		
		mTxtURL = (EditText)findViewById(R.id.txtURL);
		mTxtURL.setText(mProfileCurrent.getBasePath());
		//(ImageView)findViewById(R.id.imageSample);
		
		if(mProfileCurrent.getBasePath().equalsIgnoreCase("last.jpg"))
			new GrabCurrentWallpaperTask().execute();
		
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
		mBtnSelect.setEnabled(false);
		mBtnTest.setEnabled(false);
		findViewById(R.id.txtURL).setVisibility(View.GONE);
		
		getSavedSettings();
		
		mBtnOnline.setEnabled(gc.getCount() > 0);
		
		//mTxtZip.setText(prefs.getString("zip", mTxtZip.getText().toString()));
		
		new UpdateOnlineGalleryTask().execute();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.maker, menu);
		return true;
	}
	
	public String getBaseImageURL()
	{
		String url = "";
		if(mTxtURL != null)
			url = mTxtURL.getText().toString();
		if(url == "")
			url = prefs.getSetting("baseUrl", url);
		if(url == "" && MediaUtils.fileExists("last.jpg", true))
			url = MediaUtils.getFullFilename("last.jpg", true);;
		if(url == "")
			url = "schema.jpg";
		/* if(mBtnWeather.isChecked())
		{
			if(mTxtZip.getText().length() > 0)
				mPastZips.add(mTxtZip.getText().toString());
			url = WallChanger.MY_WEATHER_URL.replace("%USER%", WallChanger.getUser()) + "&source=google&format=image&type=image" +
				"&x=26&w=" + getHomeWidth() + "&h=" + getHomeHeight() +
				(mTxtZip.getText().length() > 0 ? "&zip=" + mTxtZip.getText() : "") +
				"&i1=" + URLEncoder.encode(url.replace(WallChanger.MY_ROOT_URL + "/images/", "").replace(WallChanger.MY_ROOT_URL, ""));
		}
		else */
		if(!MediaUtils.fileExists(url, true))
			url = WallChanger.getImageFullUrl(url);
		Logger.LogInfo("Final Base Image URL: " + url);
		return url;
	}
	
	public Boolean checkWifi()
	{
		mWifiEnabled = false;
		try {
			WifiManager wm = (WifiManager)getSystemService(WIFI_SERVICE);
			if(wm != null)
			{
				//Logger.LogInfo("WIFI Manager: " + wm.toString());
				WifiInfo wi = wm.getConnectionInfo();
				if(wi != null)
				{
					Logger.LogInfo("WIFI Info: " + wi.toString());
					if(wi.getSupplicantState().equals(SupplicantState.COMPLETED))
						mWifiEnabled = true;
				}
			}
		} catch(Exception ex) { Logger.LogError("Error checking Wifi", ex); }
		return mWifiEnabled;
	}
	
	public void onClickCurrent()
	{
		Bitmap mCurrent = ((BitmapDrawable)getWallpaper()).getBitmap(); // getSizedBitmap(((BitmapDrawable)getWallpaper()).getBitmap(), getHomeWidth(), getHomeHeight());
		MediaUtils.writeFile("last.jpg", mCurrent, true);
		mTxtURL.setText("last.jpg");
		//mImgPreview.setImageBitmap(mCacheBitmap);
		setPreview(mCurrent);
	}
	public void onClickLocalGallery()
	{
		Intent intentGallery = new Intent();
		intentGallery.setType("image/*");
		intentGallery.setAction(Intent.ACTION_GET_CONTENT);
		startActivityForResult(Intent.createChooser(intentGallery, getResourceString(R.string.s_select_base)), WallChanger.REQ_SELECT_GALLERY);
	}
	public void onClickOnlineGallery()
	{
		Intent intentOnline = new Intent(getApplicationContext(), GalleryPicker.class);
		intentOnline.setAction(Intent.ACTION_GET_CONTENT);
		intentOnline.setType("image/*");
		startActivityForResult(intentOnline, WallChanger.REQ_SELECT_ONLINE);
	}
	public void onClickWeatherPosition()
	{
		Intent intentPos = new Intent(getApplicationContext(), SelectPosition.class);
		intentPos.putExtra("position", mWeatherPosition);
		startActivityForResult(intentPos, WallChanger.REQ_POSITION);
	}
	public void onClickWeatherLocation()
	{
		Intent intentLoc = new Intent(getApplicationContext(), SelectLocation.class);
		intentLoc.putExtra("location", mWeatherLocation);
		startActivityForResult(intentLoc, WallChanger.REQ_LOCATION);
	}
	public void onClickPreview()
	{
		if(mAddWidgetTask != null && mAddWidgetTask.getStatus() == Status.RUNNING)
		{
			mAddWidgetTask.cancel(true);
		}
		mAddWidgetTask = new AddWallpaperWidgetsTask(false);
		mAddWidgetTask.execute(getBaseImageURL());
		//new DownloadToWallpaperTask(true).execute(getDynaURL());
	}
	public void onClickSelect()
	{
		System.gc();
		if(mAddWidgetTask != null && mAddWidgetTask.getStatus() == Status.RUNNING)
		{
			mAddWidgetTask.cancel(true);
		}
		mAddWidgetTask = new AddWallpaperWidgetsTask(true);
		mAddWidgetTask.execute(getBaseImageURL());
		//new DownloadToWallpaperTask().execute(getDynaURL());
	}

	@Override
	public void onClick(View v)
	{
		switch(v.getId())
		{
			case R.id.btnCurrent:
				checkWifi();
				onClickCurrent();
				break;
			case R.id.btnGallery:
				checkWifi();
				onClickLocalGallery();
				break;
			case R.id.btnOnline:
				onClickOnlineGallery();
				break;
			case R.id.progress_cancel:
				onCancelDownload();
				break;
			case R.id.btnTest:
				onClickPreview();
				break;
			case R.id.btnSelect:
				onClickSelect();
				break;
			case R.id.btnWeather:
				Boolean bChecked = mBtnWeather.isChecked();
				mBtnWeatherPosition.setEnabled(bChecked);
				break;
			case R.id.btnWeatherPosition:
				onClickWeatherPosition();
				break;
			case R.id.btnWeatherLocation:
				onClickWeatherLocation();
				break;
			case R.id.btnURL:
				mTxtURL.setVisibility(mTxtURL.getVisibility() == View.GONE ? View.VISIBLE : View.GONE);
				break;
			case R.id.btnRotate:
				
				break;
		}
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId())
		{
		case R.id.menu_settings:
			Intent intentSettings = new Intent(getApplicationContext(), Settings.class);
			startActivityForResult(intentSettings, WallChanger.REQ_SETTINGS);
			break;
		case R.id.menu_help:
			startActivity(new Intent(getApplicationContext(), Help.class));
			break;
		case R.id.menu_feedback:
			startActivity(new Intent(getApplicationContext(), Feedback.class));
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}
	
	@Override
	public Object onRetainNonConfigurationInstance() {
		//Bitmap bmp = getW
		return mSample;
		//w.get
	}
	
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onRestoreInstanceState(savedInstanceState);
	}
	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(resultCode == RESULT_CANCELED) {
			Logger.LogVerbose("Cancel for " + requestCode + ".");
			return;
		}
		if(requestCode == WallChanger.REQ_SELECT_GALLERY)
		{
			Uri selUri = data.getData();
			String selPath = getMediaPath(selUri);
			mTxtURL.setText(selPath);
			Bitmap blg = BitmapFactory.decodeFile(selPath);
			Bitmap bmp = MediaUtils.getSizedBitmap(blg, getHomeWidth(), getHomeHeight());
			MediaUtils.writeFile("last.jpg", bmp, true);
			blg = null;
			if(bmp != null)
			{
				//mCacheBitmap = ((BitmapDrawable)getWallpaper()).getBitmap(); // getSizedBitmap(((BitmapDrawable)getWallpaper()).getBitmap(), getHomeWidth(), getHomeHeight());
				setPreview(bmp);
				//mImgPreview.setImageBitmap(bmp);
				//mImgPreview.setVisibility(View.GONE);
			} else Logger.LogWarning("Unable to create thumbnail?");
		} else if (requestCode == WallChanger.REQ_SELECT_ONLINE)
		{
			String url = data.getStringExtra("url");
			//int id = data.getIntExtra("id", -1);
			//if(id > -1)
			//	url = Preferences.MY_ROOT_URL + "/dynapaper/get_image.php?id=" + id;
			Logger.LogInfo("Selected URL: " + url);
			//OnlineGalleryItem item = (OnlineGalleryItem)data.getSerializableExtra("item");
			mTxtURL.setText(url);
			prefs.setSetting("url", url);
			if(!url.startsWith("http"))
				url = WallChanger.getImageFullUrl(url);
			byte[] bmp = data.getByteArrayExtra("data");
			if(bmp != null && bmp.length > 0)
			{
				Bitmap mGalleryBitmap = BitmapFactory.decodeByteArray(bmp, 0, bmp.length);
				if(mGalleryBitmap != null)
				{
					//new UploadTask().execute(mGalleryBitmap);
					//mImgPreview.setImageBitmap(mGalleryBitmap);
					setPreview(mGalleryBitmap);
				}
			} else {
				int width = 0, height = 0;
				width = getHomeWidth() / 2;
				String zip = mTxtURL.getText().toString();
				//mPastZips.insert(zip, 0);
				String sThumbUrl = WallChanger.getImageThumbUrl(zip, width, height);
				Logger.LogWarning("Couldn't find image in Intent. Re-downloading " + sThumbUrl, new Exception("Dummy"));
				new DownloadToWallpaperTask(true).execute(sThumbUrl);
			}
	    	//new DownloadToWallpaperTask().execute(selURL);
		} else if(requestCode == WallChanger.REQ_POSITION)
		{
			if(data.hasExtra("position"))
			{
				mWeatherPosition = data.getIntExtra("position", mWeatherPosition);
				Logger.LogInfo("New Position: " + mWeatherPosition);
				onClickPreview();
			} else
				Logger.LogWarning("Unable to get position from SelectPosition Activity result data.");
		} else if(requestCode == WallChanger.REQ_LOCATION)
		{
			if(data.hasExtra("location"))
			{
				mWeatherLocation = data.getStringExtra("location");
				Logger.LogInfo("New Location: " + mWeatherLocation);
				onClickPreview();
			} else Logger.LogWarning("Unable to get location from SelectLocation result data.");
		}
	}
	
	public Boolean setPreview(Bitmap bmp)
	{
		try {
			Window win = getWindow();
			Display d = win.getWindowManager().getDefaultDisplay();
			//LayoutParams lp = win.getAttributes();
			int mw = d.getWidth();
			int mh = d.getHeight();
			int w = bmp.getWidth();
			int h = bmp.getHeight();
			int nw = (h / mh) * w;
			int nh = (w / mw) * h; // new height
			int bh = nh - h; // bar height
			nw /= 2;
			nh /= 2;
			bh /= 2;
			// Max: 600x1024, Image: 1200x1024 --> 1200x1024
			//Logger.LogInfo("Scaled " + bmp.getWidth() + "x" + bmp.getHeight() + " under " + mw + "x" + mh + " to " + w + "x" + h);
			//Bitmap scaled = Bitmap.createScaledBitmap(bmp, w, h, true);
			Logger.LogInfo("Max: " + mw + "x" + mh + ", Image: " + w + "x" + h + ", New: " + nw + "x" + nh);
			Bitmap newpic = Bitmap.createBitmap(nw, nh, Config.RGB_565);
			Canvas c = new Canvas(newpic);
			Paint p = new Paint();
			p.setStyle(Style.FILL);
			c.drawColor(Color.BLACK);
			Rect src = new Rect(0, 0, bmp.getWidth(), bmp.getHeight());
			Rect dst = new Rect(0, bh, nw, nh);
			c.drawBitmap(bmp, src, dst, p);
			Drawable pic = new BitmapDrawable(newpic);
			//pic.setBounds(pic.getMinimumWidth() / 2, 0, pic.getMinimumWidth(), pic.getMinimumHeight());
			win.setBackgroundDrawable(pic);
			return true;
		} catch(Exception ex) {
			Logger.LogError("Error setting preview.", ex);
			return false;
		}
	}
    public Boolean setHomeWallpaper(Bitmap bmp)
    {
    	try {
   			setWallpaper(bmp);
    		showToast(getResourceString(R.string.s_updated));
            return true;
        } catch (Exception ex) {
        	Logger.LogError("Error setting Wallpaper", ex);
        	showToast(getResourceString(R.string.s_invalid));
            return false;
        }

    }
    
    protected String getResourceString(int stringResourceID)
    {
    	return mResources.getString(stringResourceID);
    }
    
    public String getMD5(byte[] data)
    {
    	try {
	    	MessageDigest digest = MessageDigest.getInstance("MD5");
	    	byte[] msgDigest = digest.digest(data);
	    	StringBuffer sb = new StringBuffer();
	    	for(int i = 0; i < msgDigest.length; i++)
	    		sb.append(Integer.toHexString(0xFF & msgDigest[i]));
	    	return sb.toString();
    	} catch(NoSuchAlgorithmException nsme) { Logger.LogError("WTF! No MD5!", nsme); return null; }
    }
    
	private void showPanel(View panel, boolean slideUp)
	{
		panel.startAnimation(AnimationUtils.loadAnimation(this, slideUp ? R.anim.slide_in : R.anim.slide_out_top));
		panel.setVisibility(View.VISIBLE);
	}
	
	private void hidePanel(View panel, boolean slideDown)
	{
		panel.startAnimation(AnimationUtils.loadAnimation(this, slideDown ? R.anim.slide_out : R.anim.slide_in_top));
		panel.setVisibility(View.GONE);
	}
    
    private void onCancelDownload()
    {
    	if(mDownloadTask != null && mDownloadTask.getStatus() == Status.RUNNING)
    	{
    		mDownloadTask.cancel(true);
    		mDownloadTask = null;
    	}
    }
        
    private class GrabCurrentWallpaperTask extends AsyncTask<String, Void, Bitmap>
    {

		@Override
		protected Bitmap doInBackground(String... params) {
			try {
				String sFile = "last.jpg";
				if(params.length > 0)
					sFile = params[0];
				File fLast = MediaUtils.getFile(sFile, true);
				if(!fLast.exists())
					MediaUtils.writeFile(sFile, ((BitmapDrawable)getWallpaper()).getBitmap(), true);
				mTxtURL.setText(sFile);
				return BitmapFactory.decodeFile(fLast.getAbsolutePath());
				//mImgPreview.setImageURI(Uri.fromFile(fLast));
			} catch(Exception e) { Logger.LogError("Couldn't set Preview to last", e); return null; }
		}
		
		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
		}
		
		@Override
		protected void onPostExecute(Bitmap result) {
			setPreview(result);
		}
    	
    }
    
	private class UpdateOnlineGalleryTask extends AsyncTask<String, Integer, Boolean> {

		@Override
		protected Boolean doInBackground(String... params)
		{
			gdb.fetchAllIDs();
			Integer cStampMax = gdb.fetchLatestStamp();
			Logger.LogInfo("Latest stamp: " + cStampMax);
			
			String ret = null;
	    	String line = null;
	    	InputStream in = null;
	    	BufferedReader br = null;
	    	StringBuilder sb = null;
	    	HttpURLConnection uc = null;
	    	Long modified = null;
	    	JSONObject jsonGallery = null;
	    	Boolean success = false;
	    	
	    	String url = WallChanger.MY_GALLERY_URL.replace("%USER%", WallChanger.getUser());
	    	
	    	try {
	    		uc = (HttpURLConnection)new URL(url).openConnection();
	    		uc.setReadTimeout(20000);
	    		uc.addRequestProperty("Accept-Encoding", "gzip, deflate");
	    		uc.addRequestProperty("Version", ((Integer)WallChanger.VERSION_CODE).toString());
	    		
	    		if(prefs.hasSetting("gallery_update"))
	    		{
	    			String sLastMod = prefs.getString("gallery_update", "");
	    			if(sLastMod != "")
	    			{
	    				try {
	    					modified = Long.parseLong(sLastMod);
	    				} catch(NumberFormatException nfe) { }
	    			}
		    		//modified = prefs.getLong("gallery_update", Long.MIN_VALUE);
		    		if(modified != null)
		    			uc.setIfModifiedSince(modified);
	    		}
	    		if(Logger.hasDb() && prefs.getSetting("allow", true)) // only upload on WIFI
	    		{
	    			String sDbLogData = Logger.getDbLogs();
	    			if(sDbLogData != "")
	    			{
	    				//uc.setRequestProperty("LOG_ERRORS", sDbLogData);
	    				uc.setDoOutput(true);
	    				GZIPOutputStream out = new GZIPOutputStream(uc.getOutputStream());
	    				out.write(sDbLogData.getBytes());
	    				out.flush();
	    				out.close();
	    				//Logger.LogInfo("Gallery Error Log submitted " + sDbLogData.length() + " bytes");
	    			}
	    		}
	    		uc.connect();
	    		if(uc.getResponseCode() == HttpURLConnection.HTTP_OK)
	    		{
	    			String encoding = uc.getContentEncoding();
	    			Iterator<String> keys = uc.getHeaderFields().keySet().iterator();
	    			StringBuilder sbHeaders = new StringBuilder();	    			
	    			while(keys.hasNext())
	    			{
	    				String key = keys.next();
	    				sbHeaders.append(key);
	    				sbHeaders.append(":\"");
	    				sbHeaders.append(uc.getHeaderField(key));
	    				sbHeaders.append("\",");
	    				//Logger.LogInfo()
	    			}
	    			if(sbHeaders.length() > 0)
	    			{
	    				sbHeaders.setLength(sbHeaders.length() - 1);
	    				Logger.LogInfo("Gallery Headers: {" + sbHeaders.toString() + "}");
	    			}
	    			//prefs.setSetting("gallery_update", ((Long)uc.getDate()).toString());
	    			//Logger.LogInfo("Encoding: " + encoding + " @ " + uc.getLastModified() + " : " + uc.getLastModified());
	    			if(encoding != null && encoding.equalsIgnoreCase("gzip"))
	    				in = new GZIPInputStream(uc.getInputStream());
	    			else if(encoding != null && encoding.equalsIgnoreCase("deflate"))
	    				in = new InflaterInputStream(uc.getInputStream(), new Inflater(true));
	    			else
	    				in = new BufferedInputStream(uc.getInputStream());
	    			br = new BufferedReader(new InputStreamReader(in));
		    		sb = new StringBuilder();
		    		while((line = br.readLine()) != null)
		    			sb.append(line + '\n');
		    		ret = sb.toString();
		    		//Logger.LogInfo("Gallery Response: " + sb.toString());
		    		modified = uc.getDate();
		    		if(uc.getLastModified() > 0)
		    			modified = uc.getLastModified();
		    		jsonGallery = JSON.Parse(ret);
		    		if(jsonGallery != null)
		    		{
		    			try {
				    		if(jsonGallery.has("user") && jsonGallery.get("user") != WallChanger.getUser())
				    		{
				    			String user = jsonGallery.getString("user");
				    			if(user.length()>1)
				    				WallChanger.setUser(user);
				    		}
			    			if(jsonGallery.has("zip"))
				    		{
			    				try {
					    			String zip = jsonGallery.getString("zip");
					    			Logger.LogInfo("Found zipcode: " + zip);
					    			publishProgress(Integer.parseInt(zip));
					    		} catch(Exception ex) { }
				    		}
							try {
								JSONArray jsonImages = jsonGallery.getJSONArray("images");
								GalleryItem[] items = new GalleryItem[jsonImages.length()];
								for(int imgIndex = 0; imgIndex < items.length; imgIndex++)
									items[imgIndex] = new GalleryItem(jsonImages.getJSONObject(imgIndex));
								
								int adds = gdb.createItems(items);
								
								if(adds > 0)
								{
									publishProgress(0);
									new DownloadThumbZipTask().execute(WallChanger.MY_THUMBS_ZIP_URL);
								}
								
								JSONArray jsonInactive = jsonGallery.optJSONArray("inactive");
								int removes = 0;
								if(jsonInactive != null)
									for(int iInactive = 0; iInactive < jsonInactive.length(); iInactive++)
										removes += gdb.deleteItem(jsonInactive.optLong(iInactive, -1)) ? 1 : 0;	
								
								Logger.LogInfo("Successfully add/updated " + adds + " records" + (removes > 0 ? " & removed " + removes + " records" : "") + "!");
								success = true;
								//prefs.setSetting("gallery_update", modified.toString());
							} catch (Exception je) {
								Logger.LogError("Exception getting images: " + je.toString(), je);
							}
						} catch (JSONException je) {
							Logger.LogError("JSONException getting user: " + je.toString(), je);
						}
		    		} else Logger.LogWarning("Gallery response is null.");
	    		}
			}
			catch(MalformedURLException mex) { Logger.LogError(mex.toString(), mex); }
			catch(ProtocolException pex) { Logger.LogError(pex.toString(), pex); }
			catch(IOException ex) { Logger.LogError(ex.toString(), ex); }
			catch(Exception ex) { Logger.LogError(ex.toString(), ex); }
	    	finally {
				if(uc != null) uc.disconnect();
				in = null;
				br = null;
				sb = null;
				uc = null;
				jsonGallery = null;
	    	}
	    	//mGalleryCursor = gdb.fetchAllItems();
	    	gdb.close();
	    	return success;
		}
		
		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
			if(values.length == 1)
			{
				if(values[0] > 0)
				{
					if(mWeatherLocation == "")
						mWeatherLocation = values[0].toString();
				} else if (values[0] == 0) {
					mBtnOnline.setTextColor(Color.GRAY);
				}
			}
		}

		@Override
		protected void onPostExecute(Boolean success)
		{
			if(mBtnOnline != null && success)
				mBtnOnline.setEnabled(true);
		}
		
	}
	
	public class DownloadThumbZipTask extends AsyncTask<String, Void, Void>
	{

		@Override
		protected Void doInBackground(String... urls)
		{
			ZipInputStream s = null;
			String url = urls[0];
			HttpURLConnection uc = null;
			try {
				Logger.LogInfo("Downloading " + url);
				uc = (HttpURLConnection)new URL(url).openConnection();
				uc.setConnectTimeout(8000);
				uc.setReadTimeout(10000);
				uc.connect();
				int iBytes = uc.getContentLength();
				if(uc.getResponseCode() < 400)
				{
					s = new ZipInputStream(new BufferedInputStream(uc.getInputStream()));
					ZipEntry ze;
					int iFiles = 0, iTotal = 0;
					while((ze = s.getNextEntry()) != null)
					{
						iTotal++;
						ByteArrayOutputStream baos = new ByteArrayOutputStream();
						byte[] buffer = new byte[1024];
						int count;
						while ((count = s.read(buffer)) != -1)
							baos.write(buffer, 0, count);
						String filename = ze.getName();
						byte[] bytes = baos.toByteArray();
						if(MediaUtils.writeFile(filename, bytes, true))
							iFiles++;
					}
					Logger.LogInfo("Thumb Zip retrieved (" + iBytes + " bytes). " + iFiles + "/" + iTotal + " written.");
				}
			} catch(IOException ix) { Logger.LogError("Unable to get thumb zip.", ix); }
			finally { 
				if(s != null)
					try {
						s.close();
					} catch (IOException e) {
						Logger.LogError("Error closing thumb zip stream.", e);
					}
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			mBtnOnline.setTextColor(Color.BLACK);
		}
	}


	public class AddWallpaperWidgetsTask extends AsyncTask<String, Integer, Bitmap>
	{
		private Boolean mSetWallpaper = true;
		
		public AddWallpaperWidgetsTask(Boolean doSetWallpaper)
		{
			mSetWallpaper = doSetWallpaper;
		}

		@Override
		protected Bitmap doInBackground(String... params)
		{
			Bitmap base = null;
			publishProgress(-2);
			publishProgress(0, 3);
			String url = params[0];
			if(url.trim().equals("")) url = MediaUtils.getFullFilename("last.jpg", true);
			base = MediaUtils.readFileBitmap(url, true);
			if(base == null)
				base = downloadBitmap(url);
			if(base == null) return null;
			int w = getHomeWidth();
			int h = getHomeHeight();
			//base = Bitmap.createScaledBitmap(base, w, h, true);
			Bitmap ret = Bitmap.createBitmap(w, h, Config.ARGB_8888);
			Canvas c = new Canvas(ret);
			c.save();
			Paint p = new Paint();
			p.setStyle(Style.FILL);
			Rect src = new Rect(0, 0, base.getWidth(), base.getHeight());
			RectF dst = new RectF(new Rect(0, 0, w, h));
			c.drawBitmap(base, src, dst, p);
			base = null;
			
			publishProgress(-3);
			Widget[] widgets = getSelectedWidgets();
			publishProgress(1, 1 + widgets.length);
			for(int i=0; i < widgets.length; i++)
			{
				if(!widgets[i].applyTo(ret, c))
					showToast(getResourceString(R.string.s_error, R.string.s_adding, R.string.s_widgets));
				publishProgress(1 + i, 1 + widgets.length);
			}
			c.restore();
			Canvas.freeGlCaches();
			return ret;
		}

	    @Override
	    protected void onPreExecute() {
	    	mProgressBar.setProgress(0);
			mProgressLabel.setText(getResourceString(R.string.s_adding, R.string.btn_weather, R.string.s_widgets));
			showPanel(mProgressPanel, true);
			
	    	//showToast("Downloading image.");
	    	mBtnSelect.setEnabled(false);
	    	mBtnTest.setEnabled(false);
	    }
	    
	    @Override
		protected void onCancelled() {
			hidePanel(mProgressPanel, false);
	    	mBtnSelect.setEnabled(true);
	    	mBtnTest.setEnabled(true);
		}
		
		
		@Override
		protected void onProgressUpdate(Integer... values) {
			if(values.length > 1)
			{
				mProgressBar.setMax(values[1]);
				mProgressBar.setProgress(values[0]);
			} else if(values[0] == -1)
				mProgressBar.setIndeterminate(true);
			else if(values[0] == -2)
				mProgressLabel.setText(getResourceString(R.string.s_downloading));
			else if(values[0] == -3)
				mProgressLabel.setText(getResourceString(R.string.s_adding, R.string.btn_weather, R.string.s_widgets));
			else if(values[0] == 0)
				mProgressBar.setIndeterminate(false);
		}
		
		protected void onPostExecute(Bitmap result) {
    		hidePanel(mProgressPanel, true);
	    	if(result == null)
	    		showToast("Invalid image.");
	    	else {
	    		//mImgPreview.setVisibility(View.VISIBLE);
	    		//mImgPreview.setImageBitmap(result);
	    		setPreview(result);
	    		//MediaUtils.writeFile("last.jpg", result, true);
	    		if(mSetWallpaper)
	    		{
		    		setHomeWallpaper(result);
		    		finish();
	    		}
		    	//mCacheBitmap = result;
		    	mBtnSelect.setEnabled(true);
		    	mBtnTest.setEnabled(true);
	    	}
	    }
		
		private Bitmap downloadBitmap(String url)
		{
			Bitmap ret = null;
			InputStream s = null;
			try {
				if(url.startsWith("/"))
					return MediaUtils.readFileBitmap(url, true);
	    		HttpURLConnection uc = (HttpURLConnection)new URL(url).openConnection();
	    		uc.setConnectTimeout(15000);
	    		uc.connect();
	    		publishProgress(0);
	    		if(uc.getResponseCode() >= 400) throw new IOException(uc.getResponseCode() + " on " + url);
	    		Integer length = uc.getContentLength();
	    		Logger.LogInfo("Response received. " + length + " bytes.");
	    		publishProgress(-2);
	    		s = new BufferedInputStream(uc.getInputStream(), WallChanger.DOWNLOAD_CHUNK_SIZE);
	    		ByteArrayBuffer bab = new ByteArrayBuffer(length <= 0 ? 32 : length);
	    		byte[] b = new byte[WallChanger.DOWNLOAD_CHUNK_SIZE];
	    		int read = 0;
	    		int position = 0;
	    		while((read = s.read(b,0,WallChanger.DOWNLOAD_CHUNK_SIZE)) > 0)
	    		{
	    			position += read;
	    			bab.append(b, 0, read);
	    			publishProgress(position, length > position ? length : position + s.available());
	    		}
	    		b = bab.toByteArray();
	    		MediaUtils.writeFile(url.substring(url.lastIndexOf("/")+1), b, true);
	    		ret = BitmapFactory.decodeByteArray(b, 0, b.length);
	    	} catch(IOException ex) { Logger.LogError("Couldn't download base image. " + url, ex); }
	    	finally {
	    		try {
	    			if(s != null)
	    				s.close();
	    		} catch(IOException ex) { Logger.LogError("Error closing stream while downloading base image.", ex); }
	    	}
	    	return ret;
		}
		
	}
	
	public String getZip()
	{
		return mWeatherLocation;
	}
	
	public Widget[] getSelectedWidgets()
	{
		ArrayList<Widget> al = new ArrayList<Widget>();
		if(mBtnWeather.isChecked())
		{
			Widget w = new Weather(getApplicationContext(), getZip());
			Point pt = new Point(0,0);
			switch(mWeatherPosition)
			{
			case 0: // top left
				pt = new Point(-90,-60);
				break;
			case 1: // top center
				pt = new Point(0,-60);
				break;
			case 2: // top right
				pt = new Point(80,-60);
				break;
			case 3: // middle left
				pt = new Point(-90,0);
				break;
			case 4: // middle center
				pt = new Point(0,0);
				break;
			case 5: // middle right
				pt = new Point(80,0);
				break;
			case 6: // bottom left
				pt = new Point(-90,60);
				break;
			case 7: // bottom center
				pt = new Point(0,60);
				break;
			case 8: // bottom right
				pt = new Point(80,60);
				break;
			}
			w.setPosition(pt);
			al.add(w);
		}
		Widget[] ret = new Widget[al.size()];
		ret = al.toArray(ret);
		return ret;
	}
	
	private class DownloadToWallpaperTask extends AsyncTask<String, Integer, Bitmap> {
		public Boolean Testing = false;
		public DownloadToWallpaperTask() { Testing = false; }
		public DownloadToWallpaperTask(Boolean doTest) { Testing = doTest; }
	        /** The system calls this to perform work in a worker thread and
	      * delivers it the parameters given to AsyncTask.execute() */
	    protected Bitmap doInBackground(String... urls)
	    {
	    	Bitmap ret = null;
	    	BufferedInputStream s = null;
	    	try {
	    		publishProgress(-1);
	    		String url = urls[0];
	    		HttpURLConnection uc = (HttpURLConnection)new URL(url).openConnection();
	    		uc.setConnectTimeout(15000);
	    		uc.connect();
	    		publishProgress(0);
	    		if(uc.getResponseCode() >= 400) throw new IOException(uc.getResponseCode() + " on " + url);
	    		int length = uc.getContentLength();
	    		Logger.LogInfo("Response received. " + length + " bytes.");
	    		s = new BufferedInputStream(uc.getInputStream(), WallChanger.DOWNLOAD_CHUNK_SIZE);
	    		ByteArrayBuffer bab = new ByteArrayBuffer(length <= 0 ? 32 : length);
	    		byte[] b = new byte[WallChanger.DOWNLOAD_CHUNK_SIZE];
	    		int read = 0;
	    		int position = 0;
	    		while((read = s.read(b,0,WallChanger.DOWNLOAD_CHUNK_SIZE)) > 0)
	    		{
	    			position += read;
	    			bab.append(b, 0, read);
	    			publishProgress(position, length > position ? length : position + s.available());
	    		}
	    		b = bab.toByteArray();
	    		ret = BitmapFactory.decodeByteArray(b, 0, b.length);
	    	} catch(IOException ex) { Logger.LogError(ex.toString(), ex); }
	    	finally {
	    		try {
	    			if(s != null)
	    				s.close();
	    		} catch(IOException ex) { Logger.LogError(ex.toString(), ex); }
	    	}
	    	return ret;
	    }
	    
	    @Override
	    protected void onPreExecute() {
	    	mProgressBar.setProgress(0);
			mProgressLabel.setText(getText(R.string.s_downloading));
			showPanel(mProgressPanel, true);
			
	    	//showToast("Downloading image.");
	    	mBtnSelect.setEnabled(false);
	    	mBtnTest.setEnabled(false);
	    	
	    }
	    
		@Override
		protected void onCancelled() {
			hidePanel(mProgressPanel, false);
	    	mBtnSelect.setEnabled(true);
	    	mBtnTest.setEnabled(true);
		}
		
		@Override
		protected void onProgressUpdate(Integer... values) {
			if(values.length > 1)
			{
				mProgressBar.setMax(values[1]);
				mProgressBar.setProgress(values[0]);
			} else if(values[0] == -1)
				mProgressBar.setIndeterminate(true);
			else if(values[0] == 0)
				mProgressBar.setIndeterminate(false);
		}
		
		/** The system calls this to perform work in the UI thread and delivers
	      * the result from doInBackground() */
	    protected void onPostExecute(Bitmap result) {
    		hidePanel(mProgressPanel, true);
	    	if(result == null)
	    		showToast("Invalid image.");
	    	else {
	    		//mImgPreview.setVisibility(View.VISIBLE);
	    		//mImgPreview.setImageBitmap(result);
	    		setPreview(result);
	    		if(Testing)
	    			MediaUtils.writeFile("last.jpg", result, true);
	    		if(!Testing)
	    		{
		    		setHomeWallpaper(result);
		    		finish();
	    		}
		    	//mCacheBitmap = result;
		    	mBtnSelect.setEnabled(true);
		    	mBtnTest.setEnabled(true);
	    	}
	    }
	}
	
	public String getMediaPath(Uri uri) {
		String[] projection = { MediaStore.Images.Media.DATA };
	    Cursor cursor = managedQuery(uri, projection, null, null, null);
	    int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
	    cursor.moveToFirst();
	    return cursor.getString(column_index);
	}
	
	public void getSavedSettings()
	{
		mWeatherLocation = prefs.getSetting("zip", mWeatherLocation);
		if(mBtnWeather != null)
			mBtnWeather.setChecked(prefs.getBoolean("weather", mBtnWeather.isChecked()));
		String user = WallChanger.getUser();
		if(prefs.getSetting("user", user) != "")
			user = prefs.getSetting("user", user);
		WallChanger.setUser(user);
		if(mTxtURL != null)
			mTxtURL.setText(prefs.getSetting("url", mTxtURL.getText().toString()));
		String sPastZips = prefs.getSetting("past_zips", "90210");
		if(sPastZips != "")
		{
			mPastZips.clear();
			String[] aPast = sPastZips.split("\\|");
			for(int i = 0; i < aPast.length; i++)
				mPastZips.add(aPast[i]);
		}
	}
	
	public void setSavedSettings()
	{
		if(mWeatherLocation != null)
			prefs.setSetting("zip", mWeatherLocation);
		if(mBtnWeather != null)
			prefs.setSetting("weather", mBtnWeather.isChecked());
		if(WallChanger.getUser() != null && WallChanger.getUser() != "")
			prefs.setSetting("user", WallChanger.getUser());
		if(mTxtURL != null)
			prefs.setSetting("url", mTxtURL.getText().toString());
		if(mPastZips.getCount() > 0)
		{
			StringBuilder sPastZips = new StringBuilder();
			for(int i = 0; i < mPastZips.getCount(); i++)
			{
				sPastZips.append(mPastZips.getItem(i));
				if(i < mPastZips.getCount() - 1)
					sPastZips.append("|");
			}
			prefs.setSetting("past_zips", sPastZips.toString());
		}
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		getSavedSettings();
	}
	
	@Override
	protected void onStop() {
		setSavedSettings();
		super.onStop();
	}
}
