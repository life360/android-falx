package com.life360.falx.monitor;

/**
 * Created by sudheer on 11/22/17.
 */

public class WakelockState {
    boolean locked;
    long maxDuration;
    public WakelockState(boolean isLocked) {
        locked = isLocked;
    }
    public WakelockState(boolean isLocked, long duration) {
        locked = isLocked;
        if (isLocked) {
            maxDuration = duration;
        }
    }
}
