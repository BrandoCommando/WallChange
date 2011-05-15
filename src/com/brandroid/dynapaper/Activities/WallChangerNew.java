package com.brandroid.dynapaper.Activities;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.http.client.HttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.brandroid.GalleryItem;
import com.brandroid.JSON;
import com.brandroid.dynapaper.GalleryDbAdapter;
import com.brandroid.dynapaper.Preferences;
import com.brandroid.dynapaper.R;
import com.google.ads.AdRequest;
import com.google.ads.AdSize;
import com.google.ads.AdView;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.MaskFilter;
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
	private EditText mTxtURL, mTxtZip;
	private ProgressBar mProgressBar;
	private ImageView mImgPreview;
	private Button mBtnSelect, mBtnTest, mBtnOnline;
	private CheckBox mBtnWeather, mBtnGPS;
	private Intent mIntent;
	private Bitmap mCacheBitmap;
	//private String mGPSLocation = null;
	
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
		findViewById(R.id.btnSelect).setOnClickListener(this);
		//findViewById(R.id.btnStocks).setOnClickListener(this);
		findViewById(R.id.btnGPS).setOnClickListener(this);
		findViewById(R.id.btnTest).setOnClickListener(this);
		findViewById(R.id.btnUndo).setOnClickListener(this);
		findViewById(R.id.btnURL).setOnClickListener(this);
		
		mBtnWeather = (CheckBox)findViewById(R.id.btnWeather);
		mBtnWeather.setOnClickListener(this);
		mBtnGPS = (CheckBox)findViewById(R.id.btnGPS);
		mBtnGPS.setOnClickListener(this);
		
		mBtnOnline = (Button)findViewById(R.id.btnOnline);
		mBtnOnline.setOnClickListener(this);
		mBtnOnline.setEnabled(false);
		
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
		findViewById(R.id.btnUndo).setEnabled(false);
		mBtnSelect.setEnabled(false);
		mBtnTest.setEnabled(false);
		mTxtZip = (EditText)findViewById(R.id.txtZip);
		findViewById(R.id.txtURL).setVisibility(View.GONE);
		findViewById(R.id.progressBar1).setVisibility(View.GONE);
		
		mTxtZip.setText(prefs.getString("zip", mTxtZip.getText().toString()));
		
		mImgPreview.setVisibility(View.INVISIBLE);
		
		new UpdateOnlineGalleryTask().execute((String[])null);
		
		if(!isPaidMode())
			addAds();
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
			url = MY_ROOT_URL + "/images/weather.php?source=google&format=image&" +
				"&x=26&w=" + getHomeWidth() + "&h=" + getHomeHeight() +
				(mTxtZip.getText().length() > 0 ? "&zip=" + mTxtZip.getText() : "") +
				"&i1=" + URLEncoder.encode(url.replace(MY_ROOT_URL + "/images/", "").replace(MY_ROOT_URL, ""));
		else
			url = getImageFullUrl(url);
		Log.i(LOG_KEY, "Final DynaURL: " + url);
		return url;
	}

	@Override
	public void onClick(View v)
	{
		switch(v.getId())
		{
			case R.id.btnCurrent:
				mCacheBitmap = ((BitmapDrawable)getWallpaper()).getBitmap(); // getSizedBitmap(((BitmapDrawable)getWallpaper()).getBitmap(), getHomeWidth(), getHomeHeight());
				mImgPreview.setImageBitmap(mCacheBitmap);
				new UploadTask().execute(mCacheBitmap);
				break;
			case R.id.btnGallery:
				Intent intentGallery = new Intent();
				intentGallery.setType("image/*");
				intentGallery.setAction(Intent.ACTION_GET_CONTENT);
				startActivityForResult(Intent.createChooser(intentGallery, getResourceString(R.string.s_select_base)), REQ_SELECT_GALLERY);
				break;
			case R.id.btnOnline:
				Intent intentOnline = new Intent(getApplicationContext(), GalleryPicker.class);
				intentOnline.setAction(Intent.ACTION_GET_CONTENT);
				intentOnline.setType("image/*");
				startActivityForResult(intentOnline, REQ_SELECT_ONLINE);
				break;
			case R.id.btnGPS:
				//mGPSLocation = null;
				mTxtZip.setEnabled(mBtnGPS.isChecked());
				break;
			case R.id.btnTest:
				new DownloadToWallpaperTask(true).execute(getDynaURL());
				break;
			case R.id.btnSelect:
				new DownloadToWallpaperTask().execute(getDynaURL());
				break;
			case R.id.btnWeather:
				Boolean bWeather = mBtnWeather.isChecked();
				//Boolean bGPS = mBtnGPS.isChecked();
				mTxtZip.setEnabled(bWeather);
				//mBtnGPS.setEnabled(bWeather);
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
		if(requestCode == REQ_SELECT_GALLERY)
		{
			Uri selUri = data.getData();
			String selPath = getMediaPath(selUri);
			Bitmap bmp = getSizedBitmap(BitmapFactory.decodeFile(selPath), getHomeWidth(), getHomeHeight());
			if(bmp != null)
			{
				//mCacheBitmap = ((BitmapDrawable)getWallpaper()).getBitmap(); // getSizedBitmap(((BitmapDrawable)getWallpaper()).getBitmap(), getHomeWidth(), getHomeHeight());
				new UploadTask().execute(bmp);
				mImgPreview.setVisibility(View.INVISIBLE);
				mImgPreview.setImageBitmap(bmp);
			} else Log.e(LOG_KEY, "Unable to create thumbnail?");
		} else if (requestCode == REQ_SELECT_ONLINE)
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
			} else {
				String sThumbUrl = getImageThumbUrl(mTxtURL.getText().toString() + "?w=" + (getHomeWidth() / 2));
				Log.w(LOG_KEY, "Couldn't find image in Intent. Re-downloading " + sThumbUrl);
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
        	Log.e(LOG_KEY, "Wallchanger Exception during update: " + ex.toString());
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
    	} catch(NoSuchAlgorithmException nsme) { Log.e(LOG_KEY, "WTF! No MD5!"); return null; }
    }
    
	private class UploadTask extends AsyncTask<Bitmap, Void, String>
	{

		@Override
		protected String doInBackground(Bitmap... pics)
		{
			URL url = null;
			HttpURLConnection con = null;
			OutputStream out = null;
			InputStream in = null;
			StringBuilder ret = new StringBuilder();
			InputStreamReader sr = null;
			try {
				ByteArrayOutputStream stream = new ByteArrayOutputStream();
				pics[0].compress(CompressFormat.JPEG, getUploadQuality(), stream);
				byte[] data = stream.toByteArray(); 
				String md5 = getMD5(data);
				//Log.i(LOG_KEY, "MD5: " + md5);
				url = new URL(MY_ROOT_URL + "/images/upload2.php" + getUser("?user=") + "&md5=" + md5);
				Log.i(LOG_KEY, "Checking " + url.toString() + " for " + md5);
				con = (HttpURLConnection)url.openConnection();
				con.setRequestProperty("If-None-Match", md5);
				con.setRequestMethod("HEAD");
				con.setConnectTimeout(5000);
				con.connect();
				if(con.getResponseCode() == 304) {
					Log.i(LOG_KEY, "Image already uploaded. No need to re-upload.");
					ret.append("user/" + getUser("","_") + md5 + ".jpg");
				} else
				{
					Log.i(LOG_KEY, "New upload!");
					con.disconnect();
					con = (HttpURLConnection)url.openConnection();
					con.setRequestMethod("POST");
					con.setConnectTimeout(15000);
					con.setDoOutput(true);
					out = con.getOutputStream();
					out.write(data);
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
			mImgPreview.setVisibility(View.VISIBLE);
			//mImgSample.setImageDrawable(getWallpaper());
			//mImgSample.setVisibility(View.VISIBLE);
		}
	}
	
	private class UpdateOnlineGalleryTask extends AsyncTask<String, Void, Boolean> {

		@Override
		protected Boolean doInBackground(String... params)
		{
			GalleryDbAdapter gdb = new GalleryDbAdapter(getApplicationContext());
			gdb.open();
			
			String cIDs = gdb.fetchAllIDs();
			Log.i(LOG_KEY, "IDs fetched: " + cIDs);
			
			String ret = null;
	    	String line = null;
	    	InputStream in = null;
	    	BufferedReader br = null;
	    	StringBuilder sb = null;
	    	HttpURLConnection uc = null;
	    	Long modified = null;
	    	JSONObject jsonGallery = null;
	    	Boolean success = false;
	    	
	    	String url = Preferences.MY_ROOT_URL + "/dynapaper/gallery.php" + getUser("?user=");
	    	
	    	try {
	    		uc = (HttpURLConnection)new URL(url).openConnection();
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
	    		uc.connect();
	    		if(uc.getResponseCode() == HttpURLConnection.HTTP_OK)
	    		{
	    			in = new BufferedInputStream(uc.getInputStream());
		    		br = new BufferedReader(new InputStreamReader(in));
		    		sb = new StringBuilder();
		    		while((line = br.readLine()) != null)
		    			sb.append(line + '\n');
		    		ret = sb.toString();
		    		modified = uc.getLastModified();
		    		jsonGallery = JSON.Parse(ret);
		    		if(jsonGallery != null)
	    			try {
			    		if(jsonGallery.has("user") && jsonGallery.get("user") != getUser())
			    		{
			    			String user = jsonGallery.getString("user");
			    			setUser(user);
			    			prefs.setSetting("user", user);
			    			Log.i(LOG_KEY, "New User: " + user);
			    		}
					} catch (JSONException je) {
						Log.e(LOG_KEY, "JSONException getting user: " + je.toString());
					}
					int adds = 0;
					try {
						JSONArray jsonImages = jsonGallery.getJSONArray("images");
						for(int imgIndex = 0; imgIndex < jsonImages.length(); imgIndex++)
						{
							GalleryItem item = new GalleryItem(jsonImages.getJSONObject(imgIndex));
							if(cIDs.contains(","+item.getID()+","))
								adds += gdb.updateItem(item) ? 1 : 0;
							else
								adds += gdb.createItem(item);
						}
					} catch (JSONException je) {
						Log.e(LOG_KEY, "JSONException getting images: " + je.toString());
					}
					
					Log.i(LOG_KEY, "Successfully added " + adds + " records!");
					success = true;
			    	//setResult(RESULT_OK, mIntent);
					
		    		//gdb.createItem(id, title, url, data, width, height, tags, rating, downloads, visible)
	    			prefs.setSetting("gallery_update", modified.toString());
	    		}
			}
			catch(MalformedURLException mex) { Log.e(LOG_KEY, mex.toString()); }
			catch(ProtocolException pex) { Log.e(LOG_KEY, pex.toString()); }
			catch(IOException ex) { Log.e(LOG_KEY, ex.toString()); }
	    	finally {
				if(uc != null) uc.disconnect();
				in = null;
				br = null;
				sb = null;
				uc = null;
				jsonGallery = null;
	    	}
	    	mGalleryCursor = gdb.fetchAllItems();
	    	gdb.close();
	    	return success;
		}

		@Override
		protected void onPostExecute(Boolean success)
		{
			if(mBtnOnline != null)
				mBtnOnline.setEnabled(success);
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
	    		uc.setConnectTimeout(15000);
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
	    	//showToast("Downloading image.");
	    	if(mImgPreview != null)
	    		mImgPreview.setVisibility(View.INVISIBLE);
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
	    		{
		    		setHomeWallpaper(result);
		    		finish();
	    		}
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
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	@Override
	protected void onStart() {
		super.onStart();
		mTxtZip.setText(prefs.getSetting("zip", mTxtZip.getText().toString()));
		mBtnWeather.setChecked(prefs.getBoolean("weather", mBtnWeather.isChecked()));
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		prefs.setSetting("zip", mTxtZip.getText().toString());
		prefs.setSetting("weather", mBtnWeather.isChecked());
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
