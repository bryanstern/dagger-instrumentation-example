package com.circle.testexample;

import android.app.Instrumentation.ActivityMonitor;

import com.circle.testexample.ui.AccountActivity;
import com.circle.testexample.ui.MainActivity;

import rx.Observable;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.mockito.Mockito.when;

public class MainActivityTest extends InjectedBaseActivityTest<MainActivity> {
    public MainActivityTest() {
        super(MainActivity.class);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();

        getActivity();
    }

    public void testLoginSuccess() {
        when(mockApi.login("real@user.com", "secret")).thenReturn(Observable.just(true));

        ActivityMonitor monitor = getInstrumentation().addMonitor(AccountActivity.class.getName(), null, true);

        onView(withId(R.id.username)).perform(typeText("real@user.com"));
        onView(withId(R.id.password)).perform(typeText("secret"));
        onView(withId(R.id.button)).perform(click());

        assertEquals(1, monitor.getHits());

        getInstrumentation().removeMonitor(monitor);
    }

    public void testLoginFailure() {
        when(mockApi.login("real@user.com", "secret")).thenReturn(Observable.just(false));

        onView(withId(R.id.username)).perform(typeText("real@user.com"));
        onView(withId(R.id.password)).perform(typeText("secret"));
        onView(withId(R.id.button)).perform(click());

        onView(withText(getActivity().getString(R.string.error_invalid_credentials)))
                .check(matches(isDisplayed()));
    }


}
