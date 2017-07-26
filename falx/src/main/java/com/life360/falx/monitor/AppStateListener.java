package com.life360.falx.monitor;

/**
 * Created by remon on 7/25/17.
 */

public interface AppStateListener {

    void onEnterBackground();
    void onEnterForeground();
}
