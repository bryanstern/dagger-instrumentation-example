package com.circle.testexample.ui;

import android.app.Activity;
import android.os.Bundle;

import com.circle.testexample.data.Api;
import com.circle.testexample.App;

import javax.inject.Inject;

public abstract class BaseActivity extends Activity {
    @Inject
    Api mApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        App.getInstance().graph().inject(this);
    }

    protected Api getApi() {
        return mApi;
    }
}
