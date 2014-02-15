package com.openhackday2;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class MyKandoListviewAdapter extends ArrayAdapter<CommentItem>{
	
	static class ViewHolder{
	    public TextView record;
	    public TextView comment;
	    public TextView title;
	}
	
	private LayoutInflater mInflater;
	private Context mContext;
	
	public MyKandoListviewAdapter (Context context){
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
            
            convertView = mInflater.inflate(R.layout.my_kando_listview, null);
            holder.comment = (TextView)convertView.findViewById(R.id.my_kando_listview_comment);
            holder.title = (TextView)convertView.findViewById(R.id.my_kando_listview_title);
            holder.record = (TextView)convertView.findViewById(R.id.my_kando_listview_record);
            
            convertView.setTag(holder);
        }else {
            holder = (ViewHolder)convertView.getTag();
        }
        
        holder.comment.setText(item.comment);
        holder.title.setText(item.title);
        holder.record.setText(item.record);
		
		return convertView;
	}

}
