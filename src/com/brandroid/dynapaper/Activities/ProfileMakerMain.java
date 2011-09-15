package com.brandroid.dynapaper.Activities;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

import org.apache.http.util.ByteArrayBuffer;

import com.brandroid.dynapaper.R;
import com.brandroid.dynapaper.WallChanger;
import com.brandroid.dynapaper.widget.Weather;
import com.brandroid.dynapaper.widget.Widget;
import com.brandroid.dynapaper.widget.Widgets;
import com.brandroid.util.ImageUtilities;
import com.brandroid.util.Logger;
import com.brandroid.util.MediaUtils;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.ActionBar.TabListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Bitmap.Config;
import android.graphics.Paint.Style;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

public class ProfileMakerMain extends BaseActivity
{
	private boolean mUseMultiplePanes = false;
	private HashMap<String, Fragment> mFragHash = new HashMap<String, Fragment>();
	private View mProgressPanel;
	private ProgressBar mProgressBar;
	private TextView mProgressLabel;
	private Button mProgressCancel;
	private String mWeatherLocation = "";
	private int mWeatherPosition = 4; // middle center
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.wallchanger);
		mProgressBar = (ProgressBar)findViewById(R.id.progress_progress);
		mProgressPanel = findViewById(R.id.progress_progress);
		mProgressLabel = (TextView)findViewById(R.id.progress_label);
		mProgressCancel = (Button)findViewById(R.id.progress_cancel);
		if(isGTV())
		{
			showToast("Welcome, GoogleTV user!");
		}
		if(true)
			getActionBar().hide();
		else
			findViewById(R.id.title_frame).setVisibility(View.GONE);
		MediaUtils.init(getApplicationContext());
		try {
		if(!MediaUtils.writeFile("pewp", new byte[] { }, true))
			Logger.LogWarning("Unable to write to pewp");
		else
			Logger.LogInfo("Wrote to pewp!");
		} catch(IOException fnfe) { Logger.LogError("Unable to write to pewp!", fnfe); }
		mUseMultiplePanes = (null != findViewById(R.id.detail_container));
		if (null == savedInstanceState) {
			FragmentManager fm = getSupportFragmentManager();
			FragmentTransaction ft = fm.beginTransaction();
			mFragHash.put("list", new ProfileMakerList());
			ft.replace(R.id.list, mFragHash.get("list"), "list");
			ft.commit();
			ShowDetailFragment(new PreviewFragment(), "preview");
			ShowPreview(((BitmapDrawable)getWallpaper()).getBitmap());
		}
	}
	
	public void ShowDetailFragment(Fragment frag, String tag)
	{
		Boolean bExists = false;
		if(mFragHash.containsKey(tag))
			bExists = true;
		FragmentManager fm = getSupportFragmentManager();
		FragmentTransaction ft = fm.beginTransaction();
		int id = R.id.detail_container;
		if(!mUseMultiplePanes)
			id = R.id.list;
		if(!mFragHash.containsValue(frag))
			ft.replace(id, frag, tag);
		else
			ft.show(frag);
	    ft.addToBackStack(null);
		mFragHash.put(tag, frag);
		ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
		ft.commit();
	}
	
	private PreviewFragment getPreviewFragment() {
		PreviewFragment ret = (PreviewFragment)getSupportFragmentManager().findFragmentByTag("preview");
		if(ret == null)
		{
			if(mFragHash.containsKey("preview") && mFragHash.get("preview") != null)
				ret = (PreviewFragment)mFragHash.get("preview");
			else
				ret = new PreviewFragment();
			
			ShowDetailFragment(ret, "preview");
		}
		return ret;
	}
	
	public void ShowPreview(Bitmap bmp)
	{
		PreviewFragment fragPreview = getPreviewFragment();
		if(fragPreview != null)
			fragPreview.setImageBitmap(bmp);
		else
			showToast("Preview fragment not found");
	}
    public Boolean setHomeWallpaper(Bitmap bmp)
    {
    	try {
   			setWallpaper(bmp);
    		showToast(getResourceString(R.string.s_updated));
    		Intent startMain = new Intent(Intent.ACTION_MAIN);
            startMain.addCategory(Intent.CATEGORY_HOME);
            startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(startMain);
            return true;
        } catch (Exception ex) {
        	Logger.LogError("Error setting Wallpaper", ex);
        	showToast(getResourceString(R.string.s_invalid));
            return false;
        }

    }

	public String getWeatherLocation()
	{
		return mWeatherLocation;
	}
	
	public void setWeatherLocation(String location)
	{
		if(location.equals("")) return;
		Logger.LogInfo("New Location: " + location);
		mWeatherLocation = location;
		///TODO: Fix Weather location update
		/*
		if(mBtnWeatherLocation != null)
		{
			mBtnWeatherLocation.setText(location);
			mBtnWeatherLocation.setTextColor(Color.BLACK);
		}
		*/
	}
	
	public int getWeatherPosition()
	{
		return mWeatherPosition;
	}
	
	public void setWeatherPosition(int index)
	{
		Logger.LogInfo("New Position: " + mWeatherPosition);
		mWeatherPosition = index;
		/*
		if(mBtnWeatherPosition != null)
		{
			Bitmap bmp = null;
			if(mWeatherPosition == 4)
				bmp = BitmapFactory.decodeResource(mResources, R.drawable.arrow_green_center);
			else
				bmp = ImageUtilities.rotateImage(BitmapFactory.decodeResource(mResources, R.drawable.arrow_green), SelectPosition.getDegreesFromIndex(mWeatherPosition));
			if(bmp != null)
			{
				LayerDrawable ld = new LayerDrawable(new Drawable[]{mResources.getDrawable(android.R.drawable.btn_default) , new BitmapDrawable(bmp)});
				mBtnWeatherPosition.setBackgroundDrawable(ld);
			}
		}
		*/
	}
	
	
	public boolean isGTV() { return getPackageManager().hasSystemFeature("com.google.android.tv"); } 

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if(isGTV()) return false;
		menu.add("Old UI");
		getMenuInflater().inflate(R.menu.maker, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId())
		{
		case R.id.menu_settings:
			Logger.LogInfo("Settings menu selected");
			startActivityForResult(new Intent(getApplicationContext(), Settings.class), WallChanger.REQ_SETTINGS);
			break;
		case R.id.menu_help:
			Logger.LogInfo("Help menu selected");
			startActivity(new Intent(getApplicationContext(), Help.class));
			break;
		case R.id.menu_feedback:
			Logger.LogInfo("Feedback menu selected");
			ShowDetailFragment(new Feedback(), "feedback");
			//startActivity(new Intent(getApplicationContext(), Feedback.class));
			break;
		default:
			if(item.getTitle().equals("Old UI"))
				startActivity(new Intent(getApplicationContext(), ProfileMaker.class));
			break;
		}
		return super.onOptionsItemSelected(item);
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
			int bw = base.getWidth();
			int bh = base.getHeight();
			if(w < h)
			{
				int tmp = w;
				w = h;
				h = tmp;
				tmp = bw;
				bw = bh;
				bh = tmp;
			}
			//base = Bitmap.createScaledBitmap(base, w, h, true);
			Bitmap ret = Bitmap.createBitmap(w, h, Config.ARGB_8888);
			Canvas c = new Canvas(ret);
			c.save();
			Paint p = new Paint();
			p.setStyle(Style.FILL);
			Rect src = new Rect(0, 0, bw, bh);
			RectF dst = new RectF(new Rect(0, 0, w, h));
			c.drawBitmap(base, src, dst, p);
			base = null;
			
			publishProgress(-3);
			Widgets widgets = getSelectedWidgets();
			int count = widgets.size();
			publishProgress(1, 1 + count);
			for(int i=0; i < count; i++)
			{
				if(!widgets.get(i).applyTo(ret, c))
					showToast(getResourceString(R.string.s_error, R.string.s_adding, R.string.s_widgets));
				publishProgress(1 + i, 1 + count);
			}
			c.restore();
			//Canvas.freeGlCaches();
			return ret;
		}

	    @Override
	    protected void onPreExecute() {
	    	mProgressBar.setProgress(0);
			mProgressLabel.setText(getResourceString(R.string.s_adding, R.string.btn_weather, R.string.s_widgets));
			showPanel(mProgressPanel, true);
			
	    	//showToast("Downloading image.");
	    	//mBtnSelect.setEnabled(false);
	    	//mBtnTest.setEnabled(false);
	    }
	    
	    @Override
		protected void onCancelled() {
			hidePanel(mProgressPanel, false);
	    	//mBtnSelect.setEnabled(true);
	    	//mBtnTest.setEnabled(true);
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
	    		ShowPreview(result);
	    		//MediaUtils.writeFile("last.jpg", result, true);
	    		if(mSetWallpaper)
	    		{
		    		setHomeWallpaper(result);
		    		finish();
	    		}
		    	//mCacheBitmap = result;
		    	//mBtnSelect.setEnabled(true);
		    	//mBtnTest.setEnabled(true);
	    	}
	    }
		
		private Bitmap downloadBitmap(String url)
		{
			Bitmap ret = null;
			InputStream s = null;
			try {
				if(url.startsWith("/"))
				{
					ret = MediaUtils.readFileBitmap(url, true);
					if(ret != null) return ret;
				}
				Logger.LogDebug("Trying to download " + url);
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

	public Widgets getSelectedWidgets()
	{
		Widgets ret = new Widgets();
		boolean bWeather = true;
		if(bWeather)
		{
			Widget w = new Weather(getApplicationContext(), getWeatherLocation());
			Point pt = new Point(0,0);
			switch(getWeatherPosition())
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
			ret.add(w);
		}
		return ret;
	}
	

}