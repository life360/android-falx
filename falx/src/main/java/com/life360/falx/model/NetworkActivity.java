package com.life360.falx.model;

/**
 * Created by remon on 9/19/17.
 */

public class NetworkActivity {
    int count;
    int bytesReceived;
    double responseDuration;
    String url;

    public NetworkActivity(int count, int bytesReceived, double responseDuration, String url) {
        this.count = count;
        this.bytesReceived = bytesReceived;
        this.responseDuration = responseDuration;
        this.url = url;
    }

    @Override
    public String toString() {
        return "NetworkActivity{" +
                "count=" + count +
                ", bytesReceived=" + bytesReceived +
                ", responseDuration=" + responseDuration +
                ", url='" + url + '\'' +
                '}';
    }

    public int getCount() {
        return count;
    }

    public int getBytesReceived() {
        return bytesReceived;
    }

    public double getResponseDuration() {
        return responseDuration;
    }

    public String getUrl() {
        return url;
    }
}
