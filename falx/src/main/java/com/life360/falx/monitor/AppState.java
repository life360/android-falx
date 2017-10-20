package com.life360.falx.monitor;

/**
 * Created by remon on 7/25/17.
 */

public enum AppState {
    FOREGROUND(FalxConstants.EVENT_FOREGROUND),
    BACKGROUND(FalxConstants.EVENT_BACKGROUND);

    AppState(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

    private final String name;
}
