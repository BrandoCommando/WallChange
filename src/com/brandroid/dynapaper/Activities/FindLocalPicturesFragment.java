package com.brandroid.dynapaper.Activities;

import java.io.File;
import java.util.ArrayList;

import com.brandroid.dynapaper.R;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;

public class FindLocalPicturesFragment extends Fragment implements OnClickListener
{
	private View view;
	private View mDialog;
	private TextView mStatus, mPath;
	private Button mStop;
	private GridView mGrid;
	private FindFileTask finderTask;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.find_files, null);
		mStatus = (TextView)view.findViewById(R.id.find_status);
		mPath = (TextView)view.findViewById(R.id.find_path);
		mStop = (Button)view.findViewById(R.id.find_stop);
		mDialog = (View)view.findViewById(R.id.find_dialog);
		mGrid = (GridView)view.findViewById(R.id.find_grid);
		return view;
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		mStop.setOnClickListener(this);
		finderTask = new FindFileTask();
		finderTask.execute("/mnt/sdcard/DCIM", "/mnt/sdcard/Camera/", "/mnt/sdcard/", "/mnt/ext_sdcard", "/mnt/sdcard_ext", "/mnt/usbdrive", "/");
	}
	
	public void onClick(View v) {
		if(v.equals(mStop))
		{
			finderTask.cancel(true);
		}
	}
	
	public class FindFileTask extends AsyncTask<String, String, File[]>
	{
		public Boolean isRecursive = true;
		private ArrayList<File> mHolder = new ArrayList<File>();
		
		@Override
		protected void onProgressUpdate(String... values)
		{
			if(values.length > 0)
				mStatus.setText("Searching for files: " + values[0] + " found");
			if(values.length > 1)
				mPath.setText(values[1]);
		}
		
		@Override
		protected File[] doInBackground(String... params)
		{
			for(String path : params)
			{
				publishProgress(path, "" + mHolder.size());
				File fPath = new File(path);
				if(!fPath.exists()) continue;
				if(isGood(fPath))
					mHolder.add(fPath);
				if(!fPath.isDirectory()) continue;
				checkPath(fPath);
			}
			return null;
		}
		
		private Boolean isGood(File f)
		{
			if(!f.exists()||!f.isFile()) return false;
			//String ext = f.getName().substring(f.getName().lastIndexOf(".")+1).toLowerCase();
			if(f.getName().matches("/\\.(gif|bmp|png|jpg|jpeg|tif)$/"))
				return true;
			return false;
		}
		
		private void checkPath(File dir)
		{
			publishProgress("" + mHolder.size(), dir.getAbsolutePath());
			for(String s : dir.list())
			{
				File f = new File(s);
				if(isGood(f))
				{
					mHolder.add(f);
					publishProgress("" + mHolder.size());
				}
			}
		}
		
		public File[] getFiles() { return (File[])mHolder.toArray(); }
	}
}
