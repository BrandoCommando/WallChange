package com.brandroid.dynapaper.Activities;

import com.brandroid.dynapaper.R;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

public class PreviewFragment extends BaseFragment
{
	private ImageView mPreview;
	private boolean mCreated = false;
	private Bitmap mPendingPreview = null;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mCreated = false;
		View view = inflater.inflate(R.layout.preview_fragment, null);
		mPreview = (ImageView)view.findViewById(R.id.img_preview);
		return view;
	}
	
	@Override
	public void onStop() {
		super.onStop();
		mCreated = false;
	}
	
	@Override
	public void onPause() {
		super.onPause();
		mCreated = false;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		mCreated = true;
		if(mPendingPreview != null)
			setImageBitmap(mPendingPreview);
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState)
	{
		super.onViewCreated(view, savedInstanceState);
		mCreated = true;
		if(mPendingPreview != null)
			setImageBitmap(mPendingPreview);
	}
	
	public void setImageBitmap(Bitmap bmp)
	{
		if(mCreated)
			mPreview.setImageBitmap(bmp);
		else
			mPendingPreview = bmp;
	}
}
