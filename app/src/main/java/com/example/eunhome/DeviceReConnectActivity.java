package com.example.eunhome;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.util.List;

public class DeviceReConnectActivity extends AppCompatActivity {
    private static final String TAG = "DeviceReConnectActivity";

    private NonSwipeableViewPager pager;
    private CircleAnimIndicator circleAnimIndicator;
    private Button btPrevious, btNext;
    private MoviePagerAdapter adapter;
    private int pagecheck=0;
    private String APssid, ssid, password, device;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_setting);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN); // editText가 키보드 위로 올라가도록 설정

        //권한 설정
        TedPermission.with(this)
                .setPermissionListener(permissionlistener)
                .setRationaleConfirmText("권한이 필요해요")
                .setDeniedMessage("왜 거부하셨어요...\n하지만 [설정] > [권한] 에서 권한을 허용할 수 있어요.")
                .setPermissions(Manifest.permission.ACCESS_COARSE_LOCATION)
                .check();

        //액션바 셜정
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("인터넷 연결");

        //액션바 뒤로가기 설정
        actionBar.setDisplayHomeAsUpEnabled(true);

        viewpagerSetting();
    }

    private void viewpagerSetting() {
        btPrevious = findViewById(R.id.btPrevious); // 이전버튼
        btNext = findViewById(R.id.btNext); // 다음버튼
        pager = findViewById(R.id.viewpagerDevice); // 뷰페이저 등록
        device = getIntent().getStringExtra("device");
        //어댑터 세팅
        adapter = new MoviePagerAdapter(getSupportFragmentManager());

        //프래그먼트 세팅
        DevicesAPCheckFragment devicesAPCheckFragment = new DevicesAPCheckFragment();
        adapter.addItem(devicesAPCheckFragment);
        DeviceWiFiReFragment deviceWiFiReFragment = new DeviceWiFiReFragment();
        adapter.addItem(deviceWiFiReFragment);
        WiFiSettingReFragment wiFiSettingReFragment = new WiFiSettingReFragment();
        adapter.addItem(wiFiSettingReFragment);
//        pager.setOffscreenPageLimit(adapter.getCount()); // 페이지 올릴때마다 바꿔줘야함.
        pager.setAdapter(adapter);

        circleAnimIndicator = findViewById(R.id.circleDevice); // 뷰페이저 인디케이트 설정
        pager.addOnPageChangeListener(mOnPageChangeListener); // 페이지 변경될 때마다 인디케이트도 변경
        //Indicator 초기화
        initIndicaotor(adapter.getCount());
        btClick();
    }

    private void btClick() {
        btPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e(TAG, "onClick: previous");
                pager.setCurrentItem(getItem(-1), true);
                if(pagecheck == 0){
                    finish();
                }else{
                    pagecheck --;
                }
            }
        });
        btNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e(TAG, "onClick: next" );
                pager.setCurrentItem(getItem(+1), true);
                pagecheck ++;
                Log.e(TAG, "adapter.getCount() : "+adapter.getCount()+" pagecheck : "+ pagecheck);
                if(adapter.getCount()-1 == pagecheck){
                    //AP모드 접속시켜줘야함. 그러기 위해선 ssid를 가져와야함.
                    apmodeset();
                }else if(adapter.getCount() == pagecheck){
                    nextActivity();
                }
            }
        });
    }

    private void apmodeset() {
        SharedPreferences wifi = getSharedPreferences("wifi",MODE_PRIVATE);
        APssid = wifi.getString("APssid",null);
        WifiConfiguration conf = new WifiConfiguration();
        conf.SSID = "\"" + APssid + "\"";
        conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE); // 개방형 네트워크의 경우
        WifiManager wifiManager = (WifiManager)getApplicationContext().getSystemService(WIFI_SERVICE);
        wifiManager.addNetwork(conf);
        List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
        for( WifiConfiguration i : list ) {
            if(i.SSID != null && i.SSID.equals("\"" + APssid + "\"")) { // APssid가 같은 것만
                wifiManager.disconnect();
                wifiManager.enableNetwork(i.networkId, true);
                wifiManager.reconnect(); // 수정해야함.
                Log.e(TAG, "apmodeset wifienabled: "+wifiManager.isWifiEnabled());
                break;
            }
        }
    }

    private void nextActivity() {
        Log.e(TAG, "nextActivity: 혹시 너 들어와지니?" );
        Log.e(TAG, "pagecheck : "+pagecheck);
        SharedPreferences wifi = getSharedPreferences("wifi",MODE_PRIVATE);
        SharedPreferences.Editor editor = wifi.edit();
        ssid = wifi.getString("ssid",null);
        password = wifi.getString("password",null);
        Intent intent = new Intent(DeviceReConnectActivity.this, DeviceRegisteringReActivity.class);
        intent.putExtra("ssid",ssid);
        intent.putExtra("password",password);
        intent.putExtra("APssid",APssid);
        intent.putExtra("position",getIntent().getIntExtra("position",9999));
        editor.clear();
        editor.apply();
        pagecheck = 2; // 다시 다음버튼을 눌렀을 때 이동 시켜줘야함.
        startActivity(intent);
    }

    private int getItem(int i) {
        return pager.getCurrentItem() + i;
    }


    private void initIndicaotor(int count) {

        //애니메이션 속도
        circleAnimIndicator.setItemMargin(15);
        //indecator 생성
        circleAnimIndicator.setAnimDuration(300);
        circleAnimIndicator.createDotPanel(count, R.drawable.indicator_non , R.drawable.indicator_on);
    }

    private ViewPager.OnPageChangeListener mOnPageChangeListener = new ViewPager.OnPageChangeListener() {

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

        @Override
        public void onPageSelected(int position) {
            Log.e(TAG, "onPageSelected: "+position);
            switch (position){
                case 0:
                    btNext.setEnabled(true);
                    break;
                case 1:
                    refresh();
                    break;
                case 2:
                    btNext.setEnabled(false);

            }
            circleAnimIndicator.selectDot(position);
        }

        @Override
        public void onPageScrollStateChanged(int state) {
        }
    };

    private void refresh() {
        adapter.notifyDataSetChanged();
        btNext.setEnabled(false);
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

    //버튼 활성화 / 비활성화 변경
    public void changeButton(String passward){
        if(!passward.isEmpty()){
            btNext.setEnabled(true);
        }else{
            btNext.setEnabled(false);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "onDestroy: 혹여나 남아있나 싶어서" );
    }
}
