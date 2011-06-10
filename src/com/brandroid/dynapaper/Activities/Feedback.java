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
import java.net.URLEncoder;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.brandroid.JSON;
import com.brandroid.Logger;
import com.brandroid.dynapaper.GalleryItem;
import com.brandroid.dynapaper.R;
import com.brandroid.dynapaper.WallChanger;
import com.brandroid.dynapaper.Activities.ProfileMaker.DownloadThumbZipTask;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.RatingBar;

public class Feedback extends BaseActivity
{
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.feedback);
		final EditText txtFeedback = (EditText)findViewById(R.id.txtFeedback);
		final EditText txtName = (EditText)findViewById(R.id.txtFeedbackName);
		final RatingBar rateFeedback = (RatingBar)findViewById(R.id.rateFeedback);
		findViewById(R.id.btnSend).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				new SendFeedbackTask().execute("rating", ((Float)rateFeedback.getRating()).toString(), "feedback", txtFeedback.getText().toString(), "name", txtName.getText().toString());
				showToast(R.string.s_thanks);
				finish();
			}
		});
	}
	
	private class SendFeedbackTask extends AsyncTask<String, Integer, Boolean> {

		@Override
		protected Boolean doInBackground(String... params)
		{
			HttpURLConnection uc = null;
	    	Boolean success = false;
	    	
	    	String url = WallChanger.MY_FEEDBACK_URL.replace("%USER%", WallChanger.getUser());
	    	for(int i=0; i<params.length; i+=2)
	    		url += "&" + params[i] + (i < params.length - 1 ? "=" + URLEncoder.encode(params[i + 1]) : "");
	    	
	    	try {
	    		Logger.LogInfo("Sending Feedback to " + url);
	    		uc = (HttpURLConnection)new URL(url).openConnection();
	    		uc.setReadTimeout(20000);
	    		uc.addRequestProperty("Accept-Encoding", "gzip, deflate");
	    		uc.addRequestProperty("Version", ((Integer)WallChanger.VERSION_CODE).toString());
	    		
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
	    		uc.connect();
	    		if(uc.getResponseCode() == HttpURLConnection.HTTP_OK)
	    		{
	    			Logger.LogInfo("Feedback sent.");
	    			success = true;
	    		}
			}
			catch(Exception ex) { Logger.LogError("Error sending feedback.", ex); }
	    	finally {
				if(uc != null) uc.disconnect();
				uc = null;
	    	}
	    	return success;
		}
	}

}
