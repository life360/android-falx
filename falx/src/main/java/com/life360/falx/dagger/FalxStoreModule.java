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

import android.content.Context;

import com.life360.falx.monitor_store.FalxEventStorable;
import com.life360.falx.monitor_store.FalxEventStore;
import com.life360.falx.monitor_store.FalxRealm;
import com.life360.falx.monitor_store.RealmStore;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by remon on 7/17/17.
 */

@Module
public class FalxStoreModule {

    public FalxStoreModule() {
    }

    @Provides
    RealmStore provideRealmStore() {
        return new FalxRealm();
    }

    @Provides
    @Singleton
    FalxEventStorable provideEventStore(Context appContext, RealmStore realmStore) {
        return new FalxEventStore(realmStore, appContext);
    }
}
