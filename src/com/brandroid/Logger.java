package com.brandroid;

import java.util.ArrayList;
import java.util.List;

import com.brandroid.dynapaper.WallChanger;

import android.util.Log;

public class Logger
{
	private static String[] sLastMessage = new String[] {"", "", "", "", ""};
	private static Integer[] iLastCount = new Integer[] {0,0,0,0,0};
	public static Boolean LoggingEnabled = true;
	private static final String LOG_KEY = WallChanger.LOG_KEY;

	private static boolean CheckLastLog(String msg, int level)
	{
		if(!LoggingEnabled) return true;
		level -= 2;
		if(msg.equalsIgnoreCase(sLastMessage[level]))
		{
			iLastCount[level]++;
			return true;
		} else if(iLastCount[level] > 0)
		{
			Log.println(level, LOG_KEY, "The last message repeated " + iLastCount[level] + " times");
			iLastCount[level] = 0;
		}
		sLastMessage[level] = msg;
		return false;
	}
	private static int getMyStackTraceCount(StackTraceElement[] els)
	{
		int ret = 0;
		for(int i = 0; i < els.length; i++)
			if(els[i].getClassName().contains("com.brandroid"))
				ret++;
		return ret;
	}
	private static StackTraceElement[] getMyStackTrace(Exception e)
	{
		StackTraceElement[] elArray = e.getStackTrace();
		StackTraceElement[] ret = new StackTraceElement[getMyStackTraceCount(elArray)];
		int j = 0;
		for(int i = 0; i < elArray.length; i++)
			if(elArray[i].getClassName().contains("com.brandroid"))
				ret[j++] = elArray[i];
		return ret;
	}
	public static void LogError(String msg)
	{
		if(!CheckLastLog(msg, Log.ERROR))
			Log.e(LOG_KEY, msg);
	}
	public static void LogError(String msg, Exception ex)
	{
		if(!CheckLastLog(ex.getMessage(), Log.ERROR))
		{
			ex.setStackTrace(getMyStackTrace(ex));
			Log.e(LOG_KEY, msg, ex);
		}
	}
	public static void LogWarning(String msg)
	{
		if(!CheckLastLog(msg, Log.WARN))
			Log.w(LOG_KEY, msg);
	}
	public static void LogWarning(String msg, Exception w)
	{
		if(!CheckLastLog(w.getMessage(), Log.WARN))
		{
			w.setStackTrace(getMyStackTrace(w));
			Log.w(LOG_KEY, msg, w);
		}
	}
	public static void LogInfo(String msg)
	{
		if(!CheckLastLog(msg, Log.INFO))
			Log.i(LOG_KEY, msg);
	}
	public static void LogDebug(String msg)
	{
		if(!CheckLastLog(msg, Log.DEBUG))
			Log.d(LOG_KEY, msg);
	}
	public static void LogVerbose(String msg)
	{
		if(!CheckLastLog(msg, Log.VERBOSE))
			Log.v(LOG_KEY, msg);
	}
}
