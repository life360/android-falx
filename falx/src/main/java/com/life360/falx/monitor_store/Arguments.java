package com.life360.falx.monitor_store;

import io.realm.RealmObject;

/**
 * Created by Vikas on 9/21/17.
 */

public class Arguments extends RealmObject {
    private String key;
    private Double value;

    public Arguments() {
    }

    public Arguments(String key, Double value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }
}
