package com.life360.falx.monitor_store;

import android.util.Log;

import io.realm.CompactOnLaunchCallback;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmMigration;

/**
 * Created by Vikas on 9/19/17.
 */

public class FalxRealm implements RealmStore {

    private static final String DEFAULT_REAL_FILE_NAME = "falx.realm";
    private String realmFileName;
    final RealmMigration realmMigration;
    final FalxLibraryModule libraryModule;
    private RealmConfiguration realmConfiguration;

    public FalxRealm() {
        this.realmFileName = DEFAULT_REAL_FILE_NAME;
        realmMigration = new FalxRealmMigration();
        libraryModule = new FalxLibraryModule();
        realmConfiguration = null;
    }

    @Override
    public Realm realmInstance() {
        return Realm.getInstance(getRealmConfiguration());
    }

    private synchronized RealmConfiguration getRealmConfiguration() {
        if (realmConfiguration == null) {
            realmConfiguration = new RealmConfiguration.Builder()
                    .name(realmFileName)
                    .schemaVersion(1) //updated schema version to 1
                    .migration(realmMigration)
                    .modules(libraryModule)
                    .compactOnLaunch(new CompactOnLaunchCallback() {
                        @Override
                        public boolean shouldCompact(long totalBytes, long usedBytes) {
                            Log.d("size", "total :" + totalBytes + "  used :" + usedBytes);
                            return true;
                        }
                    })
                    .build();
        }
        return realmConfiguration;
    }

    public void clearStore() {
        Realm realm = this.realmInstance();
        realm.beginTransaction();
        realm.deleteAll();
        realm.commitTransaction();
    }
}
