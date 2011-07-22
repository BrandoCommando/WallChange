package com.brandroid.dynapaper.Activities;

import java.util.Timer;
import java.util.TimerTask;

import com.brandroid.dynapaper.R;
import com.brandroid.dynapaper.WallChanger;
import com.brandroid.util.Logger;

import android.content.Intent;
import android.graphics.Color;
import android.location.Criteria;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.EditText;

public class SelectLocation extends BaseActivity
{
	private AutoCompleteTextView mTxtZip;
	private CheckBox mBtnGPS;
	private LocationListener locationListener;
	private LocationManager locationManager;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTitle(getString(R.string.s_zipcode));
		Intent intent = getIntent();
		if(intent == null)
			intent = new Intent();
		
		setContentView(R.layout.location);
		
		mTxtZip = (AutoCompleteTextView)findViewById(R.id.txtZip);
		mBtnGPS = (CheckBox)findViewById(R.id.btnGPS);
		
		findViewById(R.id.btnSelectLocation).setOnClickListener(this);
		mBtnGPS.setOnClickListener(this);
		
		if(intent.hasExtra("location"))
			mTxtZip.setText(intent.getStringExtra("location"));
		
		//mPastZips = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line);
		mTxtZip.setTag(false);
		mTxtZip.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				((EditText)v).setText("");
				if(v.getTag() != null && v.getTag().getClass().equals(Boolean.class) && ((Boolean)v.getTag()).equals(false))
				{
					v.setTag(true);
					InputMethodManager mgr = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
					mgr.hideSoftInputFromWindow(v.getWindowToken(), 0);
					((AutoCompleteTextView)v).showDropDown();
				}
			}
		});
		mTxtZip.setAdapter(mPastZips);
		
	}
	public void onClick(View v)
	{
		switch(v.getId())
		{
		case R.id.btnSelectLocation:
			Intent ret = new Intent();
			ret.putExtra("location", mTxtZip.getText().toString());
			setResult(RESULT_OK, ret);
			finish();
			break;
		case R.id.btnGPS:
			onClickGPS();
			break;
		}
	}
	public void onClickGPS()
	{
		try {
			LocationListener ll = getLocationListener();
			if(mBtnGPS.isChecked() && ll != null)
				locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, ll);
			else if(ll != null)
				locationManager.removeUpdates(ll);
			if(ll == null)
				throw new Exception("Location listener is null");
		} catch(Exception ex) {
			mBtnGPS.setEnabled(false);
			mBtnGPS.setChecked(false);
			showToast(getResourceString(R.string.s_error, R.string.btn_gps));
			Logger.LogError("Error toggling GPS", ex);
		}
		//mTxtZip.setEnabled(mBtnGPS.isChecked());
	}
	
	public LocationListener getLocationListener()
	{
		if(locationListener != null) return locationListener;
		locationManager = (LocationManager)getSystemService(LOCATION_SERVICE);
		final Timer cancelTimer = new Timer(false);
		final TimerTask cancelTask = new TimerTask() {
			public void run() {
				locationManager.removeUpdates(getLocationListener());
			}
		};
		locationListener = new LocationListener()
		{
			public void onStatusChanged(String provider, int status, Bundle extras)
			{
				Logger.LogWarning("LocationListener Status Change: " + status);
			}
			public void onProviderEnabled(String provider) { Logger.LogInfo("Location Provider \"" + provider + "\" enabled."); }
			public void onProviderDisabled(String provider) {
				Logger.LogWarning("Location Provider \"" + provider + "\" disabled.");
			}
			public void onLocationChanged(Location location) {
				if(WallChanger.setLastLocation(location))
				{
					String lat = ((Double)location.getLatitude()).toString();
					if(lat.length() > 6)
						lat = lat.substring(0, Math.max(6, lat.indexOf(".") + 4));
					String lng = ((Double)location.getLongitude()).toString();
					if(lng.length() > 6)
						lng = lng.substring(0, Math.max(6, lng.indexOf(".") + 4));
					mTxtZip.setText(lat+","+lng);
				}
			}
		};
		locationManager.addGpsStatusListener(new GpsStatus.Listener() {
			public void onGpsStatusChanged(int event) {
				switch(event)
				{
					case GpsStatus.GPS_EVENT_STARTED:
						try {
							cancelTimer.schedule(cancelTask, 30000);
						} catch(IllegalStateException ise) { Logger.LogError("Couldn't schedule GPS cancel Timer", ise); }
						mBtnGPS.setTextColor(Color.DKGRAY);
						break;
					case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
						mBtnGPS.setTextColor(Color.CYAN);
						break;
					case GpsStatus.GPS_EVENT_FIRST_FIX:
						mBtnGPS.setTextColor(Color.GREEN);
						break;
					case GpsStatus.GPS_EVENT_STOPPED:
						mBtnGPS.setTextColor(Color.WHITE);
						mBtnGPS.setChecked(false);
						break;
					default: Logger.LogWarning("GpsListener Status Change: " + event);
				}
			}
		});
		Criteria c = new Criteria();
		c.setAccuracy(Criteria.ACCURACY_FINE);
		String provider = locationManager.getBestProvider(c, true);
		if(provider == null) return null;
		Logger.LogInfo("Best provider: " + provider);
		LocationProvider lp = locationManager.getProvider(provider);
		Logger.LogInfo(lp.getName() + " accuracy: " + lp.getAccuracy()); 
		Location loc = locationManager.getLastKnownLocation(provider);
		if(loc == null)
			if(provider != LocationManager.NETWORK_PROVIDER)
				loc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
			
		if(loc != null)
			WallChanger.setLastLocation(loc);
		
		return locationListener;
	}
}
