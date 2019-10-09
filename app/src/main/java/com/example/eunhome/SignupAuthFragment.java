package com.example.eunhome;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
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
import com.amazonaws.mobile.client.results.UserCodeDeliveryDetails;
import com.google.android.material.textfield.TextInputLayout;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.amazonaws.mobile.auth.core.internal.util.ThreadUtils.runOnUiThread;

public class SignupAuthFragment extends Fragment {

    public static SignupAuthFragment newInstance() {
        return new SignupAuthFragment();
    }

    private static final String TAG= "SignupAuthFragment";

    private TextInputLayout emailInputLayout,pwInputLayout, pwInputCheckLayout, nameInputLayout, phoneInputLayout;
    private EditText inputAuthEmail, inputPassWd, inputPassWdCheck, inputName, inputPhone;
    private Button btAuth;
    private TextView textLogin;

    private Map<String, String> attributes = new HashMap<>();

    //프로그래스 다이얼로그
    private com.example.eunhome.ProgressDialog progressDialog;

    private String authEmail = null, passwd = null, passwdcheck = null, name = null, phone = null;

    //정규식
    public static final Pattern VALID_EMAIL_ADDRESS_REGEX = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);
    public static final Pattern VALID_PASSWD_REGEX = Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[$@$!%*#?&])[A-Za-z\\d$@$!%*#?&]{8,}$");
    public static final Pattern VALID_NAME_REGEX = Pattern.compile("^[가-힣]{2,4}|[a-zA-Z]{2,10}\\s[a-zA-Z]{2,10}$");


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_signup_auth, container, false);

        emailInputLayout = view.findViewById(R.id.emailInputLayout);
        pwInputLayout = view.findViewById(R.id.pwInputLayout);
        pwInputCheckLayout = view.findViewById(R.id.pwInputCheckLayout);
        nameInputLayout = view.findViewById(R.id.nameInputLayout);
        phoneInputLayout = view.findViewById(R.id.phoneInputLayout);

        progressDialog = new ProgressDialog(getContext());

        inputAuthEmail = emailInputLayout.getEditText();
        inputPassWd = pwInputLayout.getEditText();
        inputPassWdCheck = pwInputCheckLayout.getEditText();
        inputName = nameInputLayout.getEditText();
        inputPhone = phoneInputLayout.getEditText();

        //이메일
        inputAuthEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                authEmail = null;
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                authEmail = null;
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(editable.toString().isEmpty()){
                    emailInputLayout.setError("이메일을 입력해주세요.");
                }else if(!validateEmail(editable.toString().trim())){
                    emailInputLayout.setError("이메일 형식을 지켜주세요.");
                }else{
                    emailInputLayout.setError(null);
                    authEmail = editable.toString().trim();
                }
            }
        });

        //이름
        inputName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(editable.toString().isEmpty()){
                    nameInputLayout.setError("이름을 입력해주세요.");
                }else if(!vaildateName(editable.toString().trim())){
                    nameInputLayout.setError("한글 또는 영문으로 입력해주세요.");
                }else{
                    nameInputLayout.setError(null);
                    name = editable.toString().trim();
                }
            }
        });

        //비밀번호
        inputPassWd.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                passwd = null;
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                passwd = null;
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(editable.toString().isEmpty()){
                    pwInputLayout.setError("비밀번호를 입력해주세요.");
                }else if(!vaildatePW(editable.toString().trim())){
                    pwInputLayout.setError("최소 8자리에 숫자, 문자, 특수문자 각각 1개 이상 포함시켜주세요.");
                }else{
                    pwInputLayout.setError(null);
                    passwd = editable.toString().trim();
                }
            }
        });

        //비밀번호 확인
        inputPassWdCheck.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                passwdcheck = null;
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                passwdcheck = null;
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(editable.toString().isEmpty()){
                    pwInputCheckLayout.setError("비밀번호 확인을 입력해주세요.");
                }else if(!editable.toString().equals(passwd)){
                    pwInputCheckLayout.setError("비밀번호와 일치하지 않습니다.");
                }else{
                    pwInputCheckLayout.setError(null);
                    passwdcheck = editable.toString().trim();
                }
            }
        });

        inputPhone.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(editable.toString().isEmpty()){
                    phoneInputLayout.setError("휴대폰번호를 입력해주세요.");
                }else{
                    phoneInputLayout.setError(null);
                    phone = "+"+editable.toString().trim();
                }
            }
        });

        // 로그인 텍스트 클릭
        textLogin = view.findViewById(R.id.textLogin);
        textLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e(TAG, "onClick: textLogin");
                ((StartActivity)getActivity()).replaceFragment(LoginFragment.newInstance());
            }
        });

        // 이메일 인증 클릭
        btAuth = view.findViewById(R.id.btAuth);
        btAuth.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                confirm();
                Log.e(TAG, "onClick: btAuth");
            }
        });
        return view;
    }

    private void confirm() {
        progressDialog.ShowProgressDialog();
        if(authEmail != null && passwd != null && passwdcheck != null && name != null && phone != null){ //이메일 비밀번호 체크 했을때만 다음단계
            SharedPreferences sharedPreferences = getContext().getSharedPreferences("email",Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("email",authEmail);
            editor.apply();
            attributes.put("email",authEmail);
            attributes.put("name",name);
            attributes.put("phone_number",phone);
            AWSMobileClient.getInstance().signUp(authEmail, passwdcheck, attributes, null, new Callback<SignUpResult>() {
                @Override
                public void onResult(final SignUpResult result) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.d(TAG, "Sign-up callback state: " + result.getConfirmationState());
                            if (!result.getConfirmationState()) {
                                final UserCodeDeliveryDetails details = result.getUserCodeDeliveryDetails();
                                Log.e(TAG,"Confirm sign-up with: " + details.getDestination());
                                Toast.makeText(getContext(),"입력한 "+details.getDestination()+"으로 인증번호를 보냈습니다.",Toast.LENGTH_SHORT).show();
                                progressDialog.HideProgressDialog();
                                ((StartActivity)getActivity()).replaceFragment(SignupFragment.newInstance());
                            }
                        }
                    });
                }
                @Override
                public void onError(Exception e) {
                    progressDialog.HideProgressDialog();
                    Log.e(TAG, "onError: "+e.getLocalizedMessage());
                    if(e.getLocalizedMessage().contains("An account with the given email already exists.")){
                        Looper.prepare();
                        Toast.makeText(getContext(),"이미 가입 되어있는 이메일 입니다.",Toast.LENGTH_SHORT).show();
                        Looper.loop();
                    }
                }
            });
        }else{
            progressDialog.HideProgressDialog();
            Toast.makeText(getContext(),"이메일 및 비밀번호를 다시 확인해주세요.",Toast.LENGTH_SHORT).show();
        }
    }

    // 이메일 검사
    private boolean validateEmail(String authEmail) {
        Matcher matcher = VALID_EMAIL_ADDRESS_REGEX.matcher(authEmail);
        return matcher.find();
    }

    //패스워드 검사
    private boolean vaildatePW(String passwd){
        Matcher matcher = VALID_PASSWD_REGEX.matcher(passwd);
        return matcher.find();
    }

    private boolean vaildateName(String name){
        Matcher matcher = VALID_NAME_REGEX.matcher(name);
        return matcher.find();
    }
}
