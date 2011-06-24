package com.brandroid.data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;

import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import com.brandroid.util.Logger;

public class HashData
{
	private Hashtable<String, String> values;
	private ArrayList<HashData> children;
	public HashData() {
		values = new Hashtable<String, String>();
		children = new ArrayList<HashData>();	
	}
	public HashData(JSONObject json)
	{
		values = new Hashtable<String, String>();
		children = new ArrayList<HashData>();
		Iterator<String> keys = json.keys();
		while(keys.hasNext())
		{
			String key = keys.next();
			Object oVal = json.opt(key);
			if(oVal == null) continue;
			if(oVal.getClass().equals(String.class))
			{
				//Logger.LogDebug("HashData Added " + key + "=" + (String)oVal);
				values.put(key, (String)oVal);
			} else if(oVal.getClass().equals(JSONObject.class))
			{
				JSONObject kid = (JSONObject)oVal;
				if(kid.has("@attributes"))
					kid = kid.optJSONObject("@attributes");
				if(kid.has("data"))
					values.put(key, kid.optString("data"));
				else
					Logger.LogWarning("HashData unable to find data for " + key);
			} else Logger.LogWarning("HashData unable to add " + key);
		}
	}
	public HashData(XmlPullParser xml)
	{
		values = new Hashtable<String, String>();
		children = new ArrayList<HashData>();
		String startName = xml.getName();
		try {
			while(xml.next() != XmlPullParser.END_DOCUMENT)
			{
				if(xml.getEventType() == XmlPullParser.START_TAG)
				{
					if(xml.getAttributeCount() == 1 && xml.getAttributeName(0).equalsIgnoreCase("data"))
						AddValue(xml.getName(), xml.getAttributeValue(0));
					else AddChild(new HashData(xml));
				} else if(xml.getEventType() == XmlPullParser.END_TAG && xml.getName().equalsIgnoreCase(startName))
					break;
			}
		}
		catch(XmlPullParserException pe) { Logger.LogError("Xml Exception getting HashData.", pe); }
		catch(IOException ie) { Logger.LogError("IOError getting HashData.", ie); }
	}
	public static HashData Parse(XmlPullParser xml)
	{
		return new HashData(xml);
	}
	public void Merge(HashData newData)
	{
		String key;
		if(newData == null || newData.values == null) return;
		Enumeration<String> keys = newData.values.keys();
		while(keys.hasMoreElements())
		{
			key = keys.nextElement();
			values.put(key, newData.values.get(key));
		}
	}
	public void AddValue(String key, String value)
	{
		values.put(key, value);
	}
	public void AddChild(HashData child)
	{
		children.add(child);
	}
	public String getValue(String key) { return values.get(key); }
	public Boolean hasValue(String key) { return values.containsKey(key); }
	
	public int size() { return children.size(); }
	
	@Override
	public String toString()
	{
		StringBuilder ret = new StringBuilder("{");
		Enumeration<String> keys = values.keys();
		while(keys.hasMoreElements())
		{
			String key = keys.nextElement();
			ret.append(key + ":\"" + values.get(key) + "\",");
		}
		ret.setLength(ret.length() - 1);
		if(ret.length() == 0) return null;
		ret.append("}");
		return ret.toString();
	}
	
}
