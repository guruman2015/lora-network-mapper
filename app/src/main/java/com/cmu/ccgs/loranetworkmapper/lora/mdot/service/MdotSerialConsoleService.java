package com.cmu.ccgs.loranetworkmapper.lora.mdot.service;

import android.content.Context;
import android.content.Intent;

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
            default:
                ret = super.onStartCommand(intent, flags, startId);
                break;
        }
        stopSelf(startId);
        return ret;
    }

    protected void sendPing(){
        sendMessage("AT+PING\r\n");
    }

    protected void queryConfig(){
        sendMessage("AT&V\r\n");
    }

    protected void onNewData(String data){
        super.onNewData(data);
    }
}
