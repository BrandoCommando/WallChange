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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.brandroid.JSON;
import com.brandroid.dynapaper.GalleryDbAdapter;
import com.brandroid.dynapaper.Preferences;
import com.brandroid.dynapaper.R;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class GalleryUpdater extends WallChangerActivity {
	/**
	 * @see android.app.Activity#onCreate(Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.status_message);
		
		Intent mIntent = getIntent();
		if(mIntent == null) mIntent = new Intent();
		
		String user = prefs.getSetting("user", "");
		GalleryDbAdapter gdb = new GalleryDbAdapter(this);
		gdb.open();
		
		String ret = null;
    	String line = null;
    	InputStream in = null;
    	BufferedReader br = null;
    	StringBuilder sb = null;
    	HttpURLConnection uc = null;
    	Long modified = null;
    	String url = Preferences.MY_ROOT_URL + "/dynapaper/gallery.php";
    	if(user != "")
    		url += "?user=" + user;
    	try {
    		uc = (HttpURLConnection)new URL(url).openConnection();
    		if(prefs.hasSetting("gallery_update"))
    		{
	    		modified = prefs.getLong("gallery_update", Long.MIN_VALUE);
	    		if(modified > Long.MIN_VALUE)
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
	    		JSONObject jsonGallery = JSON.Parse(ret);
    			try {
		    		if(jsonGallery.has("user") && jsonGallery.get("user") != user)
		    		{
						user = jsonGallery.getString("user");
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
						JSONObject pic = jsonImages.getJSONObject(imgIndex);
						int id = Integer.parseInt(pic.getString("id"));
						String purl = pic.getString("url");
						String title = purl;
						if(pic.has("title"))
							title = pic.getString("title");
						String tags = "";
						Float rating = null;
						int downloads = 0;
						int width = 0;
						int height = 0;
						try {
							String[] dims = pic.getString("dim").split("x");
							width = Integer.parseInt(dims[0]);
							height = Integer.parseInt(dims[1]);
						} catch(Exception nfe) { }
						if(pic.has("tags")) tags = pic.getString("tags");
						try {
							if(pic.has("rating")) rating = Float.parseFloat(pic.getString("rating"));
						} catch(NumberFormatException nfe) { }
						adds += gdb.createItem(id, title, purl, (byte[])null, width, height, tags, rating, downloads, true);
					}
				} catch (JSONException je) {
					Log.e(LOG_KEY, "JSONException getting images: " + je.toString());
				}
				
				Log.i(LOG_KEY, "Successfully added " + adds + " records!");
		    	setResult(RESULT_OK, mIntent);
				
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
    	}
    	mGalleryCursor = gdb.fetchAllItems();
    	gdb.close();
    	finish();
	}
}
