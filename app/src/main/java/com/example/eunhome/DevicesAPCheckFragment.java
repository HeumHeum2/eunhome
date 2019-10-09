package com.example.eunhome;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class DevicesAPCheckFragment extends Fragment {
    private static final String TAG = "DevicesAPCheckFragment";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(TAG, "onCreate" );
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_devices_ap_check, container, false);
        Log.e(TAG, "onCreateView");
        return view;
    }

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
        Log.e(TAG, "onResume" );
    }

}
