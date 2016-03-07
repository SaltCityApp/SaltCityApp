package com.tylerrockwood.passwordkeeper;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

import java.io.IOException;


public class MainActivity extends AppCompatActivity implements LoginFragment.OnLoginListener, PasswordFragment.OnLogoutListener, GoogleApiClient.OnConnectionFailedListener {

    public static final String FIREBASE_REPO = "tarik-passwordkeeper";
    public static final String FIREBASE_URL = "https://" + FIREBASE_REPO + ".firebaseio.com/";
    public static final String FIREBASE = "FIREBASE";
    public static final String TAG = "LOG";
    private static final int REQUEST_CODE_GOOGLE_LOGIN = 1;
    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            Firebase.setAndroidContext(this);
        }


        GoogleSignInOptions gso = new GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();


        Firebase firebase = new Firebase(FIREBASE_URL);
        if (firebase.getAuth() == null || isNotExpired(firebase.getAuth()) == false ) {
            switchToLoginFragment();
        } else {
            switchToPasswordFragment(FIREBASE_URL + "/users/" + firebase.getAuth().getUid());
        }
    }

    @Override
    public void onLogin(String email, String password) {
        //TODO: Log user in with username & password
        Firebase firebase = new Firebase(FIREBASE_URL);

        firebase.authWithPassword(email, password, new MyAuthResultHandler());
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e(TAG, "onConnectionFailed:" + connectionResult.getErrorMessage());
    }

    class MyAuthResultHandler implements Firebase.AuthResultHandler {

        @Override
        public void onAuthenticated(AuthData authData) {
            switchToPasswordFragment(FIREBASE_URL + "/users/" + authData.getUid());
        }

        @Override
        public void onAuthenticationError(FirebaseError firebaseError) {
            Log.e(TAG, "onAuthError:" + firebaseError.getMessage());
        }
    }

    @Override
    public void onGoogleLogin() {
        //TODO: Log user in with Google Account
        Intent intent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(intent, REQUEST_CODE_GOOGLE_LOGIN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode ==  REQUEST_CODE_GOOGLE_LOGIN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if(result.isSuccess()) {
                GoogleSignInAccount account = result.getSignInAccount();
                String emailAddress = account.getEmail();
                getGoogleOAuthToken(emailAddress);
            }
        }
    }

    private void getGoogleOAuthToken(final String emailAddress) {
        AsyncTask<Void, Void, String> task = new AsyncTask<Void, Void, String>() {
            String errorMessage = null;

            @Override
            protected String doInBackground(Void... params) {
                String token = null;
                try {
                    String scope = "oauth2:profile email";
                    token = GoogleAuthUtil.getToken(MainActivity.this, emailAddress, scope);
                } catch (IOException transientEx) {
                /* Network or server error */
                    errorMessage = "Network error: " + transientEx.getMessage();
                } catch (UserRecoverableAuthException e) {
                /* We probably need to ask for permissions, so start the intent if there is none pending */
                    Intent recover = e.getIntent();
                    startActivityForResult(recover, MainActivity.REQUEST_CODE_GOOGLE_LOGIN);
                } catch (GoogleAuthException authEx) {
                    errorMessage = "Error authenticating with Google: " + authEx.getMessage();
                }
                return token;
            }
            @Override
            protected void onPostExecute(String token) {
                Log.d("FPK", "onPostExecute");
                //TODO
                if (token != null) {
                    onGoogleLoginWithToken(token);
                } else {
                    showLoginError(errorMessage);
                }
            }
        };
        task.execute();
    }

    private void onGoogleLoginWithToken(String oAuthToken) {
        //TODO: Log user in with Google OAuth Token
        Firebase firebase = new Firebase(FIREBASE_URL);
        firebase.authWithOAuthToken("google", oAuthToken, new MyAuthResultHandler());
    }

    @Override
    public void onLogout() {
        //TODO: Log the user out.
        Firebase firebase = new Firebase(FIREBASE_URL);
        firebase.unauth();
        switchToLoginFragment();
    }

    // MARK: Provided Helper Methods
    private void switchToLoginFragment() {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fragment, new LoginFragment(), "Login");
        ft.commit();
    }

    private void switchToPasswordFragment(String repoUrl) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment passwordFragment = new PasswordFragment();
        Bundle args = new Bundle();
        args.putString(FIREBASE, repoUrl);
        passwordFragment.setArguments(args);
        ft.replace(R.id.fragment, passwordFragment, "Passwords");
        ft.commit();
    }

    private void showLoginError(String message) {
        LoginFragment loginFragment = (LoginFragment) getSupportFragmentManager().findFragmentByTag("Login");
        loginFragment.onLoginError(message);
    }

    private boolean isNotExpired(AuthData authData) {
        return authData.getExpires() > (System.currentTimeMillis() / 1000);
    }
}
