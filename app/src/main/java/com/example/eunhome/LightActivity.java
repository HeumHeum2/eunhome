package com.example.eunhome;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttClientStatusCallback;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttManager;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttNewMessageCallback;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttQos;
import com.amazonaws.regions.Region;
import com.amazonaws.services.iot.AWSIotClient;
import com.amazonaws.services.iot.model.AttachPolicyRequest;

import java.io.UnsupportedEncodingException;

public class LightActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "LightActivity";
    private String topic = "topic/Light";
    private TextView textLightStatus;
    private ImageView imgLightStatus;
    private AWSIotMqttManager mqttManager;
    private Button btLightPublish;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_light);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        init();
    }

    private void init(){
        textLightStatus = findViewById(R.id.textLightStatus);
        imgLightStatus = findViewById(R.id.imgLightStatus);
        btLightPublish = findViewById(R.id.btLightPublish);
        mqtt();

        btLightPublish.setOnClickListener(this);
    }

    private void mqtt() {
        String clientid = AWSMobileClient.getInstance().getIdentityId();
        String endpoint = "a2lewy1etbgc6q-ats.iot.ap-northeast-2.amazonaws.com";
        mqttManager = new AWSIotMqttManager(clientid, endpoint);
        awsinit(clientid);
    }

    public void awsinit(final String clientid){
        Thread setting = new Thread(new Runnable() {
            @Override
            public void run(){
                AttachPolicyRequest attachPolicyRequest = new AttachPolicyRequest();
                attachPolicyRequest.setPolicyName("IoTPolicy");
                attachPolicyRequest.setTarget(clientid);
                AWSIotClient mIoTAndroidClient = new AWSIotClient(AWSMobileClient.getInstance());
                mIoTAndroidClient.setRegion(Region.getRegion("ap-northeast-2"));
                mIoTAndroidClient.attachPolicy(attachPolicyRequest);
                connect();
            }
        });
        setting.start();
        try {
            setting.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void connect(){
        try{
            mqttManager.connect(AWSMobileClient.getInstance(), new AWSIotMqttClientStatusCallback() {
                @Override
                public void onStatusChanged(AWSIotMqttClientStatus status, Throwable throwable) {
                    Log.d(TAG, "Connection Status :  "+status);
                    if(status.toString().equals("Connected")){
                        subscribe();
                    }
                }
            });
        }catch (final Exception e){
            Log.e(TAG, "Connection error", e);
        }
    }

    public void subscribe() {
        Log.d(TAG, "topic = " + topic);
        try {
            mqttManager.subscribeToTopic(topic, AWSIotMqttQos.QOS0,
                    new AWSIotMqttNewMessageCallback() {
                        @Override
                        public void onMessageArrived(final String topic, final byte[] data) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        String message = new String(data, "UTF-8");
                                        Log.d(TAG, "Message arrived:");
                                        Log.d(TAG, "Topic: " + topic);
                                        Log.d(TAG, "Message: " + message);
                                        if(message.equals("ON")){
                                            imgLightStatus.setImageResource(R.drawable.ic_light_on);
                                            btLightPublish.setText(R.string.off);
                                        }else{
                                            imgLightStatus.setImageResource(R.drawable.ic_light_off);
                                            btLightPublish.setText(R.string.on);
                                        }
                                        textLightStatus.setText(message);
                                    } catch (UnsupportedEncodingException e) {
                                        Log.e(TAG, "Message encoding error.", e);
                                    }
                                }
                            });
                        }
                    });
        } catch (Exception e) {
            Log.e(TAG, "Subscription error.", e);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btLightPublish :
                String strPublish = btLightPublish.getText().toString();
                if(!strPublish.isEmpty()){
                    Log.d(TAG, "onClick: 비어있지 않아!");
                    publish(strPublish);
                }else{
                    Log.d(TAG, "onClick: 비어있어");
                }
                break;
        }
    }

    public void publish(String message) {
        try {
            mqttManager.publishString(message, "inTopic", AWSIotMqttQos.QOS0);
        } catch (Exception e) {
            Log.e(TAG, "Publish error.", e);
        }
    }
}
