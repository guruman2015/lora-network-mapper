package com.cmu.ccgs.loranetworkmapper.fragment;

import android.Manifest;
import android.content.Context;
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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.cmu.ccgs.loranetworkmapper.R;
import com.cmu.ccgs.loranetworkmapper.adapter.StatsAdapter;
import com.cmu.ccgs.loranetworkmapper.lora.mdot.service.MdotSerialConsoleService;

/**
 * Created by cef on 4/2/16.
 */
public class StatsFragment extends Fragment implements View.OnClickListener {

    Button mConnect;
    Button mDisconnect;
    Button mRefresh;
    Button mPing;

    RecyclerView mRecyclerView;
    StatsAdapter mAdapter = null;

    private final int REQUEST_PERMISSION_LOCATION_CODE = 3;

    private LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            getAdapter().updateLocation(location.getLatitude(), location.getLongitude(), 0);
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

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_stats, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
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
        }
    }
}
