package com.brandroid;

//import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
//import org.json.JSONStringer;
import org.json.JSONTokener;

public class JSON
{
	private JSONObject json;
	private static JSONException last;
	
	public JSON()
	{
		json = new JSONObject();
	}
	
	public JSON(String jsonString) throws JSONException
	{
		json = new JSONObject(new JSONTokener(jsonString));
	}
	
	public JSONObject getJSON() { return json; }
	
	public static Boolean Parse(String jsonString, JSON[] outJSON)
	{
		try {
			outJSON = new JSON[1];
			outJSON[0] = new JSON(jsonString);
			return true;
		} catch(JSONException ex) { last = ex; outJSON = new JSON[0]; return false; }
	}
	
	public static JSONObject Parse(String jsonString)
	{
		try {
			return new JSON(jsonString).getJSON();
		} catch(JSONException ex) { last = ex; return null; }
	}
	
	public static JSONException getLastException() { return last; }
	
	@Override
	public String toString() {
		return json.toString();
	}
}
