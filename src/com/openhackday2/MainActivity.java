package com.openhackday2;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
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
		
		// get data from amazon
		Map<String, String> keyMap = new HashMap<String, String>();  
		keyMap.put("AWSAccessKeyId", "AKIAIDOUP55F5PEKC4WA");  
		keyMap.put("Version", "2011-08-01");  
		keyMap.put("Operation", "ItemSearch");  
		keyMap.put("Keywords", "9784798029351");  // bar code 
		keyMap.put("SearchIndex", "Books");
		keyMap.put("AssociateTag", "idhitsu-22");  
		keyMap.put("Service", "AWSECommerceService");  
		SignedRequestsHelper signedRequestsHelper;
		try {
			signedRequestsHelper = new SignedRequestsHelper();
			String urlStr = signedRequestsHelper.sign(keyMap);  
			URL url = new URL(urlStr);
			System.out.println(url);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  
		
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			Bundle bundle = data.getExtras();
			String scanResult = bundle.getString("result");
			mResultTextView.setText(scanResult);
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
}