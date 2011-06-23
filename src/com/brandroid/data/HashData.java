package com.brandroid.data;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;

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
	public void AddValue(String key, String value)
	{
		values.put(key, value);
	}
	public void AddChild(HashData child)
	{
		children.add(child);
	}
	public String getValue(String key) { return values.get(key); }
	
	public int size() { return children.size(); }
	
}
