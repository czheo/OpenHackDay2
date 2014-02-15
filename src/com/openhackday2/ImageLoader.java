package com.openhackday2;

import java.io.InputStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

public class ImageLoader extends AsyncTaskLoader<Bitmap> {
    /** 対象のアイテム. */
    public ImageItem item;
 
    /**
     * コンストラクタ.
     * @param context {@link Context}
     * @param item {@link ImageItem}
     */
    public ImageLoader(Context context, ImageItem item) {
        super(context);
        this.item = item;
    }
 
    @Override
    public Bitmap loadInBackground() {
		String urldisplay = item.url;
		Bitmap image = null;
		try {
			InputStream in = new java.net.URL(urldisplay).openStream();
			image = BitmapFactory.decodeStream(in);
		} catch (Exception e) {
			Log.e("Error", e.getMessage());
			e.printStackTrace();
		}
		return image;
    }
}
