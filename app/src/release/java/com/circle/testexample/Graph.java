package com.circle.testexample;

import com.circle.testexample.ui.BaseActivity;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {DataModule.class})
public interface Graph {

    void inject(BaseActivity activity);

    public final static class Initializer {
        public static Graph init(boolean mockMode) {
            return Dagger_Graph.builder()
                    .build();
        }
    }
}