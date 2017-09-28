package com.life360.falx.monitor;

import com.life360.falx.monitor_store.FalxMonitorEvent;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;

/**
 * Created by remon on 7/19/17.
 */

public class Monitor {

    /**
     * Stops the Monitor from observing and collecting data.
     */
    public void stop() {}

    /**
     * Get a observable where the monitor can publish events.
     */
    public Observable<FalxMonitorEvent> getEventObservable() {
        return eventPublishSubject;
    }

    protected PublishSubject<FalxMonitorEvent> eventPublishSubject = PublishSubject.create();
}
