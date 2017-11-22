package com.life360.falx.dagger;

import android.content.Context;

import com.life360.falx.model.FalxEventEntity;
import com.life360.falx.model.FalxMonitorEvent;
import com.life360.falx.monitor_store.AggregatedFalxMonitorEvent;
import com.life360.falx.monitor_store.FalxEventStorable;
import com.life360.falx.monitor_store.FalxEventStore;
import com.life360.falx.monitor_store.RealmStore;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import io.realm.RealmList;

/**
 * Created by Vikas on 9/19/17.
 */

public class FakeFalxEventStore implements FalxEventStorable {

    private static final String TAG = FakeFalxEventStore.class.getSimpleName();

    private CompositeDisposable compositeDisposable;

    public FakeFalxEventStore(RealmStore store, Context context) {
        compositeDisposable = new CompositeDisposable();
    }


    public Date getSyncDate() {
        return new Date();
    }

    public void setSyncDate(Date syncDate) {
    }

    @Override
    public void save(FalxMonitorEvent event) {
    }

    @Override
    public void deleteOldEvents() {
    }

    @Override
    public void deleteAllEvents() {

    }

    @Override
    public List<AggregatedFalxMonitorEvent> aggregateEvents(String eventName) {
        return aggregatedEvents(eventName, true);
    }

    /**
     * Get aggregated argument values for a list of events.
     *
     * @param eventName
     * @param allowPartialDays
     * @return a list of events, can be empty if no stored events found
     */
    @Override
    public List<AggregatedFalxMonitorEvent> aggregatedEvents(String eventName, boolean allowPartialDays) {

        List<AggregatedFalxMonitorEvent> aggregatedFalxEvents = new ArrayList<>();
        return aggregatedFalxEvents;
    }

    private AggregatedFalxMonitorEvent createAggregatedEvents(final RealmList<FalxEventEntity> events,
                                                              Map<String, Double> aggregatedArguments,
                                                              long timestamp) {
        if (events == null || events.size() == 0) {
            return null;
        }

        AggregatedFalxMonitorEvent aggregatedEvent = new AggregatedFalxMonitorEvent(
                events.first().getName(),
                events.size(),
                timestamp);

        // todo
        return aggregatedEvent;
    }

    @Override
    public List<AggregatedFalxMonitorEvent> allAggregatedEvents(boolean allowPartialDays) {
        List<AggregatedFalxMonitorEvent> allEvents = new ArrayList<>();
        //todo
        this.setSyncDate(new Date());
        this.deleteOldEvents();
        return allEvents;
    }

    @Override
    public URI eventToJSONFile(String fileName) {
        this.deleteOldEvents();

        // todo
        try {
            return new URI(fileName);
        } catch (URISyntaxException e) {
            return null;
        }
    }

    @Override
    public void subscribeToEvents(Observable<FalxMonitorEvent> observable) {
        compositeDisposable.add(observable
                .subscribeOn(Schedulers.io())
                .subscribe(new Consumer<FalxMonitorEvent>() {
                    @Override
                    public void accept(FalxMonitorEvent event) throws Exception {
                        FakeFalxEventStore.this.save(event);
                    }
                }));
    }

    @Override
    public void clearSubscriptions() {
        compositeDisposable.clear();
    }

    private boolean isSameDate(Calendar cal1, Calendar cal2) {
        return (cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH) &&
                cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH));

    }
}
