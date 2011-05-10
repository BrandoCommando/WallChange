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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.brandroid.JSON;
import com.brandroid.OnlineGalleryItem;
import com.brandroid.dynapaper.GalleryDbAdapter;
import com.brandroid.dynapaper.Preferences;
import com.brandroid.dynapaper.R;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class OnlineGalleryPicker extends WallChangerActivity implements OnClickListener
{
	private ProgressBar mProgressBar = null;
	private GridView mGridView = null;
	private OnlineGalleryItem[] mGalleryItems;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent intent = getIntent();
		if(intent == null)
			intent = new Intent();
		String action = intent.getAction();
		setContentView(R.layout.online_picker);
		
		prefs = Preferences.getPreferences(OnlineGalleryPicker.this);
		
		mGridView = (GridView)findViewById(R.id.gridView1);
		
		setTitle(getResourceString(R.string.btn_online));
		
		if(mGalleryCursor == null)
			mGalleryCursor = new GalleryDbAdapter(this).open().fetchAllItems();
		startManagingCursor(mGalleryCursor);
		
		String[] from = new String[]{GalleryDbAdapter.KEY_TITLE};
		int[] to = new int[]{R.id.grid_item_text};
		
		SimpleCursorAdapter items = new SimpleCursorAdapter(this, R.layout.grid_item, mGalleryCursor, from, to);
		
		mGridView.setAdapter(items);
		
		//new DownloadStringTask().execute(ONLINE_GALLERY_URL);
	}
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		
	}
	private class DownloadStringTask extends AsyncTask<String, Void, String>
	{
	    /** The system calls this to perform work in a worker thread and
	      * delivers it the parameters given to AsyncTask.execute() */
	    protected String doInBackground(String... urls)
	    {
	    	String ret = null;
	    	String line = null;
	    	InputStream in = null;
	    	BufferedReader br = null;
	    	StringBuilder sb = null;
	    	HttpURLConnection uc = null;
	    	Long modified = null;
	    	String url = urls[0];
	    	try {
	    		uc = (HttpURLConnection)new URL(url).openConnection();
	    		if(prefs.hasSetting("data_" + url) && prefs.hasSetting("modified_" + url))
	    		{
		    		modified = prefs.getLong("modified_" + url, Long.MIN_VALUE);
		    		if(modified > Long.MIN_VALUE)
		    			uc.setIfModifiedSince(modified);
	    		}
	    		uc.connect();
	    		if(uc.getResponseCode() == HttpURLConnection.HTTP_NOT_MODIFIED)
	    			ret = prefs.getString("data_" + url, "");
	    		else
	    		{
	    			in = new BufferedInputStream(uc.getInputStream());
		    		br = new BufferedReader(new InputStreamReader(in));
		    		sb = new StringBuilder();
		    		while((line = br.readLine()) != null)
		    			sb.append(line + '\n');
		    		ret = sb.toString();
		    		modified = uc.getLastModified();
	    			prefs.setSetting("modified_" + url, modified.toString());
		    		prefs.setSetting("data_" + url, ret);
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
	    	}
	    	return ret;
	    }
	    
	    @Override
	    protected void onPreExecute() {
	    	// TODO Auto-generated method stub
	    	super.onPreExecute();
	    	if(mProgressBar != null)
	    		mProgressBar.setVisibility(View.VISIBLE);
	    }
	    
	    /** The system calls this to perform work in the UI thread and delivers
	      * the result from doInBackground() */
	    protected void onPostExecute(String result) {
	    	if(mProgressBar != null)
	    		mProgressBar.setVisibility(View.GONE);
	    	JSONObject json = JSON.Parse(result);
	    	if(json != null && json.has("images"))
	    	{
	    		try {
	    			JSONArray imgs = json.getJSONArray("images");
	    			mGalleryItems = new OnlineGalleryItem[imgs.length()];
	    			for(int i=0; i<imgs.length(); i++)
	    				mGalleryItems[i] = new OnlineGalleryItem(imgs.getJSONObject(i));
	    			mGridView.setAdapter(new ImageAdapter(OnlineGalleryPicker.this));
	    		} catch(JSONException jex) { LogError(jex.toString()); }
	    	}
	    }
	}

	
	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
	}
	
	public class ImageAdapter extends BaseAdapter {
	    private Context mContext;

	    public ImageAdapter(Context c) {
	        mContext = c;
	    }

	    public int getCount() {
	        return mGalleryItems.length;
	    }

	    public Object getItem(int position) {
	        return null;
	    }

	    public long getItemId(int position) {
	        return 0;
	    }

	    // create a new ImageView for each item referencedimage by the Adapter
		@Override
	    public View getView(int position, View convertView, ViewGroup parent) {
	        View view = convertView;
	        OnlineGalleryItem item = mGalleryItems[position]; 
	        if (convertView == null) {  // if it's not recycled, initialize some attributes
	            LayoutInflater li = getLayoutInflater();
	            view = li.inflate(R.layout.grid_item, null);
	            view.findViewById(R.id.grid_item_image).setVisibility(View.GONE);
	            view.findViewById(R.id.grid_item_rating).setVisibility(View.GONE);
	            view.findViewById(R.id.grid_item_text).setVisibility(View.GONE);
	            //imageView.setTag();
		        item.setView(view);
	        }
	        
	        try {
		        if(item.isDownloaded())
		        	((ImageView)view.findViewById(R.id.grid_item_image)).setImageBitmap(item.getBitmap());
		        else if(!item.isStarted())
		        	new DownloadImageTask().execute(item);
		        } catch(NullPointerException npe) {
		        	LogError(npe.toString());
		        	//new DownloadImageTask().execute(item);
		        }
	        
	        return view;
	    }
		
		private class DownloadImageTask extends AsyncTask<OnlineGalleryItem, Void, OnlineGalleryItem> {
	        /** The system calls this to perform work in a worker thread and
		      * delivers it the parameters given to AsyncTask.execute() */
		    protected OnlineGalleryItem doInBackground(OnlineGalleryItem... galleryItems)
		    {
		    	OnlineGalleryItem item = galleryItems[0];
		    	InputStream s = null;
		    	try {
		    		String url = item.getURL();
		    		url = MY_ROOT_URL + "/images/thumb.php?url=" + url;
		    		URLConnection uc = new URL(url).openConnection();
		    		uc.connect();
		    		s = uc.getInputStream();
		    		item.setBitmap(BitmapFactory.decodeStream(s));
		    		item.setIsDownloaded();
		    	} catch(IOException ex) { Log.e(LOG_KEY, ex.toString()); }
		    	finally {
		    		try {
		    			if(s != null)
		    				s.close();
		    		} catch(IOException ex) { Log.e(LOG_KEY, ex.toString()); }
		    	}
		    	return item;
		    }
		    
		    /** The system calls this to perform work in the UI thread and delivers
		      * the result from doInBackground() */
		    protected void onPostExecute(OnlineGalleryItem result) {
		    	View view = result.getView();
		    	ImageView imageView = (ImageView)view.findViewById(R.id.grid_item_image);
		    	ProgressBar progressBar = (ProgressBar)view.findViewById(R.id.grid_item_progress);
		    	RatingBar ratingBar = (RatingBar)view.findViewById(R.id.grid_item_rating);
		    	TextView textView = (TextView)view.findViewById(R.id.grid_item_text);
		    	textView.setText("DL: " + result.getDownloadCount());
		    	ratingBar.setRating((float)result.getRating());
		    	imageView.setImageBitmap(result.getBitmap());
		    	progressBar.setVisibility(View.GONE);
		    	textView.setVisibility(View.VISIBLE);
		    	imageView.setVisibility(View.VISIBLE);
		    	ratingBar.setVisibility(View.VISIBLE);
		    }
		}
	
	}
}
