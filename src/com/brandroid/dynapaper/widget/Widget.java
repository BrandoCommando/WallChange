package com.brandroid.dynapaper.widget;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

public abstract class Widget
{
	protected Point mPosition;
	public void setPosition(Point pt) { mPosition = pt; }
	public abstract Drawable getWidget();
	public abstract Boolean applyTo(Bitmap bmp, Canvas c);
	public abstract String toString();
	public abstract void parseString(String s);
}