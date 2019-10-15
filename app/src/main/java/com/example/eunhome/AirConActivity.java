package com.example.eunhome;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.amazonaws.amplify.generated.graphql.UpdateUserMutation;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttClientStatusCallback;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttManager;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttNewMessageCallback;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttQos;
import com.amazonaws.regions.Region;
import com.amazonaws.services.iot.AWSIotClient;
import com.amazonaws.services.iot.model.AttachPolicyRequest;
import com.apollographql.apollo.GraphQLCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import javax.annotation.Nonnull;

import type.UpdateUserInput;

public class AirConActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "AirConActivity";
    private String topic = "outTopic/AirCon";
    private String inTopic = "inTopic/AirCon";
    private AWSIotMqttManager mqttManager;
    private ArrayList<String> device;
    private ArrayList<String> devicename;
    private ProgressBar deviceProgressBar;
    private int position;
    private String changeDeviceName, powerstatus="" , tempstatus;
    private SharedPreferences userinfo;
    private Gson gson;
    private ActionBar actionBar;
    private boolean backcheck = false;
    private TextView textTemp, textHumi, textTempSetting;
    private FloatingActionButton fbtnAdd, fbtnMinus;
    private ConstraintLayout PowerOFF, PowerON, PowerSet;
    private ImageButton btnPower;
    private SharedPreferences SharedTemp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_air_con);

        gson = new Gson();
        position = getIntent().getIntExtra("position",9999);
        userinfo = getSharedPreferences("userinfo",MODE_PRIVATE);
        String json = userinfo.getString("device","");
        UserInfo user = gson.fromJson(json,UserInfo.class);
        devicename = user.getDevices_name();
        device = user.getDevices();

        actionBar = getSupportActionBar();
        actionBar.setTitle(devicename.get(position)); // gson에서 기기이름을 가져온다.
        actionBar.setDisplayHomeAsUpEnabled(true);

        init();
    }

    private void init(){
        deviceProgressBar = findViewById(R.id.airconProgressBar);

        PowerOFF = findViewById(R.id.PowerOFF);
        PowerON = findViewById(R.id.PowerON);
        PowerSet = findViewById(R.id.PowerSet);

        textTemp = findViewById(R.id.textTemp);
        textHumi = findViewById(R.id.textHumi);
        textTempSetting = findViewById(R.id.textTempSetting);
        fbtnAdd = findViewById(R.id.fbtnAdd);
        fbtnMinus = findViewById(R.id.fbtnMinus);
        btnPower = findViewById(R.id.btnPower);

        PowerON.setVisibility(View.INVISIBLE);
        PowerOFF.setVisibility(View.INVISIBLE);
        PowerSet.setVisibility(View.INVISIBLE);

        SharedTemp = getSharedPreferences("temp",MODE_PRIVATE);
        tempstatus = SharedTemp.getString("tempset","24");

        textTempSetting.setText(tempstatus);

        fbtnAdd.setOnClickListener(this);
        fbtnMinus.setOnClickListener(this);
        btnPower.setOnClickListener(this);
    }

    //액션바 세팅
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.device_setting, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        if (item.getItemId() == R.id.action_rename) {
            Log.d(TAG, "이름 변경 클릭중");
            dialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        backcheck = false;
        mqtt();
    }

    private void mqtt() {
        String clientid = AWSMobileClient.getInstance().getIdentityId();
        String endpoint = "a2lewy1etbgc6q-ats.iot.ap-northeast-2.amazonaws.com";
        mqttManager = new AWSIotMqttManager(clientid, endpoint);
        deviceProgressBar.setVisibility(View.VISIBLE);
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
                    }else if(status.toString().equals("Reconnecting")){
                        mqttManager.disconnect();
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
                                        PowerSet.setVisibility(View.VISIBLE);
                                        String message = new String(data, "UTF-8");
                                        Log.d(TAG, "Message arrived:");
                                        Log.d(TAG, "Topic: " + topic);
                                        Log.d(TAG, "Message: " + message);
                                        if(message.equals("OFF")){
                                            PowerON.setVisibility(View.GONE);
                                            PowerOFF.setVisibility(View.VISIBLE);
                                            btnPower.setImageResource(R.drawable.ic_power_off);

                                            powerstatus = "ON";
                                        }else {
                                            JSONObject jsonObject = new JSONObject(message);
                                            String temp = jsonObject.getString("tempvalue");
                                            String humi = jsonObject.getString("humivalue");
                                            Log.d(TAG, "temp: " + temp);
                                            Log.d(TAG, "humi: " + humi);
                                            textTemp.setText(getString(R.string.temp, temp));
                                            textHumi.setText(getString(R.string.humi, humi));

                                            btnPower.setImageResource(R.drawable.ic_power_on);
                                            PowerON.setVisibility(View.VISIBLE);
                                            PowerOFF.setVisibility(View.GONE);

                                            powerstatus = "OFF";

                                            publish();
                                        }
                                        deviceProgressBar.setVisibility(View.GONE);
                                    } catch (UnsupportedEncodingException e) {
                                        Log.e(TAG, "Message encoding error.", e);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                        }
                    });
        } catch (Exception e) {
            Log.e(TAG, "Subscription error.", e);
        }
    }

    private void publish() {
        try {
            mqttManager.publishString(tempstatus, inTopic, AWSIotMqttQos.QOS0);
        } catch (Exception e) {
            Log.e(TAG, "Publish error.", e);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Handler delayhandler = new Handler();
        delayhandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "실행");
                if(!backcheck && PowerSet.getVisibility() == View.INVISIBLE){
                    Toast.makeText(getApplicationContext(),"인터넷 신호가 약합니다. 다시 연결해주세요.",Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(AirConActivity.this, DeviceReConnectActivity.class);
                    intent.putExtra("device",device.get(position));
                    intent.putExtra("position",position);
                    startActivity(intent);
                }
            }
        }, 10000); // 시간설정
    }

    private void dialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(AirConActivity.this);

        dialog.setTitle("기기이름변경");

        //EditText 설정
        final EditText name = new EditText(AirConActivity.this);
        dialog.setView(name);

        name.setText(devicename.get(position));

        //변경 버튼 설정
        dialog.setPositiveButton("변경", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.d(TAG, "변경 클릭");
                changeDeviceName = name.getText().toString().trim();
                mutation();
                dialog.dismiss();
            }
        });

        dialog.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.d(TAG, "취소 클릭");
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        backcheck = true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        try{
            if(powerstatus.equals("ON") || powerstatus.isEmpty()){
                Log.e(TAG, "onStop:");
                mqttManager.disconnect();
                backcheck = true;
            }else{
                backcheck = false;
            }
        }catch (Exception e){
            Log.e(TAG, "Disconnect error: ", e);
        }
    }

    private void mutation() {
        ClientFactory.init(getApplicationContext());
        UpdateUserInput updateUserInput = UpdateUserInput.builder()
                .id(device.get(position))
                .device(changeDeviceName)
                .build();

        UpdateUserMutation updateUserMutation = UpdateUserMutation.builder().input(updateUserInput).build();
        ClientFactory.appSyncClient().mutate(updateUserMutation).enqueue(mutationCallback);
    }

    private GraphQLCall.Callback<UpdateUserMutation.Data> mutationCallback = new GraphQLCall.Callback<UpdateUserMutation.Data>() {
        @Override
        public void onResponse(@Nonnull final Response<UpdateUserMutation.Data> response) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.i(TAG, "Update User :"+response.data().updateUser().id());
                    Log.i(TAG, "Update User :"+response.data().updateUser().device());
                    //쉐어드에 저장되어있던 것도 업데이트 시켜줘야함.
                    UserInfo userInfo = new UserInfo();
                    devicename.set(position, changeDeviceName);
                    userInfo.setDevices(device);
                    userInfo.setDevices_name(devicename);
                    SharedPreferences.Editor editor = userinfo.edit();
                    gson = new Gson();
                    String json = gson.toJson(userInfo);
                    editor.remove("device");
                    editor.putString("device",json);
                    editor.apply();
                    actionBar.setTitle(changeDeviceName);
                    Toast.makeText(getApplicationContext(), "변경 되었습니다.",Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public void onFailure(@Nonnull ApolloException e) {

        }
    };

    @Override
    public void onClick(View v) {
        int tempset = Integer.parseInt(tempstatus);
        SharedPreferences.Editor editor = SharedTemp.edit();
        switch (v.getId()){
            case R.id.fbtnAdd :
                ++tempset;
                tempstatus = Integer.toString(tempset);
                textTempSetting.setText(tempstatus);
                editor.putString("tempset",tempstatus);
                editor.apply();
                break;
            case R.id.fbtnMinus :
                --tempset;
                tempstatus = Integer.toString(tempset);
                textTempSetting.setText(tempstatus);
                editor.putString("tempset",tempstatus);
                editor.apply();
                break;
            case R.id.btnPower :
                publishPower();
                break;
        }
    }

    private void publishPower(){
        try {
            PowerON.setVisibility(View.INVISIBLE);
            PowerOFF.setVisibility(View.INVISIBLE);
            deviceProgressBar.setVisibility(View.VISIBLE);
            mqttManager.publishString(powerstatus, inTopic, AWSIotMqttQos.QOS0);
        } catch (Exception e) {
            Log.e(TAG, "Publish error.", e);
        }
    }
}
