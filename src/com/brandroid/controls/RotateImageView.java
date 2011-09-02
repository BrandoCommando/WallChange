package com.brandroid.controls;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.widget.ImageView;

public class RotateImageView extends ImageView
{
	private float mRotation = 0;

	public RotateImageView(Context context) {
		super(context);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		Bitmap src = getDrawingCache(false);
		Matrix m = new Matrix();
		m.preRotate(mRotation);
		Paint p = new Paint();
		p.setStyle(Style.FILL);
		canvas.drawBitmap(src, m, p);
	}

	public void setRotation(int mRotation) {
		this.mRotation = mRotation;
	}

	public float getRotation() {
		return mRotation;
	}
}
