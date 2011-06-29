package com.brandroid.dynapaper;

import android.app.backup.BackupAgentHelper;
import android.app.backup.SharedPreferencesBackupHelper;

public class WallBackupAgent extends BackupAgentHelper
{
	@Override
	public void onCreate() {
		SharedPreferencesBackupHelper helper = new SharedPreferencesBackupHelper(this, "default.xml");
	    addHelper("wc", helper);
	}
}
