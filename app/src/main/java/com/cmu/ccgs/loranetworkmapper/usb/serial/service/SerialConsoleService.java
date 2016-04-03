package com.cmu.ccgs.loranetworkmapper.usb.serial.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.hoho.android.usbserial.driver.UsbSerialDriver;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by cef on 3/25/16.
 */
public class SerialConsoleService extends Service {

    /*
     * Read and Write threads
     */
    private final ExecutorService mExecutor = Executors.newFixedThreadPool(2);

    /*
     * Commands: Connect, Disconnect, Send Command
     */

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    /*
     * Commands
     */
    protected void connect(){

    }

    protected void disconnect(){

    }

    protected void sendMessage(String message){

    }

    /*
     * USB management
     */
    protected UsbSerialDriver getSerialDriver(){
        return null;
    }
}
