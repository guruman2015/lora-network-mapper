package com.cmu.ccgs.loranetworkmapper.usb.serial.service;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.cmu.ccgs.loranetworkmapper.MainActivity;
import com.cmu.ccgs.loranetworkmapper.lora.mdot.driver.MdotCdcAcmSerialDriver;
import com.cmu.ccgs.loranetworkmapper.lora.mdot.driver.UsbId;
import com.hoho.android.usbserial.driver.ProbeTable;
import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;
import com.hoho.android.usbserial.util.HexDump;
import com.hoho.android.usbserial.util.SerialInputOutputManager;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by cef on 3/25/16.
 */
public class SerialConsoleService extends Service {

    private static final String TAG = SerialConsoleService.class.getSimpleName();

    /*
     * Read and Write threads
     */
    private final ExecutorService mExecutor = Executors.newFixedThreadPool(2);

    private UsbSerialPort mPort = null;
    private SerialInputOutputManager mReadManager = null;
    private SerialInputOutputManager mWriteManager = null;

    private int BUFFER_SIZE = 1024;
    private StringBuffer mReceiveBuffer = new StringBuffer(BUFFER_SIZE);

    /*
     * Commands: Connect, Disconnect, Send Command
     */
    public static final String KEY_ACTION = "action";
    public static final String KEY_MESSAGE = "message";

    public static final String NOTIFY_CONNECTED = "com.cmu.ccgs.broadcast.message";
    public static final String NOTIFY_DISCONNECTED = "com.cmu.ccgs.broadcast.message";
    public static final String NOTIFY_MESSAGE_RECEIVED = "com.cmu.ccgs.broadcast.message";

    public static final int ACTION_CONNECT = 0;
    public static final int ACTION_DISCONNECT = 1;
    public static final int ACTION_SEND = 2;

    public static void connect(Context context){
        Intent i = new Intent(context, SerialConsoleService.class);
        i.putExtra(KEY_ACTION, ACTION_CONNECT);
        context.startService(i);
    }

    public static void disconnect(Context context){
        Intent i = new Intent(context, SerialConsoleService.class);
        i.putExtra(KEY_ACTION, ACTION_DISCONNECT);
        context.startService(i);
    }

    public static void sendMessage(Context context, String message){
        Intent i = new Intent(context, SerialConsoleService.class);
        i.putExtra(KEY_ACTION, ACTION_SEND);
        i.putExtra(KEY_MESSAGE, message);
        context.startService(i);
    }

    public void notifyConnected(){
        Intent intent = new Intent(NOTIFY_CONNECTED);
        LocalBroadcastManager.getInstance(SerialConsoleService.this).sendBroadcast(intent);
    }

    public void notifyDisconnected(){
        Intent intent = new Intent(NOTIFY_DISCONNECTED);
        LocalBroadcastManager.getInstance(SerialConsoleService.this).sendBroadcast(intent);
    }

    public void notifyUpdate(String message){
        Intent intent = new Intent(NOTIFY_MESSAGE_RECEIVED);
        intent.putExtra(KEY_MESSAGE, message);
        LocalBroadcastManager.getInstance(SerialConsoleService.this).sendBroadcast(intent);
    }

    private final SerialInputOutputManager.Listener mListener =
            new SerialInputOutputManager.Listener() {

                @Override
                public void onRunError(Exception e) {
                    Log.d(TAG, "Runner stopped.");
                }

                @Override
                public void onNewData(final byte[] data) {
                    Log.d(TAG, "Received: " + HexDump.dumpHexString(data));
                    mReceiveBuffer.append(new String(data));
                    String message = mReceiveBuffer.toString();
                    if(message.contains("\n")){
                        notifyUpdate(message.subSequence(0, message.lastIndexOf("\n")).toString());
                        mReceiveBuffer = new StringBuffer(BUFFER_SIZE);
                        mReceiveBuffer.append(message.substring(message.lastIndexOf("\n") + 1));
                    }
                }
            };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        switch(intent.getIntExtra(KEY_ACTION, 0)){
            case ACTION_CONNECT:
                connect();
                break;
            case ACTION_DISCONNECT:
                disconnect();
                break;
            case ACTION_SEND:
                sendMessage(intent.getStringExtra(KEY_MESSAGE));
                break;
        }
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        disconnect();
        super.onDestroy();
    }

    /*
         * Commands
         */
    protected void connect(){
        Intent i = new Intent(this, SerialConsoleService.class);
        PendingIntent pi = PendingIntent.getService(this, 0, i, 0);
        UsbSerialDriver driver = getSerialDriver();
        UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);
        UsbDeviceConnection connection = manager.openDevice(driver.getDevice());
        if(connection == null){
            manager.requestPermission(driver.getDevice(), pi);
        } else {
            disconnect();
            mPort = driver.getPorts().get(0);
            try {
                mPort.open(connection);
                mPort.setDTR(true);
                mPort.setParameters(115200, UsbSerialPort.DATABITS_8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);

                mReadManager = new SerialInputOutputManager(mPort, mListener, false);
                mWriteManager = new SerialInputOutputManager(mPort, mListener, true);

                mExecutor.submit(mReadManager);
                mExecutor.submit(mWriteManager);

                notifyConnected();

                Log.e(TAG, "Connected to " + mPort);
            } catch (IOException e){
                Log.e(TAG, "Failed to connect", e);
                mPort = null;
            }
        }
    }

    protected void disconnect(){
        if(mPort != null){
            mReadManager.stop();
            mWriteManager.stop();
            try {
                mPort.close();
            } catch (IOException e){
                e.printStackTrace();
            } finally {
                mPort = null;
            }
            mReadManager = null;
            mWriteManager = null;
            Log.e(TAG, "Disconnected");
        }
        notifyDisconnected();
        stopSelf();
    }

    protected void sendMessage(String message){
        if(mWriteManager != null){
            mWriteManager.writeAsync(message.getBytes());
        }
    }

    /*
     * USB management
     */
    protected UsbSerialDriver getSerialDriver(){
        UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);

        ProbeTable customTable = new ProbeTable();
        customTable.addProduct(UsbId.VENDOR_ID,
                UsbId.PRODUCT_ID,
                MdotCdcAcmSerialDriver.class);

        UsbSerialProber prober = new UsbSerialProber(customTable);
        List<UsbSerialDriver> availableDrivers = prober.findAllDrivers(manager);

        // Open a connection to the first available driver.
        UsbSerialDriver driver = availableDrivers.get(0);
        return driver;
    }
}
