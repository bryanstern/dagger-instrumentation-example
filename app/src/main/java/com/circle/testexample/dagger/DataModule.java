package com.circle.testexample.dagger;

import com.circle.testexample.Api;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public final class DataModule {

    @Provides @Singleton
    public Api provideApi() {
        return new Api();
    }

}
