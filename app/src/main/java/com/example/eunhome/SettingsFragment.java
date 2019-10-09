package com.example.eunhome;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.amazonaws.mobile.client.AWSMobileClient;

public class SettingsFragment extends Fragment {
    private static final String TAG= "SettingsFragment";
    private TextView textLogout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        textLogout = view.findViewById(R.id.textLogout);

        textLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e(TAG, "onClick: logout" );
                showMessge();
            }
        });

        return view;
    }
    private void logout() {
        SharedPreferences userinfo = getContext().getSharedPreferences("userinfo",Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = userinfo.edit();
        editor.clear();
        editor.apply();
        AWSMobileClient.getInstance().signOut();
        Intent intent = new Intent(getContext(), StartActivity.class);
        startActivity(intent);
        getActivity().finish();
        Log.e(TAG, "logout: signOut");
    }

    public void showMessge() {
        //다이얼로그 객체 생성
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        //속성 지정
        builder.setTitle("안내");
        builder.setMessage("로그아웃 하시겠습니까?");
        //아이콘
        builder.setIcon(android.R.drawable.ic_dialog_alert);

        //예 버튼 눌렀을 때
        builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //텍스트 뷰 객체를 넣어줌..
                logout();
            }
        });

        //아니오 버튼 눌렀을 때
        builder.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                return;
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
