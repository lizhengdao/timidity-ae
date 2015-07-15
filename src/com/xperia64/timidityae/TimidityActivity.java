/*******************************************************************************
 * Copyright (C) 2014 xperia64 <xperiancedapps@gmail.com>
 * 
 * Copyright (C) 1999-2008 Masanao Izumo <iz@onicos.co.jp>
 *     
 * Copyright (C) 1995 Tuukka Toivonen <tt@cgs.fi>
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.xperia64.timidityae;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.UriPermission;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.OpenableColumns;
//import com.actionbarsherlock.app.SherlockFragment;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.xperia64.timidityae.R;

public class TimidityActivity extends SherlockFragmentActivity implements FileBrowserFragment.ActionFileBackListener, PlaylistFragment.ActionPlaylistBackListener, FileBrowserDialog.FileBrowserDialogListener {
	//public static TimidityActivity staticthis;
	private MenuItem menuButton;
	private MenuItem menuButton2;
	private int mode=0;
	private FileBrowserFragment fileFrag;
	private PlayerFragment playFrag;
	private PlaylistFragment plistFrag;
	ViewPager viewPager;
	boolean needFileBack=false;
	boolean needPlaylistBack=false;
	boolean fromPlaylist=false;
	boolean needService=true;
	String currSongName;
	boolean needInit=false;
	boolean fromIntent=false;
	boolean deadlyDeath=false;
	public boolean localfinished;
	int oldTheme;
	AlertDialog alerty;
	private BroadcastReceiver activityReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
        	int cmd = intent.getIntExtra(getResources().getString(R.string.ta_cmd), -5); // -V
        	switch(cmd)
        	{
        	case -5:
        		break;
        	case 0:
        		currSongName=intent.getStringExtra(getResources().getString(R.string.ta_filename));
        		if(viewPager.getCurrentItem()==1)
        		{
	        		menuButton.setIcon(R.drawable.ic_menu_agenda);
	        		menuButton.setTitle(getResources().getString(R.string.view));
	        		menuButton.setTitleCondensed(getResources().getString(R.string.viewcon));
	        		menuButton.setVisible((!JNIHandler.type)&&Globals.isPlaying==0);
	            	menuButton.setEnabled((!JNIHandler.type)&&Globals.isPlaying==0);
	        		menuButton2.setIcon(R.drawable.ic_menu_info_details);
	        		menuButton2.setTitle(getResources().getString(R.string.playback));
	        		menuButton2.setTitleCondensed(getResources().getString(R.string.playbackcon));
	        		menuButton2.setVisible((!JNIHandler.type)&&Globals.isPlaying==0);
	            	menuButton2.setEnabled((!JNIHandler.type)&&Globals.isPlaying==0);
        		}
        		playFrag.play(intent.getIntExtra(getResources().getString(R.string.ta_startt),0), intent.getStringExtra(getResources().getString(R.string.ta_songttl)));
        		break;
        	case 1:
        		break;
        	case 2:
        		try{
        			fileFrag.getDir(intent.getStringExtra(getResources().getString(R.string.ta_currpath)));
        		}catch(IllegalStateException e){
        			
        		}
        		
        		//System.out.println(integExtrnt.getStrina(getResources().getString(R.string.ta_currpath)));
        		break;
        	case 3:
        		//System.out.println("case 3");
        		currSongName=intent.getStringExtra(getResources().getString(R.string.ta_filename));
        		if(viewPager.getCurrentItem()==1)
        		{
        			menuButton.setIcon(R.drawable.ic_menu_agenda);
	        		menuButton.setTitle(getResources().getString(R.string.view));
	        		menuButton.setTitleCondensed(getResources().getString(R.string.viewcon));
	        		menuButton.setVisible((!JNIHandler.type)&&Globals.isPlaying==0);
	            	menuButton.setEnabled((!JNIHandler.type)&&Globals.isPlaying==0);
	        		menuButton2.setIcon(R.drawable.ic_menu_info_details);
	        		menuButton2.setTitle(getResources().getString(R.string.playback));
	        		menuButton2.setTitleCondensed(getResources().getString(R.string.playbackcon));
	        		menuButton2.setVisible((!JNIHandler.type)&&Globals.isPlaying==0);
	            	menuButton2.setEnabled((!JNIHandler.type)&&Globals.isPlaying==0);
        		}
        		playFrag.play(intent.getIntExtra(getResources().getString(R.string.ta_startt),0), 
        				intent.getStringExtra(getResources().getString(R.string.ta_songttl)), 
        				intent.getIntExtra(getResources().getString(R.string.ta_shufmode), 0), 
        				intent.getIntExtra(getResources().getString(R.string.ta_loopmode),1));
        		break;
        	case 4:
        		plistFrag.currPlist=Globals.tmpplist;
        		plistFrag.getPlaylists(plistFrag.mode?plistFrag.plistName:null);
        		Globals.tmpplist=null;
        		break;
        	case 5: // Notifiy pause/stop
        		if(!intent.getBooleanExtra(getResources().getString(R.string.ta_pause), false)&&Globals.hardStop)
        		{
        			Globals.hardStop=false;
        			if(viewPager.getCurrentItem()==1)
            		{
            			menuButton.setIcon(R.drawable.ic_menu_agenda);
    	        		menuButton.setTitle(getResources().getString(R.string.view));
    	        		menuButton.setTitleCondensed(getResources().getString(R.string.viewcon));
    	        		menuButton.setVisible(false);
    	            	menuButton.setEnabled(false);
    	        		menuButton2.setIcon(R.drawable.ic_menu_info_details);
    	        		menuButton2.setTitle(getResources().getString(R.string.playback));
    	        		menuButton2.setTitleCondensed(getResources().getString(R.string.playbackcon));
    	        		menuButton2.setVisible(false);
    	            	menuButton2.setEnabled(false);
            		}
        			playFrag.setInterface(0);
        			TimidityActivity.this.runOnUiThread(new Runnable() {
				        public void run() {
        			if(playFrag.ddd!=null)
        			{
        				if(playFrag.ddd.isShowing())
        				{
        					playFrag.ddd.dismiss();
        					playFrag.ddd=null;
        				}
        			}
        			if(alerty!=null)
        			{
        				if(alerty.isShowing())
        				{
        					alerty.dismiss();
        					alerty=null;
        				}
        			}
				        }
        		});
        		}
        		playFrag.pauseStop(intent.getBooleanExtra(getResources().getString(R.string.ta_pause), false), 
        				intent.getBooleanExtra(getResources().getString(R.string.ta_pausea),false));
        		break;
        	case 6: // notify art
        		//currSongName = intent.getStringExtra(getResources().getString(R.string.ta_filename));
        		if(playFrag!=null)
        			playFrag.setArt();
        		break;
        	case 7:
        		fileFrag.localfinished=true;
        		break;
        	case 8:
        		localfinished=true;
        		break;

        		
        	}
        }
    };
    
   /* @Override
    protected void onPause()
    {
    	
    }
    
    */@Override
    protected void onResume()
    {
    	deadlyDeath=false;
    	super.onResume();
    }
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntentData(intent);
    }
	@SuppressLint("InlinedApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		deadlyDeath=false;
		if(savedInstanceState==null)
		{
			Globals.reloadSettings(this, getAssets());
		}else{
			if(!savedInstanceState.getBoolean("justtheme", false))
			{
				Globals.reloadSettings(this, getAssets());
			}
		}
		try {
    		System.loadLibrary("timidityhelper");   
    	}
        catch( UnsatisfiedLinkError e) {
        	Log.i("Bad:","Cannot grab timidity");
        	Globals.nativeMidi = Globals.onlyNative=true;
        }
		oldTheme = Globals.theme;
		if (Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH){
	        this.setTheme((Globals.theme==1)?com.actionbarsherlock.R.style.Theme_Sherlock_Light_DarkActionBar:com.actionbarsherlock.R.style.Theme_Sherlock);
	   }else{
		   this.setTheme((Globals.theme==1)?android.R.style.Theme_Holo_Light_DarkActionBar:android.R.style.Theme_Holo);
	   }
	super.onCreate(savedInstanceState);
	if(savedInstanceState==null)
	{
		Log.i("Timidity","Initializing");
		needInit=Globals.initialize(TimidityActivity.this);
	}else{
		Log.i("Timidity","Resuming...");
		needService=!isMyServiceRunning(MusicService.class);
		Fragment tmp = getSupportFragmentManager().getFragment(savedInstanceState,"playfrag");
		if(tmp!=null)
			playFrag = (PlayerFragment) tmp;
		
		tmp = getSupportFragmentManager().getFragment(savedInstanceState,"plfrag");
		if(tmp!=null)
			plistFrag = (PlaylistFragment) tmp;
		tmp = getSupportFragmentManager().getFragment(savedInstanceState,"fffrag");
		if(tmp!=null)
			fileFrag = (FileBrowserFragment) tmp;
		if(!isMyServiceRunning(MusicService.class))
		{
			Globals.reloadSettings(this, getAssets());
			initCallback2();
			if(viewPager!=null)
			{
				if(viewPager.getCurrentItem()==1)
				{
					viewPager.setCurrentItem(0);
				}
			}
		}
		/*if(!savedInstanceState.getBoolean("justtheme", false))
		{
			Globals.reloadSettings(this, getAssets());
		}*/
		
	}
	/*IntentFilter filter = new IntentFilter();
	filter.addAction("com.xperia64.timidityae20.ACTION_STOP");
	filter.addAction("com.xperia64.timidityae20.ACTION_PAUSE");
	filter.addAction("com.xperia64.timidityae20.ACTION_NEXT");
	filter.addAction("com.xperia64.timidityae20.ACTION_PREV");*/
	//registerReceiver(receiver, filter);

	setContentView(R.layout.main);
	
	
	if (activityReceiver != null) {
		//Create an intent filter to listen to the broadcast sent with the action "ACTION_STRING_ACTIVITY"
		            IntentFilter intentFilter = new IntentFilter(getResources().getString(R.string.ta_rec));
		//Map the intent filter to the receiver
		            registerReceiver(activityReceiver, intentFilter);
		        }

		//Start the service on launching the application
		        if(needService)
		        {
		        	needService=false;
		        	Globals.probablyFresh=0;
		        	//System.out.println("Starting service");
		        	startService(new Intent(this, MusicService.class));
		        }
		        
	viewPager = (ViewPager) findViewById(R.id.vp_main);
	viewPager.setAdapter(new TimidityFragmentPagerAdapter());
	viewPager.addOnPageChangeListener(new OnPageChangeListener() {

        @Override
        public void onPageSelected(int index) {
        	mode=index;
        	switch(index)
        	{       	
        	case 0:
        		fromPlaylist=false;
        		if(getSupportActionBar()!=null)
				{
        			if(menuButton!=null)
        			{
        				menuButton.setIcon(R.drawable.ic_menu_refresh);
        				menuButton.setVisible(true);
        				menuButton.setEnabled(true);
        				menuButton.setTitle(getResources().getString(R.string.refreshfld));
        				menuButton.setTitleCondensed(getResources().getString(R.string.refreshcon));
        			}
        			if(menuButton2!=null)
        			{
        				menuButton2.setIcon(R.drawable.ic_menu_home);
        				menuButton2.setTitle(getResources().getString(R.string.homefld));
        				menuButton2.setTitleCondensed(getResources().getString(R.string.homecon));
        				menuButton2.setVisible(true);
        				menuButton2.setEnabled(true);
        			}
					getSupportActionBar().setDisplayHomeAsUpEnabled(needFileBack);
				}else{
					getSupportActionBar().setDisplayHomeAsUpEnabled(false);
					getSupportActionBar().setHomeButtonEnabled(false);
				}
        		if(fileFrag!=null)
        			if(fileFrag.getListView()!=null)
        				fileFrag.getListView().setFastScrollEnabled(true);
        		break;
        	case 1:
        		if(getSupportActionBar()!=null)
				{
        			if(menuButton!=null)
        			{
        				menuButton.setIcon(R.drawable.ic_menu_agenda);
        				menuButton.setTitle(getResources().getString(R.string.view));
        				menuButton.setTitleCondensed(getResources().getString(R.string.viewcon));
        				menuButton.setVisible((!JNIHandler.type)&&Globals.isPlaying==0);
            			menuButton.setEnabled((!JNIHandler.type)&&Globals.isPlaying==0);
        			}
        			if(menuButton2!=null)
        			{
        				menuButton2.setIcon(R.drawable.ic_menu_info_details);
        				menuButton2.setTitle(getResources().getString(R.string.playback));
        				menuButton2.setTitleCondensed(getResources().getString(R.string.playbackcon));
        				menuButton2.setVisible((!JNIHandler.type)&&Globals.isPlaying==0);
            			menuButton2.setEnabled((!JNIHandler.type)&&Globals.isPlaying==0);
        			}
        			getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        			getSupportActionBar().setHomeButtonEnabled(false);
				}
        		break;
        	case 2:
        		fromPlaylist=true;
        		if(getSupportActionBar()!=null)
				{
        			if(menuButton!=null)
        			{
        			menuButton.setIcon(R.drawable.ic_menu_refresh);
        			menuButton.setTitle(getResources().getString(R.string.refreshpls));
        			menuButton.setTitleCondensed(getResources().getString(R.string.refreshcon));
        			menuButton.setVisible(true);
            		menuButton.setEnabled(true);
        			}
        			if(menuButton2!=null)
        			{
        				menuButton2.setIcon(R.drawable.ic_menu_add);
        				menuButton2.setTitle(getResources().getString(R.string.add));
        				menuButton2.setTitleCondensed(getResources().getString(R.string.addcon));
        				if(plistFrag!=null)
        				{
        					menuButton2.setVisible((plistFrag.plistName!=null&&plistFrag.mode)?!plistFrag.plistName.equals("CURRENT"):true);
        					menuButton2.setEnabled((plistFrag.plistName!=null&&plistFrag.mode)?!plistFrag.plistName.equals("CURRENT"):true);
        				}
        			}
            		if(plistFrag!=null)
            			if(plistFrag.getListView()!=null)
            				plistFrag.getListView().setFastScrollEnabled(true);
				if(needPlaylistBack)
				{
					getSupportActionBar().setDisplayHomeAsUpEnabled(true);
				}
				}else{
					getSupportActionBar().setDisplayHomeAsUpEnabled(false);
					getSupportActionBar().setHomeButtonEnabled(false);
				}
        		break;
        	}	
        	
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {

        }

        @Override
        public void onPageScrollStateChanged(int arg0) {

        }
    });
	}
	@SuppressLint("NewApi")
	public void initCallback()
	{
		if(Build.VERSION.SDK_INT>=android.os.Build.VERSION_CODES.LOLLIPOP)
		{
			List<UriPermission> permissions = getContentResolver().getPersistedUriPermissions();
			int trueExt=0;
			for(File f : getExternalFilesDirs(null))
			{
				if(f!=null)
					trueExt++;
			}
			if((permissions==null||permissions.isEmpty())&&Globals.shouldLolNag&&trueExt>1)
			{
				new AlertDialog.Builder(this).setTitle("SD Card Access").setCancelable(false)
				.setMessage("Would you like to give Timidity AE write access to your external sd card? This is recommended if you're converting files or would like to place Timidity AE's data directory there. Problems may occur if a directory other than the root of your SD card is selected.")
				.setPositiveButton("Yes", new OnClickListener(){

					@Override
					public void onClick(DialogInterface dialog, int which) {
						Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
					    startActivityForResult(intent, 42);
					}
					
				})
				.setNegativeButton("No, do not ask again", new OnClickListener(){

					@Override
					public void onClick(DialogInterface dialog, int which) {
						Globals.prefs.edit().putBoolean("shouldLolNag", Globals.shouldLolNag=false).commit();
						initCallback2();
					}
					
				}).setNeutralButton("No", new OnClickListener(){

					@Override
					public void onClick(DialogInterface dialog, int which) {
						initCallback2();
					}
				}).show();
			}else{
				for(UriPermission permission : permissions)
				{
					if(permission.isReadPermission()&&permission.isWritePermission())
					{
						Globals.theFold=permission.getUri();
					}
				}
				
				initCallback2();
			}
		}else{
			initCallback2();
		}
	}
	public void initCallback2()
	{
		int x = JNIHandler.init(Globals.dataFolder+"timidity/","timidity.cfg", Globals.mono, Globals.defSamp, Globals.sixteen, Globals.buff, Globals.aRate, false);
		if(x!=0&&x!=-99)
		{
			Globals.nativeMidi=true;
			Toast.makeText(this, String.format(getResources().getString(R.string.tcfg_error), x), Toast.LENGTH_LONG).show();
		}
		handleIntentData(getIntent());
	}
	public void handleIntentData(Intent in)
	{
		if(in.getData()!=null)
		{
			String data;
			if((data=in.getData().getPath())!=null&&in.getData().getScheme()!=null)
			{
				if(in.getData().getScheme().equals("file"))
				{
					if(new File(data).exists())
					{
						File f = new File(data.substring(0,data.lastIndexOf('/')+1));
						if(f.exists())
						{
							if(f.isDirectory())
							{
								ArrayList<String> files = new ArrayList<String>();
								int position=-1;
								int goodCounter=0;
								for(File ff: f.listFiles())
								{
									if(ff!=null&&ff.isFile())
									{
										int dotPosition = ff.getName().lastIndexOf('.');
										String extension="";
										if (dotPosition != -1) 
										{
											extension = (ff.getName().substring(dotPosition)).toLowerCase(Locale.US);
											if(extension!=null)
											{
												if((Globals.showVideos?Globals.musicVideoFiles:Globals.musicFiles).contains("*"+extension+"*"))
												{
															
													files.add(ff.getPath());
													if(ff.getPath().equals(data))
														position=goodCounter;
													goodCounter++;
												}
											}
										}
									}
								}
								if(position==-1)
									Toast.makeText(this, getResources().getString(R.string.intErr1), Toast.LENGTH_SHORT).show();
								else{
								selectedSong(files,position,true,false,false);
								fileFrag.getDir(data.substring(0,data.lastIndexOf('/')+1));
								}
							}
						}
					}else{
						Toast.makeText(this, getResources().getString(R.string.srv_fnf),Toast.LENGTH_SHORT).show();
					}
				}else if(in.getData().getScheme().equals("http")||in.getData().getScheme().equals("https")){
					if(!data.endsWith("/"))
					{
						if(!Globals.getExternalCacheDir(this).exists())
						{
							Globals.getExternalCacheDir(this).mkdirs();
						}
						final Globals.DownloadTask downloadTask = new Globals.DownloadTask(this);
						downloadTask.execute(in.getData().toString(), in.getData().getLastPathSegment());
						in.setData(null);
					}else{Toast.makeText(this, "This is a directory, not a file",Toast.LENGTH_SHORT).show();}
				}else if(in.getData().getScheme().equals("content")&&(data.contains("downloads")))
				{
					String filename = null;
					Cursor cursor = null;
					try {
					    cursor = this.getContentResolver().query(in.getData(), new String[] {
					        OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE}, null, null, null );
					    if (cursor != null && cursor.moveToFirst()) {
					        filename = cursor.getString(0);
					    }
					} finally {
					    if (cursor != null)
					        cursor.close();
					}
					try
					{
						InputStream input = getContentResolver().openInputStream(in.getData());
						if(new File(Globals.getExternalCacheDir(this).getAbsolutePath()+'/'+filename).exists())
						{
							new File(Globals.getExternalCacheDir(this).getAbsolutePath()+'/'+filename).delete();
						}
						OutputStream output = new FileOutputStream(Globals.getExternalCacheDir(this).getAbsolutePath()+'/'+filename);

			            byte[] buffer = new byte[4096];
			            int count;
			            while ((count = input.read(buffer)) != -1) {
			                output.write(buffer, 0, count);
			            }
			            output.close();
					} catch (IOException e)
					{
						e.printStackTrace();
						return;
					}
				
					
					File f = new File(Globals.getExternalCacheDir(this).getAbsolutePath()+'/');
					if(f.exists())
					{
						if(f.isDirectory())
						{
							ArrayList<String> files = new ArrayList<String>();
							int position=-1;
							int goodCounter=0;
							for(File ff: f.listFiles())
							{
								if(ff!=null&&ff.isFile())
								{
									int dotPosition = ff.getName().lastIndexOf('.');
									String extension="";
									if (dotPosition != -1) 
									{
										extension = (ff.getName().substring(dotPosition)).toLowerCase(Locale.US);
										if(extension!=null)
										{
											if((Globals.showVideos?Globals.musicVideoFiles:Globals.musicFiles).contains("*"+extension+"*"))
											{
														
												files.add(ff.getPath());
												if(ff.getPath().equals(Globals.getExternalCacheDir(this).getAbsolutePath()+'/'+filename))
													position=goodCounter;
												goodCounter++;
											}
										}
									}
								}
							}
							if(position==-1)
								Toast.makeText(this, getResources().getString(R.string.intErr1), Toast.LENGTH_SHORT).show();
							else{
							selectedSong(files,position,true,false,false);
							fileFrag.getDir(Globals.getExternalCacheDir(this).getAbsolutePath());
							}
						}
					}
					
				}else{
					System.out.println(in.getDataString());
					Toast.makeText(this, getResources().getString(R.string.intErr2)+" ("+in.getData().getScheme()+")",Toast.LENGTH_SHORT).show();
				}
			}
		}
	}
	@SuppressLint("NewApi")
	public void downloadFinished(String data, String theFilename)
	{
		ArrayList<String> files = new ArrayList<String>();
		String name = Globals.getExternalCacheDir(this).getAbsolutePath()+'/'+theFilename;
		int dotPosition = name.lastIndexOf('.');
		String extension="";
		if (dotPosition != -1) 
		{
			extension = (name.substring(dotPosition)).toLowerCase(Locale.US);
			if(extension!=null)
			{
				if((Globals.showVideos?Globals.musicVideoFiles:Globals.musicFiles).contains("*"+extension+"*"))
				{
							
					files.add(name);
					selectedSong(files,0,true,false,false);
					fileFrag.getDir(name.substring(0,name.lastIndexOf('/')+1));
				}
			}
		}
	}
	@Override
	public void onDestroy()
	{
		try{
		unregisterReceiver(activityReceiver);
		}catch(IllegalArgumentException e)
		{
			
		}
		super.onDestroy();
		//if(deadlyDeath)
			//System.exit(0);
	}
	private boolean isMyServiceRunning(Class<?> serviceClass) {
	    ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
	    for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
	        if (serviceClass.getName().equals(service.service.getClassName())) {
	            return true;
	        }
	    }
	    return false;
	}
	@Override
	protected void onSaveInstanceState(Bundle icicle) {
		  super.onSaveInstanceState(icicle);
		  icicle.putBoolean("justtheme", true);
		  if(playFrag!=null)
			  getSupportFragmentManager().putFragment(icicle,"playfrag",playFrag);
		  if(plistFrag!=null)
			  getSupportFragmentManager().putFragment(icicle,"plfrag",plistFrag);
		  if(fileFrag!=null)
			  getSupportFragmentManager().putFragment(icicle,"fffrag",fileFrag);
		}
	@Override
	  public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getSupportMenuInflater();
	    inflater.inflate(R.menu.main_menu, menu);
	    menuButton=menu.findItem(R.id.add);
	    menuButton2=menu.findItem(R.id.subtract);
	    return true;
	  } 
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item)
	{
		if(item.getItemId()==R.id.add)
		{
			//System.out.println("add");
			switch(mode)
			{
			case 0:
				fileFrag.getDir(fileFrag.currPath);
				break;
			case 1:
				if(playFrag!=null&&!JNIHandler.type)
				{
					playFrag.incrementInterface();
				}/*else{
					Toast.makeText(this, "Non midi file", Toast.LENGTH_SHORT).show();
				}*/
				break;
			case 2:
				plistFrag.getPlaylists(plistFrag.mode?plistFrag.plistName:null);
				break;
			}
		}else if(item.getItemId()==R.id.subtract){
			//System.out.println("subtract");
			switch(mode)
			{
			case 0:
				if(fileFrag!=null)
					fileFrag.getDir(Globals.defaultFolder);
				break;
			case 1:
				if(playFrag!=null)
				{
					if((!JNIHandler.type)&&Globals.isPlaying==0)
					{
					playFrag.showMidiDialog();
					}/*else{
						Toast.makeText(this, "Non midi file", Toast.LENGTH_SHORT).show();
					}*/
				}
				break;
			case 2:
				plistFrag.add();
				break;
				
			}
		}else if(item.getItemId()==android.R.id.home)
		{
			 onBackPressed();
		}else if(item.getItemId()==R.id.quit)
		{
			deadlyDeath=true;
			Intent new_intent = new Intent();
		    new_intent.setAction(getResources().getString(R.string.msrv_rec));
		    new_intent.putExtra(getResources().getString(R.string.msrv_cmd), 5);
		    sendBroadcast(new_intent);
			stopService(new Intent(this, MusicService.class));
		    unregisterReceiver(activityReceiver);
		    android.os.Process.killProcess(android.os.Process.myPid());
			//System.exit(0);
		}else if(item.getItemId()==R.id.asettings)
		{
			Intent mainact = new Intent(this, SettingsActivity.class);
            mainact.setFlags( Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivityForResult( mainact, 1 );
		}else if(item.getItemId()==R.id.ahelp)
		{
			new AlertDialog.Builder(this).setTitle(getResources().getString(R.string.helpt))
			.setMessage(getResources().getString(R.string.helper)).setNegativeButton("OK", new OnClickListener(){

				@Override
				public void onClick(DialogInterface dialog, int which) {
					
				}
				
			}).show();
		}
		return super.onMenuItemSelected(featureId, item);
	}
	@Override
	public void onBackPressed()
	{
		
		switch(mode)
		{
		case 0:
			if(fileFrag!=null)
				if(fileFrag.currPath!=null)
					if(!fileFrag.currPath.matches("[/]+"))
					{
						fileFrag.getDir(new File(fileFrag.currPath).getParent().toString());
					}else
					{
						if(Globals.useDefaultBack)
						{
							super.onBackPressed();
							return;
						}
						viewPager.setCurrentItem(1);
					}
			break;
		case 1:
			if(Globals.useDefaultBack)
			{
				super.onBackPressed();
				return;
			}
			viewPager.setCurrentItem((fromPlaylist)?2:0);
			break;
		case 2:
			if(plistFrag.mode)
				plistFrag.getPlaylists(null);
			else
			{
				if(Globals.useDefaultBack)
				{
					super.onBackPressed();
					return;
				}
				viewPager.setCurrentItem(1);
			}
			break;
		}
	}
	public void selectedSong(ArrayList<String> files, int songNumber, boolean begin, boolean loc, boolean dontloadplist)
	{
		fromPlaylist=loc;
		if(viewPager!=null)
			viewPager.setCurrentItem(1);
			Globals.plist=files;
		if(plistFrag!=null&&!dontloadplist)
			plistFrag.currPlist=files;
		Intent new_intent = new Intent();
	    new_intent.setAction(getResources().getString(R.string.msrv_rec));
	    new_intent.putExtra(getResources().getString(R.string.msrv_cmd), 5);
	    sendBroadcast(new_intent);
		new_intent = new Intent();
	    new_intent.setAction(getResources().getString(R.string.msrv_rec));
	    new_intent.putExtra(getResources().getString(R.string.msrv_cmd), 0);
	   
	    if(fileFrag!=null)
	    	new_intent.putExtra(getResources().getString(R.string.msrv_currfold),fileFrag.currPath);
	    new_intent.putExtra(getResources().getString(R.string.msrv_songnum), songNumber);
	    new_intent.putExtra(getResources().getString(R.string.msrv_begin), begin);
	    new_intent.putExtra(getResources().getString(R.string.msrv_dlplist), dontloadplist);
	    sendBroadcast(new_intent);
	    //System.out.println("sent bradcast");
	}
	public class TimidityFragmentPagerAdapter extends FragmentPagerAdapter {
	final String[] pages = {"Files", "Player", "Playlists"};
	public TimidityFragmentPagerAdapter() {
	super(getSupportFragmentManager());
	}
	@Override
	public int getCount() {
	return pages.length;
	}
	@Override
	public Fragment getItem(int position) {
		switch(position)
		{
		case 0:
			fileFrag = FileBrowserFragment.create(Globals.defaultFolder);
			return fileFrag;
		case 1:
			playFrag = PlayerFragment.create();
			return playFrag;
		case 2:
			//System.out.println("creationist");
			plistFrag = PlaylistFragment.create(Globals.dataFolder+"playlists/");
			return plistFrag;
		default:
			return null;//PageFragment.create(position + 1);
		}
	
	}
	@Override
	public CharSequence getPageTitle(int position) {
		return pages[position];
		}
	}
	/*public static class PageFragment extends SherlockFragment {
	public static final String ARG_PAGE = "ARG_PAGE";
	//private int mPage;
	public static PageFragment create(int page) {
	Bundle args = new Bundle();
	args.putInt(ARG_PAGE, page);
	PageFragment fragment = new PageFragment();
	fragment.setArguments(args);
	return fragment;
	}
	@Override
	public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	//mPage = getArguments().getInt(ARG_PAGE);
	}
	/*@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	Bundle savedInstanceState) {
		View view = null;
		switch(mPage)
		{
		case 1: // Files
			//view = inflater.inflate(R.layout.filebrowser, container, false);
			break;
		case 2: //
			break;
		case 3: // Playlists
			break;
		default:
			view = inflater.inflate(R.layout.fragment_page, container, false);
			TextView textView = (TextView) view;
			textView.setText("Fragment #" + mPage);
			break;
		}
	
	return view;
	}*//*
	
	}*/
	@Override
	public void needFileBackCallback(boolean yes) {
		needFileBack=yes;
		if(getSupportActionBar()!=null)
		{
			if(viewPager.getCurrentItem()==0)
			{
				if(needFileBack)
				{
					getSupportActionBar().setDisplayHomeAsUpEnabled(true);
				}else{
					getSupportActionBar().setDisplayHomeAsUpEnabled(false);
					getSupportActionBar().setHomeButtonEnabled(false);
				}
			}
		}
	}
	@Override
	public void needPlaylistBackCallback(boolean yes, boolean current) {
		needPlaylistBack=yes;
		if(getSupportActionBar()!=null)
		{
			if(viewPager.getCurrentItem()==2)
			{
				if(needPlaylistBack)
				{
					getSupportActionBar().setDisplayHomeAsUpEnabled(true);
					menuButton2.setVisible(!current);
					menuButton2.setEnabled(!current);
				}else{
					menuButton2.setVisible(true);
					menuButton2.setEnabled(true);
					getSupportActionBar().setDisplayHomeAsUpEnabled(false);
					getSupportActionBar().setHomeButtonEnabled(false);
				}
			}
		}
	}
	/*@Override
	public ArrayList<String> getCurrentPlaylist() {
		return Globals.plist;
	}*/
	// Broadcast actions
	// This is painful.
	public void play()
	{
		Intent new_intent = new Intent();
	    new_intent.setAction(getResources().getString(R.string.msrv_rec));
	    new_intent.putExtra(getResources().getString(R.string.msrv_cmd), 1);
	    sendBroadcast(new_intent);
	}
	public void pause()
	{
		Intent new_intent = new Intent();
	    new_intent.setAction(getResources().getString(R.string.msrv_rec));
	    new_intent.putExtra(getResources().getString(R.string.msrv_cmd), 2);
	    sendBroadcast(new_intent);
	}
	public void next()
	{
		Intent new_intent = new Intent();
	    new_intent.setAction(getResources().getString(R.string.msrv_rec));
	    new_intent.putExtra(getResources().getString(R.string.msrv_cmd), 3);
	    sendBroadcast(new_intent);
	}
	public void prev()
	{
		Intent new_intent = new Intent();
	    new_intent.setAction(getResources().getString(R.string.msrv_rec));
	    new_intent.putExtra(getResources().getString(R.string.msrv_cmd), 4);
	    sendBroadcast(new_intent);
	}
	public void stop()
	{
		Intent new_intent = new Intent();
	    new_intent.setAction(getResources().getString(R.string.msrv_rec));
	    new_intent.putExtra(getResources().getString(R.string.msrv_cmd), 5);
	    sendBroadcast(new_intent);
	}
	public void loop(int mode)
	{
		Intent new_intent = new Intent();
	    new_intent.setAction(getResources().getString(R.string.msrv_rec));
	    new_intent.putExtra(getResources().getString(R.string.msrv_cmd), 6);
	    new_intent.putExtra(getResources().getString(R.string.msrv_loopmode), mode);
	    sendBroadcast(new_intent);
	}
	public void shuffle(int mode)
	{
		Intent new_intent = new Intent();
	    new_intent.setAction(getResources().getString(R.string.msrv_rec));
	    new_intent.putExtra(getResources().getString(R.string.msrv_cmd), 7);
	    new_intent.putExtra(getResources().getString(R.string.msrv_shufmode), mode);
	    sendBroadcast(new_intent);
	}
	public void seek(int time)
	{
		Intent new_intent = new Intent();
	    new_intent.setAction(getResources().getString(R.string.msrv_rec));
	    new_intent.putExtra(getResources().getString(R.string.msrv_cmd), 9);
	    new_intent.putExtra(getResources().getString(R.string.msrv_seektime), time);
	    sendBroadcast(new_intent);
	}
	public void writeFile(String input, String output)
	{
		Intent new_intent = new Intent();
	    new_intent.setAction(getResources().getString(R.string.msrv_rec));
	    new_intent.putExtra(getResources().getString(R.string.msrv_cmd), 14);
	    new_intent.putExtra(getResources().getString(R.string.msrv_infile), input);
	    new_intent.putExtra(getResources().getString(R.string.msrv_outfile), output);
	    sendBroadcast(new_intent);
	}
	@SuppressLint("NewApi")
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	    // Check which request we're responding to
	    if (requestCode == 1) {
	        if(oldTheme!=Globals.theme)
	        {
	        	Intent intent = getIntent();
	        	intent.putExtra("justtheme", true);
	        	intent.putExtra("needservice", false);
	            finish();
	            startActivity(intent);	
	        }
	       
	    }else if(requestCode==42)
	    {
	    	 if (resultCode == RESULT_OK) {
	    	        Uri treeUri = data.getData();
	    	        getContentResolver().takePersistableUriPermission(treeUri,
	    	                Intent.FLAG_GRANT_READ_URI_PERMISSION |
	    	                Intent.FLAG_GRANT_WRITE_URI_PERMISSION);  
	    	    }
	    	 initCallback2();
	    }
	}
	public void readyForInit() {
		if(needInit)
			initCallback();
	}
	public void dynExport()
	{
	    localfinished=false;
		if(Globals.isMidi(currSongName)&&Globals.isPlaying==0)
		{
			
		
		AlertDialog.Builder alert = new AlertDialog.Builder(this);

		alert.setTitle("Convert to WAV File");
		alert.setMessage("Exports the MIDI/MOD file to WAV.\nNative Midi must be disabled in settings.\nWarning: WAV files are large.");
		InputFilter filter = new InputFilter() { 
	        public CharSequence filter(CharSequence source, int start, int end, 
	Spanned dest, int dstart, int dend) { 
	                for (int i = start; i < end; i++) { 
	                	String IC = "*/*\n*\r*\t*\0*\f*`*?***\\*<*>*|*\"*:*";
	                        if (IC.contains("*"+source.charAt(i)+"*")) { 
	                                return ""; 
	                        } 
	                } 
	                return null; 
	        } 
		};
		// Set an EditText view to get user input 
		final EditText input = new EditText(this);
		input.setFilters(new InputFilter[]{filter});
		alert.setView(input);

		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog, int whichButton) {
		  String value = input.getText().toString();
		  if(!value.toLowerCase(Locale.US).endsWith(".wav"))
			  value+=".wav";
		  String parent=currSongName.substring(0,currSongName.lastIndexOf('/')+1);
		  boolean alreadyExists = new File(parent+value).exists();
		  boolean aWrite=true;
		  String needRename = null;
		  String probablyTheRoot = "";
		  String probablyTheDirectory = "";
		  try{
		        new FileOutputStream(parent+value,true).close();
		  }catch(FileNotFoundException e)
		  {
			aWrite=false;  
		  } catch (IOException e)
		{
			e.printStackTrace();
		}
		  if(aWrite&&!alreadyExists)
			  new File(parent+value).delete();
		  if(aWrite&&new File(parent).canWrite())
		  {
			  value=parent+value;
		  }else if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP&& Globals.theFold!=null)
		  {
			  //TODO
			  // Write the file to getExternalFilesDir, then move it with the Uri
			  // We need to tell JNIHandler that movement is needed.
			  
			  String[] tmp = Globals.getDocFilePaths(TimidityActivity.this,parent);
			  probablyTheDirectory = tmp[0];
			  probablyTheRoot = tmp[1];
			if(probablyTheDirectory.length()>1)
			{
				needRename = parent.substring(parent.indexOf(probablyTheRoot)+probablyTheRoot.length())+value;
				value = probablyTheDirectory+'/'+value;
			}else{
				value=Environment.getExternalStorageDirectory().getAbsolutePath()+'/'+value;
			}
		  }else{
			  value=Environment.getExternalStorageDirectory().getAbsolutePath()+'/'+value;
		  }
		  final String finalval = value;
		  final boolean canWrite = aWrite;
		  final String needToRename = needRename;
		  final String probRoot = probablyTheRoot;
		  if(new File(finalval).exists()||(new File(probRoot+needRename).exists()&&needToRename!=null))
		  {
			  AlertDialog dialog2 = new AlertDialog.Builder(TimidityActivity.this).create();
			    dialog2.setTitle("Warning");
			    dialog2.setMessage("Overwrite WAV file?");
			    dialog2.setCancelable(false);
			    dialog2.setButton(DialogInterface.BUTTON_POSITIVE, getResources().getString(android.R.string.yes), new DialogInterface.OnClickListener() {
			        public void onClick(DialogInterface dialog, int buttonId) {
			        	if(!canWrite&&Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP)
			        	{
			        		if(needToRename!=null)
			        		{
			        			Globals.tryToDeleteFile(TimidityActivity.this, probRoot+needToRename);
			        			Globals.tryToDeleteFile(TimidityActivity.this, finalval);
			        		}else{
			        			Globals.tryToDeleteFile(TimidityActivity.this, finalval);
			        		}
			        	}else{
			        		new File(finalval).delete();
			        	}
			        		
				        	saveWavPart2(finalval, needToRename);
			        	
			        }

					
			    });
			    dialog2.setButton(DialogInterface.BUTTON_NEGATIVE, getResources().getString(android.R.string.no), new DialogInterface.OnClickListener() {
			        public void onClick(DialogInterface dialog, int buttonId) {
			          
			        }
			    });
			    dialog2.show();
		  }else{
		     saveWavPart2(finalval, needToRename);
		  }
		  
		}
		});

		alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
		  public void onClick(DialogInterface dialog, int whichButton) {
		    // Canceled.
		  }
		});


		alerty = alert.show();
		
	}
	}
	
	public void saveCfg()
	{
		localfinished=false;
		if(Globals.isMidi(currSongName)&&Globals.isPlaying==0)
		{
			
		
		AlertDialog.Builder alert = new AlertDialog.Builder(this);

		alert.setTitle("Save Cfg");
		alert.setMessage("Save a MIDI configuration file");
		InputFilter filter = new InputFilter() { 
	        public CharSequence filter(CharSequence source, int start, int end, 
	Spanned dest, int dstart, int dend) { 
	                for (int i = start; i < end; i++) { 
	                	String IC = "*/*\n*\r*\t*\0*\f*`*?***\\*<*>*|*\"*:*";
	                        if (IC.contains("*"+source.charAt(i)+"*")) { 
	                                return ""; 
	                        } 
	                } 
	                return null; 
	        } 
		};
		// Set an EditText view to get user input 
		final EditText input = new EditText(this);
		input.setFilters(new InputFilter[]{filter});
		alert.setView(input);

		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog, int whichButton) {
		  String value = input.getText().toString();
		  if(!value.toLowerCase(Locale.US).endsWith(Globals.compressCfg?".tzf":".tcf"))
			  value+=(Globals.compressCfg?".tzf":".tcf");
		  String parent=currSongName.substring(0,currSongName.lastIndexOf('/')+1);
		  boolean aWrite=true;
		  boolean alreadyExists = new File(parent+value).exists();
		  String needRename = null;
		  String probablyTheRoot = "";
		  String probablyTheDirectory = "";
		  try{
		        new FileOutputStream(parent+value,true).close();
		  }catch(FileNotFoundException e)
		  {
			aWrite=false;  
		  } catch (IOException e)
		{
			e.printStackTrace();
		}
		  if(!alreadyExists&&aWrite)
			  new File(parent+value).delete();
		  if(aWrite&&new File(parent).canWrite())
		  {
			  value=parent+value;
		  }else if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP&& Globals.theFold!=null)
		  {
			  //TODO
			  // Write the file to getExternalFilesDir, then move it with the Uri
			  // We need to tell JNIHandler that movement is needed.
			  
			  String[] tmp = Globals.getDocFilePaths(TimidityActivity.this,parent);
			  probablyTheDirectory = tmp[0];
			  probablyTheRoot = tmp[1];
			if(probablyTheDirectory.length()>1)
			{
				needRename = parent.substring(parent.indexOf(probablyTheRoot)+probablyTheRoot.length())+value;
				value = probablyTheDirectory+'/'+value;
			}else{
				value=Environment.getExternalStorageDirectory().getAbsolutePath()+'/'+value;
				return;
			}
		  }else{
			  value=Environment.getExternalStorageDirectory().getAbsolutePath()+'/'+value;
		  }
		  final String finalval = value;
		  final boolean canWrite = aWrite;
		  final String needToRename = needRename;
		  final String probRoot = probablyTheRoot;
		  if(new File(finalval).exists()||(new File(probRoot+needRename).exists()&&needToRename!=null))
		  {
			  AlertDialog dialog2 = new AlertDialog.Builder(TimidityActivity.this).create();
			    dialog2.setTitle("Warning");
			    dialog2.setMessage("Overwrite config file?");
			    dialog2.setCancelable(false);
			    dialog2.setButton(DialogInterface.BUTTON_POSITIVE, getResources().getString(android.R.string.yes), new DialogInterface.OnClickListener() {
			        public void onClick(DialogInterface dialog, int buttonId) {
			        	if(!canWrite&&Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP)
			        	{
			        		if(needToRename!=null)
			        		{
			        			Globals.tryToDeleteFile(TimidityActivity.this, probRoot+needToRename);
			        			Globals.tryToDeleteFile(TimidityActivity.this, finalval);
			        		}else{
			        			Globals.tryToDeleteFile(TimidityActivity.this, finalval);
			        		}
			        	}else{
			        		new File(finalval).delete();
			        	}
			        	saveCfgPart2(finalval, needToRename);
			        }
			    });
			    dialog2.setButton(DialogInterface.BUTTON_NEGATIVE, getResources().getString(android.R.string.no), new DialogInterface.OnClickListener() {
			        public void onClick(DialogInterface dialog, int buttonId) {
			          
			        }
			    });
			    dialog2.show();
		  }else{
			  saveCfgPart2(finalval, needToRename);
		  }
		}
		});

		alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
		  public void onClick(DialogInterface dialog, int whichButton) {
		    // Canceled.
		  }
		});

		alerty = alert.show();
		}
	}
	public void saveCfgPart2(final String finalval, final String needToRename)
	{
		Intent new_intent = new Intent();
	    new_intent.setAction(getResources().getString(R.string.msrv_rec));
	    new_intent.putExtra(getResources().getString(R.string.msrv_cmd), 16);
	    new_intent.putExtra(getResources().getString(R.string.msrv_outfile), finalval);
	    sendBroadcast(new_intent);
		//JNIHandler.setupOutputFile("/sdcard/whya.wav");
		//System.out.println(path.get(position));
		//System.out.println(JNIHandler.play(path.get(position)));
	  final ProgressDialog prog;
	  prog = new ProgressDialog(TimidityActivity.this);
	  prog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
		    @Override
		    public void onClick(DialogInterface dialog, int which) {
		    	
		        dialog.dismiss();
		    }
		});
	  prog.setTitle("Saving CFG");
	  prog.setMessage("Saving...");       
	  prog.setIndeterminate(true);
	  prog.setCancelable(false);
	  prog.show();
        new Thread(new Runnable() {                 
			@Override
			public void run() {
			    while(!localfinished&&prog.isShowing()){
			    	try {
			    		
			    	Thread.sleep(25);
			    	} catch (InterruptedException e){}}
			    
			    TimidityActivity.this.runOnUiThread(new Runnable() {
			        public void run() {
			        	String trueName = finalval;
			        	if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP&& Globals.theFold!=null&&needToRename!=null)
			        	{
			        		if(Globals.renameDocumentFile(TimidityActivity.this, finalval, needToRename))
			        		{
			        			trueName=needToRename;
			        		}else{
			        			trueName="Error";
			        		}
			        	}
			        	Toast.makeText( TimidityActivity.this, "Wrote "+trueName, Toast.LENGTH_SHORT).show();
			        	prog.dismiss();
			        	fileFrag.getDir(fileFrag.currPath);
			        }
			    });
			    
			}
        }).start();
	}
	public void saveWavPart2(final String finalval, final String needToRename)
	{
		Intent new_intent = new Intent();
	    new_intent.setAction(getResources().getString(R.string.msrv_rec));
	    new_intent.putExtra(getResources().getString(R.string.msrv_cmd), 15);
	    new_intent.putExtra(getResources().getString(R.string.msrv_outfile), finalval);
	    sendBroadcast(new_intent);
		//JNIHandler.setupOutputFile("/sdcard/whya.wav");
		//System.out.println(path.get(position));
		//System.out.println(JNIHandler.play(path.get(position)));
	  final ProgressDialog prog;
	  prog = new ProgressDialog(TimidityActivity.this);
	  prog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
		    @Override
		    public void onClick(DialogInterface dialog, int which) {
		    	
		        dialog.dismiss();
		    }
		});
	  prog.setTitle("Converting to WAV");
	  prog.setMessage("Converting...");       
	  prog.setIndeterminate(false);
	  prog.setCancelable(false);
	  prog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
	  prog.show();
        new Thread(new Runnable() {                 
			@Override
			public void run() {
			    while(!localfinished&&prog.isShowing()){
			    	prog.setMax(JNIHandler.maxTime);
		    		prog.setProgress(JNIHandler.currTime);
			    	try {
			    		
			    	Thread.sleep(25);
			    	} catch (InterruptedException e){}}
			    if(!localfinished)
			    {
			    	JNIHandler.stop();
			    	 TimidityActivity.this.runOnUiThread(new Runnable() {
					        public void run() {
			    	Toast.makeText( TimidityActivity.this, "Conversion canceled", Toast.LENGTH_SHORT).show();
			    	if(!Globals.keepWav)
			    	{
			    		if(new File(finalval).exists())
			    			new File(finalval).delete();
			    	}else{
			    		fileFrag.getDir(fileFrag.currPath);
			    	}
					        }
			    	 });
			    	 
			    }else{
			    TimidityActivity.this.runOnUiThread(new Runnable() {
			        public void run() {
			        	String trueName = finalval;
			        	if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP&& Globals.theFold!=null&&needToRename!=null)
			        	{
			        		if(Globals.renameDocumentFile(TimidityActivity.this, finalval, needToRename))
			        		{
			        			trueName=needToRename;
			        		}else{
			        			trueName="Error";
			        		}
			        	}
			        	Toast.makeText( TimidityActivity.this, "Wrote "+trueName, Toast.LENGTH_SHORT).show();
			        	prog.dismiss();
			        	fileFrag.getDir(fileFrag.currPath);
			        }
			    });
			    }
			}
        }).start();
	}
	public void loadCfg()
	{
		new FileBrowserDialog().create( 0, Globals.configFiles, this, this, getLayoutInflater(), true, currSongName.substring(0,currSongName.lastIndexOf('/')), "Loaded");
	}
	public void loadCfg(String path)
	{
		 Intent new_intent = new Intent();
		    new_intent.setAction(getResources().getString(R.string.msrv_rec));
		    new_intent.putExtra(getResources().getString(R.string.msrv_cmd), 17);
		    new_intent.putExtra(getResources().getString(R.string.msrv_infile), path);
		    sendBroadcast(new_intent);
	}
	@Override
	public void setItem(String path, int type)
	{
		loadCfg(path);
	}
	@Override
	public void write()
	{
		
	}
	@Override
	public void ignore()
	{
		
	}
}