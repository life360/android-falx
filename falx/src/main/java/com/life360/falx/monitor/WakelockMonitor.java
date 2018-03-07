// Copyright 2018 Life360, Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.life360.falx.monitor;

import android.support.annotation.VisibleForTesting;

import com.life360.falx.dagger.UtilComponent;
import com.life360.falx.model.FalxMonitorEvent;
import com.life360.falx.model.OnOffSessionData;
import com.life360.falx.util.Clock;
import com.life360.falx.util.Logger;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by sudheer on 11/22/17.
 * Monitor for wake locks. Supports following cases
 * - wakelock acquired with a max duration
 *   optionally released (earlier than max duration or later)
 *   For this case, when acquired, we log an event with the max duration into the store. If it was never released, we assume it was held for full max duration
 *   If the release was called, then we will use the difference between the release time and acquire time and ignore the max duration.
 * - wakelock acquired and released.
 *   In this case, the duration is calculated as the time between acquired and released timestamps.
 */

public class WakelockMonitor extends Monitor {
    private final String TAG = "WakelockMonitor";

    private Disposable stateDisposable;
    private UtilComponent utilComponent;

    private long startTime;

    @Inject
    Clock clock;

    @Inject
    Logger logger;

    private String metricName;
    private String label;

    private long maxDuration;
    OnOffSessionData sessionData;
    FalxMonitorEvent monitorEvent;

    /**
     * Test code can pass in a TestUtilsComponent to this special constructor to inject
     * fake objects instead of what is provided by UtilComponent
     *
     * @param utilComponent
     */
    public WakelockMonitor(@NonNull UtilComponent utilComponent, @NonNull Observable<WakelockState> stateObservable, String metricName, String label) {
        init(utilComponent, stateObservable, metricName, label);
    }


    private void init(UtilComponent utilComponent, Observable<WakelockState> stateObservable, String metricName, String aLabel) {

        // Create our UtilComponent module, since it will be only used by FalxApi
        this.utilComponent = utilComponent;
        utilComponent.inject(this);
        this.metricName = metricName;
        this.label = aLabel;

        stateDisposable = stateObservable
                .observeOn(Schedulers.single())                         // Use a single background thread to sequentially process the received data
                .subscribe(new Consumer<WakelockState>() {
                    @Override
                    public void accept(WakelockState state) throws Exception {
                        logger.d(TAG, label + " accept: locked = " + state.locked);
                        if (state.locked) {
                            if (state.maxDuration > 0) {
                                acquired(state.maxDuration);
                            } else {
                                acquired();
                            }
                        } else {
                            released();
                        }
                    }
                });
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    void acquired() {
        // Is a session in progress?
        if (startTime == 0) {
            startTime = clock.currentTimeMillis();

            logger.d(TAG, label + " acquired... at: " + startTime);
        } else {
            logger.d(TAG, label + " already acquired, at: " + startTime);
        }
    }


    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    void acquired(long aMaxDuration) {
        acquired();
        maxDuration = aMaxDuration;
        sessionData = new OnOffSessionData(metricName, startTime, startTime + maxDuration);
        monitorEvent = new FalxMonitorEvent(sessionData.getName(), sessionData.getArgumentMap());
        eventPublishSubject.onNext(monitorEvent);
    }


    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    boolean released() {
        long endTime = clock.currentTimeMillis();
        if (endTime < startTime) {
            logger.e(TAG, label + " Likely error in timer schedule to mark end of a wakelock session");

            endTime = startTime;
        }

        logger.d(Logger.TAG, label + " wakelock released, duration (seconds): " + ((endTime - startTime) / 1000));

        if (monitorEvent != null) {
            sessionData.endTime = endTime;
            //replace the session data
            monitorEvent.updateArgs(sessionData.getArgumentMap());
            eventPublishSubject.onNext(monitorEvent);
        } else {
            OnOffSessionData sessionData1 = new OnOffSessionData(metricName, startTime, endTime);
            eventPublishSubject.onNext(new FalxMonitorEvent(sessionData1.getName(), sessionData1.getArgumentMap()));
        }
        startTime = 0;
        sessionData = null;
        maxDuration = 0;
        monitorEvent = null;

        return true;
    }

    @Override
    public void stop() {
        super.stop();

        if (!stateDisposable.isDisposed()) {
            stateDisposable.dispose();
            logger.d(TAG, label + " stop: disposed stateDisposable");
        }
    }

}
