package com.openhackday2;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class AllKandoListviewAdapter extends ArrayAdapter<CommentItem>{
	
	static class ViewHolder{
	    public TextView record;
	    public TextView comment;
	    public TextView title;
	}
	
	private LayoutInflater mInflater;
	private Context mContext;
	
	public AllKandoListviewAdapter (Context context){
		super(context, 0);
		mContext = context;
		mInflater = LayoutInflater.from(mContext);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		CommentItem item = getItem(position);
		ViewHolder holder = null;
        if(convertView == null) {
            holder = new ViewHolder();
            
            convertView = mInflater.inflate(R.layout.all_kando_listview, null);
            holder.comment = (TextView)convertView.findViewById(R.id.all_kando_listview_comment);
            holder.record = (TextView)convertView.findViewById(R.id.all_kando_listview_record);
            holder.title = (TextView)convertView.findViewById(R.id.all_kando_listview_title);
            
            convertView.setTag(holder);
        }else {
            holder = (ViewHolder)convertView.getTag();
        }
        
        holder.comment.setText(item.comment);
        holder.record.setText(item.record);
        holder.title.setText(item.title);
		
		return convertView;
	}

}
