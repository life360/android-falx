package com.life360.falx.monitor_store;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmMigration;

/**
 * Created by Vikas on 9/19/17.
 */

public class FalxRealm implements RealmStore {

    private static final String DEFAULT_REAL_FILE_NAME = "falx.realm";
    private String realmFileName;

    public FalxRealm() {
        this.realmFileName = DEFAULT_REAL_FILE_NAME;
    }

    @Override
    public Realm realmInstance() {
        RealmConfiguration configuration = new RealmConfiguration.Builder()
                .name(realmFileName)
                .schemaVersion(1) //updated schema version to 1
                .migration(new FalxRealmMigration())
                .modules(new FalxLibraryModule())
                .build();
        return Realm.getInstance(configuration);
    }

    public void clearStore() {
        Realm realm = this.realmInstance();
        realm.beginTransaction();
        realm.deleteAll();
        realm.commitTransaction();
    }
}
