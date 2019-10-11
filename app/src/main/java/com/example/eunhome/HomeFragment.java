package com.example.eunhome;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;

import java.util.ArrayList;

public class HomeFragment extends Fragment {

    private static final String TAG= "HomeFragment";

    private TextView textWelcome, textDeviceCheck;
    private FloatingActionButton fabDeviceAdd;
    private RecyclerView userDeviceRecyclerView;
    private SharedPreferences userinfo;
    private DeviceAdapter adapter;
    private ArrayList<String> devices;
    private ArrayList<String> devicesName;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userinfo = getContext().getSharedPreferences("userinfo",Context.MODE_PRIVATE);
        devices = new ArrayList<>();
        devicesName = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        //액션바 세팅
        ActionBar actionBar = ((MainActivity)getActivity()).getSupportActionBar();
        actionBar.hide();
        init(view);

        return view;
    }

    public void init(View view){ // 레이아웃 세팅
        textWelcome = view.findViewById(R.id.textWelcome);
        textDeviceCheck = view.findViewById(R.id.textDeviceCheck);
        fabDeviceAdd = view.findViewById(R.id.fabDeviceAdd);

        Gson gson = new Gson();
        String json = userinfo.getString("device","");
        Log.d(TAG, "json: "+json);
        UserInfo user = gson.fromJson(json, UserInfo.class);
        devices = user.getDevices();
        devicesName = user.getDevices_name();

        try{
            if(!devices.get(0).isEmpty()){
                textDeviceCheck.setVisibility(View.GONE);
            }
        }catch (Exception e){
            Log.e(TAG, "init: ",e);
            textDeviceCheck.setVisibility(View.VISIBLE);
        }

        json = userinfo.getString("user","");
        user = gson.fromJson(json, UserInfo.class);

        textWelcome.setText(getString(R.string.welcome, user.getName()));

        userDeviceRecyclerView = view.findViewById(R.id.userDeviceRecyclerView);
        userDeviceRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new DeviceAdapter(getContext());
        adapter.setItems(devices, devicesName);
        userDeviceRecyclerView.setAdapter(adapter);

//        SwipeController swipeController = new SwipeController();
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback); // 나중에 swipeController로 바꿔줘야함.(스와이프로 삭제)
        itemTouchHelper.attachToRecyclerView(userDeviceRecyclerView);

        click();
    }

    private void click() {
        fabDeviceAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(),DeviceAddActivity.class);
                startActivity(intent);
            }
        });
    }

    ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            final int position = viewHolder.getAdapterPosition();
            devices.remove(position);
            devicesName.remove(position);
            adapter.notifyItemRemoved(position);
        }
    };

    @Override
    public void onResume(){
        super.onResume();
    }
}
