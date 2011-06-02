package com.brandroid.dynapaper.Activities;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import com.brandroid.Logger;
import com.brandroid.MediaUtils;
import com.brandroid.dynapaper.GalleryItem;
import com.brandroid.dynapaper.Prefs;
import com.brandroid.dynapaper.R;
import com.brandroid.dynapaper.WallChanger;
import com.brandroid.dynapaper.Database.GalleryDbAdapter;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;

public class GalleryPicker extends BaseActivity implements OnItemClickListener, OnItemSelectedListener
{
	private GridView mGridView = null;
	private Spinner mSorting = null;
	private GalleryDbAdapter mDb = null;
	private Hashtable<String, DownloadImageTask> mDownloads;
	//private GalleryItem[] mGalleryItems;
	private Cursor mGalleryCursor;
	private int mGalleryCount;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Intent intent = getIntent();
		if(intent == null)
			intent = new Intent();
		//String action = intent.getAction();
		setContentView(R.layout.gallery_picker);
		super.onCreate(savedInstanceState);
		
		prefs = Prefs.getPreferences(GalleryPicker.this);
		
		mGridView = (GridView)findViewById(R.id.picker_grid);
		
		mSorting = (Spinner)findViewById(R.id.picker_sort);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
	            this, R.array.picker_sort_options, android.R.layout.simple_spinner_item);
	    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	    mSorting.setAdapter(adapter);
	    mSorting.setOnItemSelectedListener(this);
		
		setTitle(getResourceString(R.string.btn_online));
		
		setupCursor();
		
		addAds();
		
		//new DownloadStringTask().execute(ONLINE_GALLERY_URL);
	}
	
	private void setupCursor()
	{
		if(mGalleryCursor == null)
		{
			if(mDb == null)
				mDb = new GalleryDbAdapter(this);
			mDb.open();
			mGalleryCursor = mDb.fetchAllItems();
			Logger.LogInfo("Gallery cursor has " + mGalleryCursor.getCount() + " items");
			startManagingCursor(mGalleryCursor);
			mGalleryCursor.moveToFirst();
			mGalleryCount = mGalleryCursor.getCount();
			mDownloads = new Hashtable<String, DownloadImageTask>();
			for(int i = 0; i < mGalleryCount; i++)
			{
				GalleryItem item = new GalleryItem(mGalleryCursor);
				if(item != null && !item.hasThumbnail())
				{
					DownloadImageTask task = new DownloadImageTask((View)null, item);
					task.execute(item.getURL());
					mDownloads.put(item.getURL(), task);
				}
				mGalleryCursor.moveToNext();
			}
			mGalleryCursor.moveToFirst();
		} else Logger.LogInfo("Cursor already created.");
		//SimpleCursorAdapter items = new SimpleCursorAdapter(this, R.layout.gallery_grid_item, mGalleryCursor, from, to);
		//mGridView.setAdapter(items);
		mGridView.setAdapter(new OnlineGalleryAdapter(this));
		mGridView.setOnItemClickListener(this);
	}
	
	@Override
	public void onItemClick(AdapterView<?> adapter, View view, int position, long id)
	{
		Logger.LogInfo("Selected item #" + position);
		Intent intentResult = new Intent();
		if(position < 0 || position >= mGalleryCount)
		{
			showToast("Invalid selection.");
			setResult(RESULT_CANCELED);
			finish();
		}
		mGalleryCursor.moveToPosition(position);
		GalleryItem item = new GalleryItem(mGalleryCursor);
		intentResult.putExtra("url", item.getURL());
		intentResult.putExtra("id", item.getID());
		intentResult.putExtra("pos", position);
		//intentResult.setData(Uri.parse(item.getURL()));
		setResult(RESULT_OK, intentResult);
		finish();
	}

	public void onItemSelected(AdapterView<?> parent, View view, int pos, long id)
	{
		//String sSort = parent.getItemAtPosition(pos).toString();
		//Toast.makeText(getApplicationContext(), sSort, Toast.LENGTH_SHORT).show();
	}

	public void onNothingSelected(AdapterView<?> parent) {
		// Do nothing.
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
		if(mDownloads == null) return;
		for(int i = 0; i < mDownloads.size(); i++)
		{
			try {
				mDownloads.elements().nextElement().cancel(true);
			} catch(Exception e) { Logger.LogWarning("Error clearing downloads.", e); }
		}
		mDownloads.clear();
	}
	
	public class OnlineGalleryAdapter extends BaseAdapter
	{
		private Context mContext;
		
	    public OnlineGalleryAdapter(Context context) {
			super();
			mContext = context;
		}
	    
	    public int getCount() {
	    	return mGalleryCount;
	    	//return getCursor().getCount();
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
			if(position < 0 || position >= mGalleryCount) return view;
	        //GalleryItem item;
	        //OnlineGalleryItem item = mGalleryItems[position]; 
	        if (view == null) {  // if it's not recycled, initialize some attributes
	        	//item = new GalleryItem(getCursor());
	        	LayoutInflater li = LayoutInflater.from(mContext);
	            view = li.inflate(R.layout.gallery_grid_item, null);
	            //view.findViewById(R.id.grid_item_image).setVisibility(View.GONE);
	            //view.findViewById(R.id.grid_item_rating).setVisibility(View.GONE);
	            //view.findViewById(R.id.grid_item_text).setVisibility(View.GONE);
	            //((TextView)view.findViewById(R.id.grid_item_text)).setText(item.getDownloadCount());
	            //imageView.setTag();
	            //view.setTag(item);
	        }
	            try {
		        	//Cursor c = getCursor();
		        	//c.moveToPosition(position);
		        	mGalleryCursor.moveToPosition(position);
		        	GalleryItem item = new GalleryItem(mGalleryCursor);
		        	int id = item.getID();
		        	//Bitmap data = item.getThumbnail(); //c.getBlob(c.getColumnIndex(GalleryDbAdapter.KEY_DATA));
		        	String url = item.getURL(); //c.getString(c.getColumnIndex(GalleryDbAdapter.KEY_URL));
		        	ImageView iv = ((ImageView)view.findViewById(R.id.grid_item_image));
		        	RatingBar ratingBar = (RatingBar)view.findViewById(R.id.grid_item_rating);
			    	TextView textView = (TextView)view.findViewById(R.id.grid_item_text);
			    	ProgressBar progressBar = (ProgressBar)view.findViewById(R.id.grid_item_progress);
			    	if(WallChanger.OPTION_SHOW_GALLERY_INFO)
			    	{
			    		textView.setVisibility(View.VISIBLE);
			    		ratingBar.setVisibility(View.VISIBLE);
			    		if(item.getDownloadCount() > 0)
			    			textView.setText("DL: " + item.getDownloadCount());
			    		else textView.setVisibility(View.GONE);
			    		if(item.getRating() > 0)
			    			ratingBar.setRating(item.getRating());
			    		else
			    			ratingBar.setVisibility(View.GONE);
			    	} else {
			    		textView.setVisibility(View.GONE);
			    		ratingBar.setVisibility(View.GONE);
			    	}
		        	//ImageView iv = ;
			    	if(iv.getTag() == null && !item.hasThumbnail())
			    	{
			    		Logger.LogDebug("Thumnail needs to be downloaded!");
			    		if(mDownloads.containsKey(url))
			    		{
			    			DownloadImageTask task = mDownloads.get(url);
			    			task.mView = view;
			    			mDownloads.put(url, task);
			    		}
			    		iv.setTag(true);
			    		//DownloadImageTask task = new DownloadImageTask(view, item);
			    		//mArrayDownloads.add(task);
			    		//task.execute(url);
			    	}
			    	else if(item.hasThumbnail())
			        {
			    		Logger.LogDebug("Thumnail exists!");
			    		iv.setTag(true);
			        	iv.setImageBitmap(item.getThumbnail());
			    		//iv.setImageURI(Uri.parse(item.getThumbnailFilename(true)));
			        	progressBar.setVisibility(View.GONE);
			        	//((ImageView)view.findViewById(R.id.grid_item_image)).setImageBitmap(item.getBitmap());
			        } else Logger.LogWarning("Unknown sitch");
		        } catch(NullPointerException npe) {
		        	Logger.LogError("Null pointer getting picker view.", npe);
		        	//new DownloadImageTask().execute(item);
		        }
	        //} //else item = (GalleryItem)view.getTag();
	      //  else task = (DownloadImageTask)view.getTag();
	        
	        //if(!mCursor.moveToPosition(position)) return null;
	         //LogInfo("Rendering " + item.getID() + ":" + position + "(" + item.getDownloadCount() + ") - " + item.getURL());
	        
	        return view;
	    }
			
	}
	
	public class DownloadImageTask extends AsyncTask<String, Void, Bitmap>
	{
		private View mView;
		@SuppressWarnings("unused")
		private Boolean isDone = false;
		@SuppressWarnings("unused")
		private Boolean isStarted = false;
		private GalleryItem mItem;
		
		public DownloadImageTask(View v, GalleryItem item)
		{
			mView = v;
			mItem = item;
		}
		
        /** The system calls this to perform work in a worker thread and
	      * delivers it the parameters given to AsyncTask.execute() */
	    protected Bitmap doInBackground(String... urls)
	    {
	    	isStarted = true;
	    	//GalleryItem item = galleryItems[0];
	    	BufferedInputStream s = null;
	    	Bitmap ret = null;
	    	String url = urls[0]; //item.getURL();
    		if(url.startsWith("images/"))
			url = url.substring(7);
			int width = 0, height = 0;
    		if(mView != null)
    		{
    			width = mView.getWidth();
    			height = mView.getHeight();
    		}
    		String[] urls2try = new String[2];
    		urls2try[0] = WallChanger.getImageThumbUrl(url, width, height, false);
    		urls2try[1] = WallChanger.getImageThumbUrl(url, width, height, true); // try google storage if hostable is down
    		for(int iTry = 0; iTry < urls2try.length; iTry++)
    		{
    			url = urls2try[iTry];
    			//url = WallChanger.getImageThumbUrl(url, width, height);
		    	try {
		    		//Log.i(Preferences.LOG_KEY, url);
		    		HttpURLConnection uc = (HttpURLConnection)new URL(url).openConnection();
		    		uc.setConnectTimeout(5000);
		    		uc.setReadTimeout(5000);
		    		uc.connect();
		    		if(uc.getResponseCode() < 400) {
			    		if(uc.getURL().toString() != url)
			    			Logger.LogInfo("Redirected to " + uc.getURL() + " from " + url);
			    		else Logger.LogInfo("Downloading " + url);
			    		s = new BufferedInputStream(uc.getInputStream());
			    		ret = BitmapFactory.decodeStream(s);
			    		//item.setIsDownloading(false);
			    		if(ret != null)
			    			mItem.setThumbnail(ret);
		    		}
		    	} catch(IOException ex) { Logger.LogError("Unable to download " + url, ex); }
		    	finally {
		    		try {
		    			if(s != null)
		    				s.close();
		    		} catch(IOException ex) { Logger.LogError("Error closing stream for " + url, ex); }
		    	}
    		}
	    	return ret;
	    }
	    
	    @Override
	    protected void onCancelled() {
	    	if(mView == null) return;
	    	mView.setVisibility(View.GONE);
	    }
	    
	    @Override
	    protected void onPreExecute() {
	    	super.onPreExecute();
	    	isStarted = true;
	    	if(mView == null) return;
	    	mView.findViewById(R.id.grid_item_image).setVisibility(View.GONE);
	    }
	    
	    /** The system calls this to perform work in the UI thread and delivers
	      * the result from doInBackground() */
	    protected void onPostExecute(Bitmap bmp) {
	    	//if(--iDownloads==0);
	    	//	mDb.close();
	    	isDone = true;
	    	if(mItem != null && bmp != null)
	    		mItem.setThumbnail(bmp);
	    	mDownloads.remove(mItem.getURL());
	    	if(mView == null) return;
	    	//if(result.getBitmap() == null) { mView.setVisibility(View.GONE); return; }
	    	ImageView imageView = (ImageView)mView.findViewById(R.id.grid_item_image);
	    	ProgressBar progressBar = (ProgressBar)mView.findViewById(R.id.grid_item_progress);
	    	
	    	if(bmp != null)
	    	{
		    	//Bitmap bmp = BitmapFactory.decodeByteArray(result, 0, result.length);
		    	//if(bmp != null)
		    	imageView.setImageBitmap(bmp);
		    	imageView.setVisibility(View.VISIBLE);
	    	} else mView.setVisibility(View.GONE);
	    	progressBar.setVisibility(View.GONE);
	    }
	}


}
