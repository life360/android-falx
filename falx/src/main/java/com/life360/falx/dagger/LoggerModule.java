package com.life360.falx.dagger;

import com.life360.falx.util.Logger;
import com.life360.falx.util.LoggerImpl;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by remon on 7/17/17.
 */

@Module
public class LoggerModule {

    public LoggerModule() {
    }

    @Provides
    @Singleton
    Logger provideLogger() {
        return new LoggerImpl();
    }
}
