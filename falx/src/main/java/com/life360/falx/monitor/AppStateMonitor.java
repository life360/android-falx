package com.life360.falx.monitor;

import android.util.Log;

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

    private Disposable appStateDisposable;

    /**
     * Subscribes to a Observable to receive AppState data changes.
     * @param appStateObservable
     */
    public AppStateMonitor(@NonNull Observable<AppState> appStateObservable) {
        appStateDisposable = appStateObservable
                .observeOn(Schedulers.single())                         // Use a single background thread to sequentially process the received data
                .subscribe(new Consumer<AppState>() {
                    @Override
                    public void accept(AppState appState) throws Exception {
                        Log.d(TAG, "accept: appState = " + appState);
                    }
                });
    }

    @Override
    public void stop() {
        super.stop();

        if (!appStateDisposable.isDisposed()) {
            appStateDisposable.dispose();
            Log.d(TAG, "stop: disposed appStateDisposable");
        }
    }
}
