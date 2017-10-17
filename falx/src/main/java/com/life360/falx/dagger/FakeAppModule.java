package com.life360.falx.dagger;

import android.content.Context;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by remon on 7/17/17.
 * Todo: How can we fake the context?
 * Currently not faking it, and using Mockito with a Mock Context to use this Module.
 */

@Module
public class FakeAppModule {

    Context appContext;

    public FakeAppModule(Context application) {
        appContext = application;
    }

    @Provides
    @Singleton
    Context providesApplication() {
        return appContext;
    }
}