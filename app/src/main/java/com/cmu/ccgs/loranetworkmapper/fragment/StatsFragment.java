package com.cmu.ccgs.loranetworkmapper.fragment;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.cmu.ccgs.loranetworkmapper.R;
import com.cmu.ccgs.loranetworkmapper.adapter.StatsAdapter;
import com.cmu.ccgs.loranetworkmapper.lora.mdot.model.ATTransaction;
import com.cmu.ccgs.loranetworkmapper.lora.mdot.service.MdotSerialConsoleService;
import com.firebase.client.Firebase;
import com.hoho.android.usbserial.util.HexDump;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by cef on 4/2/16.
 */
public class StatsFragment extends Fragment implements View.OnClickListener {

    Button mConnect;
    Button mDisconnect;
    Button mRefresh;
    Button mPing;
    Button mSend;

    RecyclerView mRecyclerView;
    StatsAdapter mAdapter = null;

    Firebase mFirebase = new Firebase("https://crossmobilelora-dev.firebaseio.com/");

    private boolean mSendLocation = false;

    private Location mLastLocation;

    private final int REQUEST_PERMISSION_LOCATION_CODE = 3;

    private Map<String, String> mPacket = new HashMap<String, String>();

    private LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            getAdapter().updateLocation(location.getLatitude(), location.getLongitude(), 0);
            mLastLocation = location;
            if(mSendLocation){
                sendLocation(location);
                mSendLocation = false;
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ATTransaction transaction = intent.getParcelableExtra(MdotSerialConsoleService.KEY_TRANSACTION);
            if(transaction.isOK()){
                if(transaction.getCommand().contains("PING")){
                    parsePingResponse(transaction.getResponse());
                } else if(transaction.getCommand().contains("AT&V")){
                    parseConfigurationResponse(transaction.getResponse());
                } else if(transaction.getCommand().contains("RSSI")) {
                    parseRssiResponse(transaction.getResponse());
                } else if(transaction.getCommand().contains("SNR")){
                    parseSnrResponse(transaction.getResponse());
                } else if(transaction.getCommand().contains("SEND")){
                    parseSendResponse(transaction.getResponse());
                }
            } else {
                Toast.makeText(getActivity(), "Transaction Failed", Toast.LENGTH_SHORT).show();
            }
        }
    };

    protected void sendLocation(Location location){
        JSONObject json = new JSONObject();
        String requestId = HexDump.toHexString((int)System.currentTimeMillis());
        try {
            json.put("lat", location.getLatitude());
            json.put("lng", location.getLongitude());
            json.put("id", requestId);
        } catch(JSONException e){
            e.printStackTrace();
        }
        mPacket.clear();
        mPacket.put("id", requestId);
        mPacket.put("lat", "" + location.getLatitude());
        mPacket.put("lng", "" + location.getLongitude());
        mAdapter.updateLocation(location.getLatitude(), location.getLongitude(), 0);
        MdotSerialConsoleService.sendPacket(getActivity(), json.toString());
    }

    protected void parseRssiResponse(String response){
        // add rssi to packet
        List<String> res = Arrays.asList(response.split("\n"));
        String rssi = res.get(1).trim();
        mPacket.put("rssi", rssi);
        double rssiAve = 0;
        int count = 0;
        for(String r : Arrays.asList(rssi.split(","))){
            try {
                rssiAve += Double.parseDouble(r.trim());
                count++;
            } catch(Exception e){
                e.printStackTrace();
            }
        }
        rssiAve = rssiAve/((double)count);
        mAdapter.setRssi(rssiAve);
        MdotSerialConsoleService.querySnr(getActivity());
    }

    protected void parseSnrResponse(String response){
        // add snr to packet and send to server
        List<String> res = Arrays.asList(response.split("\n"));
        String snr = res.get(1).trim();
        mPacket.put("snr", snr);
        double snrAve = 0;
        int count = 0;
        for(String r : Arrays.asList(snr.split(","))){
            try {
                snrAve += Double.parseDouble(r.trim());
                count++;
            } catch(Exception e){
                e.printStackTrace();
            }
        }
        snrAve = snrAve/((double)count);
        mAdapter.setSnr(snrAve);
        mFirebase.child("client").push().setValue(mPacket);
        mPacket.clear();
    }

    protected void parseSendResponse(String response){
        // get response Id if available
        String timestamp = new Date().toString();
        mAdapter.updateTimestamp(timestamp);
        List<String> res = Arrays.asList(response.split("\n"));
        mPacket.put("responseId", res.get(1).trim());
        mPacket.put("timestamp", timestamp);
        MdotSerialConsoleService.queryRssi(getActivity());
    }

    protected void parsePingResponse(String response){
        List<String> configuration = Arrays.asList(response.split("\n"));
        String ping = configuration.get(configuration.size() - 3);
        List<String> properties = Arrays.asList(ping.trim().split(","));
        double rssi = Double.parseDouble(properties.get(0));
        double snr = Double.parseDouble(properties.get(1));
        String timestamp = new Date().toString();
        mAdapter.setRssiAndSnr(rssi, snr, timestamp);

        Map<String, String> post = new HashMap<String, String>();
        post.put("rssi", properties.get(0));
        post.put("snr", properties.get(1));
        post.put("timestamp", timestamp);
        if(mLastLocation != null){
            post.put("lat", "" + mLastLocation.getLatitude());
            post.put("long", "" + mLastLocation.getLongitude());
        }
        mFirebase.child("ping").push().setValue(post);
    }

    protected void parseConfigurationResponse(String response){
        List<String> configuration = Arrays.asList(response.split("\n"));
        mAdapter.setConfigData(configuration);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mReceiver,
                new IntentFilter(MdotSerialConsoleService.NOTIFY_TRANSACTION_COMPLETE));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mReceiver);
    }

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
        mSend = (Button) view.findViewById(R.id.btn_send);

        mConnect.setOnClickListener(this);
        mDisconnect.setOnClickListener(this);
        mRefresh.setOnClickListener(this);
        mPing.setOnClickListener(this);
        mSend.setOnClickListener(this);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        LinearLayoutManager manager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(manager);
        mRecyclerView.setAdapter(getAdapter());
    }

    protected StatsAdapter getAdapter(){
        if(mAdapter == null){
            mAdapter = new StatsAdapter();
        }
        return mAdapter;
    }

    protected void connect(){
        MdotSerialConsoleService.connect(getActivity());
    }

    protected void disconnect(){
        MdotSerialConsoleService.disconnect(getActivity());
    }

    protected void refresh(){
        requestLocationUpdate();
        MdotSerialConsoleService.queryDeviceConfig(getActivity());
    }

    protected void ping(){
        requestLocationUpdate();
        MdotSerialConsoleService.sendPing(getActivity());
    }

    protected void send(){
        mSendLocation = true;
        requestLocationUpdate();
    }

    private void requestLocationUpdate(){
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_PERMISSION_LOCATION_CODE);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            Criteria mCriteria = new Criteria();
            mCriteria.setAccuracy(Criteria.ACCURACY_FINE);
            LocationManager locationManager = (LocationManager)getActivity().getSystemService(Context.LOCATION_SERVICE);
            locationManager.requestSingleUpdate(mCriteria, mLocationListener, null);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        requestLocationUpdate();
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
            case R.id.btn_send:
                send();
                break;
        }
    }
}
