package com.example.eunhome;

import android.content.Context;
import android.content.Intent;
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
    private ArrayList<String> mDataName = new ArrayList<>(); // 사용자가 정할 수 있는 이름

    public DeviceAdapter(Context context){
        this.context = context;
    }

    public void setItems(ArrayList<String> items, ArrayList<String> itemsname) {
        this.mData = items;
        this.mDataName = itemsname;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView imgDevice;
        TextView textDevice;
        TextView textDeviceStatus;
        long mLastClickTime = 0;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgDevice = itemView.findViewById(R.id.imageDevice);
            textDevice = itemView.findViewById(R.id.textDevice);
            textDeviceStatus = itemView.findViewById(R.id.textDeviceStatus);
            itemView.setOnClickListener(this);
        }

        public void onBind(String device, String deviceName) {
            textDeviceStatus.setText(device);
            textDevice.setText(deviceName);
            if(device.contains("Light")){
                imgDevice.setImageResource(R.drawable.ic_light);
            }else if(device.contains("CCTV")){
                imgDevice.setImageResource(R.drawable.ic_linked_camera);
            }
        }

        @Override
        public void onClick(View v) {
            if(SystemClock.elapsedRealtime() - mLastClickTime < 1000){
                return;
            } // 중복클릭 막기
            mLastClickTime = SystemClock.elapsedRealtime();

            Log.e(TAG, "onClick: "+textDeviceStatus.getText().toString());
            Log.e(TAG, "onClick: "+textDevice.getText().toString());
            String device = textDeviceStatus.getText().toString();
            Intent intent = null;
            if(device.contains("Light")){
                intent = new Intent(context, LightActivity.class);
            }else if(device.contains("CCTV")){
                intent = new Intent(context, CCTVActivity.class);
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
        holder.onBind(mData.get(position), mDataName.get(position));
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }
}
