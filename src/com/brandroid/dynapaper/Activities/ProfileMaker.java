package com.brandroid.dynapaper.Activities;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
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
import java.util.Timer;
import java.util.TimerTask;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;
import org.apache.http.util.ByteArrayBuffer;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.brandroid.JSON;
import com.brandroid.Logger;
import com.brandroid.MediaUtils;
import com.brandroid.dynapaper.GalleryItem;
import com.brandroid.dynapaper.R;
import com.brandroid.dynapaper.WallChanger;
import com.brandroid.dynapaper.Database.GalleryDbAdapter;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
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
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class ProfileMaker extends BaseActivity implements OnClickListener
{ 
	private EditText mTxtURL, mTxtZip;
	private ImageView mImgPreview;
	private Button mBtnSelect, mBtnTest, mBtnOnline;
	private CheckBox mBtnWeather, mBtnGPS;
	private Intent mIntent;
	private View mProgressPanel;
	private ProgressBar mProgressBar;
	private TextView mProgressLabel;
	private UploadTask mUploadTask;
	private DownloadToWallpaperTask mDownloadTask;
	private GalleryDbAdapter gdb;
	private LocationProvider locationProvider;
	private LocationListener locationListener;
	private LocationManager locationManager;
	private Boolean mWifiEnabled = true;
	
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
		
		gdb = new GalleryDbAdapter(getApplicationContext());
		gdb.open();
		Cursor c = gdb.fetchAllItems();
		startManagingCursor(c);
		
		mBtnSelect = (Button)findViewById(R.id.btnSelect);
		mBtnTest = (Button)findViewById(R.id.btnTest);
		mBtnWeather = (CheckBox)findViewById(R.id.btnWeather);
		mBtnGPS = (CheckBox)findViewById(R.id.btnGPS);
		mBtnOnline = (Button)findViewById(R.id.btnOnline);
		
		mProgressPanel = findViewById(R.id.progress_layout);
		mProgressBar = (ProgressBar)findViewById(R.id.progress_progress);
		mProgressLabel = (TextView)findViewById(R.id.progress_label);
		
		mBtnTest.setOnClickListener(this);
		mBtnSelect.setOnClickListener(this);
		mBtnWeather.setOnClickListener(this);
		mBtnGPS.setOnClickListener(this);
		mBtnOnline.setOnClickListener(this);
		
		findViewById(R.id.btnCurrent).setOnClickListener(this);
		findViewById(R.id.btnGallery).setOnClickListener(this);
		findViewById(R.id.btnSelect).setOnClickListener(this);
		//findViewById(R.id.btnStocks).setOnClickListener(this);
		findViewById(R.id.btnGPS).setOnClickListener(this);
		findViewById(R.id.btnTest).setOnClickListener(this);
		findViewById(R.id.btnURL).setOnClickListener(this);
		findViewById(R.id.progress_cancel).setOnClickListener(this);
		
		mProgressPanel.setVisibility(View.GONE);
		
		mTxtURL = (EditText)findViewById(R.id.txtURL);
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
		mBtnSelect.setEnabled(false);
		mBtnTest.setEnabled(false);
		mTxtZip = (EditText)findViewById(R.id.txtZip);
		findViewById(R.id.txtURL).setVisibility(View.GONE);
		
		getSavedSettings();
		
		mBtnOnline.setEnabled(c.getCount() > 0);
		
		//mTxtZip.setText(prefs.getString("zip", mTxtZip.getText().toString()));
		
		new UpdateOnlineGalleryTask().execute((String[])null);
	}
	
	public String getDynaURL()
	{
		String url = "";
		if(mTxtURL != null)
			url = mTxtURL.getText().toString();
		if(url == "")
			url = prefs.getSetting("baseUrl", url);
		if(url == "")
			url = "schema.jpg";
		if(mBtnWeather.isChecked())
			url = WallChanger.MY_WEATHER_URL.replace("%USER%", WallChanger.getUser()) + "&source=google&format=image&type=image" +
				"&x=26&w=" + getHomeWidth() + "&h=" + getHomeHeight() +
				(mTxtZip.getText().length() > 0 ? "&zip=" + mTxtZip.getText() : "") +
				"&i1=" + URLEncoder.encode(url.replace(WallChanger.MY_ROOT_URL + "/images/", "").replace(WallChanger.MY_ROOT_URL, ""));
		else
			url = WallChanger.getImageFullUrl(url);
		Logger.LogInfo("Final DynaURL: " + url);
		return url;
	}
	
	public Boolean checkWifi()
	{
		mWifiEnabled = true;
		try {
			WifiManager wm = (WifiManager)getSystemService(WIFI_SERVICE);
			if(wm != null)
			{
				WifiInfo wi = wm.getConnectionInfo();
				if(wi != null)
				{
					Logger.LogInfo("WIFI Info: " + wi.toString());
					if(wi.getSupplicantState() == SupplicantState.COMPLETED)
						mWifiEnabled = true;
				}
			}
		} catch(Exception ex) { Logger.LogError("Error checking Wifi", ex); }
		return mWifiEnabled;
	}
	
	public void onClickCurrent()
	{
		Bitmap mCurrent = ((BitmapDrawable)getWallpaper()).getBitmap(); // getSizedBitmap(((BitmapDrawable)getWallpaper()).getBitmap(), getHomeWidth(), getHomeHeight());
		//mImgPreview.setImageBitmap(mCacheBitmap);
		onCancelUpload();
		mUploadTask = new UploadTask();
		mUploadTask.execute(mCurrent);
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
	public void onClickPreview()
	{
		new DownloadToWallpaperTask(true).execute(getDynaURL());
	}
	public void onClickSelect()
	{
		new DownloadToWallpaperTask().execute(getDynaURL());
	}
	public void onClickGPS()
	{
		try {
			LocationListener ll = getLocationListener();
			if(mBtnGPS.isChecked())
				locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, ll);
			else
				locationManager.removeUpdates(ll);
		} catch(Exception ex) { Logger.LogError("Error toggling GPS", ex); }
		//mTxtZip.setEnabled(mBtnGPS.isChecked());
	}

	@Override
	public void onClick(View v)
	{
		checkWifi();
		switch(v.getId())
		{
			case R.id.btnCurrent:
				onClickCurrent();
				break;
			case R.id.btnGallery:
				onClickLocalGallery();
				break;
			case R.id.btnOnline:
				onClickOnlineGallery();
				break;
			case R.id.progress_cancel:
				onCancelUpload();
				onCancelDownload();
				break;
			case R.id.btnGPS:
				onClickGPS();
				break;
			case R.id.btnTest:
				onClickPreview();
				break;
			case R.id.btnSelect:
				onClickSelect();
				break;
			case R.id.btnWeather:
				mTxtZip.setEnabled(mBtnWeather.isChecked());
				break;
			case R.id.btnURL:
				mTxtURL.setVisibility(mTxtURL.getVisibility() == View.GONE ? View.VISIBLE : View.GONE);
				break;
		}
	}
	
	public LocationListener getLocationListener()
	{
		if(locationListener != null) return locationListener;
		locationManager = (LocationManager)getSystemService(LOCATION_SERVICE);
		final Timer cancelTimer = new Timer(false);
		final TimerTask cancelTask = new TimerTask() {
			public void run() {
				locationManager.removeUpdates(getLocationListener());
			}
		};
		locationListener = new LocationListener()
		{
			public void onStatusChanged(String provider, int status, Bundle extras)
			{
				Logger.LogWarning("LocationListener Status Change: " + status);
			}
			public void onProviderEnabled(String provider) { Logger.LogInfo("Location Provider \"" + provider + "\" enabled."); }
			public void onProviderDisabled(String provider) {
				Logger.LogInfo("Location Provider \"" + provider + "\" disabled.");
			}
			public void onLocationChanged(Location location) {
				if(WallChanger.setLastLocation(location))
				{
					String lat = ((Double)location.getLatitude()).toString();
					if(lat.length() > 6)
						lat = lat.substring(0, Math.max(6, lat.indexOf(".") + 4));
					String lng = ((Double)location.getLongitude()).toString();
					if(lng.length() > 6)
						lng = lng.substring(0, Math.max(6, lng.indexOf(".") + 4));
					mTxtZip.setText(lat+","+lng);
				}
			}
		};
		locationManager.addGpsStatusListener(new GpsStatus.Listener() {
			public void onGpsStatusChanged(int event) {
				switch(event)
				{
					case GpsStatus.GPS_EVENT_STARTED:
						try {
							cancelTimer.schedule(cancelTask, 30000);
						} catch(IllegalStateException ise) { Logger.LogError("Couldn't schedule GPS cancel Timer", ise); }
						mBtnGPS.setTextColor(Color.DKGRAY);
						break;
					case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
						mBtnGPS.setTextColor(Color.CYAN);
						break;
					case GpsStatus.GPS_EVENT_FIRST_FIX:
						mBtnGPS.setTextColor(Color.GREEN);
						break;
					case GpsStatus.GPS_EVENT_STOPPED:
						mBtnGPS.setTextColor(Color.WHITE);
						break;
					default: Logger.LogWarning("GpsListener Status Change: " + event);
				}
			}
		});
		Criteria c = new Criteria();
		c.setAccuracy(Criteria.ACCURACY_FINE);
		String provider = locationManager.getBestProvider(c, true);
		Logger.LogInfo("Best provider: " + provider);
		LocationProvider lp = locationManager.getProvider(provider);
		Logger.LogInfo(lp.getName() + " accuracy: " + lp.getAccuracy()); 
		Location loc = locationManager.getLastKnownLocation(provider);
		if(loc == null)
			if(provider != LocationManager.NETWORK_PROVIDER)
				loc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
			
		if(loc != null)
			WallChanger.setLastLocation(loc);
		
		return locationListener;
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
			Bitmap blg = BitmapFactory.decodeFile(selPath);
			Bitmap bmp = MediaUtils.getSizedBitmap(blg, getHomeWidth(), getHomeHeight());
			blg = null;
			if(bmp != null)
			{
				//mCacheBitmap = ((BitmapDrawable)getWallpaper()).getBitmap(); // getSizedBitmap(((BitmapDrawable)getWallpaper()).getBitmap(), getHomeWidth(), getHomeHeight());
				onCancelUpload();
				mUploadTask = new UploadTask();
				mUploadTask.execute(bmp);
				//mImgPreview.setVisibility(View.GONE);
				mImgPreview.setImageBitmap(bmp);
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
			if(!url.startsWith("http"))
				url = WallChanger.getImageFullUrl(url);
			byte[] bmp = data.getByteArrayExtra("data");
			if(bmp != null && bmp.length > 0)
			{
				Bitmap mGalleryBitmap = BitmapFactory.decodeByteArray(bmp, 0, bmp.length);
				if(mGalleryBitmap != null)
				{
					//new UploadTask().execute(mGalleryBitmap);
					mImgPreview.setImageBitmap(mGalleryBitmap);
				}
			} else {
				int width = 0, height = 0;
				width = getHomeWidth() / 2;
				String sThumbUrl = WallChanger.getImageThumbUrl(mTxtURL.getText().toString(), width, height);
				Logger.LogWarning("Couldn't find image in Intent. Re-downloading " + sThumbUrl, new Exception("Dummy"));
				new DownloadToWallpaperTask(true).execute(sThumbUrl);
			}
	    	//new DownloadToWallpaperTask().execute(selURL);
		}
	}
	
    public Boolean setHomeWallpaper(Bitmap bmp)
    {
    	try {
   			setWallpaper(bmp);
    		showToast(getResourceString(R.string.s_updated));
            return true;
        } catch (Exception ex) {
        	Logger.LogError("Wallchanger Exception during update: " + ex.toString(), ex);
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
    
    private void onCancelUpload()
    {
    	if(mUploadTask != null && mUploadTask.getStatus() == Status.RUNNING)
    	{
    		mUploadTask.cancel(true);
    		mUploadTask = null;
    	}
    }
    private void onCancelDownload()
    {
    	if(mDownloadTask != null && mDownloadTask.getStatus() == Status.RUNNING)
    	{
    		mDownloadTask.cancel(true);
    		mDownloadTask = null;
    	}
    }
    
    public JSONObject downloadJSON(String url)
    {
    	HttpURLConnection uc = null;
    	InputStream in;
    	BufferedReader br;
    	StringBuilder sb;
    	JSONObject ret = null;
    	try {
    		uc = (HttpURLConnection)new URL(url).openConnection();
    		uc.setReadTimeout(10000);
    		uc.addRequestProperty("Accept-Encoding", "gzip, deflate");
    		uc.connect();
    		if(uc.getResponseCode() == HttpURLConnection.HTTP_OK)
    		{
    			String encoding = uc.getContentEncoding();
    			if(encoding != null && encoding.equalsIgnoreCase("gzip"))
    				in = new GZIPInputStream(uc.getInputStream());
    			else if(encoding != null && encoding.equalsIgnoreCase("deflate"))
    				in = new InflaterInputStream(uc.getInputStream(), new Inflater(true));
    			else
    				in = new BufferedInputStream(uc.getInputStream());
    			br = new BufferedReader(new InputStreamReader(in));
	    		sb = new StringBuilder();
	    		String line = "";
	    		while((line = br.readLine()) != null)
	    			sb.append(line + '\n');
	    		ret = JSON.Parse(sb.toString());
	    		if(ret == null)
	    			Logger.LogWarning("Unable to parse JSON: " + sb.toString());
    		} else Logger.LogWarning(uc.getResponseCode() + " returned for " + uc.getURL().toString());
    	} catch(Exception ex) { Logger.LogError("Exception reading JSON from " + url, ex); }
    	return ret;
    }
    
    private class MonitorUploadTask extends AsyncTask<String, Integer, String>
    {
    	private final static int iUpdateIntervalMS = 1000;
    	private final static int iUpdateMax = 15;
    	
    	@Override
		protected String doInBackground(String... arg0)
		{
    		String sKey = arg0[0];
			String url = WallChanger.MY_UPLOAD_PROGRESS_URL.replace("%KEY%", sKey);
			for(int i = 0; i < iUpdateMax; i++)
			{
				JSONObject j = downloadJSON(url);
				if(j != null)
					Logger.LogInfo("Upload JSON: " + j.toString());
				else
					Logger.LogWarning("Upload JSON NULL!");
				try {
					Thread.sleep((long)iUpdateIntervalMS);
				} catch (InterruptedException e) { }
			}
			return null;
		}
    }
    
	private class UploadTask extends AsyncTask<Bitmap, Integer, String>
	{
		private MonitorUploadTask mMonitor;
		
		@Override
		protected String doInBackground(Bitmap... pics)
		{
			URL url = null;
			HttpURLConnection con = null;
			DataOutputStream out = null;
			InputStream in = null;
			StringBuilder ret = new StringBuilder();
			InputStreamReader sr = null;
			publishProgress(-1);
			try {
				ByteArrayOutputStream stream = new ByteArrayOutputStream();
				pics[0].compress(CompressFormat.JPEG, WallChanger.getUploadQuality(mWifiEnabled), stream);
				byte[] data = stream.toByteArray(); 
				//int length = data.length;
				String md5 = getMD5(data);
				//Logger.LogInfo("MD5: " + md5);
				url = new URL(WallChanger.MY_USER_IMAGE_URL.replace("%USER%", WallChanger.getUser()).replace("%MD5%", md5));
				Logger.LogInfo("Checking " + url.toString());
				con = (HttpURLConnection)url.openConnection();
				//con.setRequestProperty("If-None-Match", md5);
				con.setInstanceFollowRedirects(false);
				con.setConnectTimeout(5000);
				con.connect();
				int iResponse = con.getResponseCode();
				Logger.LogInfo("Response code: " + iResponse);
				if(con.getResponseCode() == 304) {
					Logger.LogInfo("Image already uploaded. No need to re-upload.");
					String sLocation = con.getHeaderField("Location");
					sLocation = sLocation.replace(WallChanger.MY_IMAGE_ROOT_URL, "");
					//ret.append()
					ret.append(sLocation);
				} else
				{
					//mMonitor = new MonitorUploadTask();
					//mMonitor.execute(md5);
					Logger.LogInfo("New upload!");
					con.disconnect();
					url = new URL(WallChanger.MY_UPLOAD_IMAGE_URL.replace("%USER%", WallChanger.getUser()).replace("%MD5%", md5));
					con = (HttpURLConnection)url.openConnection();
					con.setRequestMethod("POST");
					con.setConnectTimeout(15000);
					con.setDoOutput(true);
					con.setDoInput(true);
					con.setUseCaches(false);
					//con.setRequestProperty("Connection", "Keep-Alive");
					//con.setRequestProperty("Content-Type", "multipart/form-data; boundary=*****");
					
					writeDataToStream(data, con.getOutputStream());
					
					in = con.getInputStream();
					sr = new InputStreamReader(in);
					char[] buf = new char[64];
					while(sr.read(buf) > 0)
					{
						ret.append(buf);
						if(buf.length < 64)
							break;
					}
				}
			} catch(Exception ex) {
				Logger.LogError("Uploading error.", ex);
			}
			finally {
				try {
					if(out!=null) out.close();
				} catch(IOException ex) { Logger.LogError("Trying to close output.", ex); }
				try {
					if(sr!=null) sr.close();
				}catch(IOException ex) { Logger.LogError("Trying to close Stream Reader.", ex); }
				try {
					if(in!=null) in.close();
				}catch(IOException ex) { Logger.LogError("Trying to close input.", ex); }
				
			}
			return ret.toString();
		}
		
		public void writeDataToStream(byte[] data, OutputStream s) throws IOException
		{
			BufferedOutputStream out = new BufferedOutputStream(s, 1);
			publishProgress(0, data.length);
			for(int i = 0; i < data.length; i += WallChanger.DOWNLOAD_CHUNK_SIZE)
			{
				int writelen = WallChanger.DOWNLOAD_CHUNK_SIZE;
				if(writelen + i > data.length)
					writelen = data.length - i;
				if(writelen <= 0) break;
				out.write(data, i, writelen);
				publishProgress(i, data.length);
			}
			out.flush();
			publishProgress(data.length, data.length);
			out.close();
			publishProgress(-1);
		}

		@Override
		protected void onPreExecute() {
			mProgressBar.setProgress(0);
			mProgressLabel.setText(getText(R.string.s_uploading));
			showPanel(mProgressPanel, true);
			
			Logger.LogInfo("Uploading image");
			mBtnSelect.setEnabled(false);
			mBtnTest.setEnabled(false);
			findViewById(R.id.btnCurrent).setEnabled(false);
		}
		
		@Override
		protected void onCancelled() {
			if(mMonitor != null && mMonitor.getStatus() == Status.RUNNING)
				mMonitor.cancel(true);
			hidePanel(mProgressPanel, false);
			mBtnSelect.setEnabled(true);
			mBtnTest.setEnabled(true);
			findViewById(R.id.btnCurrent).setEnabled(true);
		}
		
		@Override
		protected void onProgressUpdate(Integer... values)
		{
			if(values.length > 1)
			{
				mProgressBar.setIndeterminate(false);
				mProgressBar.setMax(values[1]);
				mProgressBar.setProgress(values[0]);
			} else if(values[0] == -1)
				mProgressBar.setIndeterminate(true);
			else if(values[0] == 0)
				mProgressBar.setIndeterminate(false);
		}
		
		@Override
		protected void onPostExecute(String result) {
			if(mMonitor != null && mMonitor.getStatus() == Status.RUNNING)
				mMonitor.cancel(true);
			if(result == null || result == "")
				showToast("Invalid response");
			else {
				hidePanel(mProgressPanel, true);
				Logger.LogInfo("Response received from upload.");
				String mBaseUrl = result.replaceAll("[^A-Za-z0-9\\.\\/]", "");
				mTxtURL.setText(mBaseUrl);
				prefs.setSetting("baseUrl", mBaseUrl);
				//if(mDynaUrl.indexOf("&i1=") > -1)
				//	mDynaUrl = mDynaUrl.substring(0, mDynaUrl.indexOf("&i1="));
				//mDynaUrl += "&i1=" + mBaseUrl;
				//mTextUrl.setText(mDynaUrl);
				//mTextUrl.setEnabled(true);
				mBtnSelect.setEnabled(true);
				mBtnTest.setEnabled(true);
				findViewById(R.id.btnCurrent).setEnabled(true);
			}
			//mProgressBar.setVisibility(View.GONE);
			//mImgPreview.setVisibility(View.GONE);
			//mImgSample.setImageDrawable(getWallpaper());
			//mImgSample.setVisibility(View.VISIBLE);
		}
	}

	
	private class UpdateOnlineGalleryTask extends AsyncTask<String, Integer, Boolean> {

		@Override
		protected Boolean doInBackground(String... params)
		{
			String cIDs = gdb.fetchAllIDs();
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
	    		if(Logger.hasDb())
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
	    		if(uc.getResponseCode() == HttpURLConnection.HTTP_OK)
	    		{
	    			String encoding = uc.getContentEncoding();
	    			prefs.setSetting("gallery_update", ((Long)uc.getLastModified()).toString());
	    			Logger.LogInfo("Encoding: " + encoding + " @ " + uc.getLastModified());
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
								
								Logger.LogInfo("Successfully add/updated " + adds + " records!");
								success = true;
								prefs.setSetting("gallery_update", modified.toString());
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
				if(mTxtZip.getText().equals(""))
				{
					prefs.setSetting("zip", values[0]);
					mTxtZip.setText(values[0].toString());
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
	    		Integer length = uc.getContentLength();
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
	    		mImgPreview.setImageBitmap(result);
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
		if(mTxtZip != null)
			mTxtZip.setText(prefs.getSetting("zip", mTxtZip.getText().toString()));
		if(mBtnWeather != null)
			mBtnWeather.setChecked(prefs.getBoolean("weather", mBtnWeather.isChecked()));
		WallChanger.setUser(prefs.getSetting("user", WallChanger.getUser()));
	}
	
	public void setSavedSettings()
	{
		if(mTxtZip != null)
			prefs.setSetting("zip", mTxtZip.getText().toString());
		if(mBtnWeather != null)
			prefs.setSetting("weather", mBtnWeather.isChecked());
		if(WallChanger.getUser() != null && WallChanger.getUser() != "")
			prefs.setSetting("user", WallChanger.getUser());
	}

	@Override
	protected void onDestroy() {
		Logger.LogVerbose("onDestroy");
		super.onDestroy();
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
	
	@Override
	protected void onPause() {
		super.onPause();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
	}
}
