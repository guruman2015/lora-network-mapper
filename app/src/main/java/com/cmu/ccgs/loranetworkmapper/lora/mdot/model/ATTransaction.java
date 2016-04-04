package com.cmu.ccgs.loranetworkmapper.lora.mdot.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by cef on 4/3/16.
 */
public class ATTransaction implements Parcelable {

    private String mCommand;
    private StringBuilder mResponseBuilder;

    public ATTransaction(String command){
        mCommand = command;
        mResponseBuilder = new StringBuilder();
    }

    protected ATTransaction(Parcel in) {
        mCommand = in.readString();
        mResponseBuilder = new StringBuilder().append(in.readString());
    }

    public void appendResponse(String data){
        mResponseBuilder.append(data);
    }

    public String getCommand(){
        return mCommand;
    }

    public String getResponse(){
        return mResponseBuilder.toString();
    }

    public boolean isOK(){
        return mResponseBuilder.toString().contains("OK");
    }

    public boolean isDone(){
        String response = mResponseBuilder.toString();
        return response.contains("OK") || response.contains("ERROR");
    }

    public static final Creator<ATTransaction> CREATOR = new Creator<ATTransaction>() {
        @Override
        public ATTransaction createFromParcel(Parcel in) {
            return new ATTransaction(in);
        }

        @Override
        public ATTransaction[] newArray(int size) {
            return new ATTransaction[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mCommand);
        dest.writeString(mResponseBuilder.toString());
    }
}
