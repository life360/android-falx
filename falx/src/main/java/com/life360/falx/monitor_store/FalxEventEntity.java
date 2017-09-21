package com.life360.falx.monitor_store;



import java.util.Date;
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

    }

    public FalxEventEntity(String name) {
        this.name = name;
        this.timestamp = new Date();
    }

    //TODO init this object when FalxMonitorEvent is passed as an argument
    public FalxEventEntity(FalxMonitorEvent event) {
        this.name = event.getName();
        this.timestamp = event.getTimestamp();
        this.arguments = getRealmListFromMap(event.getArguments());
    }

    private RealmList<Arguments> getRealmListFromMap(Map<String, Double> arguments){
        RealmList<Arguments> realmList = new RealmList<>();
        for( Map.Entry<String, Double> entry : arguments.entrySet()){
            realmList.add(new Arguments(entry.getKey(),entry.getValue()));
        }
        return realmList;
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

    public RealmList<Arguments> getArguments() {
        return arguments;
    }

    public void setArguments(RealmList<Arguments> arguments) {
        this.arguments = arguments;
    }

    public boolean isProcessedByAgreegated() {
        return processedByAgreegated;
    }

    public void setProcessedByAgreegated(boolean processedByAgreegated) {
        this.processedByAgreegated = processedByAgreegated;
    }
}
