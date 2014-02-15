package com.openhackday2;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.Toast;

import com.googlecode.tesseract.android.TessBaseAPI;

public class DetailActivity extends Activity implements OnClickListener {
	private ImageView mImage;
	private static final String TAG = "MainActivity ...";

	private static final String TESSBASE_PATH = "/mnt/sdcard/tesseract/";
	private static final String DEFAULT_LANGUAGE = "jpn";
	private static final String IMAGE_PATH = Environment.getExternalStorageDirectory() + "/open_hack_day.jpg";

	private WebView webview;
	private String mHtmlData = "";
	private SharedPreferences.Editor prefsEditor;
	private SharedPreferences prefs;
	private Set<String> recordSet;
	private String bookId;

	@SuppressLint({ "SetJavaScriptEnabled", "JavascriptInterface" })
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.detail);
		Intent i = getIntent();
		bookId = i.getStringExtra("bookid");
		mImage = (ImageView) findViewById(R.id.detail_image);
		findViewById(R.id.detail_button).setOnClickListener(this);
		findViewById(R.id.edit_button).setOnClickListener(this);

		// edit
		webview = (WebView) this.findViewById(R.id.webview);
		webview.getSettings().setJavaScriptEnabled(true);

		webview.loadUrl("file:///android_asset/index.html");
		HtmlShow hs = new HtmlShow();
		webview.addJavascriptInterface(hs, "MyContent");
		
		prefs = this.getSharedPreferences(
				"com.openhackday2", Context.MODE_PRIVATE);
		
		//dumy
		prefsEditor = prefs.edit();
		recordSet = prefs.getStringSet(bookId + ":mine_records", new HashSet<String>());
		String recordId = "record" + (recordSet.size() + 1);
		recordSet.add(recordId);
    	prefsEditor.putStringSet(bookId + ":mine_records", recordSet);
    	prefsEditor.putString("mine_" + recordId + ":record", "とーーーーても感動した！！！！！！！");
    	SimpleDateFormat DF = new SimpleDateFormat("yyyy-MM-dd");
    	prefsEditor.putString("mine_" + recordId + ":recordtime", DF.format(new Date()));
    	prefsEditor.apply();

	}

	public class HtmlShow {

		private String str;

		public String getContent() {
			return mHtmlData;
		}

		public void saveWebviewData(String str) {
			Toast.makeText(DetailActivity.this, str, Toast.LENGTH_SHORT).show();
			prefsEditor = prefs.edit();
			recordSet = prefs.getStringSet(bookId + ":mine_records", new HashSet<String>());
			String recordId = "record" + (recordSet.size() + 1);
			recordSet.add(recordId);
        	prefsEditor.putStringSet(bookId + ":mine_records", recordSet);
        	prefsEditor.putString("mine_" + recordId + ":record", str);
        	SimpleDateFormat DF = new SimpleDateFormat("yyyy-MM-dd");
        	prefsEditor.putString("mine_" + recordId + ":recordtime", DF.format(new Date()));
        	prefsEditor.apply();
		}
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		if (id == R.id.detail_button) {
			final CharSequence[] items = { "ギャラリー", "カメラ" };
			new AlertDialog.Builder(this).setTitle("イメージを選択").setItems(items, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int item) {
					Uri imageUri = Uri.fromFile(new File(Environment.getExternalStorageDirectory(), "open_hack_day.jpg"));
					Intent intent;
					if (item == 1) {
						intent = new Intent("android.media.action.IMAGE_CAPTURE");
					} else {
						intent = new Intent(Intent.ACTION_GET_CONTENT);
						intent.addCategory(Intent.CATEGORY_OPENABLE);
						intent.setType("image/jpeg");
					}
					intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
					intent.putExtra("crop", "true");
					intent.putExtra("aspectX", 3);
					intent.putExtra("aspectY", 1);
					intent.putExtra("outputX", 600);
					intent.putExtra("outputY", 200);

					intent.putExtra("scale", true);
					intent.putExtra("return-data", false);

					startActivityForResult(intent, 0);
				}
			}).create().show();
		} else if (id == R.id.edit_button) {
//			Intent intent = new Intent(this, DetailActivity.class);
//			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
//			startActivity(intent);
		}

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (resultCode == RESULT_OK) {
			Bitmap bitmap = BitmapFactory.decodeFile(Environment.getExternalStorageDirectory() + "/open_hack_day.jpg");
			// Bitmap resinzeBitmap = zoomBitmap(bitmap, 10, 10);
			// if (!bitmap.isRecycled()) {
			// bitmap.recycle();
			// }
			mImage.setImageBitmap(bitmap);
			ocr();

		}
	}

	public Bitmap zoomBitmap(Bitmap bitmap, int width, int height) {
		int w = bitmap.getWidth();
		int h = bitmap.getHeight();
		Matrix matrix = new Matrix();
		float scaleWidth = ((float) width / w);
		float scaleHeight = ((float) height / h);
		matrix.postScale(scaleWidth, scaleHeight);
		Bitmap newbmp = Bitmap.createBitmap(bitmap, 0, 0, w, h, matrix, true);
		return newbmp;
	}

	@SuppressLint("JavascriptInterface")
	protected void ocr() {

		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inSampleSize = 2;
		Bitmap bitmap = BitmapFactory.decodeFile(IMAGE_PATH, options);

		Log.d(TAG, "---in ocr()  before try--");
		try {
			Log.v(TAG, "not in the exception");
			ExifInterface exif = new ExifInterface(IMAGE_PATH);
			int exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

			Log.v(TAG, "Orient: " + exifOrientation);

			int rotate = 0;
			switch (exifOrientation) {
			case ExifInterface.ORIENTATION_ROTATE_90:
				rotate = 90;
				break;
			case ExifInterface.ORIENTATION_ROTATE_180:
				rotate = 180;
				break;
			case ExifInterface.ORIENTATION_ROTATE_270:
				rotate = 270;
				break;
			}

			Log.v(TAG, "Rotation: " + rotate);

			// Getting width & height of the given image.
			int w = bitmap.getWidth();
			int h = bitmap.getHeight();

			// Setting pre rotate
			Matrix mtx = new Matrix();
			mtx.preRotate(rotate);

			// Rotating Bitmap
			bitmap = Bitmap.createBitmap(bitmap, 0, 0, w, h, mtx, false);
			// tesseract req. ARGB_8888
			bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);

		} catch (IOException e) {
			Log.e(TAG, "Rotate or coversion failed: " + e.toString());
			Log.v(TAG, "in the exception");
		}

		// ImageView iv = (ImageView) findViewById(R.id.image);
		// iv.setImageBitmap(bitmap);
		// iv.setVisibility(View.VISIBLE);

		Log.v(TAG, "Before baseApi");
		TessBaseAPI baseApi = new TessBaseAPI();
		baseApi.setDebug(true);
		baseApi.init(TESSBASE_PATH, DEFAULT_LANGUAGE);
		baseApi.setImage(bitmap);
		String recognizedText = baseApi.getUTF8Text();
		baseApi.end();
		Log.v(TAG, "OCR Result: " + recognizedText);

		// clean up and show
		if (DEFAULT_LANGUAGE.equalsIgnoreCase("eng")) {
			recognizedText = recognizedText.replaceAll("[^a-zA-Z0-9]+", " ");
		}
		if (recognizedText.length() != 0) {
			mHtmlData = recognizedText.trim();
//			((TextView) findViewById(R.id.detail_ocr_show)).setText(mHtmlData);

			webview.loadUrl("file:///android_asset/index.html");
			HtmlShow hs = new HtmlShow();
			webview.addJavascriptInterface(hs, "MyContent");
		}
	}
}
