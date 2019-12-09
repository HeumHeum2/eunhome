package com.example.eunhome;


import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.amazonaws.amplify.generated.graphql.CreateUserMutation;
import com.apollographql.apollo.GraphQLCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.google.gson.Gson;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import type.CreateUserInput;

public class RegisteringDeviceActivity extends AppCompatActivity {
    private static final String TAG = "RegisteringDeviceActivity";
    private String APssid, ssid, password, device;
    private WifiManager wifiManager, wifiScanner;
    private List<ScanResult> scanDatas; // ScanResult List
    private SharedPreferences userinfo;
    private ProgressBar LoadingBar;
    private String Djson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        setContentView(R.layout.activity_registering_device);
        init();
    }

    private void init(){
        LoadingBar = findViewById(R.id.deviceLoadingbar);
        LoadingBar.setVisibility(View.VISIBLE);

        APssid = getIntent().getStringExtra("APssid");
        ssid = getIntent().getStringExtra("ssid");
        password = getIntent().getStringExtra("password");
        device = getIntent().getStringExtra("device");

        //권한 설정
        TedPermission.with(this)
                .setPermissionListener(permissionlistener)
                .setRationaleConfirmText("권한이 필요해요")
                .setDeniedMessage("왜 거부하셨어요...\n하지만 [설정] > [권한] 에서 권한을 허용할 수 있어요.")
                .setPermissions(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.CHANGE_WIFI_STATE)
                .check();

        String url = "http://192.168.4.2/settings.html";
        NetworkTask networkTask = new NetworkTask(url,ssid, password);
        networkTask.execute();
    }

    public class NetworkTask extends AsyncTask<Void, Void,String>{
        private String url;
        private String ssid;
        private String password;

        public NetworkTask(String url, String ssid, String password){
            this.url = url;
            this.ssid = ssid;
            this.password = password;
        }

        @Override
        protected String doInBackground(Void... values) {
            try {
                //ssid, password 아두이노에 넘겨주기
                JSONObject postDataParams = new JSONObject();
                postDataParams.put("ssid", ssid);
                postDataParams.put("password", password);

                return RequestHandler.sendPost(url, postDataParams);

            } catch (Exception e) {
                return e.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String s){
            super.onPostExecute(s);
            //ssid와 password 넘겨준 상태
            Log.e(TAG, "onPostExecute: "+s);
            wifiManager = (WifiManager)getApplicationContext().getSystemService(WIFI_SERVICE);
            wifiManager.disconnect();
            scanWifi();
        }
    }

    @Override
    public void onBackPressed() { // 뒤로가기 막기
        //super.onBackPressed();
    }

    public void scanWifi() {
        wifiScanner = (WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        getApplicationContext().registerReceiver(registeringReceiver, intentFilter);
        Handler delayHandler = new Handler();
        delayHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                wifiScanner.startScan();
            }
         },20000);
    }

    private BroadcastReceiver registeringReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean success = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false);
            Log.e(TAG, "onReceive: 와이파이 스캔에 들어옴" );
            if(success){
                scanSuccess();
            }else{
                Log.e(TAG, "onReceive: 와이파이 이전 값을 가져옴");
                wifiScanner.startScan();
            }
        }
    };

    private void scanSuccess() {
        Log.e(TAG, "scanSuccess: 새로운 와이파이 목록");
        String status = "";
        scanDatas = wifiScanner.getScanResults();
        for(ScanResult scanResult : scanDatas){
            String SSID = scanResult.SSID;
            Log.e(TAG, "scanlist : "+SSID);
            if(SSID.contains(device)){
                Log.e(TAG, "APmode");
                status = "APmode";
                WifiConfiguration conf = new WifiConfiguration();
                conf.SSID = "\"" + SSID + "\"";
                conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE); // 개방형 네트워크의 경우
                wifiScanner.addNetwork(conf);
                List<WifiConfiguration> list = wifiScanner.getConfiguredNetworks();
                for( WifiConfiguration i : list ) {
                    if(i.SSID != null && i.SSID.equals("\"" + SSID + "\"")) { // APssid가 같은 것만
                        wifiScanner.disconnect();
                        wifiScanner.enableNetwork(i.networkId, true);
                        wifiScanner.reconnect(); // 수정해야함.
                        break;
                    }
                }
                LoadingBar.setVisibility(View.GONE);
                Toast.makeText(RegisteringDeviceActivity.this,"가정 내 와이파이를 다시 입력해주세요.", Toast.LENGTH_SHORT).show();
                finish();
                break;
            }
        }
        if(!status.equals("APmode")){
            save();
        }
    }

    private void save() {
        userinfo = getSharedPreferences("userinfo",MODE_PRIVATE);
        Gson gson = new Gson();
        String json = userinfo.getString("user","");
        Djson = userinfo.getString("device","");
        UserInfo user = gson.fromJson(json, UserInfo.class);
        String email = user.getEmail();
        ClientFactory.init(this); // AWSAppSyncClient 등록
        CreateUserInput input = CreateUserInput.builder()
                .id(APssid)
                .name(email)
                .device(device)
                .status("OFF")
                .build();
        CreateUserMutation addUserMutation = CreateUserMutation.builder().input(input).build();
        ClientFactory.appSyncClient().mutate(addUserMutation).enqueue(mutateCallback);
    }

    //DB 저장(dynomoDB)
    private GraphQLCall.Callback<CreateUserMutation.Data> mutateCallback = new GraphQLCall.Callback<CreateUserMutation.Data>() {
        @Override
        public void onResponse(@Nonnull final Response<CreateUserMutation.Data> response) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    LoadingBar.setVisibility(View.GONE);
                    ArrayList<String> devices = new ArrayList<>();
                    ArrayList<String> devicesName = new ArrayList<>();
                    ArrayList<String> devicesStatus = new ArrayList<>();
                    Gson gson = new Gson();
                    UserInfo userInfo;

                    if(!Djson.isEmpty()){
                        userInfo = gson.fromJson(Djson, UserInfo.class);
                        devices = userInfo.getDevices();
                        devicesName = userInfo.getDevices_name();
                        devicesStatus = userInfo.getDevices_status();
                        devices.add(APssid);
                        devicesName.add(device);
                        devicesStatus.add("OFF");
                    }else{
                        userInfo = new UserInfo();
                        devices.add(APssid);
                        devicesName.add(device);
                        devicesStatus.add("OFF");
                        userInfo.setDevices(devices);
                        userInfo.setDevices_name(devicesName);
                        userInfo.setDevices_status(devicesStatus);
                    }
                    SharedPreferences.Editor editor = userinfo.edit();
                    String json = gson.toJson(userInfo);
                    editor.putString("device", json);
                    editor.apply();

                    Intent intent = new Intent(RegisteringDeviceActivity.this, RegisteringSuccessActivity.class);
                    intent.putExtra("device",device);
                    startActivity(intent);
                    finish();
                }
            });
        }

        @Override
        public void onFailure(@Nonnull final ApolloException e) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.e(TAG, "Failed to perform AddUserMutation", e);
                    if(e.getLocalizedMessage().contains("failed due to conflict")){
                        LoadingBar.setVisibility(View.GONE);
                        Toast.makeText(getApplicationContext(),"이미 등록한 기기 입니다.",Toast.LENGTH_SHORT).show();
                    }
                    RegisteringDeviceActivity.this.finish();
                }
            });
        }
    };

    //권한 리스너
    PermissionListener permissionlistener = new PermissionListener() {
        @Override
        public void onPermissionGranted() {
            Log.e(TAG, "onPermissionGranted: 권한 허가");
        }

        @Override
        public void onPermissionDenied(List<String> deniedPermissions) {
            Log.e(TAG, "onPermissionDenied: 권한 거부"+deniedPermissions.toString());
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getApplicationContext().unregisterReceiver(registeringReceiver);
    }
}
