package com.wikitude.samples;

import java.io.InputStream;
import java.net.URL;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.wikitude.sdksamples.R;

public class SamplePoiDetailActivity extends Activity {

	public static final String EXTRAS_KEY_POI_ID = "id";
	public String id;
	
	public ImageView Detail;
	public TextView left, right;
	
	public String imageFileURL = "http://140.126.11.39/1/upload/";
	public String imageIDURL = "http://140.126.11.39/1/select_detail_coordinates.php";
	public String imageDataURL = "http://140.126.11.39/1/select_img_change.php";
	public String DetailLongitude, DetailLatitude, DetailData;
	public String[] DetailDataArray = new String[6];
	public double distance, radius = 6371.009, latX, latY, longY, longX;	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		getWindow().setFlags( WindowManager.LayoutParams.FLAG_FULLSCREEN
                , WindowManager.LayoutParams.FLAG_FULLSCREEN);
		requestWindowFeature( Window.FEATURE_NO_TITLE );
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		
		this.setContentView(R.layout.sample_poidetail);
		
		/*↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓解除4.0的限制↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓*/
	    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
	    StrictMode.setThreadPolicy(policy);
	    /*↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑解除4.0的限制↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑*/
	    
	    DetailLongitude = ("" + AbstractArchitectCamActivity.longitude);
	    DetailLatitude = ("" + AbstractArchitectCamActivity.latitude);
		
		id =  getIntent().getExtras().getString(EXTRAS_KEY_POI_ID);
		
		Detail = (ImageView) findViewById(R.id.imageDetail);
	    //left = (TextView) findViewById(R.id.detailLift);
	    right = (TextView) findViewById(R.id.detailRight);
	    
	    getDetailID();
	    
	    longX = Double.parseDouble(DetailLongitude);
	    latX = Double.parseDouble(DetailLatitude);
	    longY = Double.parseDouble(DetailDataArray[0]);
	    latY = Double.parseDouble(DetailDataArray[1]);
	    
	    //distance = radius * (Math.acos(Math.sin(latX) * Math.sin(latY) + Math.cos(latX) * Math.cos(latY) * Math.cos(longY - longX)));
	    
	    double reLatX = (latX * Math.PI / 180.0);
	    double reLatY = (latY * Math.PI / 180.0);	    
	    double a = reLatX - reLatY;
	    double b = (longX - longY) * Math.PI / 180.0;
	    double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2) 
	    		+ Math.cos(reLatX) * Math.cos(reLatY) * Math.pow(Math.sin(b / 2), 2)));
	    s = s * radius;
	    s = Math.round(s * 10000) / 10000;	
	    
	    if("0".equals(DetailDataArray[0]) || "0".equals(DetailDataArray[1]))
	    {
	    	Detail.setImageResource(R.drawable.sorry);
	    	//left.setText("路況情形：" + "資料不足");
	    	right.setText("路況情形：" + "資料不足" + "\n" + "車速：" + "資料不足" + "\n" + "相距：" + "資料不足");
	    }
	    else
	    {
	    	Detail.setImageDrawable(loadImageFromURL(imageFileURL + DetailDataArray[0].toString() + "," + DetailDataArray[1].toString() + ".jpg"));
	    	//left.setText("路況情形：" + DetailDataArray[4]);
	    	right.setText("路況情形：" + DetailDataArray[4] + "\n" + "車速約：" + DetailDataArray[2] + "km/hr" + "\n" + "相距約：" + s / 1000 + "km");
	    }
		
	}
	
	public void getDetailID()
    {
		DetailData = getHtmlByGet(imageIDURL + "?id=" + id);
		DetailDataArray = DetailData.split(",");
		
		DetailData = getHtmlByGet(imageDataURL + "?longitude=" + DetailDataArray[0].substring(0, 5) + "&latitude=" + DetailDataArray[1].substring(0, 4));
		DetailDataArray = DetailData.split(",");
		
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
