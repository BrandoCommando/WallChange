package com.brandroid.util;

import java.util.ArrayList;
import java.util.List;

import com.brandroid.dynapaper.WallChanger;
import com.brandroid.dynapaper.Database.LoggerDbAdapter;

import android.util.Log;

public class Logger
{
	private static String[] sLastMessage = new String[] {"", "", "", "", ""};
	private static Integer[] iLastCount = new Integer[] {0,0,0,0,0};
	public static Boolean LoggingEnabled = true;
	public static final Integer MIN_DB_LEVEL = Log.DEBUG;
	private static final String LOG_KEY = WallChanger.LOG_KEY;
	private static LoggerDbAdapter dbLog;

	private static boolean CheckLastLog(String msg, int level)
	{
		if(!LoggingEnabled) return true;
		level -= 2;
		if(level < 0 || level > 4) return false;
		if(sLastMessage[level] != null && msg != null && msg.equalsIgnoreCase(sLastMessage[level]))
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
	private static void LogToDB(int level, String msg, String stack)
	{
		if(level < MIN_DB_LEVEL) return;
		if(dbLog == null) return;
		dbLog.createItem(msg, level, stack);
	}
	public static Boolean hasDb() { return dbLog != null; }
	public static void setDb(LoggerDbAdapter newDb) { dbLog = newDb; }
	public static String getDbLogs() { return dbLog.getAllItemsAndClear(); }
	public static void LogError(String msg)
	{
		if(CheckLastLog(msg, Log.ERROR)) return;
		LogToDB(Log.ERROR, msg, "");
		Log.e(LOG_KEY, msg);
	}
	public static void LogError(String msg, Exception ex)
	{
		if(CheckLastLog(ex.getMessage(), Log.ERROR)) return;
		ex.setStackTrace(getMyStackTrace(ex));
		LogToDB(Log.ERROR, msg, Log.getStackTraceString(ex));
		Log.e(LOG_KEY, msg, ex);
	}
	public static void LogWarning(String msg)
	{
		if(CheckLastLog(msg, Log.WARN)) return;
		LogToDB(Log.WARN, msg, "");
		Log.w(LOG_KEY, msg);
	}
	public static void LogWarning(String msg, Exception w)
	{
		if(CheckLastLog(w.getMessage(), Log.WARN)) return;
		w.setStackTrace(getMyStackTrace(w));
		LogToDB(Log.WARN, msg, Log.getStackTraceString(w));
		Log.w(LOG_KEY, msg, w);
	}
	public static void LogInfo(String msg)
	{
		if(CheckLastLog(msg, Log.INFO)) return;
		LogToDB(Log.INFO, msg, "");
		Log.i(LOG_KEY, msg);
	}
	public static void LogInfo(String msg, String stack)
	{
		if(CheckLastLog(msg, Log.INFO)) return;
		LogToDB(Log.DEBUG, msg, stack);
		Log.d(LOG_KEY, msg);
	}
	public static void LogDebug(String msg)
	{
		if(CheckLastLog(msg, Log.DEBUG)) return;
		LogToDB(Log.DEBUG, msg, "");
		Log.d(LOG_KEY, msg);
	}
	public static void LogDebug(String msg, String stack)
	{
		if(CheckLastLog(msg, Log.DEBUG)) return;
		LogToDB(Log.DEBUG, msg, stack);
		Log.d(LOG_KEY, msg);
	}
	public static void LogVerbose(String msg)
	{
		if(CheckLastLog(msg, Log.VERBOSE)) return;
		LogToDB(Log.VERBOSE, msg, "");
		Log.v(LOG_KEY, msg);
	}
}
