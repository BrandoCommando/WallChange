package com.brandroid.dynapaper.widget;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;

public abstract class Widget
{
	public abstract Drawable getWidget();
	public abstract void applyTo(Bitmap bmp, Canvas c);
}
