package com.life360.falx.model;

import io.realm.RealmObject;

/**
 * Store a key-value pair where the Key is a string and the value is a floating point.
 *
 * Created by Vikas on 9/21/17.
 */

public class EventArgument extends RealmObject {
    private String key;
    private Double value;

    public EventArgument() {
    }

    public EventArgument(String key, Double value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public Double getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "EventArgument{" +
                "key='" + key + '\'' +
                ", value=" + value +
                '}';
    }
}
