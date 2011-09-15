package com.brandroid.dynapaper.Activities;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import com.brandroid.dynapaper.BrandroidBase;
import com.brandroid.dynapaper.R;
import com.brandroid.dynapaper.WallChanger;
import com.brandroid.util.Logger;
import com.brandroid.widgets.SeparatedListAdapter;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class ProfileMakerList extends ListFragment implements OnItemClickListener
{
	ListView mList;
    private String[] groups = { };
    private String[][] children = { };
    private ProfileMakerMain mMain;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.maker_list, null);
		FrameLayout adLayout = (FrameLayout)v.findViewById(R.id.adLayout);
		BaseActivity.addAds(getActivity(), adLayout);
		mMain = (ProfileMakerMain)getActivity();
		return v;
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		groups = new String[] { getResourceString(R.string.s_select_base),
			getResourceString(R.string.lbl_overlay),
			"Execute",
			getResourceString(R.string.menu_settings)
 		};
		children = new String[][] {
	            { "Current", "Gallery", "Online" },
	            { "Weather" },
	            { "Test", "Apply" },
	            { "Settings", "Old UI", "Feedback" }
	    	};
		
		SeparatedListAdapter adapter = new SeparatedListAdapter(getActivity(), R.layout.list_header_row);
		mList = getListView();
		for(int i = 0; i < groups.length; i++)
			adapter.addSection(groups[i], new ArrayAdapter<String>(getActivity(), R.layout.list_row, children[i]));

		setListAdapter(adapter);
		mList.setOnItemClickListener(this);
		
		//mList.setOnGroupClickListener(this);
		//mList.setOnChildClickListener(this);
	}

	public boolean onGroupClick(ExpandableListView parent, View v,
			int groupPosition, long id) {
		Logger.LogInfo("Clicked group " + groupPosition);
		return false;
	}

	public Drawable getWallpaper()
	{
		return getActivity().getBaseContext().getWallpaper();
	}

	public void onItemClick(AdapterView<?> adapter, View v, int childPosition, long id)
	{
		int groupPosition = 0;
		while(groupPosition < children.length && childPosition > children[groupPosition].length)
		{
			childPosition -= children[groupPosition].length + 1;
			groupPosition++;
		}
		childPosition--;
		Logger.LogInfo("Clicked child " + groupPosition + " : " + childPosition);
		if(groupPosition == 0) // Background
		{
			if(childPosition == 0) // Current
			{
				mMain.ShowPreview(((BitmapDrawable)getWallpaper()).getBitmap());
			} else if(childPosition == 1) // Gallery
			{
				mMain.showToast("Coming soon");
			} else if(childPosition == 2) // Online
			{
				mMain.ShowDetailFragment(new GalleryFragment(), "online");
			}
		} else if (groupPosition == 1) // Widgets
		{
			mMain.showToast("Coming soon");
		} else if (groupPosition == 2) // Execute
		{
			if(childPosition == 0) // Test
			{
				
				//mMain.showToast("Coming soon");
			} else if (childPosition == 1) // Apply
			{
				mMain.showToast("Coming soon");
			}
			
		} else if (groupPosition == 3) // Settings
		{
			if(childPosition == 0) // Settings
			{
				((ProfileMakerMain)getActivity()).ShowDetailFragment(new Settings(), "settings");
			} else if(childPosition == 1) // Old UI
			{
				startActivity(new Intent(getActivity().getApplicationContext(), ProfileMaker.class));
			} else if(childPosition == 2) // Feedback
			{
				((ProfileMakerMain)getActivity()).ShowDetailFragment(new Feedback(), "feedback");
			}
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
	

    /**
     * A simple adapter which maintains an ArrayList of photo resource Ids. 
     * Each photo is displayed as an image. This adapter supports clearing the
     * list of photos and adding a new photo.
     *
     */
    public class MyExpandableListAdapter extends BaseExpandableListAdapter {
        // Sample data set.  children[i] contains the children (String[]) for groups[i].
        
        public Object getChild(int groupPosition, int childPosition) {
            return children[groupPosition][childPosition];
        }

        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        public int getChildrenCount(int groupPosition) {
            return children[groupPosition].length;
        }

        public TextView getGenericView() {
            // Layout parameters for the ExpandableListView
            AbsListView.LayoutParams lp = new AbsListView.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, 64);

            TextView textView = new TextView(getActivity());
            textView.setLayoutParams(lp);
            textView.setTextSize(30f);
            // Center the text vertically
            textView.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
            // Set the text starting position
            textView.setPadding(36, 0, 0, 0);
            return textView;
        }
        
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild,
                View convertView, ViewGroup parent) {
            TextView textView = getGenericView();
            textView.setTextSize(20f);
            textView.setHeight(20);
            textView.setText(getChild(groupPosition, childPosition).toString());
            return textView;
        }

        public Object getGroup(int groupPosition) {
            return groups[groupPosition];
        }

        public int getGroupCount() {
            return groups.length;
        }

        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        public View getGroupView(int groupPosition, boolean isExpanded, View convertView,
                ViewGroup parent) {
            TextView textView = getGenericView();
            textView.setText(getGroup(groupPosition).toString());
            return textView;
        }

        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }

        public boolean hasStableIds() {
            return true;
        }

    }

}
