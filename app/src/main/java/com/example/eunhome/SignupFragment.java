package com.example.eunhome;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.Callback;
import com.amazonaws.mobile.client.results.SignUpResult;

import static com.amazonaws.mobile.auth.core.internal.util.ThreadUtils.runOnUiThread;

public class SignupFragment extends Fragment {
    private static final String TAG= "SignupFragment";

    private TextView textLogin;
    private EditText inputNumber;
    private Button btCertification;

    //프로그래스 다이얼로그
    private com.example.eunhome.ProgressDialog progressDialog;

    public static SignupFragment newInstance() {
        return new SignupFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_signup, container, false);

        //사용자 인증번호 입력
        inputNumber = view.findViewById(R.id.inputNumber);

        progressDialog = new ProgressDialog(getContext());

        //로그인 텍스트 클릭
        textLogin = view.findViewById(R.id.textLogin);
        textLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e(TAG, "onClick: OK");
                ((StartActivity)getActivity()).replaceFragment(LoginFragment.newInstance());
            }
        });

        //인증번호 확인 버튼 클릭
        btCertification = view.findViewById(R.id.btCertification);
        btCertification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!inputNumber.getText().toString().isEmpty()){ // 입력값이 비어있지 않으면
                    progressDialog.ShowProgressDialog();
                    SharedPreferences sharedPreferences = getContext().getSharedPreferences("email",Context.MODE_PRIVATE);
                    String email = sharedPreferences.getString("email",null);
                    String code = inputNumber.getText().toString();
                    AWSMobileClient.getInstance().confirmSignUp(email, code, new Callback<SignUpResult>() {
                        @Override
                        public void onResult(final SignUpResult signUpResult) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Log.d(TAG, "Sign-up callback state: " + signUpResult.getConfirmationState());
                                    if (signUpResult.getConfirmationState()) {
                                        SharedPreferences sharedPreferences = getContext().getSharedPreferences("email",Context.MODE_PRIVATE);
                                        SharedPreferences.Editor editor = sharedPreferences.edit();
                                        editor.clear();
                                        editor.apply();
                                        progressDialog.HideProgressDialog();
                                        Toast.makeText(getContext(),"회원가입 되었습니다.",Toast.LENGTH_SHORT).show();
                                        ((StartActivity)getActivity()).replaceFragment(LoginFragment.newInstance());
                                    }
                                }
                            });
                        }
                        @Override
                        public void onError(Exception e) {
                            progressDialog.HideProgressDialog();
                            Log.e(TAG, "Confirm sign-up error", e);
                            if(e.getLocalizedMessage().contains("Invalid verification code provided, please try again.")){
                                Looper.prepare();
                                Toast.makeText(getContext(),"잘못된 인증번호입니다. 다시 입력해주세요.",Toast.LENGTH_SHORT).show();
                                Looper.loop();
                            }
                        }
                    });

                }else{
                    Toast.makeText(getContext(),"인증번호를 입력해주세요.",Toast.LENGTH_SHORT).show();
                }
            }
        });
        return view;
    }
}
