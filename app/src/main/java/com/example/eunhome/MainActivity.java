package com.example.eunhome;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends BaseActivity {
    private static final String TAG= "MainActivity";

    // 프레그먼트 세팅
    private FragmentManager fragmentManager = getSupportFragmentManager();
    private HomeFragment homeFragment = new HomeFragment();
    private VoiceFragment voiceFragment = new VoiceFragment();
    private AlertFragment alertFragment = new AlertFragment();
    private SettingsFragment settingsFragment = new SettingsFragment();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.frameLayoutMain,homeFragment).commitAllowingStateLoss(); // 맨 처음 보여줄 화면
        BottomNavigationView bottomNavigationView = findViewById(R.id.navigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener(new ItemSelectedListener());
    }

    //프래그먼트 변경
    public class ItemSelectedListener implements BottomNavigationView.OnNavigationItemSelectedListener{

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
            FragmentTransaction transaction = fragmentManager.beginTransaction();

            switch (menuItem.getItemId()){
                case R.id.homeItem :
                    transaction.replace(R.id.frameLayoutMain,homeFragment).commitAllowingStateLoss();
                    break;

                case R.id.voiceItem :
                    transaction.replace(R.id.frameLayoutMain,voiceFragment).commitAllowingStateLoss();
                    break;

                case R.id.alertItem :
                    transaction.replace(R.id.frameLayoutMain,alertFragment).commitAllowingStateLoss();
                    break;

                case R.id.settingsItem :
                    transaction.replace(R.id.frameLayoutMain,settingsFragment).commitAllowingStateLoss();
                    break;
            }
            return true;
        }
    }
}
