package com.openhackday2;

import java.io.File;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageView;

public class DetailActivity extends Activity implements OnClickListener {
	private ImageView mImage;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.detail);
		
		mImage = (ImageView) findViewById(R.id.detail_image);
		findViewById(R.id.detail_button).setOnClickListener(this);

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
					intent.putExtra("aspectX", 1);
					intent.putExtra("aspectY", 1);
					intent.putExtra("outputX", 300);
					intent.putExtra("outputY", 300);
					intent.putExtra("scale", true);
					intent.putExtra("return-data", false);

					startActivityForResult(intent, 0);
				}
			}).create().show();
		}

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (resultCode == RESULT_OK) {
			Bitmap bitmap = BitmapFactory.decodeFile(Environment.getExternalStorageDirectory() + "/open_hack_day.jpg");
//			Bitmap resinzeBitmap = zoomBitmap(bitmap, 10, 10);
//			if (!bitmap.isRecycled()) {
//				bitmap.recycle();
//			}
			mImage.setImageBitmap(bitmap);
			
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

}
