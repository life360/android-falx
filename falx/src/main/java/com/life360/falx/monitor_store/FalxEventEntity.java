package com.life360.falx.monitor_store;


import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import io.realm.RealmList;
import io.realm.RealmObject;

/**
 * Created by Vikas on 9/19/17.
 */

public class FalxEventEntity extends RealmObject {
    private String name;
    private Date timestamp;
    private RealmList<Arguments> arguments;
    private boolean processedByAgreegated = false;

    public FalxEventEntity() {
        this.timestamp = new Date();
        this.arguments = new RealmList<>();
    }

    public FalxEventEntity(FalxMonitorEvent event) {
        this.name = event.getName();
        this.timestamp = event.getTimestamp();
        this.arguments = convertMapToRealmList(event.getArguments());
    }

    private RealmList<Arguments> convertMapToRealmList(Map<String, Double> arguments) {
        RealmList<Arguments> realmList = new RealmList<>();

        for (Map.Entry<String, Double> entry : arguments.entrySet()) {
            realmList.add(new Arguments(entry.getKey(), entry.getValue()));
        }
        return realmList;
    }

    public Map<String, Double> getArguments() {
        HashMap<String, Double> argMap = new HashMap<>();

        for (Arguments arg : arguments) {
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

    public boolean isProcessedByAgreegated() {
        return processedByAgreegated;
    }

    public void setProcessedByAgreegated(boolean processedByAgreegated) {
        this.processedByAgreegated = processedByAgreegated;
    }

    @Override
    public String toString() {
        return "FalxEventEntity{" +
                "name='" + name + '\'' +
                ", timestamp=" + timestamp +
                ", arguments=" + arguments +
                ", processedByAgreegated=" + processedByAgreegated +
                '}';
    }
}
