package com.life360.falx.monitor_store;

import java.util.Date;
import java.util.Map;


/**
 * Created by Vikas on 9/19/17.
 */

public class FalxMonitorEvent {

    private String name;
    private Date timestamp;
    private Map<String, Double> arguments;

    public FalxMonitorEvent(String name, Map<String, Double> arguments) {
        this.name = name;
        this.arguments = arguments;
        this.timestamp = new Date();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public Map<String, Double> getArguments() {
        return arguments;
    }

    public void setArguments(Map<String, Double> arguments) {
        this.arguments = arguments;
    }
}
