package com.example.eunhome;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.Callback;
import com.amazonaws.mobile.client.UserStateDetails;

public class SplashActivity extends AppCompatActivity {

    private static final String TAG = "SplashActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        startLoading();
    }

    private void startLoading(){
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                start();
            }
        },2000);
    }

    private void start() {
//        autologin();
        AWSMobileClient.getInstance().initialize(getApplicationContext(), new Callback<UserStateDetails>() {
            @Override
            public void onResult(UserStateDetails userStateDetails) {
                Log.i(TAG, userStateDetails.getUserState().toString());
                switch (userStateDetails.getUserState()){
                    case SIGNED_IN:
                        Intent i = new Intent(SplashActivity.this, MainActivity.class);
                        startActivity(i);
                        finish();
                        break;
                    case SIGNED_OUT:
                        showSignIn();
                        break;
                    default:
                        AWSMobileClient.getInstance().signOut();
                        showSignIn();
                        break;
                }
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, e.toString());
            }
        });
    }

    private void showSignIn() {
        Intent intent = new Intent(SplashActivity.this,StartActivity.class);
        startActivity(intent);
        finish();
//        try {
//            AWSMobileClient.getInstance().showSignIn(this, SignInUIOptions.builder().nextActivity(MainActivity.class).logo(R.id.useLogo).backgroundColor(R.color.colorGreen).canCancel(false).build());
//        } catch (Exception e) {
//            Log.e(TAG, e.toString());
//        }
    }

//    //자동로그인
//    private void autologin() {
//        CognitoSettings cognitoSettings = new CognitoSettings(this);
//        cognitoSettings.getUserPool().getCurrentUser().getDetailsInBackground(handler);
//    }

//    GetDetailsHandler handler = new GetDetailsHandler() {
//        @Override
//        public void onSuccess(final CognitoUserDetails list) {
//            Log.e(TAG, "onSuccess: "+list.getAttributes().getAttributes());
//            Toast.makeText(SplashActivity.this, "자동로그인 되었습니다.",Toast.LENGTH_SHORT).show();
//            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
//            startActivity(intent);
//            finish();
//        }
//
//        @Override
//        public void onFailure(final Exception exception) {
//            Log.e(TAG, "onFailure: handler : "+exception.getLocalizedMessage());
//            Intent intent = new Intent(SplashActivity.this, StartActivity.class);
//            startActivity(intent);
//            finish();
//        }
//    };
}
