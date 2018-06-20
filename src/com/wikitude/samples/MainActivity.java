package com.wikitude.samples;


import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.opengl.GLES20;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsoluteLayout;
import android.widget.ImageButton;
import android.widget.Toast;

import com.wikitude.sdksamples.R;


/**
 * Activity launched when pressing app-icon.
 * It uses very basic ListAdapter for UI representation
 */
@SuppressWarnings("deprecation")
public class MainActivity extends Activity {
	
	public ImageButton drive, search;
	public static Boolean isExit = false, hasTask = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);		
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);		
		this.setContentView(this.getContentViewId());
		
		setImgBtnxy();
		
		if(AbstractArchitectCamActivity.x == 1)
		{
			final String activityUrls = "5_Browsing$Pois_6_Capture$Screen$Bonus";		
			final String className = "com.wikitude.samples.SampleCamCaptureScreenActivity";
			final String EXTRAS_KEY_ACTIVITY_ARCHITECT_WORLD_URL = "activityArchitectWorldUrl";
			
			try {
				AbstractArchitectCamActivity.x = 0;
				final Intent intent = new Intent(this, Class.forName(className));				
				intent.putExtra(EXTRAS_KEY_ACTIVITY_ARCHITECT_WORLD_URL, 
						"samples" + File.separator + activityUrls	+ File.separator + "index.html");				
				this.startActivity(intent);
				this.finish();
			} catch (Exception e) {
				Toast.makeText(this, className + "\nnot defined/accessible", Toast.LENGTH_SHORT).show();
			}
		}
		
	}

	protected int getContentViewId() 
	{
		return R.layout.list_startscreen;
	}
	/**
	 * helper to check if video-drawables are supported by this device. recommended to check before launching ARchitect Worlds with videodrawables
	 * @return true if AR.VideoDrawables are supported, false if fallback rendering would apply (= show video fullscreen)
	 */
	public static final boolean isVideoDrawablesSupported() 
	{
		String extensions = GLES20.glGetString( GLES20.GL_EXTENSIONS );
		return extensions != null && extensions.contains( "GL_OES_EGL_image_external" ) && android.os.Build.VERSION.SDK_INT >= 14 ;
	}
	
	public void main_clickHandler(View v)
	{
		switch(v.getId())
	    {
	    case R.id.imgBtn_Drive:
	    	
	    	final String activityUrls = "5_Browsing$Pois_6_Capture$Screen$Bonus";		
			final String className = "com.wikitude.samples.SampleCamCaptureScreenActivity";
			final String EXTRAS_KEY_ACTIVITY_ARCHITECT_WORLD_URL = "activityArchitectWorldUrl";
			
			try {

				final Intent intent = new Intent(this, Class.forName(className));				
				intent.putExtra(EXTRAS_KEY_ACTIVITY_ARCHITECT_WORLD_URL, 
						"samples" + File.separator + activityUrls	+ File.separator + "index.html");				
				this.startActivity(intent);
				this.finish();
			} catch (Exception e) {
				Toast.makeText(this, className + "\nnot defined/accessible", Toast.LENGTH_SHORT).show();
			}
	    	break;
	    	
	    case R.id.imgBtn_Search:
	    	
	    	final Intent intent = new Intent(this, SearchList.class);
	    	this.startActivity(intent);
	    	this.finish();
	    	break;
	    }
	}
	
	public void setImgBtnxy(){
		
		drive = (ImageButton) findViewById(R.id.imgBtn_Drive);
		search = (ImageButton) findViewById(R.id.imgBtn_Search);
		
		DisplayMetrics dm = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(dm);        
        
        ViewGroup.LayoutParams layDrive = 
        		new AbsoluteLayout.LayoutParams(AbsoluteLayout.LayoutParams.WRAP_CONTENT,
        		    AbsoluteLayout.LayoutParams.WRAP_CONTENT, dm.widthPixels / 4 - 60, dm.heightPixels / 2 - 60);
        drive.setLayoutParams(layDrive);
        
        
        ViewGroup.LayoutParams laySearch = 
        		new AbsoluteLayout.LayoutParams(AbsoluteLayout.LayoutParams.WRAP_CONTENT,
        		    AbsoluteLayout.LayoutParams.WRAP_CONTENT, (dm.widthPixels / 4) * 3 - 60, dm.heightPixels / 2 - 60);
        search.setLayoutParams(laySearch);
    	
    }
	
	/*↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓按兩次返回鍵退出↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓*/	
	Timer tExit = new Timer();
	TimerTask task = new TimerTask() 
	{	
		@Override
		public void run() 
		{
			isExit = false;
			hasTask = true;
		}
	};	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) 
	{
		System.out.println("TabHost_Index.java onKeyDown");
		if (keyCode == KeyEvent.KEYCODE_BACK) 
		{
			if(isExit == false ) 
			{
				isExit = true;
				Toast.makeText(this, "再按一次退出應用程式", Toast.LENGTH_SHORT).show();
				if(!hasTask) 
				{
					tExit.schedule(task, 2000);
				}
			} 
			else 
			{
				this.finish();
				System.exit(0);
			}
		}
		return false;
	}	
	/*↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑按兩次返回鍵退出↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑*/
}
