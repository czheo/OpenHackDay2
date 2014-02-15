package com.openhackday2;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class MyTimeLineActivity extends Activity implements OnClickListener, OnPageChangeListener {
	private ViewPager mViewPager;
	private ImageView mImageView;
	private TextView mTextViewKando, mTextViewHatena;
	private View mViewKando, mViewHatena;
	private List<View> mViews;
	private ListView mMyListView;
	private int mOffset = 0;
	private int mCurrIndex = 0;
	private int mImageWidth = 0;
	private SharedPreferences prefs;
	private Set<String> recordSet;
	private Set<String> bookSet;

	public static Bitmap mBitmap;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.my_timeline);

		mViewPager = (ViewPager) findViewById(R.id.my_viewpager);
		mImageView = (ImageView) findViewById(R.id.my_cursor);
		mTextViewKando = (TextView) findViewById(R.id.my_textview_kando);
		mTextViewHatena = (TextView) findViewById(R.id.my_textview_hatena);

		LayoutInflater inflater = getLayoutInflater();
		mViewKando = inflater.inflate(R.layout.my_kando, null);
		mViewHatena = inflater.inflate(R.layout.my_hatena, null);

		mViews = new ArrayList<View>();
		mViews.add(mViewKando);
		mViews.add(mViewHatena);

		mViewPager.setAdapter(new ViewPagerAdapter(mViews));
		mViewPager.setCurrentItem(0);
		mViewPager.setOnPageChangeListener(this);

		mTextViewKando.setOnClickListener(this);
		mTextViewHatena.setOnClickListener(this);

		// My View
		mMyListView = (ListView) mViewKando.findViewById(R.id.my_kando_listview);
		
		// 保存したレコード
		prefs = this.getSharedPreferences(
				"com.openhackday2", Context.MODE_PRIVATE);

		MyKandoListviewAdapter myListViewAdapter = new MyKandoListviewAdapter(this);
		bookSet = prefs.getStringSet("books", new HashSet<String>());
        
        Iterator<String> bookitr = bookSet.iterator();
        while(bookitr.hasNext()){
        	String bookid = bookitr.next();
        	recordSet = prefs.getStringSet(bookid + ":mine_records", new HashSet<String>());
    		Iterator<String> recorditr = recordSet.iterator();
            while(recorditr.hasNext()){
            	String recordid = recorditr.next();
            	String record = prefs.getString(bookid + ":mine_" + recordid + ":record", "");
            	String comment = prefs.getString(bookid + ":mine_" + recordid + ":comment", "");
            	String title = prefs.getString(bookid + ":title", "");
            	CommentItem commentItem = new CommentItem();
            	commentItem.id = recordid;
            	commentItem.comment = comment;
            	commentItem.title = title;
            	commentItem.record = record;
            	myListViewAdapter.add(commentItem);
            }
        }
		
		mMyListView.setAdapter(myListViewAdapter);

//		mMyListView.setOnItemClickListener(new OnItemClickListener() {
//			@Override
//			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//
//				Intent intent = new Intent(getApplicationContext(), DetailActivity.class);
//				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
//				startActivity(intent);
//			}
//		});

		initImageView();

		// mViewMy.findViewById(R.id.list_tab_my_add).setOnClickListener(this);
		// findViewById(R.id.list_tab_save_button).setOnClickListener(this);

	}

	private void initImageView() {
		mImageWidth = BitmapFactory.decodeResource(getResources(), R.drawable.cursor).getWidth();
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		int screenW = dm.widthPixels;
		mOffset = (screenW / 2 - mImageWidth) / 2;
		Matrix matrix = new Matrix();
		matrix.postTranslate(mOffset, 0);
		mImageView.setImageMatrix(matrix);
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		// if (id == R.id.list_tab_my_add) {
		// Intent intent = new Intent(this, DetailActivity.class);
		// intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
		// Intent.FLAG_ACTIVITY_CLEAR_TOP);
		// startActivity(intent);
		// }
	}

	@Override
	public void onPageScrollStateChanged(int arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPageSelected(int arg0) {
		int one = mOffset * 2 + mImageWidth;

		Animation animation = new TranslateAnimation(one * mCurrIndex, one * arg0, 0, 0);
		mCurrIndex = arg0;
		animation.setFillAfter(true);
		animation.setDuration(300);
		mImageView.startAnimation(animation);
	}

}
