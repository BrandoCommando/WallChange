package com.brandroid.data;

import java.util.Hashtable;

public class WeatherData extends HashData
{
	private Hashtable<String, String> values;
	private Conditions[] mConditions;
	
	public String getValue(String key) { return values.get(key); }
	public Conditions getConditions(int days) { return mConditions[days]; }
	
	public class Conditions
	{
		private Hashtable<String, String> values;
		//public String Condition, Temp_Hi, Temp_Low, Date, Wind, Day of Week;
		//public 
		public String getValue(String key) { return values.get(key); }
		public String getCondition() { return getValue("condition"); }
	}
}
