package com.example.eunhome;

import android.content.Context;
import android.content.Intent;
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

import com.amazonaws.amplify.generated.graphql.ListUsersQuery;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.Callback;
import com.amazonaws.mobile.client.UserStateDetails;
import com.amazonaws.mobile.client.results.SignInResult;
import com.amazonaws.mobileconnectors.appsync.fetcher.AppSyncResponseFetchers;
import com.apollographql.apollo.GraphQLCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import javax.annotation.Nonnull;

import static com.amazonaws.mobile.auth.core.internal.util.ThreadUtils.runOnUiThread;

public class LoginFragment extends Fragment {
    private static final String TAG= "LoginFragment";

    private EditText email, passwd;
    private Button btlogin;
    private TextView textSignup;
    private UserInfo userInfo;
    private SharedPreferences userinfo;
    private SharedPreferences.Editor editor;
    private Gson gson;

    //프로그래스 다이얼로그
    private com.example.eunhome.ProgressDialog progressDialog;

    //프래그먼트를 넘겨주기 위한 instance
    public static LoginFragment newInstance() {
        return new LoginFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userinfo = getContext().getSharedPreferences("userinfo", Context.MODE_PRIVATE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_login,container,false);

        progressDialog = new ProgressDialog(getContext());

        email = view.findViewById(R.id.inputEmail);
        passwd = view.findViewById(R.id.inputPw);

        //가입 버튼
        textSignup = view.findViewById(R.id.textSignup);
        textSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e(TAG, "onClick: textSignup");
                ((StartActivity)getActivity()).replaceFragment(SignupAuthFragment.newInstance());
            }
        });

        //로그인 버튼
        btlogin = view.findViewById(R.id.btLogin);
        btlogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                confirm();
            }
        });

        return view;
    }

    private void confirm() {
        if(email.getText().toString().isEmpty()){
            Toast.makeText(getContext(),"이메일을 입력해주세요.",Toast.LENGTH_SHORT).show();
        }else if(passwd.getText().toString().isEmpty()){
            Toast.makeText(getContext(),"비밀번호를 입력해주세요.",Toast.LENGTH_SHORT).show();
        }else{
            progressDialog.ShowProgressDialog();
            AWSMobileClient.getInstance().signIn(email.getText().toString().trim(), passwd.getText().toString().trim(), null, new Callback<SignInResult>() {
                @Override
                public void onResult(final SignInResult signInResult) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.d(TAG, "Sign-in callback state: " + signInResult.getSignInState());
                            switch (signInResult.getSignInState()) {
                                case DONE:
                                    AWSMobileClient.getInstance().initialize(getContext(), new Callback<UserStateDetails>() {
                                        @Override
                                        public void onResult(UserStateDetails userStateDetails) {
                                            Log.i(TAG, "onResult: "+ userStateDetails.getUserState());
                                            try {
                                                Map<String, String> userAttributes = AWSMobileClient.getInstance().getUserAttributes();
                                                Log.d(TAG, "userAttributes: " + Arrays.toString(userAttributes.entrySet().toArray()));
                                                Log.d(TAG, "onResult: "+userAttributes.get("name"));
                                                editor = userinfo.edit();
                                                gson = new Gson();
                                                userInfo = new UserInfo();
                                                userInfo.setEmail(userAttributes.get("email"));
                                                userInfo.setName(userAttributes.get("name"));
                                                userInfo.setPhone_number(userAttributes.get("phone_number").replace("+",""));
                                                String json = gson.toJson(userInfo);
                                                editor.putString("user", json);
                                                editor.apply();
                                                ClientFactory.init(getContext());
                                                ClientFactory.appSyncClient().query(ListUsersQuery.builder().build())
                                                        .responseFetcher(AppSyncResponseFetchers.CACHE_AND_NETWORK) //캐시를 가져옴
                                                        .enqueue(usersCallback);
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                        }
                                        @Override
                                        public void onError(Exception e) {
                                            Log.e(TAG, "onError: ",e);
                                        }
                                    });
                                    break;
                                case SMS_MFA:
                                    progressDialog.HideProgressDialog();
                                    Toast.makeText(getContext(),"Please confirm sign-in with SMS.",Toast.LENGTH_SHORT).show();
                                    break;
                                case NEW_PASSWORD_REQUIRED:
                                    progressDialog.HideProgressDialog();
                                    Toast.makeText(getContext(),"Please confirm sign-in with new password.",Toast.LENGTH_SHORT).show();
                                    break;
                                default:
                                    progressDialog.HideProgressDialog();
                                    Toast.makeText(getContext(),"Unsupported sign-in confirmation: " + signInResult.getSignInState(),Toast.LENGTH_SHORT).show();
                                    break;
                            }
                        }
                    });
                }
                @Override
                public void onError(Exception e) {
                    progressDialog.HideProgressDialog();
                    Looper.prepare();
                    if(e.getLocalizedMessage().contains("User does not exist.")){
                        Toast.makeText(getContext(),"존재 하지 않은 이메일입니다.",Toast.LENGTH_SHORT).show();
                    }else if(e.getLocalizedMessage().contains("Incorrect username or password.")){
                        Toast.makeText(getContext(),"이메일, 비밀번호를 다시 확인해주세요.",Toast.LENGTH_SHORT).show();
                    }else if(e.getLocalizedMessage().contains("User is not confirmed.")){
                        Toast.makeText(getContext(),"인증 화면 만들어줘야함!",Toast.LENGTH_SHORT).show();
                    }
                    Looper.loop();
                }
            });
        }
    }

    private GraphQLCall.Callback<ListUsersQuery.Data> usersCallback = new GraphQLCall.Callback<ListUsersQuery.Data>() {
        @Override
        public void onResponse(@Nonnull Response<ListUsersQuery.Data> response) {
            ArrayList<String> devices = new ArrayList<>();
            ArrayList<String> devices_name = new ArrayList<>();
            for(int i = 0 ; i < response.data().listUsers().items().size() ; i++){
                if(email.getText().toString().trim().equals(response.data().listUsers().items().get(i).name())){
                    devices.add(response.data().listUsers().items().get(i).id());
                    devices_name.add(response.data().listUsers().items().get(i).device());
                }
            }
            UserInfo user = new UserInfo();
            user.setDevices(devices);
            user.setDevices_name(devices_name);
            String json = gson.toJson(user);
            editor.putString("device", json);
            editor.apply();
            Looper.prepare();
            Toast.makeText(getContext(),"로그인 되었습니다.",Toast.LENGTH_SHORT).show();
            progressDialog.HideProgressDialog();
            Intent intent = new Intent(getContext(), MainActivity.class);
            startActivity(intent);
            getActivity().finish();
            Looper.loop();
//            runOnUiThread(new Runnable(){
//                @Override
//                public void run() {
//                }
//            });
        }

        @Override
        public void onFailure(@Nonnull ApolloException e) {
            Log.e("ERROR", e.toString());
        }
    };
}
