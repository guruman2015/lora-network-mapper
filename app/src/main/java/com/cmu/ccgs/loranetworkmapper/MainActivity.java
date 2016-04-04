package com.cmu.ccgs.loranetworkmapper;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.cmu.ccgs.loranetworkmapper.fragment.SerialConsoleFragment;
import com.cmu.ccgs.loranetworkmapper.fragment.StatsFragment;
import com.cmu.ccgs.loranetworkmapper.lora.mdot.service.MdotSerialConsoleService;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;

public class MainActivity extends AppCompatActivity {

    private ViewPager mViewPager;

    private GoogleMap mGoogleMap;

    private TabLayout mTabLayout;

    private final int REQUEST_PERMISSION_LOCATION_CODE = 0;

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d("MdotActivity", "Service connected");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d("MdotActivity", "Service disconnected");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mTabLayout = (TabLayout) findViewById(R.id.tabs);

        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        mViewPager.setAdapter(new ViewPagerAdapter(getSupportFragmentManager()));
        mTabLayout.setupWithViewPager(mViewPager);

        Intent i = new Intent(this, MdotSerialConsoleService.class);
        bindService(i, mConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mConnection);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        MdotSerialConsoleService.disconnect(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void requestLocationPermissions(){
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_PERMISSION_LOCATION_CODE);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            mGoogleMap.setMyLocationEnabled(true);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch(requestCode){
            case REQUEST_PERMISSION_LOCATION_CODE:
                requestLocationPermissions();
                break;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private class ViewPagerAdapter extends FragmentPagerAdapter {

        public ViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment = null;
            switch (position){
                case 0:
                    fragment = new StatsFragment();
                    break;
                case 1: {
                    SupportMapFragment map = SupportMapFragment.newInstance();
                    map.getMapAsync(new OnMapReadyCallback() {
                        @Override
                        public void onMapReady(GoogleMap googleMap) {
                            mGoogleMap = googleMap;
                            requestLocationPermissions();
                        }
                    });
                    fragment = map;
                }
                break;
                case 2:
                    fragment = new SerialConsoleFragment();
                    break;
            }
            return fragment;
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            String title = "";
            switch (position){
                case 0:
                    title = "Stats";
                    break;
                case 1:
                    title = "Map";
                    break;
                case 2:
                    title = "Console";
                    break;
            }
            return title;
        }
    }
}
