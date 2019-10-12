package com.example.eunhome;

import android.os.Bundle;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class DeviceAddActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_add);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("기기 추가");

        actionBar.setDisplayHomeAsUpEnabled(true);

        deviceAdd();
    }

    private void deviceAdd() {
        ArrayList<String> list = new ArrayList<>();
        ArrayList<Integer> integers = new ArrayList<>();

        integers.add(R.drawable.ic_light);
        list.add("Light");

        integers.add(R.drawable.ic_linked_camera);
        list.add("CCTV");

        integers.add(R.drawable.ic_toys);
        list.add("AirCon");


        RecyclerView recyclerView = findViewById(R.id.deviceRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        DeviceViewAdapter adapter = new DeviceViewAdapter(list,integers,this);
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }
}
