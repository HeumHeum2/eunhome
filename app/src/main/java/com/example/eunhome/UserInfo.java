package com.example.eunhome;

import java.util.ArrayList;

public class UserInfo {
    private String email;
    private String name;
    private String phone_number;
    private ArrayList<String> devices;
    private ArrayList<String> devices_name;
    private ArrayList<String> devices_status;

    public ArrayList<String> getDevices_status() {
        return devices_status;
    }

    public void setDevices_status(ArrayList<String> devices_status) {
        this.devices_status = devices_status;
    }

    public ArrayList<String> getDevices_name() {
        return devices_name;
    }

    public void setDevices_name(ArrayList<String> devices_name) {
        this.devices_name = devices_name;
    }

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

    public String getPhone_number() {
        return phone_number;
    }

    public void setPhone_number(String phone_number) {
        this.phone_number = phone_number;
    }

    public ArrayList<String> getDevices() {
        return devices;
    }

    public void setDevices(ArrayList<String> devices) {
        this.devices = devices;
    }
}
