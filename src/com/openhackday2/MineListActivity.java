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
	private SharedPreferences prefs;
	private Set<String> bookSet;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.mine_list);

		//list view 
		mListView = (ListView) findViewById(R.id.mine_list_listview);

		MineListListviewAdapter myListViewAdapter = new MineListListviewAdapter(this);
		mListView.setAdapter(myListViewAdapter);

		mListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

				Intent intent = new Intent(getApplicationContext(), DetailActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
			}
		});

		prefs = this.getSharedPreferences(
				"com.openhackday2", Context.MODE_PRIVATE);
		
        bookSet = prefs.getStringSet("books", new HashSet<String>());
        

		// mViewMy.findViewById(R.id.list_tab_my_add).setOnClickListener(this);
		findViewById(R.id.mine_list_add_button).setOnClickListener(this);

	}


	@Override
	public void onClick(View v) {
		int id = v.getId();
        if (id == R.id.mine_list_add_button) {
        	Intent intent = new Intent(this,DetailActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
        }
		
	}

}
