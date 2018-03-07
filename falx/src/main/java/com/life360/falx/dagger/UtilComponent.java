// Copyright 2018 Life360, Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.life360.falx.dagger;

import com.life360.falx.FalxApi;
import com.life360.falx.monitor.AppStateMonitor;
import com.life360.falx.monitor.NetworkMonitor;
import com.life360.falx.monitor.OnOffMonitor;
import com.life360.falx.monitor.RealtimeMessagingMonitor;
import com.life360.falx.monitor.WakelockMonitor;
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
    void inject(RealtimeMessagingMonitor rtDataMonitor);
    void inject(OnOffMonitor onOffMonitor);
    void inject(WakelockMonitor wakelockMonitor);
}
