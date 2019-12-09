package com.example.eunhome;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.ViewHolder> {

    private static final String TAG = "DeviceAdapter";
    Context context;
    private ArrayList<String> mData = new ArrayList<>(); // 기기 고유 이름
    private ArrayList<String> mDataName = new ArrayList<>(); // 기기 이름
    private ArrayList<String> mDataStatus = new ArrayList<>(); // 기기 상태

    public DeviceAdapter(Context context){
        this.context = context;
    }

    public void setItems(ArrayList<String> items, ArrayList<String> items_name, ArrayList<String> items_status) {
        this.mData = items;
        this.mDataName = items_name;
        this.mDataStatus = items_status;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView imgDevice;
        TextView textDeviceName;
        TextView textDeviceID;
        TextView textDeviceStatus;
        long mLastClickTime = 0;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgDevice = itemView.findViewById(R.id.imageDevice);
            textDeviceID = itemView.findViewById(R.id.textDeviceID);
            textDeviceName = itemView.findViewById(R.id.textDeviceName);
            textDeviceStatus = itemView.findViewById(R.id.textDeviceStatus);
            itemView.setOnClickListener(this);
        }

        public void onBind(String device, String deviceName, String deviceStatus) {
            textDeviceID.setText(device);
            textDeviceName.setText(deviceName);
            if(!deviceStatus.isEmpty()){
                if(deviceStatus.equals("OFF")){
                    textDeviceStatus.setTextColor(Color.RED);
                }
                textDeviceStatus.setText(deviceStatus);
            }
            if(device.contains("Light")){
                imgDevice.setImageResource(R.drawable.ic_light);
            }else if(device.contains("CCTV")){
                imgDevice.setImageResource(R.drawable.ic_linked_camera);
            }else if(device.contains("AirCon")){
                imgDevice.setImageResource(R.drawable.ic_toys);
            }else if(device.contains("GasValve")){
                imgDevice.setImageResource(R.drawable.ic_gasvalve);
            }
        }

        @Override
        public void onClick(View v) {
            if(SystemClock.elapsedRealtime() - mLastClickTime < 1000){
                return;
            } // 중복클릭 막기
            mLastClickTime = SystemClock.elapsedRealtime();

            Log.e(TAG, "onClick: "+ textDeviceID.getText().toString());
            Log.e(TAG, "onClick: "+ textDeviceName.getText().toString());
            String device = textDeviceID.getText().toString();
            Intent intent = null;
            if(device.contains("Light")){
                intent = new Intent(context, LightActivity.class);
            }else if(device.contains("CCTV")){
                intent = new Intent(context, CCTVActivity.class);
            }else if(device.contains("AirCon")){
                intent = new Intent(context, AirConActivity.class);
            }else if(device.contains("GasValve")){
                intent = new Intent(context, GasValveActivity.class);
            }
            intent.putExtra("position", getAdapterPosition());
            context.startActivity(intent);
        }
    }

    @Override
    public DeviceAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.item_device_status, parent, false);
        DeviceAdapter.ViewHolder viewHolder = new DeviceAdapter.ViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.onBind(mData.get(position), mDataName.get(position), mDataStatus.get(position));
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }
}
