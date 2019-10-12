package com.example.eunhome;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

public class RegisteringSuccessActivity extends AppCompatActivity{

    private ImageView imgDevice;
    private String device;
    private TextView textDevice;
    private Button btDeviceClear;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registering_success);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        init();
    }

    public void init(){
        device = getIntent().getStringExtra("device");
        imgDevice = findViewById(R.id.imgDevice);
        textDevice = findViewById(R.id.textDevice);
        btDeviceClear = findViewById(R.id.btDeviceClear);

        setting();
        btClick();

    }

    //사진 세팅
    private void setting() {
        if(device.equals("Light")){
            imgDevice.setImageResource(R.drawable.ic_light_white);
        }else if(device.equals("CCTV")){
            imgDevice.setImageResource(R.drawable.ic_camera_white);
        }else if(device.equals("AirCon")){
            imgDevice.setImageResource(R.drawable.ic_toys);
        }
        textDevice.setText(device);
    }

    private void btClick() {
        btDeviceClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegisteringSuccessActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
        });
    }

    @Override
    public void onBackPressed() { // 뒤로가기 막기
        //super.onBackPressed();
    }
}
