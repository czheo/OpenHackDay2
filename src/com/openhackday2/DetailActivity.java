package com.openhackday2;

import java.io.File;
import java.io.FileNotFoundException;
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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.googlecode.tesseract.android.TessBaseAPI;

public class DetailActivity extends Activity implements OnClickListener {
	private ImageView mImage;
	private static final String TAG = "MainActivity ...";

	private static final String TESSBASE_PATH = "/mnt/sdcard/tesseract/";
	private static final String DEFAULT_LANGUAGE = "jpn";
	private static final String IMAGE_PATH = Environment.getExternalStorageDirectory() + "/open_hack_day.jpg";
	private Uri mImageUri = Uri.fromFile(new File(Environment.getExternalStorageDirectory(), "open_hack_day.jpg"));
	
	private WebView webview;
	private String mHtmlData = "";
	private SharedPreferences.Editor prefsEditor;
	private SharedPreferences prefs;
	private Set<String> recordSet;
	private String bookId;
	private String bookTitle;
	private EditText comment;

	@SuppressLint({ "SetJavaScriptEnabled", "JavascriptInterface" })
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.detail);
		Intent i = getIntent();
		bookId = i.getStringExtra("bookid");
		bookTitle = i.getStringExtra("booktitle");
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
		
		comment = (EditText) findViewById(R.id.comment);

	}

	public class HtmlShow {

		private String str;

		public String getContent() {
			return mHtmlData;
		}

		public void saveWebviewData(String str) {
			Toast.makeText(DetailActivity.this, str, Toast.LENGTH_SHORT).show();
			if (str != null) {
				prefsEditor = prefs.edit();
				recordSet = prefs.getStringSet(bookId + ":mine_records",
						new HashSet<String>());
				String recordId = "record" + (recordSet.size() + 1);
				recordSet.add(recordId);
				prefsEditor.putStringSet(bookId + ":mine_records", recordSet);
				prefsEditor.putString("mine_" + recordId + ":record", str);
				prefsEditor.putString("mine_" + recordId + ":comment", comment.getText().toString());
				prefsEditor.putString("mine_" + recordId + ":title", bookTitle);
				SimpleDateFormat DF = new SimpleDateFormat("yyyy-MM-dd");
				prefsEditor.putString("mine_" + recordId + ":recordtime",
						DF.format(new Date()));
				prefsEditor.apply();
			}
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
					if (item == 1) {
						Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
						takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);
						startActivityForResult(takePhotoIntent, 1);
					} else {
						Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
						intent.addCategory(Intent.CATEGORY_OPENABLE);
						intent.setType("image/jpeg");
						intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);
						intent.putExtra("crop", "true");
						intent.putExtra("aspectX", 3);
						intent.putExtra("aspectY", 1);
						intent.putExtra("outputX", 600);
						intent.putExtra("outputY", 200);
						intent.putExtra("scale", true);
						intent.putExtra("return-data", false);
						startActivityForResult(intent, 0);
					}

				}
			}).create().show();
		} else if (id == R.id.edit_button) {
			// Intent intent = new Intent(this, DetailActivity.class);
			// intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
			// Intent.FLAG_ACTIVITY_CLEAR_TOP);
			// startActivity(intent);
		}

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (resultCode == RESULT_OK) {
			if (requestCode == 1) {
				Intent intent = new Intent("com.android.camera.action.CROP");
				intent.setDataAndType(mImageUri, "image/*");
				intent.putExtra("crop", "true");
				intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);
				intent.putExtra("aspectX", 3);
				intent.putExtra("aspectY", 1);
				intent.putExtra("outputX", 600);
				intent.putExtra("outputY", 200);
				intent.putExtra("scale", true);
				intent.putExtra("return-data", false);
				startActivityForResult(intent, 2);
			} else {
				Bitmap bitmap;
				try {
					bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), mImageUri);
					mImage.setImageBitmap(bitmap);
					ocr(bitmap);
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		}

//		super.onActivityResult(requestCode, resultCode, data);
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
	protected void ocr(Bitmap bitmap) {

//		BitmapFactory.Options options = new BitmapFactory.Options();
//		options.inSampleSize = 2;
//		 = BitmapFactory.decodeFile(IMAGE_PATH, options);

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
			// ((TextView)
			// findViewById(R.id.detail_ocr_show)).setText(mHtmlData);

			webview.loadUrl("file:///android_asset/index.html");
			HtmlShow hs = new HtmlShow();
			webview.addJavascriptInterface(hs, "MyContent");
		}
	}
}
