package com.circle.testexample;

import android.test.ActivityInstrumentationTestCase2;

import com.circle.testexample.data.Api;

import javax.inject.Inject;

public class InjectedBaseActivityTest extends ActivityInstrumentationTestCase2 {
    @Inject
    Api mockApi;

    public InjectedBaseActivityTest(Class activityClass) {
        super(activityClass);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        App app = (App) getInstrumentation().getTargetContext().getApplicationContext();
        app.graph().inject(this);
    }

    @Override
    protected void tearDown() throws Exception {

    }
}
