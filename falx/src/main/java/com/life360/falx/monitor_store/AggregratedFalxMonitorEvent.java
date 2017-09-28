package com.life360.falx.monitor_store;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Vikas on 9/19/17.
 */

public class AggregratedFalxMonitorEvent {

    public static final String DURATION = "duration";
    public static final String COUNT = "count";
    public static final String TIMESTAMP = "timestamp";
    private String name;
    private Map<String, Double> arguments = new HashMap<>();

    public AggregratedFalxMonitorEvent(String name, int count, Date timestamp) {
        this.name = name;
        this.arguments.put(AggregratedFalxMonitorEvent.COUNT, new Double(count));
        Calendar cal = Calendar.getInstance();
        cal.setTime(timestamp);
        this.arguments.put(AggregratedFalxMonitorEvent.TIMESTAMP, new Double(cal.getTimeInMillis()));
    }

    public String getName() {
        return name;
    }

    public Double getDuration() {
        if (this.arguments.containsKey(AggregratedFalxMonitorEvent.DURATION)) {
            return this.arguments.get(AggregratedFalxMonitorEvent.DURATION);
        }
        return 0.0;
    }

    public Double getCount() {
        if (this.arguments.containsKey(AggregratedFalxMonitorEvent.COUNT)) {
            return this.arguments.get(AggregratedFalxMonitorEvent.COUNT);
        }
        return 0.0;
    }

    public Double getTimestamp() {
        if (this.arguments.containsKey(AggregratedFalxMonitorEvent.TIMESTAMP)) {
            return this.arguments.get(AggregratedFalxMonitorEvent.TIMESTAMP);
        }
        return 0.0;
    }

    public Map<String,Double> getArguments(){
        return  arguments;
    }

    @Override
    public String toString() {
        return "AggregratedFalxMonitorEvent{" +
                "name='" + name + '\'' +
                ", arguments=" + arguments +
                '}';
    }
}
