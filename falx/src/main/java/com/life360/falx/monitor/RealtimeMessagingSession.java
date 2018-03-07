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

package com.life360.falx.monitor;


import android.support.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Note: Todo generate a Builder
 * Created by remon on 11/17/17.
 */

public class RealtimeMessagingSession {
    long sessionDuration;
    int numMessagesReceived;
    HashMap<String, Double> extras;

    public RealtimeMessagingSession(long sessionDuration, int numMessagesReceived, @Nullable Map<String, Double> extras) {
        this.sessionDuration = sessionDuration;
        this.numMessagesReceived = numMessagesReceived;

        if (extras != null) {
            this.extras = new HashMap<>();
            for (String key : extras.keySet()) {
                final Double value = extras.get(key);
                this.extras.put(key, value);
            }
        }
    }

    @Nullable
    public HashMap<String, Double> getExtras() {
        return extras;
    }
}
