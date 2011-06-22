package com.brandroid;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.zip.GZIPInputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

import org.json.JSONObject;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

public class NetUtils
{
	public static JSONObject downloadJSON(String url)
	{
		HttpURLConnection uc = null;
		InputStream in;
		BufferedReader br;
		StringBuilder sb;
		JSONObject ret = null;
		try {
			uc = (HttpURLConnection)new URL(url).openConnection();
			uc.setReadTimeout(10000);
			uc.addRequestProperty("Accept-Encoding", "gzip, deflate");
			uc.connect();
			if(uc.getResponseCode() == HttpURLConnection.HTTP_OK)
			{
				String encoding = uc.getContentEncoding();
				if(encoding != null && encoding.equalsIgnoreCase("gzip"))
					in = new GZIPInputStream(uc.getInputStream());
				else if(encoding != null && encoding.equalsIgnoreCase("deflate"))
					in = new InflaterInputStream(uc.getInputStream(), new Inflater(true));
				else
					in = new BufferedInputStream(uc.getInputStream());
				br = new BufferedReader(new InputStreamReader(in));
	    		sb = new StringBuilder();
	    		String line = "";
	    		while((line = br.readLine()) != null)
	    			sb.append(line + '\n');
	    		ret = JSON.Parse(sb.toString());
	    		if(ret == null)
	    			Logger.LogWarning("Unable to parse JSON: " + sb.toString());
			} else Logger.LogWarning(uc.getResponseCode() + " returned for " + uc.getURL().toString());
		} catch(Exception ex) { Logger.LogError("Exception reading JSON from " + url, ex); }
		return ret;
	}
	public static JSONObject downloadXML2JSON(String url)
	{
		HttpURLConnection uc = null;
		InputStream in;
		//BufferedReader br;
		//StringBuilder sb;
		XMLReader xr;
		XmlPullParser parser = null;
		int parserEvent = -1;
		JSONObject ret = new JSONObject();
		try {
			uc = (HttpURLConnection)new URL(url).openConnection();
			uc.setReadTimeout(10000);
			uc.addRequestProperty("Accept-Encoding", "gzip, deflate");
			uc.connect();
			if(uc.getResponseCode() == HttpURLConnection.HTTP_OK)
			{
				String encoding = uc.getContentEncoding();
				if(encoding != null && encoding.equalsIgnoreCase("gzip"))
					in = new GZIPInputStream(uc.getInputStream());
				else if(encoding != null && encoding.equalsIgnoreCase("deflate"))
					in = new InflaterInputStream(uc.getInputStream(), new Inflater(true));
				else
					in = new BufferedInputStream(uc.getInputStream());
				parser = XmlPullParserFactory.newInstance().newPullParser();
				parser.setInput(in, encoding);
				parseXML(parser, ret);
			} else Logger.LogWarning(uc.getResponseCode() + " returned for " + uc.getURL().toString());
		} catch(Exception ex) { Logger.LogError("Exception reading JSON from " + url, ex); }
		return ret;
	}
	private static int parseXML(XmlPullParser parser, JSONObject output) throws XmlPullParserException
	{
		int ret = parser.getEventType();
		if(ret == XmlPullParser.END_DOCUMENT) return ret;
		while(ret != XmlPullParser.END_DOCUMENT)
		{
			switch(ret)
			{
				case XmlPullParser.START_TAG:
					int level = parser.getDepth();
					
					break;
			}
		}
		return ret;
	}
}
