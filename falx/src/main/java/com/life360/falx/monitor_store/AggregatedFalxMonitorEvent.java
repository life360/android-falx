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
    public static final String TIMESTAMP = "timestamp";
    private String name;
    private Map<String, Double> arguments = new HashMap<>();

    public AggregatedFalxMonitorEvent(String name, int count, Date timestamp) {
        this.name = name;
        this.arguments.put(AggregatedFalxMonitorEvent.COUNT, new Double(count));
        Calendar cal = Calendar.getInstance();
        cal.setTime(timestamp);
        this.arguments.put(AggregatedFalxMonitorEvent.TIMESTAMP, new Double(cal.getTimeInMillis()));
    }

    public String getName() {
        return name;
    }

    public Double getDuration() {
        if (this.arguments.containsKey(AggregatedFalxMonitorEvent.DURATION)) {
            return this.arguments.get(AggregatedFalxMonitorEvent.DURATION);
        }
        return 0.0;
    }

    public Double getCount() {
        if (this.arguments.containsKey(AggregatedFalxMonitorEvent.COUNT)) {
            return this.arguments.get(AggregatedFalxMonitorEvent.COUNT);
        }
        return 0.0;
    }

    public Double getTimestamp() {
        if (this.arguments.containsKey(AggregatedFalxMonitorEvent.TIMESTAMP)) {
            return this.arguments.get(AggregatedFalxMonitorEvent.TIMESTAMP);
        }
        return 0.0;
    }

    /**
     * Reterun a JSONObject containing the key-value pairs for the event
     * @throws JSONException
     */
    public JSONObject getParamsAsJson() throws JSONException {
        JSONObject object = new JSONObject();

        object.put(COUNT, getCount());
        object.put(TIMESTAMP, getTimestamp());
        object.put(DURATION, getDuration());

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
