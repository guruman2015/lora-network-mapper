package com.cmu.ccgs.loranetworkmapper.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.afollestad.sectionedrecyclerview.SectionedRecyclerViewAdapter;
import com.cmu.ccgs.loranetworkmapper.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cef on 4/3/16.
 */
public class StatsAdapter extends SectionedRecyclerViewAdapter<StatsAdapter.StatsViewHolder> {

    private List<String> mConfigData = new ArrayList<String>();

    private double mLat;
    private double mLng;
    private double mDistance;

    private double mRssi;
    private double mSnr;
    private String mTimestamp;

    public StatsAdapter(){
        super();
        mConfigData.add("No config data");
    }

    public void updateLocation(double lat, double lng, double distance){
        mLat = lat;
        mLng = lng;
        mDistance = distance;
        notifyDataSetChanged();
    }

    public void setRssiAndSnr(double rssi, double snr, String timestamp){
        mRssi = rssi;
        mSnr = snr;
        mTimestamp = timestamp;
        notifyDataSetChanged();
    }

    public void setRssi(double rssi){
        mRssi = rssi;
        notifyDataSetChanged();
    }

    public void setSnr(double snr){
        mSnr = snr;
        notifyDataSetChanged();
    }

    public void updateTimestamp(String timestamp){
        mTimestamp = timestamp;
        notifyDataSetChanged();
    }

    public void setConfigData(List<String> data){
        mConfigData.clear();
        mConfigData.addAll(data);
        notifyDataSetChanged();
    }

    @Override
    public StatsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(viewType == VIEW_TYPE_HEADER ?
                R.layout.item_section_header :
                R.layout.item_log_text, parent, false);
        return new StatsViewHolder(v);
    }

    @Override
    public int getSectionCount() {
        return 3;
    }

    @Override
    public int getItemCount(int i) {
        if(i == 0){
            return 3;
        } else if(i == 1){
            return 3;
        } else {
            return mConfigData.size();
        }
    }

    @Override
    public void onBindHeaderViewHolder(StatsViewHolder statsViewHolder, int i) {
        switch (i){
            case 0:
                statsViewHolder.setText("Position");
                break;
            case 1:
                statsViewHolder.setText("LoRa");
                break;
            case 2:
                statsViewHolder.setText("Device Settings");
                break;
        }
    }

    @Override
    public void onBindViewHolder(StatsViewHolder statsViewHolder, int section, int relativePosition, int absolutePosition) {
        switch (section){
            case 0:
                bindPosition(statsViewHolder, relativePosition);
                break;
            case 1:
                bindLoraData(statsViewHolder, relativePosition);
                break;
            case 2:
                bindConfigData(statsViewHolder, relativePosition);
                break;
        }
    }

    protected void bindPosition(StatsViewHolder statsViewHolder, int position){
        switch (position){
            case 0:
                statsViewHolder.setText("Lat: " + mLat);
                break;
            case 1:
                statsViewHolder.setText("Long: " + mLng);
                break;
            case 2:
                statsViewHolder.setText("Distance: " + mDistance);
                break;
        }
    }

    protected void bindLoraData(StatsViewHolder statsViewHolder, int position){
        switch (position){
            case 0:
                statsViewHolder.setText("RSSI: " + mRssi);
                break;
            case 1:
                statsViewHolder.setText("SnR: " + mSnr);
                break;
            case 2:
                statsViewHolder.setText("Timestamp: " + mTimestamp);
                break;
        }
    }

    protected void bindConfigData(StatsViewHolder statsViewHolder, int position){
        statsViewHolder.setText(mConfigData.get(position));
    }

    public class StatsViewHolder extends RecyclerView.ViewHolder {
        private TextView mTextView;
        public StatsViewHolder(View itemView) {
            super(itemView);
            mTextView = (TextView)itemView.findViewById(R.id.text);
        }
        public void setText(String text){
            mTextView.setText(text);
        }
    }

}
