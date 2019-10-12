package com.example.eunhome;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;

public class WiFiSettingReFragment extends Fragment {

    private static final String TAG = "WiFiSettingReFragment";
    private WifiManager wifiManager;
    private RecyclerView recyclerView;
    private List<ScanResult> scanDatas; // ScanResult List
    private ArrayList<WifiData> mArrayList = new ArrayList<>();
    private WifiAdapter adapter;
    private ProgressBar loadingbar;
    private TextInputLayout pwLayout;
    private EditText inputPW;
    private String password = null;
    private SharedPreferences wifi;
    private ImageView imgScanWiFi;

    private ClickCallbackListener callbackListener = new ClickCallbackListener() {
        @Override
        public void callBack(String ssid, String cap) {
            readonly(ssid,cap);
        }

        @Override
        public void callBackno() {
            SharedPreferences wifi = getContext().getSharedPreferences("wifi",Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = wifi.edit();
            editor.clear();
            editor.apply();
            inputPW.setText("");
            inputPW.setEnabled(false);
            ((DeviceReConnectActivity)getActivity()).changeButton(""); // 다음 버튼 비활성화
        }
    };

    public WiFiSettingReFragment(){

    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        Log.e(TAG, "onCreate");

        wifiManager = (WifiManager) getContext().getSystemService(Context.WIFI_SERVICE);
        //와이파이 연결 확인
        if(!wifiManager.isWifiEnabled()){
            Toast.makeText(getContext(),"와이파이가 비활성화 되어있습니다. ... 와이파이 연결이 필요합니다.",Toast.LENGTH_SHORT).show();
            Log.e(TAG, "와이파이가 비활성화 되어있습니다. ... 와이파이 연결이 필요합니다." );
            wifiManager.setWifiEnabled(true);
        }
        wifi = getContext().getSharedPreferences("wifi",Context.MODE_PRIVATE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.e(TAG, "onCreateView" );
        View view = inflater.inflate(R.layout.frgment_wifi_setting, container,false);
        loadingbar = view.findViewById(R.id.loadingbar);

        imgScanWiFi = view.findViewById(R.id.imgScanWiFi);
        imgScanWiFi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setSelected(true);
                scanWifi();

            }
        });
        pwLayout = view.findViewById(R.id.pwLayout);
        inputPW = pwLayout.getEditText();
        recyclerView = view.findViewById(R.id.wifiRecyclerView);
        adapter = new WifiAdapter(getContext(), mArrayList);
        adapter.setCallbackListener(callbackListener);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        scanWifi();
        return view;
    }

    private void scanWifi() {
        mArrayList.clear();
        IntentFilter intentFilter = new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        loadingbar.setVisibility(View.VISIBLE);
        getContext().registerReceiver(receiver, intentFilter);
        wifiManager.startScan();
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e(TAG, "onReceive check : "+intent.getAction());
            final String action = intent.getAction();
            if(action.equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)){
                wifiManager.startScan();
                scanDatas = wifiManager.getScanResults();
                context.unregisterReceiver(this);
                for(ScanResult scanResult : scanDatas){
                    String SSID = scanResult.SSID;
                    String CAP = scanResult.capabilities;
                    int RSSI = scanResult.level;
                    Log.e(TAG, "onReceive: "+SSID);
                    Log.e(TAG, "onReceive: "+scanResult.capabilities);
                    Log.e(TAG, "onReceive: "+RSSI);
                    WifiData wifiData = new WifiData(SSID,CAP,RSSI);
                    mArrayList.add(wifiData);
                    adapter.notifyDataSetChanged();
                }
                loadingbar.setVisibility(View.GONE);
            }else if(action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)){
                getContext().sendBroadcast(new Intent("wifi.ON_NETWORK_STATE_CHANGED"));
            }
        }
    };

    @Override
    public void onResume() {
        super.onResume();


        inputPW.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                
            }

            @Override
            public void afterTextChanged(Editable s) {
                password = s.toString().trim();
                ((DeviceReConnectActivity)getActivity()).changeButton(password); // 다음 버튼 활성화
                SharedPreferences.Editor editor = wifi.edit();
                editor.putString("password", password);
                editor.apply();
            }
        });
    }

    public void readonly(String ssid, String cap){
        SharedPreferences.Editor editor = wifi.edit();
        editor.putString("ssid", ssid);
        editor.apply();
        if(cap.contains("WPA")){
            ((DeviceReConnectActivity)getActivity()).changeButton(""); // 다음 버튼 비활성화
            inputPW.setEnabled(true); // 비밀번호 readonly 해제
            inputPW.requestFocus();
        }else{
            editor.putString("password","");
            editor.apply();
            inputPW.setText("");
            inputPW.setEnabled(false); // 비밀번호 readonly
            ((DeviceReConnectActivity)getActivity()).changeButton(ssid); // 다음 버튼 활성화
        }
    }
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.e(TAG, "onActivityCreated: " );
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.e(TAG, "onStart" );
    }
}
