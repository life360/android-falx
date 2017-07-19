package com.life360.falx.dagger;

import com.life360.falx.FalxApi;

import javax.inject.Singleton;

import dagger.Component;

/**
 * Created by remon on 7/17/17.
 */

@Singleton
@Component(modules = {DateTimeModule.class, LoggerModule.class})
public interface UtilComponent {
    void inject(FalxApi api);
}
