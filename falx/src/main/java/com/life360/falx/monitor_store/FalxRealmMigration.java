package com.life360.falx.monitor_store;

import android.util.Log;

import io.realm.DynamicRealm;
import io.realm.RealmMigration;

/**
 * Created by Vikas on 11/22/17.
 */

public class FalxRealmMigration implements RealmMigration {
    private static final String LOG_TAG = FalxRealmMigration.class.getSimpleName();

    @Override
    public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {
        Log.d(LOG_TAG, "falx realm schema version :" + oldVersion);
        if (oldVersion == 0) {
            realm.deleteAll();
            Log.d(LOG_TAG, "migration performed deleted all objects");
            oldVersion++;
        }
    }
}
