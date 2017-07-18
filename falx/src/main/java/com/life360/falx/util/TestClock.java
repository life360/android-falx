package com.life360.falx.util;

/**
 * Created by remon on 7/12/17.
 */

/**
 * Wrapper for any function to query time from device.
 * Meant to make testing easier by mocking this class.
 */
public class TestClock extends Clock {

    private long currentTimeMillis;

    public void setCurrentTimeMillis(long time) {
        currentTimeMillis = time;
    }

    public long currentTimeMillis() {
        return currentTimeMillis;
    }
}
