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
 * Created by sudheer on 11/09/17.
 */

public class OnOffMonitor extends Monitor {

    private final String TAG = "OnOffMonitor";

    private Disposable stateDisposable;
    private UtilComponent utilComponent;

    private long startTime;

    @Inject
    Clock clock;

    @Inject
    Logger logger;

    private String metricName;
    private String label;

    /**
     * Test code can pass in a TestUtilsComponent to this special constructor to inject
     * fake objects instead of what is provided by UtilComponent
     *
     * @param utilComponent
     */
    public OnOffMonitor(@NonNull UtilComponent utilComponent, @NonNull Observable<Boolean> stateObservable, String metricName, String label) {
        init(utilComponent, stateObservable, metricName, label);
    }


    private void init(UtilComponent utilComponent, Observable<Boolean> stateObservable, String metricName, String aLabel) {

        // Create our UtilComponent module, since it will be only used by FalxApi
        this.utilComponent = utilComponent;
        utilComponent.inject(this);
        this.metricName = metricName;
        this.label = aLabel;

        stateDisposable = stateObservable
                .observeOn(Schedulers.single())                         // Use a single background thread to sequentially process the received data
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean state) throws Exception {
                        logger.d(TAG, label + " accept: state = " + state);
                        if (state) {
                            turnedOn();
                        } else {
                            turnedOff();
                        }
                    }
                });
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    void turnedOn() {
        // Is a session in progress?
        if (startTime == 0) {
            startTime = clock.currentTimeMillis();

            logger.d(TAG, label + " Session started... at: " + startTime);
        } else {
            logger.d(TAG, label + " Session already in progress, started at: " + startTime);
        }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    boolean turnedOff() {
        long endTime = clock.currentTimeMillis();
        if (endTime < startTime) {
            logger.e(TAG, label + " Likely error in timer schedule to mark end of a GPS session");

            endTime = startTime;
        }

        logger.d(Logger.TAG, label + " Session completed, duration (seconds): " + ((endTime - startTime) / 1000));

        OnOffSessionData sessionData = new OnOffSessionData(metricName, startTime, endTime);
        eventPublishSubject.onNext(new FalxMonitorEvent(sessionData.getName(), sessionData.getArgumentMap()));
        startTime = 0;

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
