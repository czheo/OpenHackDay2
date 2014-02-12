package com.openhackday2;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.TextView;

import com.zxing.activity.CaptureActivity;

public class MainActivity extends Activity implements OnClickListener {
	private TextView mResultTextView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.main);

		mResultTextView = (TextView) this.findViewById(R.id.tv_scan_result);

		findViewById(R.id.btn_scan_barcode).setOnClickListener(this);
		findViewById(R.id.image_link).setOnClickListener(this);
		
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			Bundle bundle = data.getExtras();
			String scanResult = bundle.getString("result");
			// get data from amazon
			Map<String, String> keyMap = new HashMap<String, String>();
			keyMap.put("AssociateTag", "kingarthur911-22");
			keyMap.put("IdType", "ISBN");
			keyMap.put("ItemId", scanResult);
			keyMap.put("Operation", "ItemLookup"); 
			keyMap.put("ResponseGroup", "Large");
			keyMap.put("ReviewPage", "1");
			keyMap.put("SearchIndex", "Books");
			keyMap.put("Service", "AWSECommerceService");  
			SignedRequestsHelper signedRequestsHelper;
			try {
				signedRequestsHelper = new SignedRequestsHelper();
				final String urlStr = signedRequestsHelper.sign(keyMap);
				mResultTextView.setText(scanResult);
				new Thread(new Runnable() {
					@Override
					public void run() {
					       Log.v("AmazonAPI", ">url: " + urlStr);

					        String xml = null;
					        try {
					            DefaultHttpClient httpClient = new DefaultHttpClient();
					            HttpParams params = httpClient.getParams();
					            httpClient.getParams().setParameter("http.useragent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_8_5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/31.0.1650.63 Safari/537.36");
					            params.setBooleanParameter(CoreProtocolPNames.USE_EXPECT_CONTINUE, false);
					            HttpConnectionParams.setConnectionTimeout(params, 30000);
					            HttpConnectionParams.setSoTimeout(params, 30000);
					            
					            HttpGet method   = new HttpGet(urlStr);

					            HttpResponse httpResponse = httpClient.execute(method);

					            if (httpResponse != null && httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
					                HttpEntity httpEntity = httpResponse.getEntity();
					                xml = EntityUtils.toString(httpEntity, "UTF-8");
					                httpEntity.consumeContent();
					            }
					            httpClient.getConnectionManager().shutdown();
					        }
					        catch (ClientProtocolException e) {
					            Log.v("AmazonAPI", e.toString());
					      
					        }
					        catch (ParseException e) {
					        	Log.v("AmazonAPI", e.toString());
					       
					        }
					        catch (IOException e){
					        	Log.v("AmazonAPI", e.toString());
					        
					        }
					        Log.v("AmazonAPI", "xml: " + xml);
					}
				}).start();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
        if (id == R.id.btn_scan_barcode) {
        	Intent openCameraIntent = new Intent(MainActivity.this,CaptureActivity.class);
			startActivityForResult(openCameraIntent, 0);
        } else  if (id == R.id.image_link) {
        	Intent intent = new Intent(this, ListTabActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
		
	}
	
    /**
     * GETするメソッド
     * @param url
     * @return
     */
    private String httpRequest(String url) {
        Log.v("AmazonAPI", ">url: " + url);

        String xml = null;
        try {
            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpParams params = httpClient.getParams();
            httpClient.getParams().setParameter("http.useragent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_8_5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/31.0.1650.63 Safari/537.36");
            params.setBooleanParameter(CoreProtocolPNames.USE_EXPECT_CONTINUE, false);
            HttpConnectionParams.setConnectionTimeout(params, 30000);
            HttpConnectionParams.setSoTimeout(params, 30000);
            
            HttpGet method   = new HttpGet(url);

            HttpResponse httpResponse = httpClient.execute(method);

            if (httpResponse != null && httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                HttpEntity httpEntity = httpResponse.getEntity();
                xml = EntityUtils.toString(httpEntity, "UTF-8");
                httpEntity.consumeContent();
            }
            httpClient.getConnectionManager().shutdown();
        }
        catch (ClientProtocolException e) {
            Log.v("AmazonAPI", e.toString());
            return null;
        }
        catch (ParseException e) {
        	Log.v("AmazonAPI", e.toString());
            return null;
        }
        catch (IOException e){
        	Log.v("AmazonAPI", e.toString());
            return null;
        }
        Log.v("AmazonAPI", "xml: " + xml);

        return xml;
    }
}