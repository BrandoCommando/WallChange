package com.brandroid.dynapaper.Activities;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
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
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class OnlineGalleryPicker extends WallChangerActivity implements OnItemClickListener
{
	private ProgressBar mProgressBar = null;
	private GridView mGridView = null;
	private GalleryDbAdapter mDb = null;
	private int iDownloads = 0;
	
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
		{
			if(mDb == null)
				mDb = new GalleryDbAdapter(this).open();
			mGalleryCursor = mDb.fetchAllItems();
			startManagingCursor(mGalleryCursor);
		}
		mGridView.setAdapter(new OnlineGalleryAdapter(this, mGalleryCursor));
		mGridView.setOnItemClickListener(this);
		
		//SimpleCursorAdapter items = new SimpleCursorAdapter(this, R.layout.grid_item, mGalleryCursor, from, to);
		
		//mGridView.setAdapter(items);
		
		//new DownloadStringTask().execute(ONLINE_GALLERY_URL);
	}
	
	@Override
	public void onItemClick(AdapterView<?> adapter, View view, int position, long id)
	{
		if(view.getTag() == null)
			setResult(RESULT_CANCELED);
		else {
			OnlineGalleryItem item = (OnlineGalleryItem)view.getTag();
			Intent intentResult = new Intent();
			intentResult.putExtra("url", item.getURL());
			intentResult.putExtra("id", item.getID());
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			item.getBitmap().compress(CompressFormat.PNG, 100, stream);
			intentResult.putExtra("bmp", stream.toByteArray());
			//intentResult.putExtra("item", item);
			intentResult.putExtra("pos", position);
			intentResult.setData(Uri.parse(item.getURL()));
			setResult(RESULT_OK, intentResult);
		}
		finish();
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
		mDb.close();
	}
	
	public class OnlineGalleryAdapter extends BaseAdapter {
	    private Context mContext;
	    private Cursor mCursor;
	    
	    public OnlineGalleryAdapter(Context c, Cursor cursor) {
	        mContext = c;
	        mCursor = cursor;
	        iDownloads = getCount();
	    }

	    public int getCount() {
	    	return mCursor.getCount();
	        //return mGalleryItems.length;
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
			if(!mCursor.moveToPosition(position)) return null;
	        View view = convertView;
	        OnlineGalleryItem item;
	        //OnlineGalleryItem item = mGalleryItems[position]; 
	        if (convertView == null) {  // if it's not recycled, initialize some attributes
	        	item = new OnlineGalleryItem(mCursor);
	            LayoutInflater li = getLayoutInflater();
	            view = li.inflate(R.layout.grid_item, null);
	            view.findViewById(R.id.grid_item_image).setVisibility(View.GONE);
	            view.findViewById(R.id.grid_item_rating).setVisibility(View.GONE);
	            view.findViewById(R.id.grid_item_text).setVisibility(View.GONE);
	            //imageView.setTag();
	            view.setTag(item);
	        } else
	        	item = (OnlineGalleryItem)view.getTag();
	        
	        try {
		        if(item.getBitmap() != null)
		        	new DownloadImageTask(view).onPostExecute(item);
		        	//((ImageView)view.findViewById(R.id.grid_item_image)).setImageBitmap(item.getBitmap());
		        else if(!item.isStarted())
		        {
		        	item.setIsDownloading(true);
		        	new DownloadImageTask(view).execute(item);
		        } else
		        	Log.w(LOG_KEY, "Unknown sitch");
	        } catch(NullPointerException npe) {
	        	LogError(npe.toString());
	        	//new DownloadImageTask().execute(item);
	        }
	        
	        return view;
	    }
		
		private class DownloadImageTask extends AsyncTask<OnlineGalleryItem, Void, OnlineGalleryItem> {
			private View mView;
			
			public DownloadImageTask(View v)
			{
				mView = v;
			}
			
	        /** The system calls this to perform work in a worker thread and
		      * delivers it the parameters given to AsyncTask.execute() */
		    protected OnlineGalleryItem doInBackground(OnlineGalleryItem... galleryItems)
		    {
		    	OnlineGalleryItem item = galleryItems[0];
		    	InputStream s = null;
		    	try {
		    		String url = item.getURL();
		    		if(url.startsWith("images/"))
		    			url = url.substring(7);
		    		url = MY_ROOT_URL + "/images/thumb.php?url=" + url;
		    		Log.i(Preferences.LOG_KEY, url);
		    		URLConnection uc = new URL(url).openConnection();
		    		uc.connect();
		    		s = uc.getInputStream();
		    		if(uc.getURL().toString() != url)
		    			Log.i(LOG_KEY, "...redirected to " + uc.getURL());
		    		Bitmap b = BitmapFactory.decodeStream(s);
		    		item.setIsDownloading(false);
		    		if(b != null)
		    		{
		    			ByteArrayOutputStream stream = new ByteArrayOutputStream();
		    			b.compress(CompressFormat.JPEG, 90, stream);
		    			byte[] data = stream.toByteArray();
		    			mDb.updateData(item.getID(), data);
		    			//mDb.updateItem(Id, title, url, data, width, height, tags, rating, downloads, visible)
			    		item.setBitmap(b);
			    		item.setIsDownloaded();
		    		} //else mDb.hideItem(item.getID());
		    	} catch(IOException ex) { Log.e(LOG_KEY, ex.toString()); }
		    	finally {
		    		try {
		    			if(s != null)
		    				s.close();
		    		} catch(IOException ex) { Log.e(LOG_KEY, ex.toString()); }
		    	}
		    	return item;
		    }
		    
		    @Override
		    protected void onPreExecute() {
		    	// TODO Auto-generated method stub
		    	super.onPreExecute();
		    	if(mView == null) return;
		    }
		    
		    /** The system calls this to perform work in the UI thread and delivers
		      * the result from doInBackground() */
		    protected void onPostExecute(OnlineGalleryItem result) {
		    	//if(--iDownloads==0);
		    	//	mDb.close();
		    	if(mView == null) return;
		    	if(result.getBitmap() == null) { mView.setVisibility(View.GONE); return; }
		    	ImageView imageView = (ImageView)mView.findViewById(R.id.grid_item_image);
		    	ProgressBar progressBar = (ProgressBar)mView.findViewById(R.id.grid_item_progress);
		    	RatingBar ratingBar = (RatingBar)mView.findViewById(R.id.grid_item_rating);
		    	TextView textView = (TextView)mView.findViewById(R.id.grid_item_text);
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
