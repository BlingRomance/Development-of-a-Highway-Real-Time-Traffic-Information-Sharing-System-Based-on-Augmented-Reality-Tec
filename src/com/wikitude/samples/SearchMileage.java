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



public class SearchMileage extends Activity {
	
	public ImageView mileage_1, mileage_2, mileage_3;
	public String imageFileURL = "http://140.126.11.39/1/upload/";
	public String imageDataURL = "http://140.126.11.39/1/select_img_mileage.php";
	public String MileageLatitude, MileageLongitude, MileageData;
	public String[] MileageDataArray = new String[18];
	public String[] DataArray = {"0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0"};
	public double distance, radius = 6378137.0, latX, latY, longY, longX;	
	public TextView txt_1, txt_2, txt_3;
	public double reLatX, reLatY, a, b, s;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        getWindow().setFlags( WindowManager.LayoutParams.FLAG_FULLSCREEN
                , WindowManager.LayoutParams.FLAG_FULLSCREEN);
		requestWindowFeature( Window.FEATURE_NO_TITLE );
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		
        setContentView(R.layout.image_mileage);
        
        /*↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓解除4.0的限制↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓*/
	    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
	    StrictMode.setThreadPolicy(policy);
	    /*↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑解除4.0的限制↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑*/
	    
	    Bundle bundleMileage = this.getIntent().getExtras();
	    
	    MileageLongitude = bundleMileage.getString("long");
	    MileageLatitude = bundleMileage.getString("lat");
	    
	    //MileageLongitude = 121.73862702691323;
	    //MileageLatitude = 25.12727797030672;
	    
	    mileage_1 = (ImageView) findViewById(R.id.imageMileage_1);
	    mileage_2 = (ImageView) findViewById(R.id.imageMileage_2);
	    mileage_3 = (ImageView) findViewById(R.id.imageMileage_3);
	    txt_1 = (TextView) findViewById(R.id.txtData_1);
	    txt_2 = (TextView) findViewById(R.id.txtData_2);
	    txt_3 = (TextView) findViewById(R.id.txtData_3);
	    
	    //longX = Double.parseDouble(MileageLongitude);
	    //latX = Double.parseDouble(MileageLatitude);
	    
	    longX = 121.73862702691323;
	    latX = 25.12727797030672;
	    
	    NumberFormat nf = NumberFormat.getInstance();
	    nf.setMaximumFractionDigits(1);
	    
	    getMileageData();	    
	    /***************************************************************************************************/
	    longY = Double.parseDouble(DataArray[0]);
	    latY = Double.parseDouble(DataArray[1]);	    
	    reLatX = (latX * Math.PI / 180.0);
	    reLatY = (latY * Math.PI / 180.0);	    
	    a = reLatX - reLatY;
	    b = (longX - longY) * Math.PI / 180.0;
	    s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2) 
	    		+ Math.cos(reLatX) * Math.cos(reLatY) * Math.pow(Math.sin(b / 2), 2)));
	    s = s * radius;
	    s = Math.round(s * 10000) / 10000;	    
	    //distance = radius * (Math.acos(Math.sin(latX) * Math.sin(latY) + Math.cos(latX) * Math.cos(latY) * Math.cos(longY - longX)));
	    if("0".equals(DataArray[0]) || "0".equals(DataArray[1]))
		{
	    	mileage_1.setImageResource(R.drawable.sorry);
	    	txt_1.setText("資料不足");
		}
	    else
	    {	    	
	    	mileage_1.setImageDrawable(loadImageFromURL(imageFileURL + DataArray[0].toString() + "," + DataArray[1].toString() + ".jpg"));
	    	txt_1.setText("里程約：" + nf.format(s / 1000) + "km處," + DataArray[4] + "," + DataArray[2] + "km/hr");	    	
	    }
	    /***************************************************************************************************/	    
	    longY = Double.parseDouble(DataArray[6]);
	    latY = Double.parseDouble(DataArray[7]);	    
	    reLatX = (latX * Math.PI / 180.0);
	    reLatY = (latY * Math.PI / 180.0);	    
	    a = reLatX - reLatY;
	    b = (longX - longY) * Math.PI / 180.0;
	    s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2) 
	    		+ Math.cos(reLatX) * Math.cos(reLatY) * Math.pow(Math.sin(b / 2), 2)));
	    s = s * radius;
	    s = Math.round(s * 10000) / 10000;	    
	    //distance = radius * (Math.acos(Math.sin(latX) * Math.sin(latY) + Math.cos(latX) * Math.cos(latY) * Math.cos(longY - longX)));
	    if("0".equals(DataArray[6]) || "0".equals(DataArray[7]))
		{
	    	mileage_2.setImageResource(R.drawable.sorry);
	    	txt_2.setText("資料不足");
		}
	    else
	    {
	    	mileage_2.setImageDrawable(loadImageFromURL(imageFileURL + DataArray[6].toString() + "," + DataArray[7].toString() + ".jpg"));
	    	txt_2.setText("里程約：" + s / 1000 + "km處," + DataArray[10] + "," + DataArray[8] + "km/hr");
	    }
	    /***************************************************************************************************/ 
	    longY = Double.parseDouble(DataArray[12]);
	    latY = Double.parseDouble(DataArray[13]);	    
	    reLatX = (latX * Math.PI / 180.0);
	    reLatY = (latY * Math.PI / 180.0);	    
	    a = reLatX - reLatY;
	    b = (longX - longY) * Math.PI / 180.0;
	    s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2) 
	    		+ Math.cos(reLatX) * Math.cos(reLatY) * Math.pow(Math.sin(b / 2), 2)));
	    s = s * radius;
	    s = Math.round(s * 10000) / 10000;
	    //distance = radius * (Math.acos(Math.sin(latX) * Math.sin(latY) + Math.cos(latX) * Math.cos(latY) * Math.cos(longY - longX)));
	    if("0".equals(DataArray[12]) || "0".equals(DataArray[13]))
		{
	    	mileage_3.setImageResource(R.drawable.sorry);
	    	txt_3.setText("資料不足");
		}
	    else
	    {
	    	mileage_3.setImageDrawable(loadImageFromURL(imageFileURL + DataArray[12].toString() + "," + DataArray[13].toString() + ".jpg"));
	    	txt_3.setText("里程約：" + s / 1000 + "km處," + DataArray[16] + "," + DataArray[14] + "km/hr");
	    }
	    
    }
    
    public void getMileageData()
    {
    	MileageData = getHtmlByGet(imageDataURL + "?longitude=" + MileageLongitude.substring(0, 5) + "&latitude=" + MileageLatitude.substring(0, 4));
    	//MileageData = getHtmlByGet(imageDataURL + "?longitude=" + "121.7200" + "&latitude=" + "25.1034");
    	MileageDataArray = MileageData.split(",");
    	
    	for(int i = 0; i < MileageDataArray.length; i++)
    	{        		
    		DataArray[i] = MileageDataArray[i];
    	}
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
			final Intent intent = new Intent(SearchMileage.this, SearchList.class);
			SearchMileage.this.startActivity(intent);
			SearchMileage.this.finish();
		}
		return false;
	}	
}
