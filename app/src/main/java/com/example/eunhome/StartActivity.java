package com.example.eunhome;

import androidx.appcompat.app.ActionBar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;


import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.GetDetailsHandler;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.util.ArrayList;
import java.util.List;

public class StartActivity extends BaseActivity {

    private static final String TAG= "StartActivity";
    private AutoScrollViewPager autoViewPager;
    private CircleAnimIndicator circleAnimIndicator;
    private ArrayList<Integer> data;
    private NetworkConnetedCheck networkConnetedCheck;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(TAG, "onCreate");

        setContentView(R.layout.activity_start);



        ActionBar ab = getSupportActionBar();
        ab.hide();

        networkConnetedCheck = new NetworkConnetedCheck(this);
        if(networkConnetedCheck.isNetworkConnected()){
            Log.e(TAG, "network check : true");
        }else{
            Log.e(TAG, "network check : false");
        }

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fm.beginTransaction();
        fragmentTransaction.add(R.id.fragmentChange, new LoginFragment());
        fragmentTransaction.commit();

        // 뷰페이저 세팅
        int smarthome1 = R.drawable.smarthome1;
        int smarthome2 = R.drawable.smarthome2;

        data = new ArrayList<>(); //이미지 경로를 저장
        data.add(smarthome1);
        data.add(smarthome2);
//        data.add("http://nick.mtvnimages.com/nick/promos-thumbs/videos/spongebob-squarepants/rainbow-meme-video/spongebob-rainbow-meme-video-16x9.jpg?quality=0.60");
//        data.add("http://nick.mtvnimages.com/nick/video/images/nick/sb-053-16x9.jpg?maxdimension=&quality=0.60");
//        data.add("https://www.gannett-cdn.com/-mm-/60f7e37cc9fdd931c890c156949aafce3b65fd8c/c=243-0-1437-898&r=x408&c=540x405/local/-/media/2017/03/14/USATODAY/USATODAY/636250854246773757-XXX-IMG-WTW-SPONGEBOB01-0105-1-1-NC9J38E8.JPG");

        autoViewPager = findViewById(R.id.autoViewPager);
        AutoScrollAdapter scrollAdapter = new AutoScrollAdapter(this, data);
        autoViewPager.setAdapter(scrollAdapter); //Auto Viewpager에 Adapter 장착
        autoViewPager.setInterval(5000); // 페이지 넘어갈 시간 간격 설정
        autoViewPager.startAutoScroll(); //Auto Scroll 시작

        circleAnimIndicator = findViewById(R.id.circleAnimIndicator); // 뷰페이저 인디케이트 설정
        autoViewPager.addOnPageChangeListener(mOnPageChangeListener); // 페이지 변경될 때마다 인디케이트도 변경

        //Indicator 초기화
        initIndicaotor();
    }

    private void initIndicaotor() {
        //원사이의 간격
        circleAnimIndicator.setItemMargin(15);
        //애니메이션 속도
        circleAnimIndicator.setAnimDuration(300);
        //indecator 생성
        circleAnimIndicator.createDotPanel(data.size(), R.drawable.indicator_non , R.drawable.indicator_on);
    }

    private AutoScrollViewPager.OnPageChangeListener mOnPageChangeListener = new AutoScrollViewPager.OnPageChangeListener() {

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

        @Override
        public void onPageSelected(int position) {
            circleAnimIndicator.selectDot(position);
        }

        @Override
        public void onPageScrollStateChanged(int state) {
        }
    };

    public void replaceFragment(Fragment fragment){
        Log.e(TAG, "replaceFragment: "+fragment);
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragmentChange,fragment).commit();
    }


    @Override
    protected void onPause() {
        super.onPause();
        Log.e(TAG, "onPause" );
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.e(TAG, "onStart " );
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.e(TAG, "onStop" );
    }

    @Override
    protected void onDestroy() {
        Log.e(TAG, "onDestroy" );
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        Log.e(TAG, "onResume " );
        super.onResume();
    }
}
