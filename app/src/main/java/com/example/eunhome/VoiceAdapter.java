package com.example.eunhome;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class VoiceAdapter extends BaseAdapter {

    public class ListContents{
        String msg;
        int type;

        ListContents(String msg, int type){
            this.msg = msg;
            this.type = type;
        }
    }

    private ArrayList m_List;
    public VoiceAdapter(){
        m_List = new ArrayList();
    }

    //아이템 추가
    public void add(String msg, int type){
        m_List.add(new ListContents(msg, type));
    }
    public void remove(int position){
        m_List.remove(position);
    }

    @Override
    public int getCount() {
        return m_List.size();
    }

    @Override
    public Object getItem(int position) {
        return m_List.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final Context context = parent.getContext();

        TextView text;
        VoiceHolder holder;
        LinearLayout layout;

        if(convertView == null){
            // view 가 null 일 경우
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.item_voice, parent, false);

            layout = convertView.findViewById(R.id.chat_layout);
            text = convertView.findViewById(R.id.textChat);

            holder = new VoiceHolder();
            holder.m_TextView = text;
            holder.layout = layout;
            convertView.setTag(holder);
        }else{
            holder = (VoiceHolder) convertView.getTag();
            text = holder.m_TextView;
            layout = holder.layout;
        }

        ListContents listContents = (ListContents)m_List.get(position);
        text.setText(listContents.msg);

        if(listContents.type == 0){
            text.setBackgroundResource(R.drawable.inbox);
            layout.setGravity(Gravity.LEFT);
        }else if(listContents.type == 1){
            text.setBackgroundResource(R.drawable.outbox);
            layout.setGravity(Gravity.RIGHT);
        }
        return convertView;
    }

    private class VoiceHolder{
        TextView m_TextView;
        LinearLayout layout;
    }
}
