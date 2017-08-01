package com.life360.falx.model;

import com.life360.falx.monitor.AppState;

import java.util.ArrayList;
import java.util.List;

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

    public List<ExtraData> getExtras() {
        List<ExtraData> extras = new ArrayList<>();
        extras.add(new ExtraData(DURATION, Long.toString(getDuration())));

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
