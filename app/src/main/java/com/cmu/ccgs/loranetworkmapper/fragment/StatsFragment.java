package com.cmu.ccgs.loranetworkmapper.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.cmu.ccgs.loranetworkmapper.R;
import com.cmu.ccgs.loranetworkmapper.usb.serial.service.SerialConsoleService;

/**
 * Created by cef on 4/2/16.
 */
public class StatsFragment extends Fragment implements View.OnClickListener {

    Button mConnect;
    Button mDisconnect;
    Button mRefresh;
    Button mPing;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_stats, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mConnect = (Button) view.findViewById(R.id.btn_connect);
        mDisconnect = (Button) view.findViewById(R.id.btn_disconnect);
        mRefresh = (Button) view.findViewById(R.id.btn_refresh);
        mPing = (Button) view.findViewById(R.id.btn_ping);

        mConnect.setOnClickListener(this);
        mDisconnect.setOnClickListener(this);
        mRefresh.setOnClickListener(this);
        mPing.setOnClickListener(this);
    }

    protected void connect(){
        SerialConsoleService.connect(getActivity());
    }

    protected void disconnect(){
        SerialConsoleService.disconnect(getActivity());
    }

    protected void refresh(){

    }

    protected void ping(){

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_connect:
                connect();
                break;
            case R.id.btn_disconnect:
                disconnect();
                break;
            case R.id.btn_refresh:
                refresh();
                break;
            case R.id.btn_ping:
                ping();
                break;
        }
    }
}
