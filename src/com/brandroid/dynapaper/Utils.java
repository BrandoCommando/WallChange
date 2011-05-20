package com.brandroid.dynapaper;

import android.graphics.Bitmap;
import android.util.Log;

public class Utils {
	public static Bitmap getSizedBitmap(Bitmap bmp, int mw, int mh)
	{
		int w = bmp.getWidth();
		int h = bmp.getHeight();
		Log.d(WallChanger.LOG_KEY, "Bitmap Size: " + w + "x" + h + "  Max Size: " + mw + "x" + mh);
		if(w > mw && h > mh)
		{
			if((w - mw) < (h - mh))
			{
				double r = (double)h / (double)w;
				w = mw;
				h = (int)Math.floor(r * (double)w);
			} else {
				double r = (double)w / (double)h;
				h = mh;
				w = (int)Math.floor(r * (double)h);
			}
			Log.d(WallChanger.LOG_KEY, "Resizing to " + w + "x" + h);
			try {
				bmp = Bitmap.createScaledBitmap(bmp, w, h, true);
				//bmp.recycle();
				//return ret;
			} catch(Exception ex) {
				Log.e(WallChanger.LOG_KEY, "Resizing Failed. Using original.");
			}
		} else Log.d(WallChanger.LOG_KEY, "No resizing needed.");
		return bmp;
	}
}
