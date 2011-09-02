package com.brandroid.dynapaper.Activities;

import com.brandroid.dynapaper.R;
import com.brandroid.dynapaper.WallChanger;
import com.brandroid.util.Logger;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class ProfileMakerList extends BaseActivity
{
	ListView mList;
	String[] mListOptions = new String[] { "Select Base Wallpaper", "Select Widgets", "Preview Wallpaper", "Apply Wallpaper", "Settings" };
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.maker_list);
		
		mList = (ListView)findViewById(R.id.list);
		
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1,
				mListOptions
				);
		mList.setAdapter(adapter);
		mList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {
				if(mListOptions[mList.getSelectedItemPosition()].equals("Select Base Wallpaper"))
				{
					
				} else if(mListOptions[mList.getSelectedItemPosition()].equals("Select Widgets"))
				{
					
				} else if(mListOptions[mList.getSelectedItemPosition()].equals("Preview Wallpaper"))
				{
					
				} else if(mListOptions[mList.getSelectedItemPosition()].equals("Apply Wallpaper"))
				{
					
				} else if(mListOptions[mList.getSelectedItemPosition()].equals("Settings"))
				{
					startActivity(new Intent(getApplicationContext(), Settings.class)); 
				} 
			}
		});
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add("Old UI");
		getMenuInflater().inflate(R.menu.maker, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId())
		{
		case R.id.menu_settings:
			Logger.LogInfo("Settings menu selected");
			startActivityForResult(new Intent(getApplicationContext(), Settings.class), WallChanger.REQ_SETTINGS);
			break;
		case R.id.menu_help:
			Logger.LogInfo("Help menu selected");
			startActivity(new Intent(getApplicationContext(), Help.class));
			break;
		case R.id.menu_feedback:
			Logger.LogInfo("Feedback menu selected");
			startActivity(new Intent(getApplicationContext(), Feedback.class));
			break;
		default:
			if(item.getTitle().equals("Old UI"))
				startActivity(new Intent(getApplicationContext(), ProfileMaker.class));
			break;
		}
		return super.onOptionsItemSelected(item);
	}

}
