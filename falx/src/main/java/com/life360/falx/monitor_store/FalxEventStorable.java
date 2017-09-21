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

    List<AggregratedFalxEvent> aggregateEvents(String eventName);

    List<AggregratedFalxEvent> aggregatedEvents(String eventName, boolean allowPartialDays);

    List<AggregratedFalxEvent> allAggregatedEvents(boolean allowPartialDays);

    URI eventToJSONFile();

    void subscribeToEvents(Observable<FalxMonitorEvent> observable);

}
