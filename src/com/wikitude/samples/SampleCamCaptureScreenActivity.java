package com.wikitude.samples;

import android.content.Intent;
import android.hardware.SensorManager;
import android.location.LocationListener;
import android.net.Uri;
import com.wikitude.architect.ArchitectView.ArchitectUrlListener;
import com.wikitude.architect.ArchitectView.SensorAccuracyChangeListener;
import com.wikitude.sdksamples.R;

public class SampleCamCaptureScreenActivity extends AbstractArchitectCamActivity {

	/**
	 * extras key for activity title, usually static and set in Manifest.xml
	 */
	protected static final String EXTRAS_KEY_ACTIVITY_TITLE_STRING = "activityTitle";
	
	/**
	 * extras key for architect-url to load, usually already known upfront, can be relative folder to assets (myWorld.html --> assets/myWorld.html is loaded) or web-url ("http://myserver.com/myWorld.html"). Note that argument passing is only possible via web-url 
	 */
	protected static final String EXTRAS_KEY_ACTIVITY_ARCHITECT_WORLD_URL = "activityArchitectWorldUrl";

	@Override
	public String getARchitectWorldPath() {
		return getIntent().getExtras().getString(EXTRAS_KEY_ACTIVITY_ARCHITECT_WORLD_URL);
	}

	@Override
	public String getActivityTitle() {
		return (getIntent().getExtras() != null && getIntent().getExtras().get(
				EXTRAS_KEY_ACTIVITY_TITLE_STRING) != null) ? getIntent().getExtras().getString(EXTRAS_KEY_ACTIVITY_TITLE_STRING) : "Test-World";
	}

	@Override
	public int getContentViewId() {
		return R.layout.sample_cam;
	}

	@Override
	public int getArchitectViewId() {
		return R.id.architectView;
	}
	
	@Override
	public String getWikitudeSDKLicenseKey() {
		return WikitudeSDKConstants.WIKITUDE_SDK_KEY;
	}
	
	@Override
	public SensorAccuracyChangeListener getSensorAccuracyListener() {
		return new SensorAccuracyChangeListener() {
			@Override
			public void onCompassAccuracyChanged( int accuracy ) {
				/* UNRELIABLE = 0, LOW = 1, MEDIUM = 2, HIGH = 3 */
				if ( accuracy < SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM && SampleCamCaptureScreenActivity.this != null && !SampleCamCaptureScreenActivity.this.isFinishing() ) {
					//Toast.makeText( SampleCamCaptureScreenActivity.this, R.string.compass_accuracy_low, Toast.LENGTH_LONG ).show();
				}
			}
		};
	}

	@Override
	public ArchitectUrlListener getUrlListener() {
		return new ArchitectUrlListener() {
			
			@Override
			public boolean urlWasInvoked(final String uriString) {
				
				Uri invokedUri = Uri.parse(uriString);
				
				// pressed "More" button on POI-detail panel
				if ("markerselected".equalsIgnoreCase(invokedUri.getHost())) {
					final Intent poiDetailIntent = new Intent(SampleCamCaptureScreenActivity.this, SamplePoiDetailActivity.class);
					poiDetailIntent.putExtra(SamplePoiDetailActivity.EXTRAS_KEY_POI_ID, String.valueOf(invokedUri.getQueryParameter("id")) );
					SampleCamCaptureScreenActivity.this.startActivity(poiDetailIntent);
					SampleCamCaptureScreenActivity.this.finish();
					return true;
				}
				else if ("button".equalsIgnoreCase(invokedUri.getHost())) {
					final Intent poiDetailIntent = new Intent(SampleCamCaptureScreenActivity.this, CamTestActivity.class);
					SampleCamCaptureScreenActivity.this.startActivity(poiDetailIntent);
					SampleCamCaptureScreenActivity.this.finish();
					return true;
				}				
				return true;
			}			
		};		
	}

	@Override
	public ILocationProvider getLocationProvider(final LocationListener locationListener) {
		return new LocationProvider(this, locationListener);
	}
	
	@Override
	public float getInitialCullingDistanceMeters() {
		// you need to adjust this in case your POIs are more than 50km away from user here while loading or in JS code (compare 'AR.context.scene.cullingDistance')
		return ArchitectViewHolderInterface.CULLING_DISTANCE_DEFAULT_METERS;
	}

}
