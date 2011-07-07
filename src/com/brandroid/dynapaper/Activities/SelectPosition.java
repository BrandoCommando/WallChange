package com.brandroid.dynapaper.Activities;

import com.brandroid.dynapaper.R;
import com.brandroid.util.ImageUtilities;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

public class SelectPosition extends BaseActivity
{
	int[] buttonIDs = {
			R.id.btnTopLeft, R.id.btnTopCenter, R.id.btnTopRight,
			R.id.btnMiddleLeft, R.id.btnMiddleCenter, R.id.btnMiddleRight,
			R.id.btnBottomLeft, R.id.btnBottomCenter, R.id.btnBottomRight};
	Bitmap bmps[] = new Bitmap[buttonIDs.length];
	
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
			iv.setOnClickListener(this);
			if(i == 4) continue;
			int deg = getDegreesFromIndex(i);
			iv.setTag(deg);
			iv.setImageBitmap(ImageUtilities.rotateImage(bmpArrow, deg));
		}
	}
	
	public static int getDegreesFromIndex(int i)
	{
		int deg = 0;
		if(i != 4)
		{
			deg = ((45 * i) + 225) % 360;
			if(i > 5)
				deg = (360 - deg) - 90;
			if(i == 5)
				deg = 0;
			if(i == 3)
				deg = 180;
			//bmps[i] = rotateImage(bmpArrow, deg);
			//iv.setImageBitmap(bmps[i]);
		}
		return deg;
	}
	
	@Override
	public void onClick(View v) {
		Intent ret = new Intent();
		int sel = 4;
		for(int i = 0; i < buttonIDs.length; i++)
			if(buttonIDs[i] == v.getId())
				sel = i;
		ret.putExtra("position", sel);
		ret.putExtra("degrees", (Integer)v.getTag());
		//ByteArrayOutputStream baos = new ByteArrayOutputStream();
		//bmps[sel].compress(CompressFormat.PNG, 100, baos);
		//ret.putExtra("img", baos.toByteArray());
		setResult(RESULT_OK, ret);
		finish();
	}
}
