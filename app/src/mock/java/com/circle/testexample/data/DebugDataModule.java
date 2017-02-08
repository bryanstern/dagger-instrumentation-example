package com.circle.testexample.data;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

import static org.mockito.Mockito.mock;

@Module
public final class DebugDataModule {


    public DebugDataModule() {

    }

    @Provides
    @Singleton
    Api provideApi() {
        return mock(Api.class);
    }

}
