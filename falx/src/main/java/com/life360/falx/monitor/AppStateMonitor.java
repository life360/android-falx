package com.life360.falx.monitor;

import android.support.annotation.VisibleForTesting;
import android.text.format.DateUtils;

import com.life360.falx.dagger.DaggerUtilComponent;
import com.life360.falx.dagger.DateTimeModule;
import com.life360.falx.dagger.LoggerModule;
import com.life360.falx.dagger.UtilComponent;
import com.life360.falx.model.SessionData;
import com.life360.falx.monitor_store.FalxMonitorEvent;
import com.life360.falx.util.Clock;
import com.life360.falx.util.Logger;

import java.util.Timer;
import java.util.TimerTask;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by remon on 7/19/17.
 */

public class AppStateMonitor extends Monitor {

    private final String TAG = "AppStateMonitor";

    @VisibleForTesting
    static final long TIME_BETWEEN_ACTIVITY_TRANSITION = 5 * DateUtils.SECOND_IN_MILLIS;

    private Disposable appStateDisposable;
    private UtilComponent utilComponent;
    private long startTime;
    private long endTime;

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    Timer sessionEndTimer;

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    SessionData lastSessionData;

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    @Inject
    Clock clock;

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    @Inject
    Logger logger;

    /**
     * Subscribes to a Observable to receive AppState data changes.
     * @param appStateObservable
     */
    public AppStateMonitor(@NonNull Observable<AppState> appStateObservable) {
        // Create our UtilComponent module, since it will be only used by FalxApi
        UtilComponent utilComponent = DaggerUtilComponent.builder()
                // list of modules that are part of this component need to be created here too
                .dateTimeModule(new DateTimeModule())
                .loggerModule(new LoggerModule())
                .build();

        init(utilComponent, appStateObservable);
    }

    /**
     * Test code can pass in a TestUtilsComponent to this special constructor to inject
     * fake objects instead of what is provided by UtilComponent
     * @param utilComponent
     */
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    AppStateMonitor(@NonNull UtilComponent utilComponent, @NonNull Observable<AppState> appStateObservable) {
        init(utilComponent, appStateObservable);
    }


    private void init(UtilComponent utilComponent, Observable<AppState> appStateObservable) {
        // Create our UtilComponent module, since it will be only used by FalxApi
        this.utilComponent = utilComponent;
        this.utilComponent.inject(this);

        appStateDisposable = appStateObservable
                .observeOn(Schedulers.single())                         // Use a single background thread to sequentially process the received data
                .subscribe(new Consumer<AppState>() {
                    @Override
                    public void accept(AppState appState) throws Exception {
                        logger.d(TAG, "accept: appState = " + appState);

                        switch (appState) {
                            case BACKGROUND:
                                onBackground();
                                break;

                            case FOREGROUND:
                                onForeground();
                                break;
                        }
                    }
                });
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    boolean onForeground() {
        // Is a session in progress?
        if (startTime == 0) {
            startTime = clock.currentTimeMillis();

            logger.d(TAG, "Session started... at: " + startTime);
        } else {
            logger.d(TAG, "Session already in progress, started at: " + startTime);

            // reset session end timer if there is one
            if (sessionEndTimer != null) {
                sessionEndTimer.cancel();
                sessionEndTimer = null;
            }
        }

        return true;
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    boolean onBackground() {
        endTime = clock.currentTimeMillis();

        // Start a timer to end the session in TIME_BETWEEN_ACTIVITY_TRANSITION milliseconds
        if (sessionEndTimer != null) {
            sessionEndTimer.cancel();
            sessionEndTimer = null;
        }

        sessionEndTimer = new Timer();

        TimerTask task = new TimerTask() {

            @Override
            public void run() {
                onSessionEnded();
            }
        };

        sessionEndTimer.schedule(task, TIME_BETWEEN_ACTIVITY_TRANSITION);

        return true;
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    void onSessionEnded() {
        if (endTime < startTime) {
            logger.e(TAG, "Likely error in timer schedule to mark end of a session");

            endTime = startTime;
        }

        logger.d(TAG, "Session completed, duration (seconds): " + ((endTime - startTime) / 1000));

        lastSessionData = new SessionData(AppState.FOREGROUND, startTime, endTime);

        // Save to local data store
        saveToDataStore();

        startTime = 0;
        endTime = 0;

        if (sessionEndTimer != null) {
            sessionEndTimer.cancel();
            sessionEndTimer = null;
        }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    protected void saveToDataStore() {
//        FalxData falxData = new FalxData(lastSessionData.getName(), endTime, lastSessionData.getArgumentMap());
//        falxData.save();

        eventPublishSubject.onNext(new FalxMonitorEvent(lastSessionData.getName(), lastSessionData.getArgumentMap()));
    }

    @Override
    public void stop() {
        super.stop();

        if (!appStateDisposable.isDisposed()) {
            appStateDisposable.dispose();
            logger.d(TAG, "stop: disposed appStateDisposable");
        }
    }
}
