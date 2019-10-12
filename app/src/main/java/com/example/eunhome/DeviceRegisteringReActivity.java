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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import org.json.JSONObject;

import java.util.List;

public class DeviceRegisteringReActivity extends AppCompatActivity {
    private static final String TAG = "DeviceRegisteringReActivity";
    private ProgressBar circularProgressbar;
    private TextView textLoadingPercent;
    private String APssid, ssid, password, device;
    private WifiManager wifiManager, wifiScanner;
    private List<ScanResult> scanDatas; // ScanResult List
    private int value = 0;
    private SharedPreferences userinfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        setContentView(R.layout.activity_registering_device);
        init();
    }

    private void init(){
        circularProgressbar = findViewById(R.id.circularProgressbar);
        textLoadingPercent = findViewById(R.id.textLoadingPercent);
        APssid = getIntent().getStringExtra("APssid");
        ssid = getIntent().getStringExtra("ssid");
        password = getIntent().getStringExtra("password");
        device = getIntent().getStringExtra("device");

        //권한 설정
        TedPermission.with(this)
                .setPermissionListener(permissionlistener)
                .setRationaleConfirmText("권한이 필요해요")
                .setDeniedMessage("왜 거부하셨어요...\n하지만 [설정] > [권한] 에서 권한을 허용할 수 있어요.")
                .setPermissions(Manifest.permission.ACCESS_COARSE_LOCATION)
                .setPermissions(Manifest.permission.CHANGE_WIFI_STATE)
                .check();

        String url = "http://192.168.4.2/settings.html";
        NetworkTask networkTask = new NetworkTask(url,ssid, password);
        networkTask.execute();
    }

    public class NetworkTask extends AsyncTask<Integer, Integer,String>{
        private String url;
        private String ssid;
        private String password;

        public NetworkTask(String url, String ssid, String password){
            this.url = url;
            this.ssid = ssid;
            this.password = password;
        }

        @Override
        protected void onPreExecute(){
            value = 0;
            circularProgressbar.setProgress(value);
            textLoadingPercent.setText(value+"%");
        }

        @Override
        protected String doInBackground(Integer... values) {
            while(!isCancelled()){
                value++;
                if(value >= 60){
                    break;
                }else{
                    publishProgress(value);
                }
                try{
                    Thread.sleep(100);
                }catch (InterruptedException e){
                    Log.e(TAG, "doInBackground: ",e);
                }
            }
            try {
                //ssid, password 아두이노에 넘겨주기
                JSONObject postDataParams = new JSONObject();
                postDataParams.put("ssid",ssid);
                postDataParams.put("password",password);

                return RequestHandler.sendPost(url,postDataParams);

            } catch (Exception e) {
                return e.getMessage();
            }
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            circularProgressbar.setProgress(values[0].intValue());
            textLoadingPercent.setText(values[0].toString()+"%");
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
        getApplicationContext().registerReceiver(deviceReceiver, intentFilter);
        Handler delayHandler = new Handler();
        delayHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
//                if(wifiScanner.isScanAlwaysAvailable()) {
//                    Log.e(TAG, "와이파이 활성화");
//                    wifiScanner.startScan();
//                }else{
//                    Log.e(TAG, "와이파이 비활성화");
//                    wifiScanner.setWifiEnabled(true);
//                    scanWifi();
//                }
                wifiScanner.startScan();
            }
         },18000);
    }

    private BroadcastReceiver deviceReceiver = new BroadcastReceiver() {
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
                Toast.makeText(DeviceRegisteringReActivity.this,"가정 내 와이파이를 다시 입력해주세요.", Toast.LENGTH_SHORT).show();
                finish();
                break;
            }
        }
        if(!status.equals("APmode")){
            BackgroundTask backgroundTask = new BackgroundTask();
            backgroundTask.execute();
        }
    }

    //권한 리스너
    PermissionListener permissionlistener = new PermissionListener() {
        @Override
        public void onPermissionGranted() {
//            Toast.makeText(DeviceSettingActivity.this, "권한 허가", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "onPermissionGranted: 권한 허가");
        }

        @Override
        public void onPermissionDenied(List<String> deniedPermissions) {
            Log.e(TAG, "onPermissionDenied: 권한 거부"+deniedPermissions.toString());
//            Toast.makeText(DeviceSettingActivity.this, "권한 거부\n" + deniedPermissions.toString(), Toast.LENGTH_SHORT).show();
        }
    };

    class BackgroundTask extends AsyncTask<Integer, Integer, Integer> {

        protected void onPreExecute() {
            circularProgressbar.setProgress(value);
        }

        protected Integer doInBackground(Integer ... values) {
            while (!isCancelled()) {
                value++;
                if (value >= 100) {
                    break;
                } else {
                    publishProgress(value);
                }

                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {}
            }
            return value;
        }

        protected void onProgressUpdate(Integer ... values) {
            circularProgressbar.setProgress(values[0]);
            textLoadingPercent.setText(values[0].toString()+"%");
        }

        protected void onPostExecute(Integer result) {
            circularProgressbar.setProgress(100);
            textLoadingPercent.setText("100%");
            Toast.makeText(DeviceRegisteringReActivity.this, "연결되었습니다.", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(DeviceRegisteringReActivity.this, LightActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra("position",getIntent().getIntExtra("position",9999));
            startActivity(intent);
            finish();
        }
    }
}
