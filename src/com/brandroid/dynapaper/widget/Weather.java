package com.brandroid.dynapaper.widget;

import java.util.Map;

import org.json.JSONObject;

import com.brandroid.JSON;
import com.brandroid.Logger;
import com.brandroid.NetUtils;
import com.brandroid.dynapaper.R;
import com.brandroid.dynapaper.WallChanger;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

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
	
	public String getCurrentCondition()
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
		return JSON.FollowPathToString(mData, new String[] { "output", "current", "condition" }, 0, "Dunno");
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
		Bitmap widget = ((BitmapDrawable)getWidget()).getBitmap();
		c.save();
		c.drawBitmap(widget, (bmp.getWidth() / 2) - (widget.getWidth() / 2), (bmp.getHeight() / 2) - (widget.getHeight() / 2), getPaint());
		c.restore();
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
			if(sCondition.contains(MAP_STRINGS[i]))
				return MAP_DRAWABLE[i];
		return R.drawable.ww_dunno;
	}
	private Drawable getDrawableFromCondition(String sCondition)
	{
		return getDrawable(getDrawableIDFromCondition(sCondition));
	}
	
}
