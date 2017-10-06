package com.life360.falx.model;


import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import io.realm.RealmList;
import io.realm.RealmObject;

/**
 * Created by Vikas on 9/19/17.
 */

public class FalxEventEntity extends RealmObject {
    public static final String KEY_NAME = "name";
    private String name;

    public static final String KEY_TIMESTAMP = "timestamp";
    private Date timestamp;

    public static final String KEY_ARGUMENTS = "arguments";
    private RealmList<EventArgument> arguments;

    public static final String KEY_PROCESSED_FOR_AGGREGATION = "processedForAggregation";
    private boolean processedForAggregation = false;

    public FalxEventEntity() {
        this.timestamp = new Date();
        this.arguments = new RealmList<>();
    }

    public FalxEventEntity(FalxMonitorEvent event) {
        this.name = event.getName();
        this.timestamp = event.getTimestamp();
        this.arguments = convertMapToRealmList(event.getArguments());
    }

    private RealmList<EventArgument> convertMapToRealmList(Map<String, Double> arguments) {
        RealmList<EventArgument> realmList = new RealmList<>();

        for (Map.Entry<String, Double> entry : arguments.entrySet()) {
            realmList.add(new EventArgument(entry.getKey(), entry.getValue()));
        }
        return realmList;
    }

    public Map<String, Double> getArguments() {
        HashMap<String, Double> argMap = new HashMap<>();

        for (EventArgument arg : arguments) {
            argMap.put(arg.getKey(), arg.getValue());
        }

        return argMap;
    }

    public String getName() {
        return name;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public boolean isProcessedForAggregation() {
        return processedForAggregation;
    }

    public void setProcessedForAggregation(boolean processedForAggregation) {
        this.processedForAggregation = processedForAggregation;
    }

    @Override
    public String toString() {
        return "FalxEventEntity{" +
                "name='" + name + '\'' +
                ", timestamp=" + timestamp +
                ", arguments=" + arguments +
                ", processedForAggregation=" + processedForAggregation +
                '}';
    }
}
