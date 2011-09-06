package com.brandroid.dynapaper.Activities;

import com.brandroid.dynapaper.BrandroidBase;
import com.brandroid.dynapaper.R;
import com.brandroid.dynapaper.WallChanger;
import com.brandroid.util.Logger;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;

public class ProfileMakerList extends ListFragment
{
	ListView mList;
	String[] mListOptions = new String[0];
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mListOptions = new String[] { getResourceString(R.string.s_select_base),
				getResourceString(R.string.lbl_overlay),
				getResourceString(R.string.btn_test, R.string.s_wallpaper),
				getResourceString(R.string.btn_select, R.string.s_wallpaper),
				getResourceString(R.string.menu_settings),
				"Old UI",
				getResourceString(R.string.s_feedback)
				};
		View v = inflater.inflate(R.layout.maker_list, null);
		FrameLayout adLayout = (FrameLayout)v.findViewById(R.id.adLayout);
		BaseActivity.addAds(getActivity(), adLayout);
		return v;
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		mList = getListView();
		setListAdapter(new ArrayAdapter<String>(getActivity().getApplicationContext(),
				android.R.layout.simple_list_item_1,
				mListOptions
				));
	}
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		Logger.LogDebug("onListItemClick (" + position + ") on " + v.toString());
		switch(position)
		{
			case 0: // Select Base
				return;
			case 1: // Select Widgets
				return;
			case 2: // Preview
				return;
			case 3: // Apply
				return;
			case 4: // Settings
				((ProfileMakerMain)getActivity()).ShowDetailFragment(new Settings(), "settings");
				break;
			case 5: // Old UI
				startActivity(new Intent(getActivity().getApplicationContext(), ProfileMaker.class));
				return;
			case 6: // Feedback
				((ProfileMakerMain)getActivity()).ShowDetailFragment(new Feedback(), "feedback");
				break;
		}
	}
	
	

    private String getResourceString(int... resourceIDs)
    {
    	StringBuilder ret = new StringBuilder();
    	for(int i = 0; i < resourceIDs.length; i++)
    	{
    		ret.append(getText(resourceIDs[i]));
    		ret.append(" ");
    	}
    	ret.setLength(ret.length() - 1); // remove last space
    	return ret.toString();
    }
	

}
