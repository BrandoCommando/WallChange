package com.brandroid.dynapaper.widget;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;
import java.util.StringTokenizer;

import org.json.JSONArray;
import org.json.JSONObject;

import com.brandroid.util.Logger;
import com.brandroid.data.WeatherData;
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
			mJSON = NetUtils.downloadJSON(url);
			mData = new WeatherData(mJSON);
		}
		return mData;
	}
	public String[] getConditions()
	{
		WeatherData api = getAPIResults();
		if(api == null) return null;
		String[] ret = new String[api.getForecast().length];
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
			x += i * 100;
			WeatherData.Forecast f = api.getForecast(i);
			String low = f.getTempLow(), hi = f.getTempHi();
			p.setColor(Color.TRANSPARENT);
			p.setAlpha(alpha);
			//c.save();
			c.drawBitmap(((BitmapDrawable)widget).getBitmap(), x, y, p);
			if(low != null)
			{
				p.setShadowLayer(0, 0, 0, 0);
				p.setColor(Color.WHITE);
				p.setAlpha(alpha - 50);
				c.drawText(hi, x + wh - 12, y + h - p.getTextSize() - 2, p);
				p.setColor(Color.BLUE);
				p.setAlpha(alpha);
				p.setShadowLayer(2f, 2, 2, Color.BLACK);
				c.drawText(low, x + wh - 10, y + h - p.getTextSize(), p);
			} else Logger.LogWarning("Couldn't find low :(");
			if(hi != null)
			{
				p.setShadowLayer(0, 0, 0, 0);
				p.setColor(Color.WHITE);
				p.setAlpha(alpha - 50);
				c.drawText(hi, x + wh - 12, y + p.getTextSize() - 2, p);
				p.setColor(Color.RED);
				p.setAlpha(alpha);
				p.setShadowLayer(2f, 2, 2, Color.BLACK);
				c.drawText(hi, x + wh - 10, y + p.getTextSize(), p);
			}
			else Logger.LogWarning("Couldn't find high :(");
			//c.restore();
		}
	}
	private String join (String[] c) {
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
