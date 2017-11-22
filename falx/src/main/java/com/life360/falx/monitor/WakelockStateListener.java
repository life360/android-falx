package com.life360.falx.monitor;

/**
 * Created by sudheer on 11/22/17.
 */

public interface WakelockStateListener {
    void acquired(long maxDurartion);
    void acquired();
    void released();
}
