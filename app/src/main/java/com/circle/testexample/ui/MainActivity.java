package com.circle.testexample.ui;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.circle.testexample.R;

import rx.Subscription;
import rx.android.observables.AndroidObservable;
import rx.functions.Action1;


public class MainActivity extends BaseActivity {

    Subscription loginSubscription;

    EditText mUsernameEditText, mPasswordEditText;
    Button mLoginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        mUsernameEditText = (EditText) findViewById(R.id.username);
        mPasswordEditText = (EditText) findViewById(R.id.password);
        mLoginButton = (Button) findViewById(R.id.button);

        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doLogin(mUsernameEditText.getEditableText().toString(), mPasswordEditText.getEditableText().toString());
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (loginSubscription != null) {
            loginSubscription.unsubscribe();
        }
    }

    void doLogin(String username, String password) {
        loginSubscription = AndroidObservable.bindActivity(this, getApi().login(username, password))
                .subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean successful) {
                        if (successful) {
                            startActivity(new Intent(MainActivity.this, AccountActivity.class));
                        } else {
                            new AlertDialog.Builder(MainActivity.this)
                                    .setMessage(R.string.error_invalid_credentials)
                                    .show();
                        }
                    }
                });
    }


}
