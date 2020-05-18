package com.polito.humantohuman.Activities;

import android.app.Dialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.polito.humantohuman.AuthUser;
import com.polito.humantohuman.HTTPClient.HTTPClient;
import com.polito.humantohuman.HTTPClient.HTTPClientBuilder;
import com.polito.humantohuman.Listeners.AddedDeviceListener;
import com.polito.humantohuman.R;
import com.polito.humantohuman.ResponseHandler.AddDeviceHandler;

import static com.polito.humantohuman.Constants.SERVER_ENDPOINT.ID_DEVICE_ENDPOINT;
import static com.polito.humantohuman.Constants.TIME.RESPONSE_TIME_OUT;
import static com.polito.humantohuman.Constants.TIME.TIME_OUT;

/**
 * This Activity is used for everything related with the authentication of the user
 * After a successful authentication it loads the scanActivity
 */
public class AuthenticationActivity extends AppCompatActivity implements AddedDeviceListener {

    public static AuthUser authUser;
    private SignInButton signIn;
    private Button sigAnymously;
    private Dialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.slide_in,R.anim.slide_out);
        setContentView(R.layout.activity_authentication);
        signIn = findViewById(R.id.sign_in);
        signIn.setOnClickListener(new SignInGoogleClickListener());
        sigAnymously = findViewById(R.id.sig_ano);
        sigAnymously.setOnClickListener(new SigInAnonymouslyClickListener());
        authUser = new AuthUser(this);
        if(authUser.isSignedIn()) { signedInSucceed(); }
        dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.adding_device_popup);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        onGoogleLoginResult(resultCode,data);
    }

    //This method should go on Activity result!
    public void onGoogleLoginResult(int result, Intent data){
        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
        task.addOnCompleteListener(new OnGoogleSignInCompleteListener());
    }

    @Override
    public void onAddedDeviceListener(boolean correct) {
        dialog.cancel();
        try{
            StartActivity.launchNextActivity(this, Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        } catch (IndexOutOfBoundsException e){
            Intent i = new Intent(this, ScanActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
        }
        finish();
    }


    /**
     * When the user sigIn with google
     */
    private class OnGoogleSignInCompleteListener implements OnCompleteListener<GoogleSignInAccount> {
        @Override
        public void onComplete(Task<GoogleSignInAccount> task) {
            try {
                GoogleSignInAccount acct = task.getResult(ApiException.class);
                Log.d("Firebase auth", "firebaseAuthWithGoogle:" + acct.getId());
                AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
                authUser.firebaseAuth.signInWithCredential(credential)
                        .addOnCompleteListener(new OnFirebaseSigInCompleteListener());
            } catch (ApiException e) {
                Log.d("Firebase auth error", e.getMessage());
                signedInFail();
            }
        }
    }

    /**
     * When the user sigIn with a email, google acocount... We have to pass it to firebase login
     */
    private class OnFirebaseSigInCompleteListener implements OnCompleteListener<AuthResult> {
        @Override
        public void onComplete(@NonNull Task<AuthResult> task) {
            if (task.isSuccessful()) {
                // Sign in success,
                Log.d("Firebase auth", "signInWithCredential:success");
                FirebaseUser user = authUser.firebaseAuth.getCurrentUser();
                signedInSucceed();
            } else {
                // If sign in fails, display a message to the user.
                Log.w("Firebase Auth", "signInWithCredential:failure", task.getException());
                Log.d("Error", "Firebase user has not be logged in");
                signedInFail();
            }
        }
    }
    //Class to handle when a user ahs clicked on the sign_in button
    private class SignInGoogleClickListener implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            //First we should check if the user has been signed with google
            if(!authUser.isSignedInGoogle(AuthenticationActivity.this)) {
                Intent signInIntent = authUser.googleClient.getSignInIntent();
                AuthenticationActivity.this.startActivityForResult(signInIntent, AuthUser.RC_SIGN_IN);
            }
        }
    }

    private class SigInAnonymouslyClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            Toast.makeText(AuthenticationActivity.this,getString(R.string.log_anonymously),Toast.LENGTH_SHORT).show();
            authUser.firebaseAuth.signInAnonymously()
                    .addOnCompleteListener(new OnFirebaseSigInCompleteListener());
        }
    }

    private void signedInSucceed() {

        HTTPClient client = new HTTPClientBuilder(this,new AddDeviceHandler(this,this))
                .addAuth()
                .setJsonHeader()
                .setResponseTimeOut(RESPONSE_TIME_OUT)
                .setTimeOut(RESPONSE_TIME_OUT)
                .setRetriesAndTimeout(1, TIME_OUT)
                .setUrl(ID_DEVICE_ENDPOINT)
                .build();
        try {
            client.post();
            dialog.show();
        } catch (HTTPClient.InternetError internetError) {
            dialog.cancel();
            StartActivity.launchNextActivity(this, Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            finish();
        }

    }

    private void signedInFail() {
        Toast.makeText(this,getString(R.string.error_while_login), Toast.LENGTH_LONG);
    }


}
