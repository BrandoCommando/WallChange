package com.brandroid.dynapaper.widget;

import java.util.ArrayList;

public class Widgets extends java.util.AbstractList<Widget>
{
	private ArrayList<Widget> mWidgets = new ArrayList<Widget>();

	@Override
	public boolean add(Widget w) { return mWidgets.add(w); };
	
	@Override
	public Widget get(int location) {
		return mWidgets.get(location);
	}

	@Override
	public int size() {
		return mWidgets.size();
	}
	
	public Widgets() { mWidgets = new ArrayList<Widget>(); }
	public Widgets(String toParse)
	{
		
	}
	
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		for(Widget w : mWidgets)
		{
			sb.append(w.toString());
			sb.append(",");
		}
		if(sb.length() > 0)
			sb.setLength(sb.length() - 1);
		return sb.toString();
	}
}
