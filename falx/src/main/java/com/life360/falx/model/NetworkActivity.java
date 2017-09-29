package com.life360.falx.model;

/**
 * Created by remon on 9/19/17.
 */

public class NetworkActivity {
    int count;
    int bytesReceived;
    String url;

    public NetworkActivity(int count, int bytesReceived, String url) {
        this.count = count;
        this.bytesReceived = bytesReceived;
        this.url = url;
    }

    @Override
    public String toString() {
        return "NetworkActivity{" +
                "count=" + count +
                ", bytesReceived=" + bytesReceived +
                ", url='" + url + '\'' +
                '}';
    }

    public int getCount() {
        return count;
    }

    public int getBytesReceived() {
        return bytesReceived;
    }

    public String getUrl() {
        return url;
    }
}
