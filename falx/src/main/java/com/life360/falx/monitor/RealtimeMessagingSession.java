package com.life360.falx.monitor;


/**
 * Note: Todo generate a Builder
 * Created by remon on 11/17/17.
 */

public class RealtimeMessagingSession {
    // Note: Not all of these values will be reported with the current design for Falx Events

    long connectTime;
    long sessionDuration;
    int numMessagesReceived;
    long totalBytesReceived;
    int numTopicsRequested;

    public RealtimeMessagingSession(long connectTime, long sessionDuration, int numMessagesReceived, long totalBytesReceived, int numTopicsRequested) {
        this.connectTime = connectTime;
        this.sessionDuration = sessionDuration;
        this.numMessagesReceived = numMessagesReceived;
        this.totalBytesReceived = totalBytesReceived;
        this.numTopicsRequested = numTopicsRequested;
    }
}
