package com.openhackday2;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class MineListListviewAdapter extends ArrayAdapter<CommentItem> {
	
	static class ViewHolder{
	    public TextView datetime; 
	    public TextView record;
	    public TextView comment;
	}
	
	public MineListListviewAdapter(Context context) {
        super(context, 0);
        mContext = context;
		mInflater = LayoutInflater.from(mContext);
	}
	
	private LayoutInflater mInflater;
	private Context mContext;

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		CommentItem item = getItem(position);
		
		ViewHolder holder = null;
        if(convertView == null) {
            holder = new ViewHolder();
            
            convertView = mInflater.inflate(R.layout.mine_list_listview, null);
            holder.datetime = (TextView)convertView.findViewById(R.id.mine_list_listview_datetime);
            holder.record = (TextView)convertView.findViewById(R.id.mine_list_listview_record);
            holder.comment = (TextView)convertView.findViewById(R.id.mine_list_listview_comment);
            convertView.setTag(holder);
        }else {
            holder = (ViewHolder)convertView.getTag();
        }
        
        holder.datetime.setText(item.datetime);
        holder.comment.setText(item.comment);
        holder.record.setText(item.record);
		
		return convertView;
	}

}
