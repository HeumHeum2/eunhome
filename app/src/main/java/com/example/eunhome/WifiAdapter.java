package com.example.eunhome;

import android.animation.ValueAnimator;
import android.content.Context;
import android.database.DataSetObserver;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class WifiAdapter extends RecyclerView.Adapter<WifiAdapter.ViewHolder> {
    private static final String TAG = "WifiAdapter";

    private ArrayList<WifiData> scanDatas;
    private Context context;

    private ClickCallbackListener callbackListener;

    private SparseBooleanArray selectedItems = new SparseBooleanArray();
    // 직전에 클릭됐던 Item의 position
    private int prePosition = -1;

    WifiAdapter(Context context, ArrayList<WifiData> list){
        this.context = context;
        this.scanDatas = list;
    }

    @Override
    public WifiAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View view = inflater.inflate(R.layout.item_wifi, parent, false);
        WifiAdapter.ViewHolder viewHolder = new WifiAdapter.ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.onBind(scanDatas.get(position), position,callbackListener);
    }

    @Override
    public int getItemCount() {
        return scanDatas.size();
    }

    //메인액티비티에서 전달 받은 콜백메서드를 set 하는 메서드
    public void setCallbackListener(ClickCallbackListener callbackListener) {
        this.callbackListener = callbackListener;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private TextView textSSID;
        private ImageView imgLock, imgWiFi, imgCheck;
        private CardView cardView;
        private WifiData wifiData;
        private int position;
        private ClickCallbackListener callbackListener;

        ViewHolder(@NonNull final View itemView) {
            super(itemView);

            textSSID = itemView.findViewById(R.id.textSSID);
            imgLock = itemView.findViewById(R.id.imgLock);
            imgWiFi = itemView.findViewById(R.id.imgWiFi);
            imgCheck = itemView.findViewById(R.id.imgCheck);
            cardView = itemView.findViewById(R.id.cardviewWiFi);
            setListener();
        }

        public void setListener(){
            cardView.setOnClickListener(this);
        }

        void onBind(WifiData wifiData, int position, ClickCallbackListener callbackListener) {
            this.wifiData = wifiData;
            this.position = position;
            this.callbackListener = callbackListener;

            textSSID.setText(wifiData.getSsid());
            imgCheck.setImageResource(R.drawable.ic_check);
            imgLock.setVisibility(wifiData.getCap().contains("WPA") ? View.VISIBLE : View.GONE);

            if(wifiData.getRssi() <= -30 && wifiData.getRssi() >= -100){
                if(wifiData.getRssi() <= -90){
                    imgWiFi.setImageResource(R.drawable.ic_signal_wifi_1);
                }else if(wifiData.getRssi() <= -70){
                    imgWiFi.setImageResource(R.drawable.ic_signal_wifi_2);
                }else if (wifiData.getRssi() <= -30){
                    imgWiFi.setImageResource(R.drawable.ic_signal_wifi_4);
                }
            }

            changeVisibility(selectedItems.get(position));
            cardView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.cardviewWiFi:
                    if(selectedItems.get(position)){
                        //펼쳐진 Item 클릭 시
                        selectedItems.delete(position);
                        Log.e(TAG, "onClick: 확인1");
                        callbackListener.callBackno();

                    }else{
                        // 직전의 클릭됐던 Item의 클릭상태를 지움
                        selectedItems.delete(prePosition);
                        // 클릭한 Item의 position을 저장
                        selectedItems.put(position,true);
                        Log.e(TAG, "onClick: 확인2");
                        callbackListener.callBack(wifiData.getSsid(),wifiData.getCap());
                    }
                    //해당 포지션의 변화를 알림
                    if(prePosition != -1) notifyItemChanged(prePosition);
                    notifyItemChanged(position);
                    //클릭된 position 저장
                    prePosition = position;
                    break;
            }
        }

        private void changeVisibility(final boolean isExpanded) {
            imgCheck.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
        }
    }
}
