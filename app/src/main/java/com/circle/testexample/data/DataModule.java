package com.circle.testexample.data;

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
