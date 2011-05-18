package com.brandroid.dynapaper.Activities;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;

import com.brandroid.GalleryItem;
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
import android.view.ViewGroup;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;

public class GalleryPicker extends WallChangerActivity implements OnItemClickListener
{
	private GridView mGridView = null;
	private GalleryDbAdapter mDb = null;
	private ArrayList<OnlineGalleryAdapter.DownloadImageTask> mArrayDownloads;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent intent = getIntent();
		if(intent == null)
			intent = new Intent();
		//String action = intent.getAction();
		setContentView(R.layout.online_picker);
		
		prefs = Preferences.getPreferences(GalleryPicker.this);
		
		mGridView = (GridView)findViewById(R.id.gridView1);
		
		setTitle(getResourceString(R.string.btn_online));
		
		setupCursor();
		
		addAds();
		
		//SimpleCursorAdapter items = new SimpleCursorAdapter(this, R.layout.grid_item, mGalleryCursor, from, to);
		
		//mGridView.setAdapter(items);
		
		//new DownloadStringTask().execute(ONLINE_GALLERY_URL);
	}
	
	private void setupCursor()
	{
		if(mGalleryCursor == null)
		{
			if(mDb == null)
				mDb = new GalleryDbAdapter(this).open();
			mGalleryCursor = mDb.fetchAllItems();
			mArrayDownloads = new ArrayList<OnlineGalleryAdapter.DownloadImageTask>(mGalleryCursor.getCount());
			startManagingCursor(mGalleryCursor);
		}
		mGridView.setAdapter(new OnlineGalleryAdapter(this, mGalleryCursor));
		mGridView.setOnItemClickListener(this);
	}
	
	@Override
	public void onItemClick(AdapterView<?> adapter, View view, int position, long id)
	{
		if(view.getTag() == null)
			setResult(RESULT_CANCELED);
		else {
			GalleryItem item = (GalleryItem)view.getTag();
			if(item == null)
			{
				setResult(RESULT_CANCELED);
				finish();
			}
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
		super.onStart();
		setupCursor();
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		mDb.close();
		clearDownloads();
	}
	
	private void clearDownloads()
	{
		if(mArrayDownloads == null) return;
		for(int i = 0; i < mArrayDownloads.size(); i++)
		{
			try {
				OnlineGalleryAdapter.DownloadImageTask task = mArrayDownloads.get(i);
				task.cancel(true);
			} catch(Exception e) { Log.e(LOG_KEY, "Error clearing downloads. " + e.toString()); }
		}
		mArrayDownloads.clear();
	}
	
	public class OnlineGalleryAdapter extends BaseAdapter {
	    @SuppressWarnings("unused")
		private Context mContext;
	    private Cursor mCursor;
	    
	    public OnlineGalleryAdapter(Context c, Cursor cursor) {
	        mContext = c;
	        mCursor = cursor;
	    }

	    public int getCount() {
	    	return mCursor.getCount();
	        //return mGalleryItems.length;
	    }

	    public Object getItem(int position) {
	        return position;
	    }

	    public long getItemId(int position) {
	        return position;
	    }

	    // create a new ImageView for each item referencedimage by the Adapter
		@Override
	    public View getView(int position, View convertView, ViewGroup parent) {
			View view = convertView;
	        GalleryItem item;
	        //OnlineGalleryItem item = mGalleryItems[position]; 
	        if(!mCursor.moveToPosition(position)) return null;
	        if (convertView == null) {  // if it's not recycled, initialize some attributes
	        	item = new GalleryItem(mCursor);
	        	LayoutInflater li = getLayoutInflater();
	            view = li.inflate(R.layout.grid_item, null);
	            view.findViewById(R.id.grid_item_image).setVisibility(View.GONE);
	            //view.findViewById(R.id.grid_item_rating).setVisibility(View.GONE);
	            //view.findViewById(R.id.grid_item_text).setVisibility(View.GONE);
	            //((TextView)view.findViewById(R.id.grid_item_text)).setText(item.getDownloadCount());
	            //imageView.setTag();
	            view.setTag(item);
	        } else
	        	item = (GalleryItem)view.getTag();
	        
	        //Log.i(LOG_KEY, "Rendering " + item.getID() + ":" + position + "(" + item.getDownloadCount() + ") - " + item.getURL());
	        
	        try {
		        if(item.getBitmap() != null)
		        	new DownloadImageTask(view).onPostExecute(item);
		        	//((ImageView)view.findViewById(R.id.grid_item_image)).setImageBitmap(item.getBitmap());
		        else if(!item.isStarted())
		        {
		        	item.setIsDownloading(true);
		        	DownloadImageTask task = new DownloadImageTask(view);
		        	mArrayDownloads.add(task);
		        	task.execute(item);
		        } //else Log.w(LOG_KEY, "Unknown sitch");
	        } catch(NullPointerException npe) {
	        	LogError(npe.toString());
	        	//new DownloadImageTask().execute(item);
	        }
	        
	        return view;
	    }
		
		private class DownloadImageTask extends AsyncTask<GalleryItem, Void, GalleryItem> {
			private View mView;
			
			public DownloadImageTask(View v)
			{
				mView = v;
			}
			
	        /** The system calls this to perform work in a worker thread and
		      * delivers it the parameters given to AsyncTask.execute() */
		    protected GalleryItem doInBackground(GalleryItem... galleryItems)
		    {
		    	GalleryItem item = galleryItems[0];
		    	InputStream s = null;
		    	try {
		    		String url = item.getURL();
		    		if(url.startsWith("images/"))
		    			url = url.substring(7);
		    		url = getImageThumbUrl(url);
		    		Log.i(Preferences.LOG_KEY, url);
		    		HttpURLConnection uc = (HttpURLConnection)new URL(url).openConnection();
		    		uc.setReadTimeout(5000);
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
		    protected void onPostExecute(GalleryItem result) {
		    	//if(--iDownloads==0);
		    	//	mDb.close();
		    	mArrayDownloads.remove(this);
		    	if(mView == null) return;
		    	if(result.getBitmap() == null) { mView.setVisibility(View.GONE); return; }
		    	ImageView imageView = (ImageView)mView.findViewById(R.id.grid_item_image);
		    	ProgressBar progressBar = (ProgressBar)mView.findViewById(R.id.grid_item_progress);
		    	/*
		    	RatingBar ratingBar = (RatingBar)mView.findViewById(R.id.grid_item_rating);
		    	TextView textView = (TextView)mView.findViewById(R.id.grid_item_text);
		    	textView.setText("DL: " + result.getDownloadCount());
		    	ratingBar.setRating((float)result.getRating());
		    	textView.setVisibility(View.VISIBLE);
		    	ratingBar.setVisibility(View.VISIBLE);
		    	*/
		    	imageView.setImageBitmap(result.getBitmap());
		    	progressBar.setVisibility(View.GONE);
		    	imageView.setVisibility(View.VISIBLE);
		    }
		}
	
	}
}
