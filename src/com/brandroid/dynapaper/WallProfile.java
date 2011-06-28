package com.brandroid.dynapaper;

import java.util.ArrayList;

import android.graphics.Matrix;

import com.brandroid.dynapaper.widget.Widget;
import com.brandroid.dynapaper.widget.Widgets;

public class WallProfile
{
	private String mBasePath = "";
	private Matrix mMatrix;
	private Widgets mWidgets;
	private int mId = -1;
	
	public WallProfile()
	{
		mMatrix = new Matrix();
		mWidgets = new Widgets();
	}
	public WallProfile(int id, String path, String matrix, Widgets widgets)
	{
		mId = id;
		mBasePath = path;
		mMatrix = new Matrix();
		// Todo: Matrix String Parser
		mWidgets = widgets; 
	}
	
	public int getId() { return mId; }

	public void setBasePath(String mBasePath) {
		this.mBasePath = mBasePath;
	}
	public String getBasePath() {
		return mBasePath;
	}
	
	public void AddWidget(Widget w) { mWidgets.add(w); }
	public void RemoveWidget(Widget w) { mWidgets.remove(w); }
	public Widget[] getWidgets() {
		Widget[] ret = new Widget[mWidgets.size()];
		for(int i = 0; i < ret.length; i++)
			ret[i] = mWidgets.get(i);
		return ret;
	}
	
	public void setRotate(float degrees)
	{
		mMatrix.setRotate(degrees);
	}
}
