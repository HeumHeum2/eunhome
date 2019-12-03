package com.example.eunhome;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
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
import com.kakao.sdk.newtoneapi.TextToSpeechClient;
import com.kakao.sdk.newtoneapi.TextToSpeechListener;
import com.kakao.sdk.newtoneapi.TextToSpeechManager;

import java.util.ArrayList;

public class VoiceFragment extends Fragment implements View.OnClickListener, SpeechRecognizeListener {
    private static final String TAG= "VoiceFragment";

    private SpeechRecognizerClient client;
    private TextToSpeechClient ttsClient;
    private ImageButton btnVoice;
    private ListView voiceListView;
    private VoiceAdapter voiceAdapter;
    private String chatMessage;
    private ArrayList<String> deviceName;
    private ArrayList<String> deviceID;
    private AudioManager audioManager;

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_voice, container, false);
        //음성인식 SDK 초기화
        SpeechRecognizerManager.getInstance().initializeLibrary(getContext());
        //음성인식 클라이언트 생성
        SpeechRecognizerClient.Builder builder = new SpeechRecognizerClient.Builder()
                .setServiceType(SpeechRecognizerClient.SERVICE_TYPE_WEB);
        client = builder.build();

        //오디오 설정
        audioManager = (AudioManager)getContext().getSystemService(Context.AUDIO_SERVICE);

        //음성합성 SDK 초기화
        TextToSpeechManager.getInstance().initializeLibrary(getContext());

        //음성합성 클라이언트 생성
        ttsClient = new TextToSpeechClient.Builder()
                .setSpeechMode(TextToSpeechClient.NEWTONE_TALK_2) // 음성합성방식
                .setSpeechSpeed(1.0) // 발음 속도(0.5~4.0)
                .setSpeechVoice(TextToSpeechClient.VOICE_WOMAN_READ_CALM) // TTS 음색 모드설정
                .setListener(new TextToSpeechListener(){
                    @Override
                    public void onError(int code, String message) {
                        handleError(code);
                        ttsClient = null;
                    }

                    @Override
                    public void onFinished() {
                        int intSentSize = ttsClient.getSentDataSize(); // 세션 중에 전송한 데이터 사이즈
                        int intRecvSize = ttsClient.getReceivedDataSize(); //세션 중에 전송받은 데이터 사이즈
                        String strInacctiveText = "handleFinished() SentSize : " + intSentSize + " ResvSize : " + intRecvSize;
                        Log.i(TAG, strInacctiveText);
                    }
                })
                .setSampleRate(16000)
                .build();
        //가지고 있는 디바이스 정보 가져오기
        SharedPreferences deviceinfo = getContext().getSharedPreferences("userinfo", Context.MODE_PRIVATE);
        String json = deviceinfo.getString("device","");
        Gson gson = new Gson();
        UserInfo device = gson.fromJson(json,UserInfo.class);
        deviceID = device.getDevices();
        deviceName = device.getDevices_name();

        //레이아웃
        voiceAdapter = new VoiceAdapter();
        voiceListView = view.findViewById(R.id.VoiceListView);
        voiceListView.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        voiceListView.setAdapter(voiceAdapter);
        btnVoice = view.findViewById(R.id.com_kakao_sdk_asr_button_voice);

        client.setSpeechRecognizeListener(this);
        btnVoice.setOnClickListener(this);
        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.com_kakao_sdk_asr_button_voice :
                if(client.startRecording(true)){
                    if(ttsClient!=null && ttsClient.isPlaying()){
                        ttsClient.stop();
                    }
                    client.cancelRecording();
                }
                break;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // API를 더이상 사용하지 않을 때 finalizeLibrary()를 호출한다.
        SpeechRecognizerManager.getInstance().finalizeLibrary();
        TextToSpeechManager.getInstance().finalizeLibrary();
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
    public void onError(int errorCode, final String errorMsg)
    {
        Log.d(TAG, "onError: "+errorMsg);
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
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(!getFragmentManager().isDestroyed()){
                    voiceAdapter.add(chatMessage,1);
                    voiceAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    @Override
    public void onAudioLevel(float level) {
        Log.d(TAG, "onAudioLevel: "+level);
    }

    @Override
    public void onFinished() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String answer;
                Log.d(TAG, "onFinished: ");
                if (audioManager.getRingerMode() == AudioManager.RINGER_MODE_NORMAL) {
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), AudioManager.FLAG_PLAY_SOUND);
                }else{
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, AudioManager.FLAG_PLAY_SOUND);
                }
                if(deviceID.isEmpty()){
                    answer = "기기를 먼저 등록해주세요.";
                    voiceAdapter.add(answer,0);
                }else{
                    answer = chatMessageCheck(chatMessage);
                    voiceAdapter.add(answer, 0);
                }
                if(ttsClient.play(answer)){
                    voiceAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    //aws 연동해서 mqtt로 가져와야함.
    private String chatMessageCheck(String chatMessage) {
        String answer = "";
        if(chatMessage.contains("불") || chatMessage.contains("전등")){
            answer = "전등 얘기";
        }else if(chatMessage.contains("에어콘") || chatMessage.contains("에어컨")){
            answer = "에어컨 얘기";
        }else if(chatMessage.contains("밸브") || chatMessage.contains("가스밸브")){
            answer = "밸브 얘기";
        }else{
            for(int i = 0 ; i < deviceName.size() ; i++){
                if(chatMessage.contains(deviceName.get(i))){
                    answer = "디바이스 이름 얘기";
                }
            }
            if(answer.isEmpty()){
                answer = "잘 이해하지 못했어요. 다시 말씀해주세요.";
            }
        }
        return answer;
    }

    private void handleError(int errorCode) {
        String errorText;
        switch (errorCode) {
            case TextToSpeechClient.ERROR_NETWORK:
                errorText = "네트워크 오류";
                break;
            case TextToSpeechClient.ERROR_NETWORK_TIMEOUT:
                errorText = "네트워크 지연";
                break;
            case TextToSpeechClient.ERROR_CLIENT_INETRNAL:
                errorText = "음성합성 클라이언트 내부 오류";
                break;
            case TextToSpeechClient.ERROR_SERVER_INTERNAL:
                errorText = "음성합성 서버 내부 오류";
                break;
            case TextToSpeechClient.ERROR_SERVER_TIMEOUT:
                errorText = "음성합성 서버 최대 접속시간 초과";
                break;
            case TextToSpeechClient.ERROR_SERVER_AUTHENTICATION:
                errorText = "음성합성 인증 실패";
                break;
            case TextToSpeechClient.ERROR_SERVER_SPEECH_TEXT_BAD:
                errorText = "음성합성 텍스트 오류";
                break;
            case TextToSpeechClient.ERROR_SERVER_SPEECH_TEXT_EXCESS:
                errorText = "음성합성 텍스트 허용 길이 초과";
                break;
            case TextToSpeechClient.ERROR_SERVER_UNSUPPORTED_SERVICE:
                errorText = "음성합성 서비스 모드 오류";
                break;
            case TextToSpeechClient.ERROR_SERVER_ALLOWED_REQUESTS_EXCESS:
                errorText = "허용 횟수 초과";
                break;
            default:
                errorText = "정의하지 않은 오류";
                break;
        }

        final String statusMessage = errorText + " (" + errorCode + ")";

        Log.e(TAG, statusMessage);
    }
}
