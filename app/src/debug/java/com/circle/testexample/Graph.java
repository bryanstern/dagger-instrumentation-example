package com.circle.testexample;

import com.circle.testexample.dagger.DebugDataModule;
import com.circle.testexample.dagger.DependencyWrapper;
import com.circle.testexample.ui.BaseActivity;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {DebugDataModule.class})
public interface Graph {

    void inject(BaseActivity activity);
    void inject(DependencyWrapper wrapper);

    public final static class Initializer {
        public static Graph init(boolean mockMode) {
            return Dagger_Graph.builder()
                    .debugDataModule(new DebugDataModule(mockMode))
                    .build();
        }
    }
}
