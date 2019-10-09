package com.example.eunhome;

import java.util.List;

public class Person {
    String email;
    String name;
    String phomeNumber;
    List<DeviceList> devicelist;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhomeNumber() {
        return phomeNumber;
    }

    public void setPhomeNumber(String phomeNumber) {
        this.phomeNumber = phomeNumber;
    }

    public List<DeviceList> getDevicelist() {
        return devicelist;
    }

    public void setDevicelist(List<DeviceList> devicelist) {
        this.devicelist = devicelist;
    }
}
