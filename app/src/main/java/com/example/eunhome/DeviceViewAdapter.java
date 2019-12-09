package com.example.eunhome;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class DeviceViewAdapter extends RecyclerView.Adapter<DeviceViewAdapter.ViewHolder> {
    private static final String TAG = "DeviceViceAdapter";

    private ArrayList<String> textData;
    private ArrayList<Integer> imageData;
    private Context context;

    public class ViewHolder extends RecyclerView.ViewHolder{
        ImageView imageDevice;
        TextView textDevice;

        ViewHolder(View itemView){
            super(itemView);

            imageDevice = itemView.findViewById(R.id.imageDevice);
            textDevice = itemView.findViewById(R.id.textDeviceName);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.e(TAG, "onClick: "+textDevice.getText().toString());
                    Intent intent = new Intent(context,DeviceSettingActivity.class);
                    intent.putExtra("device",textDevice.getText().toString());
                    context.startActivity(intent);
                }
            });
        }
    }

    DeviceViewAdapter(ArrayList<String> textlist, ArrayList<Integer> imagelist, Context context){
        this.textData = textlist;
        this.imageData = imagelist;
        this.context = context;
    }


    @Override
    public DeviceViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View view = inflater.inflate(R.layout.item_device,parent,false);
        DeviceViewAdapter.ViewHolder viewHolder = new DeviceViewAdapter.ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String text = textData.get(position);
        holder.textDevice.setText(text);
        holder.imageDevice.setImageResource(imageData.get(position));
    }

    @Override
    public int getItemCount() {
        return textData.size();
    }
}
