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

package com.life360.batterytestapp;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.util.Log;

import com.life360.falx.FalxApi;
import com.life360.falx.monitor_store.AggregatedFalxMonitorEvent;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * A service that demonstrates how to get battery stats logged by Falx.
 *
 * Created by remon on 10/6/17.
 */

public class BatteryStatReporter extends IntentService {
    private static final String TAG = "BatteryStatReporter";

    /* Service settings */
    private static final String ACTION_READ_LOGS = ".ACTION_READ_LOGS";

    public static void readLogs(Context context) {
        Intent intent = new Intent(context, BatteryStatReporter.class);
        intent.setAction(context.getPackageName() + ACTION_READ_LOGS);
        context.startService(intent);
    }

    public BatteryStatReporter() {
        super(TAG);
    }

    public BatteryStatReporter(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent == null) {
            return;
        }

        String action = intent.getAction();

        if (action.endsWith(ACTION_READ_LOGS)) {
            Log.d(TAG, "Reading battery logs...");

            // Fetch aggregated events from FalxApi:
            List<AggregatedFalxMonitorEvent> aggregatedFalxMonitorEvents =
                    FalxApi.getInstance(this).allAggregatedEvents(true);

            for (AggregatedFalxMonitorEvent event : aggregatedFalxMonitorEvents) {
                Log.d(TAG, event.toString());

                // You can get logged key-value pairs for each event from the AggregatedFalxMonitorEvent object
                // AggregatedFalxMonitorEvent.getName() will give you the name of the monitor

                JSONObject params;

                try {
                    params = event.getParamsAsJson();
                    Log.d(TAG, "params: " + params.toString());
                } catch (JSONException e) {
                    Log.e(TAG, e.toString());
                }
            }

            Intent broadcastIntent = new Intent("ACTION_SEND_BATTERY_STATS_RESULT");
            sendBroadcast(broadcastIntent);
        }
    }
}
