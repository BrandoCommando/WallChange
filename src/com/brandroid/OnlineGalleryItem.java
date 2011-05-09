package com.brandroid;

import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Bitmap;
import android.view.View;

public class OnlineGalleryItem
{
	private int ID;
	private String Url;
	private String Title;
	private double Rating = Double.NaN;
	private int DownloadCount = 0;
	private View mView;
	private Boolean mDownloaded = false;
	private Boolean mDownloading = false;
	private Bitmap mBitmap;
	
	public OnlineGalleryItem(JSONObject obj)
	{
		try {
			if(obj.has("id"))
				ID = obj.getInt("id");
			if(obj.has("url"))
				Url = obj.getString("url");
			if(obj.has("title"))
				Title = obj.getString("title");
			else Title = Url;
			if(obj.has("rating"))
				Rating = obj.getDouble("rating");
			if(obj.has("dl"))
				DownloadCount = obj.getInt("dl");
		} catch(JSONException je) { }
	}

	public int getID() { return ID; }
	public String getURL() { return Url; }
	public String getTitle() { return Title; }
	public void setURL(String url) { Url = url; }
	public double getRating() { return Rating; }
	public Boolean isDownloaded() { return mDownloaded; }
	public Boolean isStarted() { return mDownloaded || mDownloading; }
	public void setIsDownloaded() { mDownloaded = true; setIsDownloading(false); }
	public void setIsDownloading(Boolean value) { mDownloading = value; }
	public int getDownloadCount() { return DownloadCount; }
	public void setView(View view) { mView = view; }
	public View getView() { return mView; }
	public void setBitmap(Bitmap bmp) { mBitmap = bmp; }
	public Bitmap getBitmap() { return mBitmap; }
	
	
}
