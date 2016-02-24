package com.ata_dev.tarik.fettest2;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.firebase.client.AuthData;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.Map;

public class MainActivity extends AppCompatActivity {

    TextView mTextFieldCondition;
    Button mButtonSunny;
    Button mButtonFoggy;
    Firebase mRef;
    EditText mUserName;
    Button mButtonLogin;
    EditText mPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mTextFieldCondition = (TextView)findViewById(R.id.textViewCondition);
        mButtonSunny = (Button)findViewById(R.id.buttonSunny);
        mButtonFoggy = (Button)findViewById(R.id.buttonFoggy);

        mUserName = (EditText)findViewById(R.id.userName);
        mPassword = (EditText) findViewById(R.id.password);
        mButtonLogin = (Button)findViewById(R.id.buttonLogin);
        mRef = new Firebase("https://fet-network.firebaseio.com/condition");

        Firebase ref = new Firebase("https://fet-network.firebaseio.com");

        mButtonLogin.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                Firebase ref = new Firebase("https://fet-network.firebaseio.com");

                Firebase.AuthResultHandler authResultHandler = new Firebase.AuthResultHandler() {
                    @Override
                    public void onAuthenticated(AuthData authData) {
                        // Authenticated successfully with payload authData
                    }
                    @Override
                    public void onAuthenticationError(FirebaseError firebaseError) {
                        // Authenticated failed with error firebaseError
                    }
                };

                ref.authWithPassword("jenny@example.com", "correcthorsebatterystaple", authResultHandler);
            }
        });

        Firebase.AuthResultHandler authResultHandler = new Firebase.AuthResultHandler() {
            @Override
            public void onAuthenticated(AuthData authData) {
                // Authenticated successfully with payload authData
            }
            @Override
            public void onAuthenticationError(FirebaseError firebaseError) {
                // Authenticated failed with error firebaseError
            }
        };
        // Authenticate users with a custom Firebase token
                ref.authWithCustomToken("<token>", authResultHandler);
        // Alternatively, authenticate users anonymously
                ref.authAnonymously(authResultHandler);
        // Or with an email/password combination
                ref.authWithPassword("jenny@example.com", "correcthorsebatterystaple", authResultHandler);

        mRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String text = dataSnapshot.getValue(String.class);
                mTextFieldCondition.setText(text);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

        mButtonFoggy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mRef.setValue("Foggy");
            }
        });
        mButtonSunny.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mRef.setValue("Sunny");
            }
        });
    }
}
