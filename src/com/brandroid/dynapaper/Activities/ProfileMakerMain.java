package com.brandroid.dynapaper.Activities;

import java.io.FileNotFoundException;
import java.io.IOException;

import com.brandroid.dynapaper.R;
import com.brandroid.dynapaper.WallChanger;
import com.brandroid.util.Logger;
import com.brandroid.util.MediaUtils;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuItem;

public class ProfileMakerMain extends BaseActivity
{
	private boolean mUseMultiplePanes = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.wallchanger);
		MediaUtils.init(getApplicationContext());
		try {
		if(!MediaUtils.writeFile("pewp", new byte[] { }, true))
			Logger.LogWarning("Unable to write to pewp");
		else
			Logger.LogInfo("Wrote to pewp!");
		} catch(IOException fnfe) { Logger.LogError("Unable to write to pewp!", fnfe); }
		mUseMultiplePanes = (null != findViewById(R.id.detail_container));
		if (null == savedInstanceState) {
			FragmentManager fm = getSupportFragmentManager();
			ProfileMakerList lf = new ProfileMakerList();
			FragmentTransaction ft = fm.beginTransaction();
			ft.replace(R.id.list, lf, "list");
			ft.commit();
		}
	}
	
	public void ShowDetailFragment(Fragment frag, String tag)
	{
		FragmentManager fm = getSupportFragmentManager();
		fm.enableDebugLogging(true);
		FragmentTransaction ft = fm.beginTransaction();
		if(mUseMultiplePanes)
		{
			ft.replace(R.id.detail_container, frag, tag);
		}
		else {
			ft.replace(R.id.list, frag, tag);
		    ft.addToBackStack(null);
		}
		ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
		ft.commit();
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
			ShowDetailFragment(new Feedback(), "feedback");
			//startActivity(new Intent(getApplicationContext(), Feedback.class));
			break;
		default:
			if(item.getTitle().equals("Old UI"))
				startActivity(new Intent(getApplicationContext(), ProfileMaker.class));
			break;
		}
		return super.onOptionsItemSelected(item);
	}

}
