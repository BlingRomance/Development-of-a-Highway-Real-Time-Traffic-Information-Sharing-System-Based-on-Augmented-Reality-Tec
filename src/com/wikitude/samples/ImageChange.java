package com.wikitude.samples;

import java.io.InputStream;
import java.net.URL;
import java.text.NumberFormat;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.os.Bundle;
import android.os.StrictMode;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.wikitude.sdksamples.R;

public class ImageChange extends Activity {
	
	public ImageView Change;
	public TextView left, right;
	
	public String imageFileURL = "http://140.126.11.39/1/upload/";
	public String imageDataURL = "http://140.126.11.39/1/select_img_change.php";
	public String ChangeLatitude, ChangeLongitude, ChangeData;
	public String[] ChangeDataArray = new String[6];
	public Double distance, radius = 6378137.0, latX, latY, longY, longX;	

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        getWindow().setFlags( WindowManager.LayoutParams.FLAG_FULLSCREEN
                , WindowManager.LayoutParams.FLAG_FULLSCREEN);
		requestWindowFeature( Window.FEATURE_NO_TITLE );
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		
        setContentView(R.layout.image_change);
        
        /*↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓解除4.0的限制↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓*/
	    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
	    StrictMode.setThreadPolicy(policy);
	    /*↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑解除4.0的限制↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑*/
	    
	    ChangeLongitude = ("" + AbstractArchitectCamActivity.longitude);
	    ChangeLatitude = ("" + AbstractArchitectCamActivity.latitude);

	    Change = (ImageView) findViewById(R.id.imageChange);
	    //left = (TextView) findViewById(R.id.txtLift);
	    right = (TextView) findViewById(R.id.txtRight);
	    
	    NumberFormat nf = NumberFormat.getInstance();
	    nf.setMaximumFractionDigits(1);
	    
	    getChangeData();	    
	    
	    longX = Double.parseDouble(ChangeLongitude);
	    latX = Double.parseDouble(ChangeLatitude);
	    longY = Double.parseDouble(ChangeDataArray[0]);
	    latY = Double.parseDouble(ChangeDataArray[1]);
	    
	    double reLatX = (latX * Math.PI / 180.0);
	    double reLatY = (latY * Math.PI / 180.0);	    
	    double a = reLatX - reLatY;
	    double b = (longX - longY) * Math.PI / 180.0;
	    double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2) 
	    		+ Math.cos(reLatX) * Math.cos(reLatY) * Math.pow(Math.sin(b / 2), 2)));
	    s = s * radius;
	    s = Math.round(s * 10000) / 10000;
	    
	    //distance = radius * (Math.acos(Math.sin(latX) * Math.sin(latY) + Math.cos(latX) * Math.cos(latY) * Math.cos(longY - longX)));
	    if("0".equals(ChangeDataArray[0]) || "0".equals(ChangeDataArray[1]))
	    {
	    	Change.setImageResource(R.drawable.sorry);
	    	//left.setText("路況情形：" + "資料不足");
	    	right.setText("路況情形：" + "資料不足" + "\n" + "車速：" + "資料不足" + "\n" + "相距：" + "資料不足");
	    }
	    else
	    {
	    	Change.setImageDrawable(loadImageFromURL(imageFileURL + ChangeDataArray[0].toString() + "," + ChangeDataArray[1].toString() + ".jpg"));
	    	//left.setText("路況情形：" + ChangeDataArray[4]);
	    	//right.setText("路況情形：" + ChangeDataArray[4] + "\n" + "車速：約" + ChangeDataArray[2] + "km/hr" + "\n" + "相距：" + nf.format(distance) + "km");
	    	right.setText("路況情形：" + ChangeDataArray[4] + "\n" + "車速約：" + ChangeDataArray[2] + "km/hr" + "\n" + "相距約：" + nf.format(s / 1000) + "km");
	    }	    
    }    
    
    public void getChangeData()
    {
    	ChangeData = getHtmlByGet(imageDataURL + "?longitude=" + ChangeLongitude.substring(0, 5) + "&latitude=" + ChangeLatitude.substring(0, 4));
    	ChangeDataArray = ChangeData.split(",");
    }
    
    public Drawable loadImageFromURL(String url){
        try{
            InputStream is = (InputStream) new URL(url).getContent();
            Drawable draw = Drawable.createFromStream(is, "src");
            return draw;
        }catch (Exception e) {
            //TODO handle error
            Log.i("loadingImg", e.toString());
            return null;
        }
    }  
    
    public String getHtmlByGet(String url)
    {    
        String result = "";
        try {
            HttpClient client = new DefaultHttpClient();   
            HttpGet get = new HttpGet(url);
            HttpResponse response = client.execute(get); 
            HttpEntity resEntity = response.getEntity();
            if (resEntity != null) {    
                result = EntityUtils.toString(resEntity, "utf-8");
                //Toast.makeText(this, result, Toast.LENGTH_SHORT).show();
                //Log.i("network", result);
            }
        } catch (Exception e) {
            e.printStackTrace();
            //Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
        }
        return result;
    }
    
    @Override
	public boolean onKeyDown(int keyCode, KeyEvent event) 
	{
		System.out.println("TabHost_Index.java onKeyDown");
		if (keyCode == KeyEvent.KEYCODE_BACK) 
		{
			AbstractArchitectCamActivity.x = 1;
			final Intent intent = new Intent(this, MainActivity.class);
	    	this.startActivity(intent);
	    	this.finish();
		}
		return false;
	}	
}
