package com.life360.falx.model;

/**
 * Created by remon on 9/19/17.
 */

public class RealtimeMessagingActivity {
    int count;
    int bytesReceived;
    String protocol;            // Mqtt, Pubnub etc.

    public RealtimeMessagingActivity(int count, int bytesReceived, String protocol) {
        this.count = count;
        this.bytesReceived = bytesReceived;
        this.protocol = protocol;
    }

    @Override
    public String toString() {
        return "RealtimeMessagingActivity{" +
                "count=" + count +
                ", bytesReceived=" + bytesReceived +
                ", protocol='" + protocol + '\'' +
                '}';
    }

    public int getCount() {
        return count;
    }

    public int getBytesReceived() {
        return bytesReceived;
    }

    public String getProtocol() {
        return protocol;
    }
}
