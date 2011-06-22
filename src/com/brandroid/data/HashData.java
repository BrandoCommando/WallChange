package com.brandroid.data;

import java.util.ArrayList;
import java.util.Hashtable;

import org.xmlpull.v1.XmlPullParser;

public class HashData
{
	private Hashtable<String, String> values;
	private ArrayList<HashData> children;
	public HashData() {
		values = new Hashtable<String, String>();
		children = new ArrayList<HashData>();
	}
	public void AddValue(String key, String value)
	{
		values.put(key, value);
	}
	public void AddChild(HashData child)
	{
		children.add(child);
	}
	
	public int size() { return children.size(); }
	
}
