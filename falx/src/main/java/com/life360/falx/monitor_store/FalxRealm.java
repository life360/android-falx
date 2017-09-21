package com.life360.falx.monitor_store;

import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * Created by Vikas on 9/19/17.
 */

public class FalxRealm implements RealmStore {

    private static final String DEFAULT_REAL_FILE_NAME = "falx.realm";
    private String realmFileName;

    public FalxRealm() {
        this.realmFileName = DEFAULT_REAL_FILE_NAME;
    }

    public FalxRealm(String realmFileName) {
        this.realmFileName = realmFileName;
    }

    @Override
    public Realm Realm() {
        RealmConfiguration configuration = new RealmConfiguration.Builder()
                .name(realmFileName)
                .build();
        return Realm().getInstance(configuration);

    }

    public void clearStore() {
        Realm realm = this.Realm();
        realm.beginTransaction();
        realm.deleteAll();
        realm.commitTransaction();
    }

}
