package com.life360.falx.monitor;

/**
 * Created by sudheer on 10/30/17.
 */

public enum GpsState {
    ON(FalxConstants.EVENT_GPS_ON),
    OFF(FalxConstants.EVENT_GPS_OFF);

    GpsState(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

    private final String name;

}
