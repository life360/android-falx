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
import android.text.format.DateUtils;

import com.life360.falx.dagger.DaggerUtilComponent;
import com.life360.falx.dagger.DateTimeModule;
import com.life360.falx.dagger.LoggerModule;
import com.life360.falx.dagger.UtilComponent;
import com.life360.falx.model.FalxMonitorEvent;
import com.life360.falx.model.NetworkActivity;
import com.life360.falx.util.Clock;
import com.life360.falx.util.Logger;

import java.util.HashMap;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by remon on 7/19/17.
 */

public class NetworkMonitor extends Monitor {

    private static final String TAG = "NetworkMonitor";

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    protected Disposable networkActivityDisposable;

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    @Inject
    Clock clock;

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    @Inject
    Logger logger;

    public static UtilComponent createDaggerComponent() {
        return DaggerUtilComponent.builder()
                // list of modules that are part of this component need to be created here too
                .dateTimeModule(new DateTimeModule())
                .loggerModule(new LoggerModule())
                .build();
    }

    public NetworkMonitor(@NonNull Observable<NetworkActivity> networkActivityObservable) {
        this(createDaggerComponent(), networkActivityObservable);

    }

    public NetworkMonitor(@NonNull UtilComponent utilComponent, @NonNull Observable<NetworkActivity> networkActivityObservable) {
        utilComponent.inject(this);

        networkActivityDisposable = networkActivityObservable
                .observeOn(Schedulers.single())
                .subscribe(new Consumer<NetworkActivity>() {

                    @Override
                    public void accept(NetworkActivity networkActivity) throws Exception {
                        logger.d(Logger.TAG, "NetworkActivity: " + networkActivity.toString());

                        saveToDataStore(networkActivity);
                    }
                });
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    protected void saveToDataStore(NetworkActivity activity) {
        HashMap<String, Double> args = new HashMap<>();
        args.put(FalxConstants.PROP_COUNT, new Double(activity.getCount()));
        args.put(FalxConstants.PROP_BYTES_RECEIVED, new Double(activity.getBytesReceived()));
        args.put(FalxConstants.PROP_DURATION, new Double(activity.getResponseDuration() / DateUtils.SECOND_IN_MILLIS));

        eventPublishSubject.onNext(new FalxMonitorEvent(FalxConstants.EVENT_NETWORK, args));
    }

    @Override
    public void stop() {
        super.stop();

        if (!networkActivityDisposable.isDisposed()) {
            networkActivityDisposable.dispose();
            logger.d(TAG, "stop: disposed networkActivityDisposable");
        }
    }

}
