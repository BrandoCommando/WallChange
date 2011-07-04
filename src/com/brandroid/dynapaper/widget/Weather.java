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
import org.w3c.dom.Text;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import com.brandroid.util.Logger;
import com.brandroid.data.WeatherData;
import com.brandroid.data.WeatherData.Forecast;
import com.brandroid.data.WeatherData.Information;
import com.brandroid.dynapaper.R;
import com.brandroid.dynapaper.WallChanger;
import com.brandroid.util.JSON;
import com.brandroid.util.NetUtils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils.StringSplitter;

public class Weather extends Widget
{
	private String sLocation;
	private Point mPosition;
	private Context mContext;
	private JSONObject mJSON = null;
	private WeatherData mData = null;
	
	// 'mostly cloudy' => 'cloudy4', 'heavy showers' => 'shower3', 'showers' => 'shower2', 'partial' => 'cloudy2', 'some' => 'shower2', 'cloud' => 'cloudy1', 'rain' => 'shower1', 'mist' => 'mist', 'snow' => 'snow2', 'fair' => 'sunny', 'sun' => 'sunny');
	public static final String[] MAP_STRINGS = new String[] {"mostly cloudy", "heavy showers", "showers", "partial", "some", "cloud", "rain", "mist", "snow", "fair", "sun", "clear", "fog"};
	public static final int[] MAP_DRAWABLE = new int[] {R.drawable.ww_cloudy4, R.drawable.ww_shower3, R.drawable.ww_shower2, R.drawable.ww_cloudy2, R.drawable.ww_shower2, R.drawable.ww_cloudy1, R.drawable.ww_shower1, R.drawable.ww_mist, R.drawable.ww_snow2, R.drawable.ww_sunny, R.drawable.ww_sunny, R.drawable.ww_sunny, R.drawable.ww_fog};
	
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
			String url = WallChanger.GOOGLE_WEATHER_API_URL.replace("%ZIP%", sLocation);
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
			if(mData == null || mData.getCurrentConditions() == null)
			{
				Logger.LogWarning("Google didn't have anything to say. Trying my weather API.");
				mJSON = NetUtils.downloadJSON(WallChanger.MY_WEATHER_API_URL.replace("%ZIP%", sLocation));
				if(mJSON != null)
					mData = new WeatherData(mJSON);
				if(mData != null)
					Logger.LogDebug("Received from my API: " + mData.toString());
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
	public String getCurrentInfoLine()
	{
		Information info = mData.getCurrentInformation();
		String ret = "";
		if(info != null)
			ret = info.getCity();
		return ret;
	}
	public String getCurrentCondition()
	{
		String ret = mData.getCurrentConditions().getCondition();
		if(mData.getCurrentConditions().hasValue("temp_f"))
			ret += (ret != "" ? ": " : "") + mData.getCurrentConditions().getValue("temp_f");
		return ret;
	}

	@Override
	public Drawable getWidget()
	{
		String cond = getCurrentCondition();
		Logger.LogDebug("Current Condition: " + cond);
		return getDrawableFromCondition(cond.toLowerCase());
	}
	@Override
	public Boolean applyTo(Bitmap bmp, Canvas c)
	{
		Boolean ret = true;
		
		WeatherData api = getAPIResults();
		if(api == null) return false;
		String[] conditions = getConditions();
		if(conditions.length == 0) return false;
		
		Point center = new Point(bmp.getWidth() / 2, bmp.getHeight() / 2);
		for(int i = conditions.length - 1; i >= 0; i--)
		{
			Paint p = new Paint();
			p.setStyle(Style.FILL);
			p.setColor(Color.BLACK);
			p.setTypeface(Typeface.DEFAULT_BOLD);
			int alpha = 255 - (i * 50);
			//Logger.LogDebug("Alpha: " + alpha);
			Drawable widget = getDrawableFromCondition(i > 0 ? conditions[i] : getCurrentCondition());
			int w = widget.getMinimumWidth(),
				h = widget.getMinimumHeight(),
				wh = w / 2,
				hh = h / 2,
				x = (int)Math.floor(center.x / 2),
				y = (int)Math.floor(center.y - (float)hh);
			int size = Math.min(Math.max(h / 6, 20), 30);
			Logger.LogInfo("Text Size: " + size + "  Height: " + h);
			p.setTextSize(size);
			float fs = p.getTextSize();
			x += i * wh;
			WeatherData.Forecast f = api.getForecast(i);
			String low = f.getTempLow(), hi = f.getTempHi(), day = f.getDay();
			p.setColor(Color.TRANSPARENT);
			p.setAlpha(alpha);
			//c.save();
			//Logger.LogInfo("Position on day " + (i + 1) + ": " + x + "," + y);
			c.drawBitmap(((BitmapDrawable)widget).getBitmap(), x, y, p);
			if(low != null)
			{
				Paint pl = new Paint(p);
				pl.setAntiAlias(true);
				pl.setTextAlign(Align.CENTER);
				pl.setColor(Color.BLUE);
				pl.setStyle(Style.FILL_AND_STROKE);
				pl.setAlpha(alpha);
				pl.setShadowLayer(3f, 2, 2, Color.WHITE);
				c.drawText(low, x + wh, y + h - fs, pl);
			} else Logger.LogWarning("Couldn't find low :(");
			if(hi != null)
			{
				Paint ph = new Paint(p);
				ph.setAntiAlias(true);
				ph.setTextAlign(Align.CENTER);
				ph.setColor(Color.RED);
				ph.setStyle(Style.FILL_AND_STROKE);
				ph.setAlpha(alpha);
				ph.setShadowLayer(3f, 2, 2, Color.BLACK);
				//c.drawText(hi, x + wh - 12, y + p.getTextSize() - 2, p);
				c.drawText(hi, x + wh, y + fs, ph);
			} else Logger.LogWarning("Couldn't find high :(");
			if(day != null)
			{
				Paint pd = new Paint(p);
				pd.setAntiAlias(true);
				pd.setTextAlign(Align.CENTER);
				pd.setColor(Color.BLACK);
				pd.setStyle(Style.FILL_AND_STROKE);
				pd.setAlpha(alpha);
				pd.setShadowLayer(3f, 2, 2, Color.WHITE);
				c.drawText(day, x + wh, (float)(y + h), pd);
			} else Logger.LogWarning("Couldn't find day :(");
			if(i == 0)
			{
				p.setShadowLayer(3f,2,2,Color.WHITE);
				p.setTextAlign(Align.LEFT);
				p.setAntiAlias(true);
				p.setColor(Color.rgb(0, 100, 0));
				c.drawText(getCurrentCondition(), x + (w / 4), y + hh - fs + ((size / 3) * 2), p);
				p.setColor(Color.BLACK);
				c.drawText(getCurrentInfoLine(), x + (w / 4), y + hh + ((size / 3) * 2), p);
			}
			//c.restore();
		}
		return ret;
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
	
	@Override
	public String toString()
	{
		return "type:\"weather\",zip:\"" + sLocation + "\"";
	}

	@Override
	public void parseString(String s) {
		JSONObject json = JSON.Parse(s);
		sLocation = json.optString("zip", sLocation);
		mPosition = new Point(json.optInt("x", 0), json.optInt("y", 0));
	}
	
}
