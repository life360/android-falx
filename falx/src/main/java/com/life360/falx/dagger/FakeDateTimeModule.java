package com.life360.falx.dagger;

import com.life360.falx.util.Clock;
import com.life360.falx.util.TestClock;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by remon on 7/17/17.
 */

@Module
public class FakeDateTimeModule {

    public FakeDateTimeModule() {
    }

    @Provides
    @Singleton
    Clock provideClock() {
        return new TestClock();
    }
}
