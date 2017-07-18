package com.life360.falx.util;

/**
 * Created by remon on 7/17/17.
 */

public interface Logger {


    int v(String tag, String msg);

    int v(String tag, String msg, Throwable tr);

    int d(String tag, String msg);

    int d(String tag, String msg, Throwable tr);

    int i(String tag, String msg);

    int i(String tag, String msg, Throwable tr);

    int w(String tag, String msg);

    int w(String tag, String msg, Throwable tr);

    int w(String tag, Throwable tr);

    int e(String tag, String msg);

    int e(String tag, String msg, Throwable tr);

//    public int wtf(String tag, String msg) {
//        throw new RuntimeException("Stub!");
//    }
//
//    public int wtf(String tag, Throwable tr) {
//        throw new RuntimeException("Stub!");
//    }
//
//    public int wtf(String tag, String msg, Throwable tr) {
//        throw new RuntimeException("Stub!");
//    }

}
