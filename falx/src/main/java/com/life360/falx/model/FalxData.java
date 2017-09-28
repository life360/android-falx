package com.life360.falx.model;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmObject;

/**
 * Created by remon on 7/27/17.
 */

public class FalxData extends RealmObject {

    protected String name;
    protected long timestamp;       // in milliseconds
    protected RealmList<ExtraData> extras;

    public FalxData() {
    }

    public FalxData(String name, long timestamp, List<ExtraData> extras) {
        this.name = name;
        this.timestamp = timestamp;

        this.extras = new RealmList<>();

        for (ExtraData extraData : extras) {
            this.extras.add(extraData);
        }
    }

    /**
     * Saves the object to a realmInstance data store.
     */
    public void save() {
        Realm realm = Realm.getDefaultInstance();

        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.copyToRealm(FalxData.this);
            }
        });
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("{name = ").append(name).append("}, timestamp: ").append(timestamp);

        if (extras != null) {
            for (ExtraData data : extras) {
                builder.append("\n").append(data.toString());
            }
        }

        return builder.toString();
    }
}
