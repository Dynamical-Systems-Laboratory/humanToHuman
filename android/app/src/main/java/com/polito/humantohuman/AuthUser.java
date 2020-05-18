package com.polito.humantohuman;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.auth.internal.IdTokenListener;
import com.google.firebase.internal.InternalTokenResult;

public class AuthUser {
    public static final int RC_SIGN_IN = 20;

    //IMPORTANT The activity should pass the result once the user has logged in with google!

    private GoogleSignInOptions gso ;
    public final GoogleSignInClient googleClient;
    public FirebaseAuth firebaseAuth;
    private String currentToken;

    public String getCurrentToken() {return  currentToken;}
    public String getUid(){return firebaseAuth.getCurrentUser().getUid();}

    private static AuthUser authUserInstance;

    public static AuthUser getInstance(Context context) {
        if (authUserInstance == null) {
            authUserInstance =  new AuthUser(context);
        }
        return authUserInstance;
    }


    public AuthUser(Context context) {

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuth.addIdTokenListener(new OnTokenChanged());

        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestIdToken(context.getString(R.string.request_id_token))
                .build();

        googleClient = GoogleSignIn.getClient(context,gso);

        if(firebaseAuth.getCurrentUser() != null){
            firebaseAuth.getCurrentUser().getIdToken(true).addOnCompleteListener(new OnTokenResult());
        }
    }

    public boolean isSignedInGoogle(Context context) { return GoogleSignIn.getLastSignedInAccount(context) != null; }
    public boolean isSignedIn() { return firebaseAuth.getInstance().getCurrentUser() != null;}

    public void refreshToken() {
        firebaseAuth.getCurrentUser().getIdToken(true).addOnCompleteListener(new OnTokenResult());
    }

    private class OnTokenResult implements OnCompleteListener<GetTokenResult>{

        @Override
        public void onComplete(@NonNull Task<GetTokenResult> task) {
            try {
                currentToken = task.getResult().getToken();
            } catch (Exception e) {
                Log.d("Firebase", "Token not updated!");
            }


        }
    }

    private class OnTokenChanged implements IdTokenListener{
        @Override
        public void onIdTokenChanged(@NonNull InternalTokenResult internalTokenResult) {
            try{
                currentToken = internalTokenResult.getToken();
            } catch (Exception e) {
                Log.d("Firebase", "Token not updated!");
            }
        }
    }


}
