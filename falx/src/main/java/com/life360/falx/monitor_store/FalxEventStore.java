package com.life360.falx.monitor_store;

import io.reactivex.Observable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import io.realm.Realm;
import io.realm.RealmResults;

import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Vikas on 9/19/17.
 */

public class FalxEventStore implements FalxEventStorable {

    private static final String FALX_EVENT_STORE_SYNC_DATE_KEY = "FalxEventStoreSyncDatekey";
    /**
     * syn value is in hrs
     */
    private static final int SYNC_INTERVAL = 24;

    private Realm realm;

    public FalxEventStore(RealmStore store) {
        this.realm = store.realmInstance();
    }


    public Date getSyncDate() {
        //TODO use sharedpreferences to read last sync date
        return null;
    }

    public void setSyncDate(Date syncDate) {
        //TODO use sharedpreferences to store last sync date
    }

    @Override
    public void save(FalxMonitorEvent event) {
        FalxEventEntity entity = new FalxEventEntity(event);

        //TODO handle FalxMonitor with dictonary of arguments

        realm.beginTransaction();
        realm.copyToRealm(entity);
        realm.commitTransaction();
    }

    @Override
    public void deleteOldEvents() {

    }

    @Override
    public List<AggregratedFalxEvent> aggregateEvents(String eventName) {
        return aggregatedEvents(eventName, true);
    }

    @Override
    public List<AggregratedFalxEvent> aggregatedEvents(String eventName, boolean allowPartialDays) {
        return null;
    }

    @Override
    public List<AggregratedFalxEvent> allAggregatedEvents(boolean allowPartialDays) {
        RealmResults<FalxEventEntity> allDistinctNameEvents = this.realm.where(FalxEventEntity.class).distinct("name");

        List<AggregratedFalxEvent> allEvents = new ArrayList<>();
        for (int i = 0; i < allDistinctNameEvents.size(); i++) {
            List<AggregratedFalxEvent> events = this.aggregatedEvents(allDistinctNameEvents.get(i).getName(), allowPartialDays);
            allEvents.addAll(events);

        }
        this.setSyncDate(new Date());
        this.deleteOldEvents();
        return allEvents;
    }

    @Override
    public URI eventToJSONFile() {
        return null;
    }

    @Override
    public void subscribeToEvents(Observable<FalxMonitorEvent> observable) {
        //TODO make sure to add it to disposable bag to dispose
        observable
                .observeOn(Schedulers.computation())
                .subscribe(new Consumer<FalxMonitorEvent>() {
                    @Override
                    public void accept(FalxMonitorEvent event) throws Exception {
                        FalxEventStore.this.save(event);
                    }
                });


    }
}
