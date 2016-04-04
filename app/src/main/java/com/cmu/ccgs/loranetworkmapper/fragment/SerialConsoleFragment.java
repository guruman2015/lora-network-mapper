package com.cmu.ccgs.loranetworkmapper.fragment;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import com.cmu.ccgs.loranetworkmapper.R;
import com.cmu.ccgs.loranetworkmapper.adapter.LogAdapter;
import com.cmu.ccgs.loranetworkmapper.lora.mdot.service.MdotSerialConsoleService;
import com.cmu.ccgs.loranetworkmapper.usb.serial.service.SerialConsoleService;

/**
 * Created by cef on 4/2/16.
 */
public class SerialConsoleFragment extends Fragment implements View.OnClickListener {

    private EditText mTextInput;
    private Button mSendButton;
    private RecyclerView mRecyclerView;
    private LogAdapter mLogAdapter;

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra(SerialConsoleService.KEY_MESSAGE);
            mLogAdapter.appendData(message);
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mReceiver, new IntentFilter(SerialConsoleService.NOTIFY_MESSAGE_RECEIVED));
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mReceiver);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_console, container, false);
        return v;
    }

    @Override
    public void onPause() {
        super.onPause();
        mTextInput.clearFocus();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mTextInput = (EditText) view.findViewById(R.id.consoleInput);
        mTextInput.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if(event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER){
                    sendText();
                }
                return false;
            }
        });
        mSendButton = (Button) view.findViewById(R.id.consoleSendButton);
        mSendButton.setOnClickListener(this);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        LinearLayoutManager manager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(manager);
        mRecyclerView.setAdapter(getAdapter());
    }

    protected LogAdapter getAdapter() {
        if (mLogAdapter == null) {
            mLogAdapter = new LogAdapter(getActivity());
        }
        return mLogAdapter;
    }

    protected void sendText(){
        String text = mTextInput.getText().toString();
        MdotSerialConsoleService.sendMessage(getActivity(), text + "\r\n");
        mTextInput.setText("");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.consoleSendButton:
                sendText();
                break;
        }
    }
}
