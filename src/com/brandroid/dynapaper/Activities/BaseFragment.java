package com.brandroid.dynapaper.Activities;

import com.brandroid.dynapaper.R;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class BaseFragment extends Fragment
{
	private String mText = "";
	
	public BaseFragment() { mText = this.getClass().toString().replace("Fragment", ""); }
	public BaseFragment(String text) { mText = text; }
	
	public String getText() { return mText; }
	public void setText(String txt) { mText = txt; }
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View fragView = inflater.inflate(R.layout.preview_fragment, container, false);

        TextView text = (TextView) fragView.findViewById(R.id.title);
        if(text != null)
        	text.setText(mText);

        return fragView;
    }

}
