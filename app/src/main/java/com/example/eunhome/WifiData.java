package com.example.eunhome;

public class WifiData {

    private String ssid;
    private String cap;
    private int rssi;

    public WifiData(String ssid, String cap, int rssi){
        this.ssid = ssid;
        this.cap = cap;
        this.rssi = rssi;
    }

    public String getSsid(){
        return ssid;
    }

    public String getCap(){
        return cap;
    }

    public int getRssi(){
        return rssi;
    }
}
