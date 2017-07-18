package com.life360.falx.dagger;

import com.life360.falx.util.Clock;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by remon on 7/17/17.
 */

@Module
public class DateTimeModule {

    public DateTimeModule() {
    }

    @Provides
    @Singleton
    Clock provideClock() {
        return new Clock();
    }
}
