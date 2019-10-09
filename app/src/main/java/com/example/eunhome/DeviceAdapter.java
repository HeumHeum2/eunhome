package com.example.eunhome;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.amazonaws.amplify.generated.graphql.ListUsersQuery;

import java.util.ArrayList;
import java.util.List;

public class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.ViewHolder> {

    private static final String TAG = "DeviceAdapter";
    Context context;
    private List<ListUsersQuery.Item> mData = new ArrayList<>();

    public DeviceAdapter(Context context){
        this.context = context;
    }

    public void setItems(List<ListUsersQuery.Item> items) {
        this.mData = items;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView imgDevice;
        TextView textDevice;
        SharedPreferences userinfo = context.getSharedPreferences("userinfo",Context.MODE_PRIVATE);

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgDevice = itemView.findViewById(R.id.imageDevice);
            textDevice = itemView.findViewById(R.id.textDevice);

            itemView.setOnClickListener(this);
        }

        public void onBind(ListUsersQuery.Item deviceList) {
            if(userinfo.getString("email",null).equals(deviceList.name())){
                textDevice.setText(deviceList.device());
                if(deviceList.device().equals("Light")){
                    imgDevice.setImageResource(R.drawable.ic_light);
                }else if(deviceList.device().equals("CCTV")){
                    imgDevice.setImageResource(R.drawable.ic_linked_camera);
                }
            }
        }

        @Override
        public void onClick(View v) {
            Log.e(TAG, "onClick: "+textDevice.getText().toString());
            String device = textDevice.getText().toString();
            Intent intent = null;
            if(device.equals("Light")){
                intent = new Intent(context, LightActivity.class);
            }else if(device.equals("CCTV")){
                intent = new Intent(context, CCTVActivity.class);
            }
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
        holder.onBind(mData.get(position));
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }
}
