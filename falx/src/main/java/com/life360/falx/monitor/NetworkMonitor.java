package com.life360.falx.monitor;

import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;

import com.life360.falx.dagger.DaggerUtilComponent;
import com.life360.falx.dagger.DateTimeModule;
import com.life360.falx.dagger.LoggerModule;
import com.life360.falx.dagger.UtilComponent;
import com.life360.falx.model.ExtraData;
import com.life360.falx.model.FalxData;
import com.life360.falx.model.NetworkActivity;
import com.life360.falx.util.Clock;
import com.life360.falx.util.Logger;

import java.util.ArrayList;

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

    private static final String NETWORK_FIELD_NAME = "falx-network";
    private static final String BYTES_RECEIVED = "bytesReceived";
    private static final String COUNT = "count";

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

    @VisibleForTesting
    protected NetworkMonitor(@android.support.annotation.NonNull UtilComponent utilComponent, @NonNull Observable<NetworkActivity> networkActivityObservable) {
        utilComponent.inject(this);

        networkActivityDisposable = networkActivityObservable
                .observeOn(Schedulers.single())
                .subscribe(new Consumer<NetworkActivity>() {

                    @Override
                    public void accept(NetworkActivity networkActivity) throws Exception {
                        logger.d(TAG, "accept: NetworkActivity: " + networkActivity.toString());

                        saveToDataStore(networkActivity);
                    }
                });
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    protected void saveToDataStore(NetworkActivity activity) {
        ArrayList<ExtraData> extras = new ArrayList<>();
        extras.add(new ExtraData(COUNT, Integer.toString(activity.getCount())));
        extras.add(new ExtraData(BYTES_RECEIVED, Integer.toString(activity.getBytesReceived())));

        FalxData falxData = new FalxData(NETWORK_FIELD_NAME, clock.currentTimeMillis(), extras);
        falxData.save();
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
