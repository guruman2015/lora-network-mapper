package com.cmu.ccgs.loranetworkmapper.lora.mdot.service;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.cmu.ccgs.loranetworkmapper.lora.mdot.model.ATTransaction;
import com.cmu.ccgs.loranetworkmapper.usb.serial.service.SerialConsoleService;

/**
 * Created by cef on 4/3/16.
 */
public class MdotSerialConsoleService extends SerialConsoleService {

    protected static Class getServiceClass(){
        return MdotSerialConsoleService.class;
    }

    public static final int ACTION_SEND_PING = 3;
    public static final int ACTION_QUERY_CONFIG = 4;
    public static final int ACTION_SEND_PACKET = 5;
    public static final int ACTION_QUERY_RSSI = 6;
    public static final int ACTION_QUERY_SNR = 7;

    private ATTransaction mTransaction = null;

    public static final String NOTIFY_TRANSACTION_COMPLETE = "com.cmu.ccgs.broadcast.transaction";
    public static final String KEY_TRANSACTION = "transaction";

    public static final String KEY_PACKET = "packet";

    public static void sendPing(Context context){
        Intent i = new Intent(context, getServiceClass());
        i.putExtra(KEY_ACTION, ACTION_SEND_PING);
        context.startService(i);
    }

    public static void queryDeviceConfig(Context context){
        Intent i = new Intent(context, getServiceClass());
        i.putExtra(KEY_ACTION, ACTION_QUERY_CONFIG);
        context.startService(i);
    }

    public static void sendPacket(Context context, String packet){
        Intent i = new Intent(context, getServiceClass());
        i.putExtra(KEY_ACTION, ACTION_SEND_PACKET);
        i.putExtra(KEY_PACKET, packet);
        context.startService(i);
    }

    public static void queryRssi(Context context){
        Intent i = new Intent(context, getServiceClass());
        i.putExtra(KEY_ACTION, ACTION_QUERY_RSSI);
        context.startService(i);
    }

    public static void querySnr(Context context){
        Intent i = new Intent(context, getServiceClass());
        i.putExtra(KEY_ACTION, ACTION_QUERY_SNR);
        context.startService(i);
    }

    public void notifyTransaction(ATTransaction transaction){
        Intent intent = new Intent(NOTIFY_TRANSACTION_COMPLETE);
        intent.putExtra(KEY_TRANSACTION, transaction);
        LocalBroadcastManager.getInstance(MdotSerialConsoleService.this).sendBroadcast(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int ret = START_STICKY;
        switch(intent.getIntExtra(KEY_ACTION, 0)){
            case ACTION_SEND_PING:
                sendPing();
                break;
            case ACTION_QUERY_CONFIG:
                queryConfig();
                break;
            case ACTION_SEND_PACKET:
                sendPacket(intent.getStringExtra(KEY_PACKET));
                break;
            case ACTION_QUERY_RSSI:
                queryRssi();
                break;
            case ACTION_QUERY_SNR:
                querySnr();
                break;
            default:
                ret = super.onStartCommand(intent, flags, startId);
                break;
        }
        stopSelf(startId);
        return ret;
    }

    protected void sendPing(){
        sendCommand("AT+PING\r\n");
    }

    protected void queryConfig(){
        sendCommand("AT&V\r\n");
    }

    protected void sendPacket(String packet){
        String command = "AT+SEND=" + packet + "\r\n";
        sendCommand(command);
    }

    protected void queryRssi(){
        sendCommand("AT+RSSI\r\n");
    }

    protected void querySnr(){
        sendCommand("AT+SNR\r\n");
    }

    protected void sendCommand(String command){
        mTransaction = new ATTransaction(command);
        sendMessage(command);
    }

    protected void onNewData(String data){
        super.onNewData(data);

        if(mTransaction != null){
            mTransaction.appendResponse(data);
            if(mTransaction.isDone()){
                notifyTransaction(mTransaction);
                mTransaction = null;
            }
        }

    }
}
