package com.brandroid.dynapaper.widget;

import android.graphics.Bitmap;

public class Weather extends Widget
{
	public static final String URL_WEATHER_GOOGLE = "http://www.google.com/ig/api?weather=%ZIP%";
	public static final String URL_WEATHER_YAHOO = "http://query.yahooapis.com/v1/public/yql?q=select+*+from+weather.forecast+where+location%3D%ZIP%&format=json&callback=";
	// 'mostly cloudy' => 'cloudy4', 'heavy showers' => 'shower3', 'showers' => 'shower2', 'partial' => 'cloudy2', 'some' => 'shower2', 'cloud' => 'cloudy1', 'rain' => 'shower1', 'mist' => 'mist', 'snow' => 'snow2', 'fair' => 'sunny', 'sun' => 'sunny');
	
	public void setParameters(String... params)
	{
		
	}
	public Bitmap getWidget()
	{
		Bitmap ret = null;
		return ret;
	}
}
