package com.example.eunhome;

import android.Manifest;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.util.List;

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

        TedPermission.with(this)
                .setPermissionListener(permissionlistener)
                .setRationaleConfirmText("권한이 필요해요")
                .setDeniedMessage("왜 거부하셨어요...\n하지만 [설정] > [권한] 에서 권한을 허용할 수 있어요.")
                .setPermissions(Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.INTERNET)
                .check();

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
}
