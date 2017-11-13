package com.life360.falx.dagger;

import com.life360.falx.FalxApi;
import com.life360.falx.monitor.AppStateMonitor;
import com.life360.falx.monitor.NetworkMonitor;
import com.life360.falx.monitor.OnOffMonitor;
import com.life360.falx.network.FalxInterceptor;

import javax.inject.Singleton;

import dagger.Component;

/**
 * Created by remon on 7/17/17.
 */

@Singleton
@Component(modules = {AppModule.class, DateTimeModule.class, LoggerModule.class, FalxStoreModule.class})
public interface UtilComponent {
    void inject(FalxApi api);
    void inject(AppStateMonitor monitor);
    void inject(FalxInterceptor interceptor);
    void inject(NetworkMonitor networkMonitor);
    void inject(OnOffMonitor onOffMonitor);
}
