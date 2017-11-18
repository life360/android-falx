package com.life360.falx.monitor;


import android.support.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Note: Todo generate a Builder
 * Created by remon on 11/17/17.
 */

public class RealtimeMessagingSession {
    long sessionDuration;
    int numMessagesReceived;
    HashMap<String, Double> extras;

    public RealtimeMessagingSession(long sessionDuration, int numMessagesReceived, @Nullable Map<String, Double> extras) {
        this.sessionDuration = sessionDuration;
        this.numMessagesReceived = numMessagesReceived;

        if (extras != null) {
            this.extras = new HashMap<>();
            for (String key : extras.keySet()) {
                final Double value = extras.get(key);
                this.extras.put(key, value);
            }
        }
    }

    @Nullable
    public HashMap<String, Double> getExtras() {
        return extras;
    }
}
