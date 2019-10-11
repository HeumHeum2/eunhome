package com.example.eunhome;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

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
import com.google.gson.Gson;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

public class LightActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "LightActivity";
    private String topic = "topic/Light";
    private ImageView imgLightStatus;
    private AWSIotMqttManager mqttManager;
    private Button btLightPublish;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_light);

        Gson gson = new Gson();
        int position = getIntent().getIntExtra("position",9999);
        SharedPreferences userinfo = getSharedPreferences("userinfo",MODE_PRIVATE);
        String json = userinfo.getString("device","");
        UserInfo user = gson.fromJson(json,UserInfo.class);
        ArrayList<String> devicename = user.getDevices_name();

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(devicename.get(position)); // gson에서 기기별명을 가져온다.
        actionBar.setDisplayHomeAsUpEnabled(true);

        init();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.device_setting, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();

        if(id == R.id.action_menu){
            Toast.makeText(this, "세팅 클릭",Toast.LENGTH_SHORT).show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void init(){
        imgLightStatus = findViewById(R.id.imgLightStatus);
        btLightPublish = findViewById(R.id.btLightPublish);
        btLightPublish.setOnClickListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mqtt();
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
        final String[] message = new String[1];
        try {
            mqttManager.subscribeToTopic(topic, AWSIotMqttQos.QOS0,
                    new AWSIotMqttNewMessageCallback() {
                        @Override
                        public void onMessageArrived(final String topic, final byte[] data) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        message[0] = new String(data, "UTF-8");
                                        Log.d(TAG, "Message arrived:");
                                        Log.d(TAG, "Topic: " + topic);
                                        Log.d(TAG, "Message: " + message[0]);
                                        if(message[0].equals("ON")){
                                            imgLightStatus.setImageResource(R.drawable.ic_light_on);
                                            btLightPublish.setText(R.string.off);
                                        }else{
                                            imgLightStatus.setImageResource(R.drawable.ic_light_off);
                                            btLightPublish.setText(R.string.on);
                                        }
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

    @Override
    protected void onStop() {
        super.onStop();
        try{
            mqttManager.disconnect();
        }catch (Exception e){
            Log.e(TAG, "Disconnect error: ", e);
        }
    }
}
