package com.circle.testexample;

import android.support.test.InstrumentationRegistry;

import com.circle.testexample.dagger.DependencyWrapper;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

public class InjectRule implements TestRule {

    DependencyWrapper dependencyWrapper = new DependencyWrapper();

    public InjectRule() {
    }

    @Override
    public Statement apply(final Statement base, Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                App app = (App) InstrumentationRegistry.getInstrumentation().getTargetContext().getApplicationContext();
                app.setMockMode(true);
                app.graph().inject(dependencyWrapper);

                base.evaluate();
            }
        };
    }
}
