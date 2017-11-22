package com.life360.falx.monitor_store;


import com.life360.falx.model.FalxMonitorEvent;

import java.net.URI;
import java.util.List;

import io.reactivex.Observable;

/**
 * Created by Vikas on 9/19/17.
 */

public interface FalxEventStorable {

    void save(FalxMonitorEvent event);

    void deleteOldEvents();

    void deleteAllEvents();

    List<AggregatedFalxMonitorEvent> aggregateEvents(String eventName);

    List<AggregatedFalxMonitorEvent> aggregatedEvents(String eventName, boolean allowPartialDays);

    List<AggregatedFalxMonitorEvent> allAggregatedEvents(boolean allowPartialDays);

    URI eventToJSONFile(String fileName);

    void subscribeToEvents(Observable<FalxMonitorEvent> observable);

    void clearSubscriptions();
}
