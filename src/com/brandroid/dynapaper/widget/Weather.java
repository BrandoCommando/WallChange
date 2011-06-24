package com.brandroid.dynapaper.widget;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.zip.GZIPInputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

import org.json.JSONArray;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import com.brandroid.util.Logger;
import com.brandroid.data.WeatherData;
import com.brandroid.data.WeatherData.Forecast;
import com.brandroid.dynapaper.R;
import com.brandroid.dynapaper.WallChanger;
import com.brandroid.util.JSON;
import com.brandroid.util.NetUtils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils.StringSplitter;

public class Weather extends Widget
{
	private String sLocation;
	private Context mContext;
	private JSONObject mJSON = null;
	private WeatherData mData = null;
	
	// 'mostly cloudy' => 'cloudy4', 'heavy showers' => 'shower3', 'showers' => 'shower2', 'partial' => 'cloudy2', 'some' => 'shower2', 'cloud' => 'cloudy1', 'rain' => 'shower1', 'mist' => 'mist', 'snow' => 'snow2', 'fair' => 'sunny', 'sun' => 'sunny');
	public static final String[] MAP_STRINGS = new String[] {"mostly cloudy", "heavy showers", "showers", "partial", "some", "cloud", "rain", "mist", "snow", "fair", "sun"};
	public static final int[] MAP_DRAWABLE = new int[] {R.drawable.ww_cloudy4, R.drawable.ww_shower3, R.drawable.ww_shower2, R.drawable.ww_cloudy2, R.drawable.ww_shower2, R.drawable.ww_cloudy1, R.drawable.ww_shower1, R.drawable.ww_mist, R.drawable.ww_snow2, R.drawable.ww_sunny, R.drawable.ww_sunny};
	
	public Weather(Context context, String location)
	{
		sLocation = location;
		mContext = context;
		//mConditionMap
	}
	
	public WeatherData getAPIResults()
	{
		if(mData == null)
		{
			String url = WallChanger.MY_WEATHER_API_URL;
			if(url.contains("%ZIP%"))
				url = url.replace("%ZIP%", sLocation);
			else
				url += (url.indexOf("?") > -1 ? "&" : "?") + "zip=" + sLocation;
			/*
			mJSON = NetUtils.downloadJSON(url);
			if(mJSON != null)
				mData = new WeatherData(mJSON);
			else*/
			{
				url = WallChanger.GOOGLE_WEATHER_API_URL.replace("%ZIP%", sLocation);
				Logger.LogDebug("Getting weather from " + url);
				HttpURLConnection uc = null;
				InputStream in = null;
				XmlPullParser parser;
				try {
					uc = (HttpURLConnection)new URL(url).openConnection();
					uc.setReadTimeout(10000);
					uc.addRequestProperty("Accept-Encoding", "gzip, deflate");
					uc.connect();
					if(uc.getResponseCode() == HttpURLConnection.HTTP_OK)
					{
						Logger.LogDebug("Received " + uc.getContentLength() + " bytes.");
						String encoding = uc.getContentEncoding();
						if(encoding != null && encoding.equalsIgnoreCase("gzip"))
							in = new GZIPInputStream(uc.getInputStream());
						else if(encoding != null && encoding.equalsIgnoreCase("deflate"))
							in = new InflaterInputStream(uc.getInputStream(), new Inflater(true));
						else
							in = new BufferedInputStream(uc.getInputStream());
						parser = XmlPullParserFactory.newInstance().newPullParser();
						parser.setInput(in, "UTF-8");
						mData = new WeatherData(parser);
						Logger.LogDebug("Weather Data from Google: " + mData.toString());
					} else throw new FileNotFoundException(url + " returned " + uc.getResponseCode());
				} catch (XmlPullParserException e) {
					Logger.LogError("Error parsing Google", e);
				} catch(IOException e) {
					Logger.LogError("Error reading Google", e);
				} catch(Exception e) {
					Logger.LogError("Exception reading Google", e);
				} finally {
					try {
						if(in != null)
							in.close();
					} catch (IOException e) {
						Logger.LogError("Error closing stream for Google", e);
					}
					if(uc != null)
						uc.disconnect();
				}
				//NetUtils.
			}
		}
		return mData;
	}
	public String[] getConditions()
	{
		WeatherData api = getAPIResults();
		if(api == null) return new String[0];
		Forecast[] fc = api.getForecast();
		if(fc == null) return new String[0];
		String[] ret = new String[fc.length];
		for(int i = 0; i < ret.length; i++)
			ret[i] = api.getForecast(i).getCondition();
		return ret;
	}
	public String getCurrentCondition()
	{
		return mData.getCurrentInformation().getValue("condition") +
			" - " + mData.getCurrentInformation().getValue("temp_f") + "°";
		
	}

	@Override
	public Drawable getWidget()
	{
		String cond = getCurrentCondition();
		Logger.LogDebug("Current Condition: " + cond);
		return getDrawableFromCondition(cond.toLowerCase());
	}
	@Override
	public void applyTo(Bitmap bmp, Canvas c)
	{
		//Bitmap widget = ((BitmapDrawable)getWidget()).getBitmap();
		Point center = new Point(bmp.getWidth() / 2, bmp.getHeight() / 2);
		WeatherData api = getAPIResults();
		String[] conditions = getConditions();
		Logger.LogDebug("Conditions: " + join(conditions));
		for(int i = conditions.length - 1; i >= 0; i--)
		{
			Paint p = new Paint();
			p.setStyle(Style.FILL);
			p.setColor(Color.BLACK);
			p.setTextSize(30);
			p.setTypeface(Typeface.DEFAULT_BOLD);
			int alpha = 255 - (i * 50);
			Logger.LogDebug("Alpha: " + alpha);
			Drawable widget = getDrawableFromCondition(i > 0 ? conditions[i] : getCurrentCondition());
			int w = widget.getMinimumHeight(),
				h = widget.getMinimumHeight(),
				wh = w / 2,
				hh = h / 2;
			int x = (int)Math.floor(center.x - (float)wh);
			int y = (int)Math.floor(center.y - (float)hh);
			float fs = p.getTextSize();
			x += i * 100;
			WeatherData.Forecast f = api.getForecast(i);
			String low = f.getTempLow(), hi = f.getTempHi();
			p.setColor(Color.TRANSPARENT);
			p.setAlpha(alpha);
			//c.save();
			c.drawBitmap(((BitmapDrawable)widget).getBitmap(), x, y, p);
			if(low != null)
			{
				//p.setShadowLayer(0, 0, 0, 0);
				//p.setColor(Color.GRAY);
				//p.setAlpha(alpha - 50);
				//c.drawText(hi, x + wh - 12, y + h - p.getTextSize() - 2, p);
				p.setColor(Color.BLUE);
				p.setStyle(Style.FILL_AND_STROKE);
				p.setAlpha(alpha);
				p.setShadowLayer(3f, 2, 2, Color.BLACK);
				c.drawText(low, x + wh - (fs / 2), y + h - fs, p);
			} else Logger.LogWarning("Couldn't find low :(");
			if(hi != null)
			{
				//p.setShadowLayer(0, 0, 0, 0);
				//p.setColor(Color.GRAY);
				//p.setAlpha(alpha - 50);
				//c.drawText(hi, x + wh - 12, y + p.getTextSize() - 2, p);
				p.setColor(Color.RED);
				c.drawText(hi, x + wh - 10, y + fs, p);
			}
			if(i == 0)
			{
				p.setColor(Color.BLACK);
				p.setShadowLayer(3f,2,2,Color.WHITE);
				c.drawText(getCurrentCondition(), x, y + hh, p);
				if(f.hasValue("temp_f"))
				{
					p.setColor(Color.rgb(0, 100, 0));
					c.drawText(f.getValue("temp_f"), x + wh - (fs / 2), y + hh - fs, p);
				}
			}
			else Logger.LogWarning("Couldn't find high :(");
			//c.restore();
		}
	}
	private String join (String[] c) {
		if(c == null) return "";
	    StringBuilder sb=new StringBuilder();
	    for(String s : c)
	        sb.append(s);
	    return sb.toString();
	}

	private Drawable getDrawable(int id)
	{
		return mContext.getResources().getDrawable(id);
	}
	private int getDrawableIDFromCondition(String sCondition)
	{
		for(int i = 0; i < MAP_STRINGS.length; i++)
			if(sCondition.toLowerCase().contains(MAP_STRINGS[i]))
				return MAP_DRAWABLE[i];
		return R.drawable.ww_dunno;
	}
	private Drawable getDrawableFromCondition(String sCondition)
	{
		return getDrawable(getDrawableIDFromCondition(sCondition));
	}
	
}
