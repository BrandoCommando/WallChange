package com.brandroid.dynapaper;

import android.util.Log;

public class Logger
{
	private static String[] sLastMessage = new String[] {"", "", "", "", ""};
	private static Integer[] iLastCount = new Integer[] {0,0,0,0,0};
	

	private static boolean CheckLastLog(String msg, int level)
	{
		level -= 2;
		if(msg == sLastMessage[level])
		{
			iLastCount[level]++;
			return true;
		} else if(iLastCount[level] > 0)
		{
			LogInfo("The last message repeated " + iLastCount[level] + " times");
			iLastCount[level] = 0;
		}
		return false;
	}
	public static void LogError(String msg)
	{
		if(!CheckLastLog(msg, Log.ERROR))
			Log.e(WallChanger.LOG_KEY, msg);
	}
	public static void LogError(String msg, Throwable ex)
	{
		if(!CheckLastLog(msg, Log.ERROR))
			Log.e(WallChanger.LOG_KEY, msg, ex);
	}
	public static void LogWarning(String msg)
	{
		if(!CheckLastLog(msg, Log.WARN))
			Log.w(WallChanger.LOG_KEY, msg);
	}
	public static void LogInfo(String msg)
	{
		if(!CheckLastLog(msg, Log.INFO))
			Log.i(WallChanger.LOG_KEY, msg);
	}
	public static void LogDebug(String msg)
	{
		if(!CheckLastLog(msg, Log.DEBUG))
			Log.d(WallChanger.LOG_KEY, msg);
	}
	public static void LogVerbose(String msg)
	{
		if(!CheckLastLog(msg, Log.VERBOSE))
			Log.v(WallChanger.LOG_KEY, msg);
	}
}
