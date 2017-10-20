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
 * Created by remon on 10/6/17.
 */

public class BatteryStatReporter extends IntentService {
    private static final String TAG = "BatteryStatReporter";

    /* Service settings */
    private static final String ACTION_SEND_LOGS = ".ACTION_SEND_LOGS";

    public static void sendLogs(Context context) {
        Intent intent = new Intent(context, BatteryStatReporter.class);
        intent.setAction(context.getPackageName() + ACTION_SEND_LOGS);
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

        if (action.endsWith(ACTION_SEND_LOGS)) {
            Log.d(TAG, "Sending battery logs...");

            // Test fetching aggregated events from FalxApi:
            List<AggregatedFalxMonitorEvent> aggregatedFalxMonitorEvents = FalxApi.getInstance(this).allAggregatedEvents(true);

            for (AggregatedFalxMonitorEvent event : aggregatedFalxMonitorEvents) {
                Log.d(TAG, event.toString());

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
