package com.brandroid.data;

import java.util.Hashtable;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONObject;

public class WeatherData
{
	private Information mInformation;
	private Forecast[] mConditions;
	
	public WeatherData(JSONObject json)
	{
		if(json.has("weather")) // from google
		{
			json = json.optJSONObject("weather");
			if(json.has("forecast_information"))
				mInformation = new Information(json.optJSONObject("forecast_information"));
			if(json.has("forecast_conditions"))
			{
				JSONArray fca = json.optJSONArray("forecast_conditions");
				mConditions = new Forecast[fca.length()];
				for(int i = 0; i < fca.length(); i++)
					mConditions[i] = new Forecast(fca.optJSONObject(i));
			}
			if(json.has("current_conditions"))
				mConditions[0].AddValue("temp_f", json.optJSONObject("current_conditions").optString("temp_f"));
		} else if(json.has("output")) // from widget_weather.php
		{
			json = json.optJSONObject("output");
			if(json.has("current"))
				mInformation = new Information(json.optJSONObject("current"));
			if(json.has("forecast"))
			{
				JSONArray fca = json.optJSONArray("forecast");
				mConditions = new Forecast[fca.length()];
				for(int i = 0; i < fca.length(); i++)
					mConditions[i] = new Forecast(fca.optJSONObject(i));
			}
		}
	}
	
	public Information getCurrentInformation() { return mInformation; }
	public Forecast[] getForecast() { return mConditions; }
	public Forecast getForecast(int day) { return mConditions[day]; }
	
	public class Information extends HashData
	{
		public Information(JSONObject json) { super(json); }
		public String getZip() { return getValue("postal_code"); }
		public String getDate() { return getValue("forecast_date"); }
	}
	
	public class Forecast extends HashData
	{	
		public Forecast(JSONObject optJSONObject) {
			super(optJSONObject);
		}
		public String getCondition() { return getValue("condition"); }
		public String getTempHi() { return getValue("high"); }
		public String getTempLow() { return getValue("low"); }
		public String getDay() { return getValue("day_of_week"); }
	}
}
