package com.openhackday2;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.zxing.activity.CaptureActivity;

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

public class MineListActivity extends Activity implements OnClickListener {
	private ListView mListView;
	private TextView mTextView;
	private SharedPreferences prefs;
	private String bookId;
	private String bookTitle;
	private Set<String> recordSet;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.mine_list);
		Intent i = getIntent();
		bookId = i.getStringExtra("bookid");

		prefs = this.getSharedPreferences(
				"com.openhackday2", Context.MODE_PRIVATE);
		
		bookTitle = prefs.getString(bookId + ":title", "");
        mTextView = (TextView) findViewById(R.id.mine_title);
        mTextView.setText(bookTitle);
        
		//list view 
		mListView = (ListView) findViewById(R.id.mine_list_listview);

		MineListListviewAdapter myListViewAdapter = new MineListListviewAdapter(this);

		mListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

				Intent intent = new Intent(getApplicationContext(), DetailActivity.class);
				intent.putExtra("bookid", bookId);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
			}
		});
        
       recordSet = prefs.getStringSet(bookId + ":mine_records", new HashSet<String>());
        
        Iterator<String> itr = recordSet.iterator();
        while(itr.hasNext()){
        	String recordid = itr.next();
        	String record = prefs.getString(bookId + ":mine_" + recordid + ":record", "");
        	String comment = prefs.getString(bookId + ":mine_" + recordid + ":comment", "");
        	String recordTime = prefs.getString(bookId + ":mine_" + recordid + ":recordtime", "");
        	CommentItem commentItem = new CommentItem();
        	commentItem.id = recordid;
        	commentItem.comment = comment;
        	commentItem.datetime = recordTime;
        	commentItem.record = record;
        	myListViewAdapter.add(commentItem);
        }
		mListView.setAdapter(myListViewAdapter);
        
		// mViewMy.findViewById(R.id.list_tab_my_add).setOnClickListener(this);
		findViewById(R.id.mine_list_add_button).setOnClickListener(this);
		
		findViewById(R.id.btn_scan_barcode).setOnClickListener(this);
		findViewById(R.id.main).setOnClickListener(this);
		findViewById(R.id.main_my).setOnClickListener(this);
		findViewById(R.id.main_all).setOnClickListener(this);

	}


	@Override
	public void onClick(View v) {
		int id = v.getId();
        if (id == R.id.mine_list_add_button) {
        	Intent intent = new Intent(this,DetailActivity.class);
        	intent.putExtra("bookid", bookId);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
        }else if (id == R.id.btn_scan_barcode) {
        	Intent openCameraIntent = new Intent(this,CaptureActivity.class);
			startActivityForResult(openCameraIntent, 0);
        }else if (id == R.id.main) {
        	Intent intent = new Intent(this,MainActivity.class);
        	intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
        }else if (id == R.id.main_my) {
        	Intent intent = new Intent(this,MyTimeLineActivity.class);
        	intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
        }else if (id == R.id.main_all) {
        	Intent intent = new Intent(this,AllTimeLineActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
        }
		
	}

}
