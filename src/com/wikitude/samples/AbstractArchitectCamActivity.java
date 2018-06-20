package com.wikitude.samples;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.Timer;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.location.Location;
import android.location.LocationListener;
import android.media.AudioManager;
import android.opengl.GLES20;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.wikitude.architect.ArchitectView;
import com.wikitude.architect.ArchitectView.ArchitectConfig;
import com.wikitude.architect.ArchitectView.ArchitectUrlListener;
import com.wikitude.architect.ArchitectView.SensorAccuracyChangeListener;
import com.wikitude.sdksamples.R;

/**
 * Abstract activity which handles live-cycle events.
 * Feel free to extend from this activity when setting up your own AR-Activity 
 *
 */
public abstract class AbstractArchitectCamActivity extends Activity implements ArchitectViewHolderInterface{

	/**
	 * holds the Wikitude SDK AR-View, this is where camera, markers, compass, 3D models etc. are rendered
	 */
	protected static ArchitectView					architectView;
	
	/**
	 * sensor accuracy listener in case you want to display calibration hints
	 */
	protected SensorAccuracyChangeListener	sensorAccuracyListener;
	
	/**
	 * last known location of the user, used internally for content-loading after user location was fetched
	 */
	protected Location 						lastKnownLocaton;

	/**
	 * sample location strategy, you may implement a more sophisticated approach too
	 */
	protected ILocationProvider				locationProvider;
	
	/**
	 * location listener receives location updates and must forward them to the architectView
	 */
	protected LocationListener 				locationListener;
	
	/**
	 * urlListener handling "document.location= 'architectsdk://...' " calls in JavaScript"
	 */
	protected ArchitectUrlListener 			urlListener;
	
	public static Double longitude, latitude;
	public static String imei = null;
	public static int speed;
	
	protected ImageButton change, mileage;
	protected TextView right;
	
	public Double distance, radius = 6371.009, latX, latY, longY, longX;
	
	public static int x = 0;
	
	Timer timer = new Timer(true);

	/** Called when the activity is first created. */
	@SuppressLint("NewApi")
	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		
		/* pressing volume up/down should cause music volume changes */
		this.setVolumeControlStream(AudioManager.STREAM_MUSIC);

		/* set samples content view */
		this.setContentView(this.getContentViewId());
		
		this.setTitle(this.getActivityTitle() );
		
		/*↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓解除4.0的限制↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓*/
	    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
	    StrictMode.setThreadPolicy(policy);
	    /*↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑解除4.0的限制↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑*/
	    /*↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓取得手機IMEI↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓*/
	    TelephonyManager tManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
	    imei = tManager.getDeviceId();
	    /*↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑取得手機IMEI↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑*/
		
		/*  
		 *	this enables remote debugging of a WebView on Android 4.4+ when debugging = true in AndroidManifest.xml
		 *	If you get a compile time error here, ensure to have SDK 19+ used in your ADT/Eclipse.
		 *	You may even delete this block in case you don't need remote debugging or don't have an Android 4.4+ device in place.
		 *	Details: https://developers.google.com/chrome-developer-tools/docs/remote-debugging
		 */
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) 
		{
		    if (0 != ( getApplicationInfo().flags &= ApplicationInfo.FLAG_DEBUGGABLE )) 
		    {
		        WebView.setWebContentsDebuggingEnabled(true);
		    }
		}

		/* set AR-view for life-cycle notifications etc. */
		AbstractArchitectCamActivity.architectView = (ArchitectView)this.findViewById(this.getArchitectViewId());

		/* pass SDK key if you have one, this one is only valid for this package identifier and must not be used somewhere else */
		final ArchitectConfig config = new ArchitectConfig(this.getWikitudeSDKLicenseKey());

		try {
			/* first mandatory life-cycle notification */
			AbstractArchitectCamActivity.architectView.onCreate(config);
		} catch (RuntimeException rex) {
			AbstractArchitectCamActivity.architectView = null;
			Toast.makeText(getApplicationContext(), "can't create Architect View", Toast.LENGTH_SHORT).show();
			Log.e(this.getClass().getName(), "Exception in ArchitectView.onCreate()", rex);
		}

		// set accuracy listener if implemented, you may e.g. show calibration prompt for compass using this listener
		this.sensorAccuracyListener = this.getSensorAccuracyListener();
		
		// set urlListener, any calls made in JS like "document.location = 'architectsdk://foo?bar=123'" is forwarded to this listener, use this to interact between JS and native Android activity/fragment
		this.urlListener = this.getUrlListener();  
		
		// register valid urlListener in architectView, ensure this is set before content is loaded to not miss any event
		if (this.urlListener != null && AbstractArchitectCamActivity.architectView != null) 
		{
			AbstractArchitectCamActivity.architectView.registerUrlListener(this.getUrlListener());
		}
		
		// listener passed over to locationProvider, any location update is handled here
		right = (TextView) findViewById(R.id.txtStatus);		
		
		this.locationListener = new LocationListener() {

			@Override
			public void onStatusChanged(String provider, int status, Bundle extras) 
			{
			}

			@Override
			public void onProviderEnabled(String provider) 
			{
			}

			@Override
			public void onProviderDisabled(String provider) 
			{
			}

			@Override
			public void onLocationChanged(final Location location) 
			{
				longitude = location.getLongitude();
				latitude = location.getLatitude();
				speed = (int) (location.getSpeed() * 3600 / 1000);
				
				longX = 121.73862702691324;
			    latX = 25.12727797030672;
			    
			    longY = 121.01762096307324;
			    latY = 24.82297100610229;
			    
			    NumberFormat nf = NumberFormat.getInstance();
			    nf.setMaximumFractionDigits(2);
			    
			    distance = radius * (Math.acos(Math.sin(latX) * Math.sin(latY) + Math.cos(latX) * Math.cos(latY) * Math.cos(longY - longX)));			    
				//right.setText("車速：約" + speed + "km/hr" + "\n" + "里程：約" + nf.format(distance) + "km");
			    right.setText("車速：約" + speed + "km/hr");
			    
				// forward location updates fired by LocationProvider to architectView, you can set lat/lon from any location-strategy
				if (location!=null) 
				{
				// sore last location as member, in case it is needed somewhere (in e.g. your adjusted project)
				AbstractArchitectCamActivity.this.lastKnownLocaton = location;
				if ( AbstractArchitectCamActivity.architectView != null ) 
				{
					// check if location has altitude at certain accuracy level & call right architect method (the one with altitude information)
					if ( location.hasAltitude() && location.hasAccuracy() && location.getAccuracy()<7) 
					{
						AbstractArchitectCamActivity.architectView.setLocation( location.getLatitude(), location.getLongitude(), location.getAltitude(), location.getAccuracy() );
					} 
					else 
					{
						AbstractArchitectCamActivity.architectView.setLocation( location.getLatitude(), location.getLongitude(), location.hasAccuracy() ? location.getAccuracy() : 1000 );
					}
				}
				}
			}
		};

		// locationProvider used to fetch user position
		this.locationProvider = getLocationProvider(this.locationListener);
		
		//timer.schedule(new timerTask(), 1000, 5000);
		
	}
	
	@Override
	protected void onPostCreate(final Bundle savedInstanceState) {
		super.onPostCreate( savedInstanceState );
		
		if ( AbstractArchitectCamActivity.architectView != null ) 
		{			
			// call mandatory live-cycle method of architectView
			AbstractArchitectCamActivity.architectView.onPostCreate();
			
			try {
				// load content via url in architectView, ensure '<script src="architect://architect.js"></script>' is part of this HTML file, have a look at wikitude.com's developer section for API references
				AbstractArchitectCamActivity.architectView.load( this.getARchitectWorldPath() );

				if (this.getInitialCullingDistanceMeters() != ArchitectViewHolderInterface.CULLING_DISTANCE_DEFAULT_METERS) {
					// set the culling distance - meaning: the maximum distance to render geo-content
					AbstractArchitectCamActivity.architectView.setCullingDistance( this.getInitialCullingDistanceMeters() );
				}
				
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		
		// call mandatory live-cycle method of architectView
		if ( AbstractArchitectCamActivity.architectView != null ) 
		{
			AbstractArchitectCamActivity.architectView.onResume();
			
			// register accuracy listener in architectView, if set
			if (this.sensorAccuracyListener!=null) 
			{
				AbstractArchitectCamActivity.architectView.registerSensorAccuracyChangeListener( this.sensorAccuracyListener );
			}
		}

		// tell locationProvider to resume, usually location is then (again) fetched, so the GPS indicator appears in status bar
		if ( this.locationProvider != null ) 
		{
			this.locationProvider.onResume();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		
		// call mandatory live-cycle method of architectView
		if ( AbstractArchitectCamActivity.architectView != null ) 
		{
			AbstractArchitectCamActivity.architectView.onPause();
			
			// unregister accuracy listener in architectView, if set
			if ( this.sensorAccuracyListener != null ) 
			{
				AbstractArchitectCamActivity.architectView.unregisterSensorAccuracyChangeListener( this.sensorAccuracyListener );
			}
		}
		
		// tell locationProvider to pause, usually location is then no longer fetched, so the GPS indicator disappears in status bar
		if ( this.locationProvider != null ) 
		{
			this.locationProvider.onPause();
		}
	}
	
	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		// call mandatory live-cycle method of architectView
		if ( AbstractArchitectCamActivity.architectView != null ) 
		{
			AbstractArchitectCamActivity.architectView.onDestroy();
		}
	}

	@Override
	public void onLowMemory() {
		super.onLowMemory();
		if ( AbstractArchitectCamActivity.architectView != null ) 
		{
			AbstractArchitectCamActivity.architectView.onLowMemory();
		}
	}

	/**
	 * title shown in activity
	 * @return
	 */
	public abstract String getActivityTitle();
	
	/**
	 * path to the architect-file (AR-Experience HTML) to launch
	 * @return
	 */
	@Override
	public abstract String getARchitectWorldPath();
	
	/**
	 * url listener fired once e.g. 'document.location = "architectsdk://foo?bar=123"' is called in JS
	 * @return
	 */
	@Override
	public abstract ArchitectUrlListener getUrlListener();
	
	/**
	 * @return layout id of your layout.xml that holds an ARchitect View, e.g. R.layout.camview
	 */
	@Override
	public abstract int getContentViewId();
	
	/**
	 * @return Wikitude SDK license key, checkout www.wikitude.com for details
	 */
	@Override
	public abstract String getWikitudeSDKLicenseKey();
	
	/**
	 * @return layout-id of architectView, e.g. R.id.architectView
	 */
	@Override
	public abstract int getArchitectViewId();

	/**
	 * 
	 * @return Implementation of a Location
	 */
	@Override
	public abstract ILocationProvider getLocationProvider(final LocationListener locationListener);
	
	/**
	 * @return Implementation of Sensor-Accuracy-Listener. That way you can e.g. show prompt to calibrate compass
	 */
	@Override
	public abstract ArchitectView.SensorAccuracyChangeListener getSensorAccuracyListener();
	
	/**
	 * helper to check if video-drawables are supported by this device. recommended to check before launching ARchitect Worlds with videodrawables
	 * @return true if AR.VideoDrawables are supported, false if fallback rendering would apply (= show video fullscreen)
	 */
	public static final boolean isVideoDrawablesSupported() {
		String extensions = GLES20.glGetString(GLES20.GL_EXTENSIONS);
		return extensions != null && extensions.contains("GL_OES_EGL_image_external") && android.os.Build.VERSION.SDK_INT >= 14 ;
	}
	
	public void clickHandler(View v)
	{
		switch(v.getId())
	    {
	    case R.id.imgBtn_Change:
	    	
	    	final Intent itChange = new Intent(this, ImageChange.class);
	    	this.startActivity(itChange);
	    	this.finish();
	    	
	    	//Toast.makeText(getApplicationContext(), "Change", Toast.LENGTH_LONG).show();
	    	break;
	    case R.id.imgBtn_Mileage:
	    	
	    	final Intent itMileage = new Intent(this, ImageMileage.class);
	    	this.startActivity(itMileage);
	    	this.finish();
	    	
	    	//Toast.makeText(getApplicationContext(), "Mileage",Toast.LENGTH_LONG).show();
	    	break;
	    }
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) 
	{
		System.out.println("TabHost_Index.java onKeyDown");
		if (keyCode == KeyEvent.KEYCODE_BACK) 
		{
			//AbstractArchitectCamActivity.x = 1;
			final Intent intent = new Intent(this, MainActivity.class);
	    	this.startActivity(intent);
	    	this.finish();
		}
		return false;
	}
}