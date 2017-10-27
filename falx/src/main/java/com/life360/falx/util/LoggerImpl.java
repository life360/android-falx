package com.life360.falx.util;

import android.util.Log;

/**
 * Created by remon on 7/17/17.
 */

public class LoggerImpl implements Logger {
    private boolean enabled;

    @Override
    public int v(String tag, String msg) {
        if (enabled) {
            return Log.v(tag, msg);
        } else {
            return 0;
        }
    }

    @Override
    public int v(String tag, String msg, Throwable tr) {
        if (enabled) {
            return Log.v(tag, msg, tr);
        } else {
            return 0;
        }
    }

    @Override
    public int d(String tag, String msg) {
        if (enabled) {
            return Log.d(tag, msg);
        } else {
            return 0;
        }
    }

    @Override
    public int d(String tag, String msg, Throwable tr) {
        if (enabled) {
            return Log.d(tag, msg, tr);
        } else {
            return 0;
        }
    }

    @Override
    public int i(String tag, String msg) {
        if (enabled) {
            return Log.i(tag, msg);
        } else {
            return 0;
        }
    }

    @Override
    public int i(String tag, String msg, Throwable tr) {
        if (enabled) {
            return Log.i(tag, msg, tr);
        } else {
            return 0;
        }
    }

    @Override
    public int w(String tag, String msg) {
        if (enabled) {
            return Log.w(tag, msg);
        } else {
            return 0;
        }
    }

    @Override
    public int w(String tag, String msg, Throwable tr) {
        if (enabled) {
            return Log.w(tag, msg, tr);
        } else {
            return 0;
        }
    }

    @Override
    public int w(String tag, Throwable tr) {
        if (enabled) {
            return Log.w(tag, tr);
        } else {
            return 0;
        }
    }

    @Override
    public int e(String tag, String msg) {
        if (enabled) {
            return Log.e(tag, msg);
        } else {
            return 0;
        }
    }

    @Override
    public int e(String tag, String msg, Throwable tr) {
        if (enabled) {
            return Log.e(tag, msg, tr);
        } else {
            return 0;
        }
    }

    @Override
    public void setEnabled(boolean enable) {
        enabled = enable;
    }
}
