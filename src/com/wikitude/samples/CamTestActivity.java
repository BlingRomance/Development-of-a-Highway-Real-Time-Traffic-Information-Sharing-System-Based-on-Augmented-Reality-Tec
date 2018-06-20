package com.wikitude.samples;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import com.wikitude.sdksamples.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.util.Log;
import android.view.SurfaceView;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;

public class CamTestActivity extends Activity {
	private static final String TAG = "CamTestActivity";
	Preview preview;
	Button buttonClick;
	Camera camera;
	Activity act;
	Context ctx;
	//private SurfaceView surfaceView;
    
    Timer timer = new Timer(true);
    
    private String saveWhere =  "/sdcard/";
    private String actionUrl = "http://140.126.11.39/1/upload_image.php";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ctx = this;
		act = this;
		getWindow().setFlags( WindowManager.LayoutParams.FLAG_FULLSCREEN
                , WindowManager.LayoutParams.FLAG_FULLSCREEN);
		requestWindowFeature( Window.FEATURE_NO_TITLE );

		setContentView(R.layout.main);
		
		/*↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓解除4.0的限制↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓*/
	    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
	    StrictMode.setThreadPolicy(policy);
	    /*↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑解除4.0的限制↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑*/
		
		preview = new Preview(this, (SurfaceView)findViewById(R.id.surfaceView));
		preview.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		
		//surfaceView.getHolder().setFixedSize(640, 480);
		
		((FrameLayout) findViewById(R.id.layout)).addView(preview);
		preview.setKeepScreenOn(true);
		
		timer.schedule(new timerTask(), 3000, 5000);
		
	}
	
	public class timerTask extends TimerTask
	{
		public void run()
	    {
	    	camera.autoFocus(new AutoFocusCallback(){
				@Override
				public void onAutoFocus(boolean arg0, Camera arg1) {
					camera.takePicture(shutterCallback, rawCallback, jpegCallback);
					//Toast.makeText(ctx, "ok", Toast.LENGTH_LONG).show();
					//timer.cancel();
				}
			});
	    }
	 };
	 
	
	@Override
	protected void onResume() {
		super.onResume();
		camera = Camera.open();
		camera.startPreview();
		preview.setCamera(camera);		
	}

	@Override
	protected void onPause() {
		if(camera != null) {
			camera.stopPreview();
			preview.setCamera(null);
			camera.release();
			camera = null;
			timer.cancel();
		}
		super.onPause();
	}

	private void resetCam() {
		camera.startPreview();
		preview.setCamera(camera);
	}

	ShutterCallback shutterCallback = new ShutterCallback() {
		public void onShutter() {
			 //Log.d(TAG, "onShutter'd");
		}
	};

	PictureCallback rawCallback = new PictureCallback() {
		public void onPictureTaken(byte[] data, Camera camera) {
			 //Log.d(TAG, "onPictureTaken - raw");
		}
	};

	PictureCallback jpegCallback = new PictureCallback() {
		public void onPictureTaken(byte[] data, Camera camera) {
			new SaveImageTask().execute(data);
			resetCam();
			Log.d(TAG, "onPictureTaken - jpeg");
		}
	};
	
	private class SaveImageTask extends AsyncTask<byte[], Void, Void> {

		@Override
		protected Void doInBackground(byte[]... data) {
			//FileOutputStream outStream = null;
			String imgName = AbstractArchitectCamActivity.longitude + "," + AbstractArchitectCamActivity.latitude;
			final File screenCaptureFile = new File(Environment.getExternalStorageDirectory().toString(), imgName + ".jpg");
			
			try {
	            
				final FileOutputStream out = new FileOutputStream(screenCaptureFile);
				out.write(data[0]);
				out.flush();
				out.close();
				
				uploadFile(actionUrl, imgName + ".jpg", screenCaptureFile);
				getHtmlByGet("http://140.126.11.39/1/insert_user.php?imei=" + AbstractArchitectCamActivity.imei + "&coordinates=" + imgName + "&speed=" + AbstractArchitectCamActivity.speed);
				getHtmlByGet("http://140.126.11.39/1/insert_mileage.php?longitude=" + AbstractArchitectCamActivity.longitude + 
						"&latitude=" + AbstractArchitectCamActivity.latitude + "&speed=" + AbstractArchitectCamActivity.speed);
		
				AbstractArchitectCamActivity.x = 1;
				final Intent intent = new Intent();
				intent.setClass(CamTestActivity.this, MainActivity.class);
		    	startActivity(intent);
		    	finish();
		    	
				
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
			}
			return null;
		}
	}
	
	private void uploadFile(String uploadUrl, String imageName, File delFile)
	{
		String end = "\r\n";
	    String twoHyphens = "--";
	    String boundary = "******";
	    
	    String srcPath = null;
	    srcPath = saveWhere + imageName;
	    
	    //Toast.makeText(this, srcPath, Toast.LENGTH_SHORT).show();
	    
	    try
	    {
	      URL url = new URL(uploadUrl);
	      HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
	      //設置每次傳輸的流大小，可以有效防止手機因為記憶體不足崩潰
	      //此方法用於在預先不知道內容長度時啟用沒有進行內部緩衝的 HTTP 請求正文的流。
	      httpURLConnection.setChunkedStreamingMode(128 * 1024);// 128K
	      //允許輸入輸出流
	      httpURLConnection.setDoInput(true);
	      httpURLConnection.setDoOutput(true);
	      httpURLConnection.setUseCaches(false);
	      //使用POST方法
	      httpURLConnection.setRequestMethod("POST");
	      httpURLConnection.setRequestProperty("Connection", "Keep-Alive");
	      httpURLConnection.setRequestProperty("Charset", "UTF-8");
	      httpURLConnection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);

	      DataOutputStream dos = new DataOutputStream(httpURLConnection.getOutputStream());
	      dos.writeBytes(twoHyphens + boundary + end);
	      dos.writeBytes("Content-Disposition: form-data; name=\"uploadedfile\"; filename=\"" + srcPath.substring(srcPath.lastIndexOf("/") + 1) + "\"" + end);
	      dos.writeBytes(end);

	      FileInputStream fis = new FileInputStream(srcPath);
	      byte[] buffer = new byte[8192]; // 8k
	      int count = 0;
	      //
	      while ((count = fis.read(buffer)) != -1)
	      {
	        dos.write(buffer, 0, count);
	      }
	      fis.close();

	      dos.writeBytes(end);
	      dos.writeBytes(twoHyphens + boundary + twoHyphens + end);
	      dos.flush();

	      InputStream is = httpURLConnection.getInputStream();
	      new InputStreamReader(is, "utf-8");
	      
	      //BufferedReader br = new BufferedReader(isr);	      
	      //String result = br.readLine();
	      //Toast.makeText(this, result, Toast.LENGTH_SHORT).show();
	      
	      dos.close();
	      is.close();
	      
	      delFile.delete();

	    } catch (Exception e)
	    {
	      e.printStackTrace();
	      //setTitle(e.getMessage());
	    }
	}
	
	public String getHtmlByGet(String url){    
        String result = "";
        try {
            HttpClient client = new DefaultHttpClient();   
            HttpGet get = new HttpGet(url);
            HttpResponse response = client.execute(get); 
            HttpEntity resEntity = response.getEntity();
            if (resEntity != null) {    
                result = EntityUtils.toString(resEntity);
                //Toast.makeText(this, result, Toast.LENGTH_SHORT).show();
                //Log.i("network", result);
            }
        } catch (Exception e) {
            e.printStackTrace();
            //Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
        }
        return result;
    }
	
}


