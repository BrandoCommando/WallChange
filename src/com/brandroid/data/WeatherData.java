package com.brandroid.data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import com.brandroid.util.Logger;

public class WeatherData
{
	private Information mInformation;
	private Forecast mCurrent;
	private Forecast[] mConditions;
	
	public WeatherData(JSONObject json)
	{
		if(json.has("weather")) // from google
		{
			json = json.optJSONObject("weather");
			if(json.has("forecast_information"))
				mInformation = new Information(json.optJSONObject("forecast_information"));
			if(json.has("current_conditions"))
			{
				mCurrent = new Forecast(json.optJSONObject("current_conditions"));
				mConditions[0].AddValue("temp_f", json.optJSONObject("current_conditions").optString("temp_f"));
			}
			if(json.has("forecast_conditions"))
			{
				JSONArray fca = json.optJSONArray("forecast_conditions");
				mConditions = new Forecast[fca.length()];
				for(int i = 0; i < fca.length(); i++)
					mConditions[i] = new Forecast(fca.optJSONObject(i));
			}
		} else if(json.has("output")) // from widget_weather.php
		{
			json = json.optJSONObject("output");
			if(json.has("current"))
			{
				mInformation = new Information(json.optJSONObject("current"));
				mCurrent = new Forecast(json.optJSONObject("current"));
			}
			if(json.has("forecast"))
			{
				JSONArray fca = json.optJSONArray("forecast");
				mConditions = new Forecast[fca.length()];
				for(int i = 0; i < fca.length(); i++)
					mConditions[i] = new Forecast(fca.optJSONObject(i));
			}
		}
	}
	public WeatherData(XmlPullParser xml) throws XmlPullParserException, IOException
	{
		int type = xml.getEventType();
		ArrayList<Forecast> forecast = new ArrayList<WeatherData.Forecast>();
		Boolean bPast1st = false;
		//int elements = 0;
		while(type != XmlPullParser.END_DOCUMENT)
		{
			if(type == XmlPullParser.START_TAG)
			{
				//elements++;
				//Logger.LogDebug("Element: " + xml.getName());
				if(xml.getName().equalsIgnoreCase("forecast_information"))
					mInformation = new Information(xml);
				if(xml.getName().equalsIgnoreCase("current_conditions"))
					mCurrent = new Forecast(xml);
				if(xml.getName().equalsIgnoreCase("forecast_conditions"))
					forecast.add(new Forecast(xml));
			}
			type = xml.next();
		}
		mConditions = new Forecast[forecast.size()];
		for(int i = 0; i < mConditions.length; i++)
			mConditions[i] = forecast.get(i);
		//Logger.LogDebug("WeatherData parsed " + elements + " elements.");
	}
	
	public Information getCurrentInformation() { return mInformation; }
	public Forecast getCurrentConditions() { return mCurrent; }
	public Forecast[] getForecast() { return mConditions; }
	public Forecast getForecast(int day) { return mConditions[day]; }
	
	public String toString()
	{
		StringBuilder ret = new StringBuilder("{");
		if(mInformation != null)
		{
			ret.append("information:");
			ret.append(mInformation.toString());
			ret.append(",");
		}
		if(mCurrent != null)
		{
			ret.append("current:");
			ret.append(mCurrent.toString());
			ret.append(",");
		}
		if(mConditions != null)
		{
			ret.append("forecast:[");
			for(int i = 0; i < mConditions.length; i++)
				ret.append(mConditions[i].toString()+",");
			if(mConditions.length > 0)
				ret.setLength(ret.length()-1);
			ret.append("],");
		}
		ret.setLength(ret.length()-1);
		ret.append("}");
		return ret.toString();
	}
	
	public class Information extends HashData
	{
		public Information(JSONObject json) { super(json); }
		public Information(XmlPullParser xml) { super(xml); }
		public String getCity() { return getValue("city"); }
		public String getZip() { return getValue("postal_code"); }
		public String getDate() { return getValue("forecast_date"); }
	}
	
	public class Forecast extends HashData
	{	
		public Forecast(JSONObject optJSONObject) {
			super(optJSONObject);
		}
		public Forecast(XmlPullParser xml) { super(xml); }
		public Forecast Merge(XmlPullParser xml) { super.Merge(new Forecast(xml)); return this; }
		public String getCondition() { return getValue("condition"); }
		public String getTempHi() { return getValue("high"); }
		public String getTempLow() { return getValue("low"); }
		public String getDay() { return getValue("day_of_week"); }
	}
}
