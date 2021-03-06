package com.openhackday2;

import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

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
import org.json.JSONException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v4.util.LruCache;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import com.zxing.activity.CaptureActivity;

public class MainActivity extends FragmentActivity implements OnClickListener {
	private BookAdapter mAdapter;
	private Handler mHandler;
	private GridView mBookGridView;
	private LruCache<String, Bitmap> mLruCache;
	private String loadBookXml;
	private int maxSize = 10 * 1024 * 1024;
	
	private SharedPreferences prefs;
	private SharedPreferences.Editor prefsEditor;
	private Set<String> bookSet;
	private Set<String> allRecordSet;

	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.main);
		
		mLruCache = new LruCache<String, Bitmap>(maxSize) {
		    @Override
		    protected int sizeOf(String key, Bitmap value) {
		        return value.getRowBytes() * value.getHeight();
		    }
		};
		
		mBookGridView = (GridView) findViewById(R.id.bookGrid);
		mHandler = new Handler();
		mAdapter = new BookAdapter(this);
		mBookGridView.setAdapter(mAdapter);
    	
		prefs = this.getSharedPreferences(
				"com.openhackday2", Context.MODE_PRIVATE);
		
        bookSet = prefs.getStringSet("books", new HashSet<String>());
        
        Iterator<String> itr = bookSet.iterator();
        while(itr.hasNext()){
        	String bookid = itr.next();
        	String imageurl = prefs.getString(bookid + ":image", "http://www.mnit.ac.in/new/PortalProfile/images/faculty/noimage.jpg");
        	Log.v("Pref", "get:" + bookid + "@" + imageurl);
        	ImageItem item = new ImageItem();
        	item.key = bookid;
        	item.url = imageurl;
        	mAdapter.add(item);
        }

		mBookGridView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
	        	Intent intent = new Intent(MainActivity.this, MineListActivity.class);
	        	ImageItem item = (ImageItem)view.getTag();
	        	intent.putExtra("bookid", item.key);
	            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
	            startActivity(intent);
			}
		});
		
		// onScrollListener の実装
		mBookGridView.setOnScrollListener(new OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (scrollState == SCROLL_STATE_IDLE) {
                    // スクロールが止まったときに読み込む
                    loadBitmap();
                }
            }
            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            }
        });

		findViewById(R.id.btn_scan_barcode).setOnClickListener(this);
		findViewById(R.id.main).setOnClickListener(this);
		findViewById(R.id.main_my).setOnClickListener(this);
		findViewById(R.id.main_all).setOnClickListener(this);
	}
	
	/**
	 * ImageLoader のコールバック.
	 */
	private LoaderCallbacks<Bitmap> callbacks = new LoaderCallbacks<Bitmap>() {
	    @Override
	    public Loader<Bitmap> onCreateLoader(int i, Bundle bundle) {
	        ImageItem item = (ImageItem) bundle.getSerializable("item");
	        ImageLoader loader = new ImageLoader(getApplicationContext(), item);
	        loader.forceLoad();
	        return loader;
	    }
	    @Override
	    public void onLoadFinished(Loader<Bitmap> loader, Bitmap bitmap) {
	        int id = loader.getId();
	        getSupportLoaderManager().destroyLoader(id);
	        // メモリキャッシュに登録する
	        ImageItem item = ((ImageLoader) loader).item;
	        Log.i("imagecache", "キャッシュに登録=" + item.key);
	        mLruCache.put(item.key, bitmap);
	        item.bitmap = bitmap;
	        setBitmap(item);
	    }
	    @Override
	    public void onLoaderReset(Loader<Bitmap> loader) {
	    }
	};
	
	/**
	 * アイテムの View に Bitmap をセットする.
	 * @param item
	 */
	private void setBitmap(ImageItem item) {
	    ImageView view = (ImageView) mBookGridView.findViewWithTag(item);
	    if (view != null) {
	        view.setImageBitmap(item.bitmap);
	        mBookGridView.invalidateViews();
	    }
	}

    public class BookAdapter extends ArrayAdapter<ImageItem> {

        public BookAdapter(Context context) {
            super(context, 0);
        }
        
        public View getView(int position, View convertView, ViewGroup parent) {
        	ImageItem item = getItem(position);
            ImageView imageView;
                imageView = new ImageView(getContext());
                imageView.setLayoutParams(new GridView.LayoutParams(200, 200));
                imageView.setTag(item);
                if (item.bitmap == null) {
                	Bitmap bitmap = mLruCache.get(item.key);
	                if (bitmap != null) {
	                    // キャッシュに存在
	                    Log.i("imageCache", "キャッシュあり=" + item.key);
	                    setBitmap(item);
	                } else {
	                    // キャッシュになし
	                    Log.i("imageCache", "キャッシュなし=" + item.key);
	                    Bundle bundle = new Bundle();
	                    bundle.putSerializable("item", item);
	                    getSupportLoaderManager().initLoader(position, bundle, callbacks);
	                }
                }
                //imageView.setAdjustViewBounds(false);
                //imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                //imageView.setPadding(8, 8, 8, 8);
            
            imageView.setImageBitmap(item.bitmap);

            return imageView;
        }
    }
    
    private void loadBitmap() {
        int first = mBookGridView.getFirstVisiblePosition();
        int count = mBookGridView.getChildCount();
        for (int i = 0; i < count; i++) {
            ImageItem item = mAdapter.getItem(i + first);
            Bitmap bitmap = mLruCache.get(item.key);
            if (bitmap != null) {
                // キャッシュに存在
                Log.i("imageCache", "キャッシュあり=" + item.key);
                setBitmap(item);
                mBookGridView.invalidateViews();
            } else {
                // キャッシュになし
                Log.i("imageCache", "キャッシュなし=" + item.key);
                Bundle bundle = new Bundle();
                bundle.putSerializable("item", item);
                getSupportLoaderManager().initLoader(i, bundle, callbacks);
            }
        }
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
				new Thread(new Runnable() {
					@Override
					public void run() {
					       Log.v("AmazonAPI", ">url: " + urlStr);

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
					                loadBookXml = EntityUtils.toString(httpEntity, "UTF-8");
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
					        Log.v("AmazonAPI", "xml: " + loadBookXml);
					        
					        mHandler.post(new Runnable() {
								@Override
								public void run() {
									ParseXML xmlReader = new ParseXML();
									xmlReader.parse(loadBookXml);
									
						        	ImageItem item = new ImageItem();
						        	prefsEditor = prefs.edit();
						        	
						        	
						        	String bookid = xmlReader.getItemValue("itemid");
						        	String imageUrl = xmlReader
										.getItemValue("imageurl");
						        	String bookTitle = xmlReader.getItemValue("booktitle");
						        	String bookAuthor = xmlReader.getItemValue("bookauthor");
						        	if (bookid != null && imageUrl != null
										&& !bookSet.contains(bookid)) {
						        		Log.v("Pref", "set:" + bookid + "@"
											+ bookSet.toString());
						        		bookSet.add(bookid);
						        		prefsEditor.putStringSet("books", bookSet);
						        		prefsEditor.putString(bookid + ":image",
						        				imageUrl);
						        		prefsEditor.putString(bookid + ":title", bookTitle);
						        		prefsEditor.putString(bookid + ":author", bookAuthor);
						        		//仮データ追加
						        		allRecordSet = prefs.getStringSet(bookid + ":all_records",
												new HashSet<String>());
										String recordId = "record" + (allRecordSet.size() + 1);
										allRecordSet.add(recordId);
										prefsEditor.putStringSet(bookid + ":all_records", allRecordSet);
										prefsEditor.putString(bookid + ":all_" + recordId + ":record", "みんなのレコード");
										prefsEditor.putString(bookid + ":all_" + recordId + ":comment", "みんなのコメント");
										SimpleDateFormat DF = new SimpleDateFormat("yyyy-MM-dd");
										prefsEditor.putString(bookid + ":all_" + recordId + ":recordtime",
												DF.format(new Date()));
						        		prefsEditor.apply();
						        		item.key = bookid;
						        		item.url = imageUrl;

						        		mAdapter.add(item);
						        	}
								}
					        });
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
        }else if (id == R.id.main) {
        	Intent intent = new Intent(MainActivity.this,MainActivity.class);
        	intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
        }else if (id == R.id.main_my) {
        	Intent intent = new Intent(MainActivity.this,MyTimeLineActivity.class);
        	intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
        }else if (id == R.id.main_all) {
        	Intent intent = new Intent(MainActivity.this,AllTimeLineActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
        }
	}
}