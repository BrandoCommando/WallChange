package com.brandroid.dynapaper.Activities;

import com.brandroid.controls.RotateImageView;
import com.brandroid.dynapaper.R;
import com.brandroid.util.Logger;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;

public class SelectPosition extends BaseActivity
{
	int[] buttonIDs = {
			R.id.btnTopLeft, R.id.btnTopCenter, R.id.btnTopRight,
			R.id.btnMiddleLeft, R.id.btnMiddleCenter, R.id.btnMiddleRight,
			R.id.btnBottomLeft, R.id.btnBottomCenter, R.id.btnBottomRight};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTitle(getString(R.string.s_position));
		Intent intent = getIntent();
		if(intent == null)
			intent = new Intent();
		setContentView(R.layout.position);
		Resources res = getResources();
		Bitmap bmpArrow = BitmapFactory.decodeResource(res, R.drawable.arrow_green);
		for(int i = 0; i < buttonIDs.length; i++)
		{
			ImageButton iv = (ImageButton)findViewById(buttonIDs[i]);
			if(i != 4)
			{
				int deg = ((45 * i) + 225) % 360;
				if(i > 5)
					deg = (360 - deg) - 90;
				if(i == 5)
					deg = 0;
				if(i == 3)
					deg = 180;
				iv.setImageBitmap(rotateImage(bmpArrow, deg));
			}
			iv.setOnClickListener(this);
		}
	}
	
	private Bitmap rotateImage(Bitmap src, int degrees)
	{
		Logger.LogDebug("rotateImage " + degrees + " degrees.");
		Bitmap ret = Bitmap.createBitmap(src.getWidth(), src.getHeight(), Config.ARGB_8888);
		Canvas c = new Canvas(ret);
		Matrix m = new Matrix(c.getMatrix());
		Paint p = new Paint();
		p.setStyle(Style.FILL);
		m.setRotate(degrees, src.getWidth() / 2, src.getHeight() / 2);
		c.drawBitmap(src, m, p);
		return ret;
	}
	
	@Override
	public void onClick(View v) {
		Intent ret = new Intent();
		int sel = 4;
		for(int i = 0; i < buttonIDs.length; i++)
			if(buttonIDs[i] == v.getId())
				sel = i;
		ret.putExtra("position", sel);
		setResult(RESULT_OK, ret);
		finish();
	}
}
