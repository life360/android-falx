package com.life360.falx.model;

/**
 * Created by remon on 9/19/17.
 */

public class NetworkActivity {
    int count;
    int bytesReceived;

    public NetworkActivity(int count, int bytesReceived) {
        this.count = count;
        this.bytesReceived = bytesReceived;
    }

    @Override
    public String toString() {
        return "NetworkActivity{" +
                "count=" + count +
                ", bytesReceived=" + bytesReceived +
                '}';
    }

    public int getCount() {
        return count;
    }

    public int getBytesReceived() {
        return bytesReceived;
    }
}
