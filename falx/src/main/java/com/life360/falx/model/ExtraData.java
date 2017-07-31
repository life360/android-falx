package com.life360.falx.model;

import io.realm.RealmObject;

/**
 * Created by remon on 7/27/17.
 */

public class ExtraData extends RealmObject {
    private String key;
    private String value;

    public ExtraData() {
    }

    public ExtraData(String key, String value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public String toString() {
        return new String("{" + key + ", " + value + "}");
    }
}
