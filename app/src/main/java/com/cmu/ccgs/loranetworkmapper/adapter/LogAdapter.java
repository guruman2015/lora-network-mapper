package com.cmu.ccgs.loranetworkmapper.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cmu.ccgs.loranetworkmapper.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by cef on 4/3/16.
 */
public class LogAdapter extends RecyclerView.Adapter<LogAdapter.LogViewHolder> {

    private List<String> mData = new ArrayList<String>();
    private LayoutInflater mInflater;

    public LogAdapter(Context context){
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void appendData(String message){
        if(message != null){
            List<String> lines = Arrays.asList(message.split("\n"));
            mData.addAll(lines);
            notifyDataSetChanged();
        }
    }

    @Override
    public LogViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.item_log_text, parent, false);
        LogViewHolder holder = new LogViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(LogViewHolder holder, int position) {
        holder.setText(mData.get(position));
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public class LogViewHolder extends RecyclerView.ViewHolder {
        TextView mTextView;
        public LogViewHolder(View itemView) {
            super(itemView);
            mTextView = (TextView) itemView.findViewById(R.id.text);
        }
        public void setText(String text){
            mTextView.setText(text);
        }
    }

}
