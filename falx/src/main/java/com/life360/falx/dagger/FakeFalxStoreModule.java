package com.life360.falx.dagger;

import android.content.Context;

import com.life360.falx.monitor_store.FalxEventStorable;
import com.life360.falx.monitor_store.FalxRealm;
import com.life360.falx.monitor_store.RealmStore;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by remon on 10/17/17.
 */

@Module
class FakeFalxStoreModule {
    public FakeFalxStoreModule() {
    }

    @Provides
    RealmStore provideRealmStore() {
        return new FalxRealm();
    }

    @Provides
    @Singleton
    FalxEventStorable provideEventStore(Context appContext, RealmStore realmStore) {
        return new FakeFalxEventStore(realmStore, appContext);
    }

}
