package com.example.eunhome;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class DeviceWiFiReFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = "DeviceWiFiReFragment";
    private String device;

    private WifiManager wifiManager;
    private RecyclerView recyclerView;
    private List<ScanResult> scanDatas; // ScanResult List
    private ArrayList<WifiData> arrayList = new ArrayList<>();
    private DeviceWifiAdapter adapter;
    private ProgressBar wifiLoadingbar;
    private ImageView imgScanWiFi;
    private TextView textNotfound;

    private ClickCallbackListener callbackListener = new ClickCallbackListener() {
        @Override
        public void callBack(String ssid, String cap) {
            Log.e(TAG, "callBack ssid :"+ssid+" cap : "+cap);
            SharedPreferences wifi = getContext().getSharedPreferences("wifi",Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = wifi.edit();
            editor.putString("APssid",ssid);
            editor.apply();
            ((DeviceReConnectActivity)getActivity()).changeButton(ssid); // 다음 버튼 비활성화
        }

        @Override
        public void callBackno() {
            ((DeviceReConnectActivity)getActivity()).changeButton(""); // 다음 버튼 비활성화
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(TAG, "onCreate" );
        //장치 확인
        device = getActivity().getIntent().getStringExtra("device");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.e(TAG, "onCreateView" );
        View view = inflater.inflate(R.layout.fragment_device_wifi_setting, container, false);

        imgScanWiFi = view.findViewById(R.id.imgScanWiFidevice);
        textNotfound = view.findViewById(R.id.textNotfound);
        textNotfound.setVisibility(View.GONE);
        recyclerView = view.findViewById(R.id.deviceWIFiRecyclerView);
        wifiLoadingbar = view.findViewById(R.id.wifiLoadingbar);
        adapter = new DeviceWifiAdapter(getContext(), arrayList);
        adapter.setCallbackListener(callbackListener);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        scanWifi();

        imgScanWiFi.setOnClickListener(this);
        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.imgScanWiFidevice :
                imgScanWiFi.setSelected(true);
                scanWifi();
                break;
        }
    }

    public void scanWifi() {
        wifiManager = (WifiManager) getContext().getSystemService(Context.WIFI_SERVICE);
        arrayList.clear();
        IntentFilter intentFilter = new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        wifiLoadingbar.setVisibility(View.VISIBLE);
        textNotfound.setVisibility(View.GONE);
        getContext().registerReceiver(deviceReceiver, intentFilter);
        wifiManager.startScan();
    }

    private BroadcastReceiver deviceReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e(TAG, "onReceive check : "+intent.getAction());
            final String action = intent.getAction();
            if(action.equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)){
                wifiManager.startScan();
                scanDatas = wifiManager.getScanResults();
                context.unregisterReceiver(this);
                int check=0;
                for(ScanResult scanResult : scanDatas){
                    String SSID = scanResult.SSID;
                    String CAP = scanResult.capabilities;
                    int RSSI = scanResult.level;
                    Log.e(TAG, "onReceive: "+SSID);
                    Log.e(TAG, "onReceive: "+scanResult.capabilities);
                    Log.e(TAG, "onReceive: "+RSSI);
                    if(SSID.contains(device)){
                        WifiData wifiData = new WifiData(SSID,CAP,RSSI);
                        arrayList.add(wifiData);
                        adapter.notifyDataSetChanged();
                        check++;
                    }
                }
                if(check == 0){
                    Log.e(TAG, "onReceive: "+device);
                    textNotfound.setVisibility(View.VISIBLE);
                }else{
                    textNotfound.setVisibility(View.GONE);
                }
                wifiLoadingbar.setVisibility(View.GONE);
            }else if(action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)){
                getContext().sendBroadcast(new Intent("wifi.ON_NETWORK_STATE_CHANGED"));
            }
        }
    };
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.e(TAG, "onActivityCreated" );
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.e(TAG, "onStart" );
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.e(TAG, "onResume ");
    }
}
