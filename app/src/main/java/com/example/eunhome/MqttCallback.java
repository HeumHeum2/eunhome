package com.example.eunhome;

import android.util.Log;

import com.amazonaws.mobileconnectors.iot.AWSIotMqttManager;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttNewMessageCallback;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

public class MqttCallback implements AWSIotMqttNewMessageCallback {

    private String TAG = "LoginFragment";

    private ArrayList<String> devices_status;
    private AWSIotMqttManager mqttManager;
    private String message;

    MqttCallback(ArrayList<String> status, AWSIotMqttManager mqttManager){
        this.devices_status = status;
        this.mqttManager = mqttManager;
        this.message = "";
    }

    @Override
    public void onMessageArrived(final String topic, final byte[] data) {
        try {
            message = new String(data, "UTF-8");
            if(message.equals("OFF")){
                devices_status.add("OFF");
            }else{
                devices_status.add("ON");
            }
            mqttManager.unsubscribeTopic(topic);

        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "Message encoding error.", e);
        }
    }

    public String getMessage() {
        return message;
    }

    public ArrayList<String> getDevices_status() {
        return devices_status;
    }
}
