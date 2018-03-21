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

package com.life360.falx.monitor_store;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Vikas on 9/19/17.
 */

public class AggregatedFalxMonitorEvent {

    public static final String DURATION = "duration";
    public static final String COUNT = "count";
    public static final String BYTES_RECEIVED = "bytesReceived";
    public static final String TIMESTAMP = "timestamp";
    private String name;
    protected Map<String, Double> arguments = new HashMap<>();

    public AggregatedFalxMonitorEvent(String name, int count, long timestamp) {
        this.name = name;
        this.arguments.put(AggregatedFalxMonitorEvent.COUNT, new Double(count));
        this.arguments.put(AggregatedFalxMonitorEvent.TIMESTAMP, new Double(timestamp));
    }

    public String getName() {
        return name;
    }

    /**
     * Return a JSONObject containing the key-value pairs for the event
     * @throws JSONException
     */
    public JSONObject getParamsAsJson() throws JSONException {
        JSONObject object = new JSONObject();

        for (String key : arguments.keySet()) {
            Double value = arguments.get(key);
            if (value == null) {
                value = 0.0;
            }

            object.put(key, value);
        }

        return object;
    }

    public void putArgument(String key, Double value) {
        arguments.put(key, value);
    }

    @Override
    public String toString() {
        return "AggregatedFalxMonitorEvent{" +
                "name='" + name + '\'' +
                ", arguments=" + arguments +
                '}';
    }
}
