package com.life360.falx.monitor;

/**
 * Created by remon on 10/20/17.
 */

public class FalxConstants {

    // Event names
    public static final String EVENT_FOREGROUND = "foreground";
    public static final String EVENT_BACKGROUND = "background";
    public static final String EVENT_NETWORK = "falx-network";
    public static final String EVENT_GPS_ON = "gps-on";
    public static final String EVENT_GPS_OFF = "gps-off";
    public static final String EVENT_ACTIVITY_DETECTION_ON = "activities-on";

    // Event property names
    public static final String PROP_DURATION = "duration";
    public static final String PROP_BYTES_RECEIVED = "bytesReceived";
    public static final String PROP_COUNT = "count";

    public static final String MONITOR_LABEL_GPS = "GPS";
    /* Monitor for use by the legacy location code */
    public static final String MONITOR_LABEL_GPS_LEGACY = "GPS-legacy";
    public static final String MONITOR_LABEL_ACTIVITY_DETECTION = "ActivityDetection";
    /* Activity detection Monitor for use by the legacy location code */
    public static final String MONITOR_LABEL_ACTIVITY_DETECTION_LEGACY = "ActivityDetection-legacy";
    public static final String MONITOR_LABEL_WAKE_LOCKS = "WakeLocks";

}
