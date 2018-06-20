package com.wikitude.samples;

import java.io.InputStream;
import java.net.URL;
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
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import com.wikitude.sdksamples.R;

public class SearchList extends Activity {

	public String poiNameURL = "http://140.126.11.39/1/select_poi_name.php";
	public String poiCoordinatesURL = "http://140.126.11.39/1/select_poi_coordinates.php";
	
	public String PoiName, PoiCoordinates;
	
	public String[] PoiNameArray, PoiCoordinatesArray;
	private ListView listView;
	private ArrayAdapter<String> listAdapter;
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		getWindow().setFlags( WindowManager.LayoutParams.FLAG_FULLSCREEN
                , WindowManager.LayoutParams.FLAG_FULLSCREEN);
		requestWindowFeature( Window.FEATURE_NO_TITLE );
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		
		setContentView(R.layout.search_list);
		
		/*↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓解除4.0的限制↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓*/
	    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
	    StrictMode.setThreadPolicy(policy);
	    /*↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑解除4.0的限制↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑*/
	   
		getPOIName();
		
		listView = (ListView)findViewById(R.id.listView);
		listAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, PoiNameArray);
		listView.setAdapter(listAdapter);
		listView.setOnItemClickListener(new OnItemClickListener(){

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			// TODO Auto-generated method stub		
			
			Intent intent = new Intent();
		    intent.setClass(SearchList.this, SearchMileage.class);
		    
		    PoiCoordinates = getHtmlByGet(poiCoordinatesURL + "?name=" + PoiNameArray[position]);
	    	PoiCoordinatesArray = PoiCoordinates.split(",");
		    
	    	Bundle bundle = new Bundle();
	        bundle.putString("long", PoiCoordinatesArray[0]);
	        bundle.putString("lat", PoiCoordinatesArray[1]);
	        intent.putExtras(bundle);
	        startActivity(intent);
	        SearchList.this.finish();
		}
		});
	}
    
    public void getPOIName()
    {
    	PoiName = getHtmlByGet(poiNameURL);
    	PoiNameArray = PoiName.split(",");
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
			final Intent intent = new Intent(SearchList.this, MainActivity.class);
			SearchList.this.startActivity(intent);
			SearchList.this.finish();
		}
		return false;
	}
}
