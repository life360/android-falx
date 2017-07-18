package com.life360.falx.util;

/**
 * Created by remon on 7/17/17.
 */

public class TestLoggerImpl implements Logger {
    @Override
    public int v(String tag, String msg) {
        return 0;
    }

    @Override
    public int v(String tag, String msg, Throwable tr) {
        return 0;
    }

    @Override
    public int d(String tag, String msg) {
        return 0;
    }

    @Override
    public int d(String tag, String msg, Throwable tr) {
        return 0;
    }

    @Override
    public int i(String tag, String msg) {
        return 0;
    }

    @Override
    public int i(String tag, String msg, Throwable tr) {
        return 0;
    }

    @Override
    public int w(String tag, String msg) {
        return 0;
    }

    @Override
    public int w(String tag, String msg, Throwable tr) {
        return 0;
    }

    @Override
    public int w(String tag, Throwable tr) {
        return 0;
    }

    @Override
    public int e(String tag, String msg) {
        return 0;
    }

    @Override
    public int e(String tag, String msg, Throwable tr) {
        return 0;
    }
}
