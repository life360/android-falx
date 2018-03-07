// Copyright 2018 Life360, Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

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

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
