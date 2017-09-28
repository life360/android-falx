package com.life360.falx.model;

import com.life360.falx.monitor.AppState;
import com.life360.falx.monitor_store.Arguments;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by remon on 7/12/17.
 */
public class SessionData {

    public static final String DURATION = "duration";

    public long startTime;          // in milliseconds
    public long endTime;            // in milliseconds
    private String name;

    public SessionData(AppState appState, long startTime, long endTime) {
        this.name = appState.toString();
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public long getDuration() {
        return endTime - startTime;
    }

    public Map<String, Double> getArgumentMap() {
        Map<String, Double> extras = new HashMap<>();
        extras.put(DURATION, new Double(getDuration()));

        return extras;
    }

    @Override
    public String toString() {
        return new StringBuilder(super.toString()).append(" startTime = ").append(startTime).append(" duration = ").append(getDuration() / 1000).toString();
    }

    public String getName() {
        return name;
    }
}
