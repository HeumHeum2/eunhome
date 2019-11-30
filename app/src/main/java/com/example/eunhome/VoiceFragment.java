package com.example.eunhome;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ListView;

import androidx.fragment.app.Fragment;

import com.google.gson.Gson;
import com.kakao.sdk.newtoneapi.SpeechRecognizeListener;
import com.kakao.sdk.newtoneapi.SpeechRecognizerClient;
import com.kakao.sdk.newtoneapi.SpeechRecognizerManager;

import java.util.ArrayList;

public class VoiceFragment extends Fragment implements View.OnClickListener, SpeechRecognizeListener{
    private static final String TAG= "VoiceFragment";

    private SpeechRecognizerClient client;
    private ImageButton btnVoice;
    private ListView voiceListView;
    private String chatMessage;
    private ArrayList<String> chatMessageList = new ArrayList<>();
    private ArrayList<String> deviceName = new ArrayList<>();
    private ArrayList<String> deviceID = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_voice, container, false);
        //음성인식 SDK 초기화
        SpeechRecognizerManager.getInstance().initializeLibrary(getContext());
        //클라이언트 생성
        SpeechRecognizerClient.Builder builder = new SpeechRecognizerClient.Builder()
                .setServiceType(SpeechRecognizerClient.SERVICE_TYPE_WEB);
        client = builder.build();
        //가지고 있는 디바이스 정보 가져오기
        SharedPreferences deviceinfo = getContext().getSharedPreferences("userinfo", Context.MODE_PRIVATE);
        Gson gson = new Gson();

        //레이아웃
        voiceListView = view.findViewById(R.id.VoiceListView);
        btnVoice = view.findViewById(R.id.com_kakao_sdk_asr_button_voice);

        client.setSpeechRecognizeListener(this);
        btnVoice.setOnClickListener(this);
        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.com_kakao_sdk_asr_button_voice :
                client.stopRecording();
                client.startRecording(true);
                break;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // API를 더이상 사용하지 않을 때 finalizeLibrary()를 호출한다.
        SpeechRecognizerManager.getInstance().finalizeLibrary();
    }

    @Override
    public void onReady() {
        Log.d(TAG, "onReady: ");
    }

    @Override
    public void onBeginningOfSpeech() {
        Log.d(TAG, "onBeginningOfSpeech: 얘기하고 있니?");
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(!getFragmentManager().isDestroyed()){
                    btnVoice.setSelected(true);
                }
            }
        });
        btnVoice.setSelected(true);
    }

    @Override
    public void onEndOfSpeech() {
        Log.d(TAG, "onEndOfSpeech: ");
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(!getFragmentManager().isDestroyed()){
                    btnVoice.setSelected(false);
                }
            }
        });
    }

    @Override
    public void onError(int errorCode, String errorMsg) {
        Log.d(TAG, "onError: ");
    }

    @Override
    public void onPartialResult(String partialResult) {
        Log.d(TAG, "onPartialResult: "+partialResult);
    }

    @Override
    public void onResults(Bundle results) {
        Log.d(TAG, "onResults: "+results);
        ArrayList<String> texts =  results.getStringArrayList(SpeechRecognizerClient.KEY_RECOGNITION_RESULTS);
        chatMessage = texts.get(0);
        chatMessageList.add(chatMessage);
    }

    @Override
    public void onAudioLevel(float level) {
        Log.d(TAG, "onAudioLevel: ");
    }

    @Override
    public void onFinished() {
        Log.d(TAG, "onFinished: ");
        if(chatMessage.contains("불") || chatMessage.contains("전등")){
            Log.d(TAG, "포함되어 있는 단어!");
        }else if(chatMessage.contains("에어콘")){

        }else{
            chatMessageList.add("등록되지 않은 기기 입니다. 다시 말씀해주세요.");
        }
    }
}
