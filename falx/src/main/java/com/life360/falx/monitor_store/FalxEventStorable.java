package com.life360.falx.monitor_store;



import java.net.URI;
import java.util.List;

import io.reactivex.Observable;

/**
 * Created by Vikas on 9/19/17.
 */

public interface FalxEventStorable {

    void save(FalxMonitorEvent event);

    void deleteOldEvents();

    List<AggregratedFalxMonitorEvent> aggregateEvents(String eventName);

    List<AggregratedFalxMonitorEvent> aggregatedEvents(String eventName, boolean allowPartialDays);

    List<AggregratedFalxMonitorEvent> allAggregatedEvents(boolean allowPartialDays);

    URI eventToJSONFile();

    void subscribeToEvents(Observable<FalxMonitorEvent> observable);

}
