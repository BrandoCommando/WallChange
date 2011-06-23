package com.brandroid.util;

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
	
	public static Object FollowPath(JSONObject json, Object oDefault, String... path)
	{
		Object tmp = null;
		for(int i = 0; i < path.length; i++)
		{
			tmp = json.opt(path[i]);
			if(tmp == null) return oDefault;
			if(i == path.length - 1) return tmp;
			if(tmp.getClass().equals(JSONObject.class))
				json = (JSONObject)tmp;
		}
		return tmp;
	}
	public static String FollowPathToString(JSONObject json, String[] path, int index, String sDefault)
	{
		if(json == null) return sDefault;
		if(index == path.length - 1)
			return json.optString(path[path.length - 1], sDefault);
		else return FollowPathToString(json.optJSONObject(path[index]), path, index + 1, sDefault);
		
	}
	
	public static JSONException getLastException() { return last; }
	
	@Override
	public String toString() {
		return json.toString();
	}
}
