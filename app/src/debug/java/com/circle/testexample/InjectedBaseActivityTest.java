package com.circle.testexample;

import android.test.ActivityInstrumentationTestCase2;

import com.circle.testexample.data.Api;
import com.circle.testexample.ui.BaseActivity;

import javax.inject.Inject;

public class InjectedBaseActivityTest extends ActivityInstrumentationTestCase2<BaseActivity> {
    @Inject
    Api mockApi;

    public InjectedBaseActivityTest(Class activityClass) {
        super(activityClass);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        App app = (App) getInstrumentation().getTargetContext().getApplicationContext();
        app.setMockMode(true);
        app.graph().inject(this);
    }

    @Override
    protected void tearDown() throws Exception {
        App.getInstance().setMockMode(false);
    }
}
