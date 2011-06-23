package com.brandroid.dynapaper.widget;

import java.util.ArrayList;
import java.util.Map;
import java.util.StringTokenizer;

import org.json.JSONArray;
import org.json.JSONObject;

import com.brandroid.util.Logger;
import com.brandroid.dynapaper.R;
import com.brandroid.dynapaper.WallChanger;
import com.brandroid.util.JSON;
import com.brandroid.util.NetUtils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils.StringSplitter;

public class Weather extends Widget
{
	private String sLocation;
	private Context mContext;
	private Paint mPaint = null;
	private JSONObject mData = null;
	
	// 'mostly cloudy' => 'cloudy4', 'heavy showers' => 'shower3', 'showers' => 'shower2', 'partial' => 'cloudy2', 'some' => 'shower2', 'cloud' => 'cloudy1', 'rain' => 'shower1', 'mist' => 'mist', 'snow' => 'snow2', 'fair' => 'sunny', 'sun' => 'sunny');
	public static final String[] MAP_STRINGS = new String[] {"mostly cloudy", "heavy showers", "showers", "partial", "some", "cloud", "rain", "mist", "snow", "fair", "sun"};
	public static final int[] MAP_DRAWABLE = new int[] {R.drawable.ww_cloudy4, R.drawable.ww_shower3, R.drawable.ww_shower2, R.drawable.ww_cloudy2, R.drawable.ww_shower2, R.drawable.ww_cloudy1, R.drawable.ww_shower1, R.drawable.ww_mist, R.drawable.ww_snow2, R.drawable.ww_sunny, R.drawable.ww_sunny};
	
	public Weather(Context context, String location)
	{
		sLocation = location;
		mContext = context;
		//mConditionMap
	}
	
	public JSONObject getAPIResults()
	{
		if(mData == null)
		{
			String url = WallChanger.MY_WEATHER_API_URL;
			if(url.contains("%ZIP%"))
				url = url.replace("%ZIP%", sLocation);
			else
				url += (url.indexOf("?") > -1 ? "&" : "?") + "zip=" + sLocation; 
			mData = NetUtils.downloadJSON(url);
		}
		return mData;
	}
	public String[] getConditions()
	{
		JSONObject api = getAPIResults();
		JSONArray forecast = (JSONArray)JSON.FollowPath(api, new JSONArray(), "output", "forecast");
		if(forecast == null || forecast.length() == 0) return null;
		//String sCurrentCond = current.optString("condition", "Dunno");
		String[] ret = new String[forecast.length()];
		for(int i = 0; i < ret.length; i++)
			ret[i] = forecast.optJSONObject(i).optString("condition", "Dunno");
		return ret;
	}
	public String getCurrentCondition()
	{
		return (String)JSON.FollowPath(getAPIResults(), "n/a", "output", "current", "condition") +
			" - " + (String)JSON.FollowPath(getAPIResults(), "n/a", "output", "current", "temp_f") + "°";
		
	}
	public String[][] getForecastTable()
	{
		JSONObject api = getAPIResults();
		JSONArray forecast = (JSONArray)JSON.FollowPath(api, new JSONArray(), "output", "forecast");
		if(forecast == null || forecast.length() == 0) return null;
		String[][] ret = new String[forecast.length()+1][forecast.optJSONObject(0).length()];
		for(int i = 0; i < forecast.length(); i++)
		{
			JSONObject day = forecast.optJSONObject(i);
			String key = null;
			int j = 0;
			while((key = (String)day.keys().next()) != null)
			{
				if(i == 0)
					ret[0][j] = key;
				ret[i + 1][j++] = day.optString(key, "n/a");
			}
		}
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
	public void applyTo(Bitmap bmp, Canvas c)
	{
		//Bitmap widget = ((BitmapDrawable)getWidget()).getBitmap();
		c.save();
		Point center = new Point(bmp.getWidth() / 2, bmp.getHeight() / 2);
		Drawable widget;
		String[] conditions = getConditions();
		Logger.LogDebug("Conditions: " + join(conditions));
		for(int i = conditions.length - 1; i >= 0; i--)
		{
			widget = getDrawableFromCondition(conditions[i]);
			int x = (int)Math.floor(center.x - ((float)widget.getMinimumWidth() / 2));
			int y = (int)Math.floor(center.y - ((float)widget.getMinimumHeight() / 2));
			x += (i * 100);
			c.drawBitmap(((BitmapDrawable)widget).getBitmap(), x, y, getPaint());
		}
		widget = getDrawableFromCondition(getCurrentCondition());
		c.drawBitmap(((BitmapDrawable)widget).getBitmap(), center.x - ((float)widget.getMinimumWidth() / 2), center.y - ((float)widget.getMinimumHeight() / 2), getPaint());
		//for(String cond : getConditions())
			
		//c.drawBitmap(widget, (bmp.getWidth() / 2) - (widget.getWidth() / 2), (bmp.getHeight() / 2) - (widget.getHeight() / 2), getPaint());
		c.restore();
	}
	private String join (String[] c) {
	    StringBuilder sb=new StringBuilder();
	    for(String s : c)
	        sb.append(s);
	    return sb.toString();
	}


	private Paint getPaint()
	{
		if(mPaint != null) return mPaint;
		mPaint = new Paint();
		mPaint.setStyle(Style.FILL);
		return mPaint;
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
