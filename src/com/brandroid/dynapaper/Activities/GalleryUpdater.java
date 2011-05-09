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

import com.brandroid.dynapaper.Preferences;
import com.brandroid.dynapaper.R;

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
		
		String ret = null;
    	String line = null;
    	InputStream in = null;
    	BufferedReader br = null;
    	StringBuilder sb = null;
    	HttpURLConnection uc = null;
    	Long modified = null;
    	String url = Preferences.MY_ROOT_URL + "";
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
}
