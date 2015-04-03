package com.circle.testexample.dagger;

import com.circle.testexample.Api;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

import static org.mockito.Mockito.mock;

@Module
public final class DebugDataModule {

    private final boolean mockMode;

    public DebugDataModule(boolean provideMocks) {
        mockMode = provideMocks;
    }

    @Provides @Singleton
    Api provideApi() {
        if (mockMode) {
            return mock(Api.class);
        } else {
            return new Api();
        }
    }

}
