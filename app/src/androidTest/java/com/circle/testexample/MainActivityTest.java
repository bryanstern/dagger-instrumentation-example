package com.circle.testexample;

import android.app.Instrumentation.ActivityMonitor;
import android.support.test.runner.AndroidJUnit4;

import com.circle.testexample.ui.AccountActivity;
import com.circle.testexample.ui.MainActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;

import rx.Observable;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(AndroidJUnit4.class)
public class MainActivityTest {

    public final ActivityRule<MainActivity> main = new ActivityRule<>(MainActivity.class);
    public final InjectRule injectRule = new InjectRule();

    @Rule
    public final TestRule rule = RuleChain.outerRule(injectRule).around(main);

    @Test
    public void testLoginSuccess() {
        when(injectRule.dependencyWrapper.api.login("real@user.com", "secret")).thenReturn(Observable.just(true));

        ActivityMonitor monitor = main.instrumentation().addMonitor(AccountActivity.class.getName(), null, true);

        onView(withId(R.id.username)).perform(typeText("real@user.com"));
        onView(withId(R.id.password)).perform(typeText("secret"));
        onView(withId(R.id.button)).perform(click());

        assertEquals(1, monitor.getHits());

        main.instrumentation().removeMonitor(monitor);
    }

    @Test
    public void testLoginFailure() {
        when(injectRule.dependencyWrapper.api.login("real@user.com", "secret")).thenReturn(Observable.just(false));

        onView(withId(R.id.username)).perform(typeText("real@user.com"));
        onView(withId(R.id.password)).perform(typeText("secret"));
        onView(withId(R.id.button)).perform(click());

        onView(withText(main.get().getString(R.string.error_invalid_credentials))).check(matches(isDisplayed()));
    }

}
