package com.example.eunhome;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetworkConnetedCheck {
    private Context context;

    public NetworkConnetedCheck(Context context){
        this.context = context;
    }

    public boolean isNetworkConnected(){
        ConnectivityManager manager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = manager.getActiveNetworkInfo();
        if(activeNetwork != null){
            int type = activeNetwork.getType();
            if(type == ConnectivityManager.TYPE_MOBILE){//쓰리지나 LTE로 연결된것(모바일을 뜻한다.)
                return true;
            }else if(type == ConnectivityManager.TYPE_WIFI){//와이파이 연결된것
                return true;
            }
        }
        return false;  //연결이 되지않은 상태
    }
}
