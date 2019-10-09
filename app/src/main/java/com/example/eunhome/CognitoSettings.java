package com.example.eunhome;

import android.content.Context;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserPool;
import com.amazonaws.regions.Regions;

public class CognitoSettings {
    private String userPoolId = "ap-northeast-2_9dcsx61oS";
    private String clientId = "4svg1thtgg1emudr7nk9did54k";
    private String clientSecret = "gen30cioi02lq298tket6ck2gau492pvvdni735rlbqvhccfag5";
    private Regions cognitoRegion = Regions.AP_NORTHEAST_2;

    private String identityPoolId = "ap-northeast-2:17f399d0-bc2d-4137-9859-1993a0a2aaaa";
    private Context context;

    public CognitoSettings(Context context){
        this.context = context;
    }

    public String getUserPoolId(){
        return userPoolId;
    }

    public String getClientId(){
        return clientId;
    }

    public String getClientSecret(){
        return clientSecret;
    }

    public Regions getCognitoRegion(){
        return cognitoRegion;
    }

    public CognitoUserPool getUserPool(){
        return new CognitoUserPool(context, userPoolId, clientId, clientSecret, cognitoRegion);
    }

    public CognitoCachingCredentialsProvider getCredentialsProvider(){
        return new CognitoCachingCredentialsProvider(
                context,
                identityPoolId,
                cognitoRegion
        );
    }
}
