package com.life360.falx.dagger;

import com.life360.falx.util.Logger;
import com.life360.falx.util.TestLoggerImpl;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by remon on 7/17/17.
 */

@Module
public class TestLoggerModule {

    public TestLoggerModule() {
    }

    @Provides
    @Singleton
    Logger provideLogger() {
        return new TestLoggerImpl();
    }
}
