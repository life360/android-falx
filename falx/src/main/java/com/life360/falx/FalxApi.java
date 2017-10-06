package com.life360.falx;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.util.Log;

import com.life360.falx.dagger.DaggerUtilComponent;
import com.life360.falx.dagger.DateTimeModule;
import com.life360.falx.dagger.LoggerModule;
import com.life360.falx.dagger.UtilComponent;
import com.life360.falx.model.NetworkActivity;
import com.life360.falx.monitor.AppState;
import com.life360.falx.monitor.AppStateListener;
import com.life360.falx.monitor.AppStateMonitor;
import com.life360.falx.monitor.Monitor;
import com.life360.falx.monitor.NetworkMonitor;
import com.life360.falx.monitor_store.AggregatedFalxMonitorEvent;
import com.life360.falx.monitor_store.FalxEventStore;
import com.life360.falx.monitor_store.FalxRealm;
import com.life360.falx.network.FalxInterceptor;
import com.life360.falx.util.Logger;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.functions.Cancellable;
import io.reactivex.subjects.PublishSubject;
import io.realm.Realm;

/**
 * Created by remon on 6/27/17.
 */
public class FalxApi {

    public static final int MONITOR_APP_STATE = 0x01;
    public static final int MONITOR_NETWORK = 0x02;
    // .. and so on

    public static FalxApi getInstance(Context context) {
        if (falxApi == null) {
            synchronized (FalxApi.class) {
                if (falxApi == null) {
                    falxApi = new FalxApi(context);
                }
            }
        }

        return falxApi;
    }

    /**
     * Add 1 or more Monitors using a integer to specify which monitors to add.
     * The monitor flags are specified by integer constants MONITOR_*
     *
     * @param monitorFlags
     */
    public void addMonitors(int monitorFlags) {
        if (!monitors.isEmpty()) {
            monitors.clear();
        }

        if ((monitorFlags & MONITOR_APP_STATE) == MONITOR_APP_STATE) {
            monitors.add(new AppStateMonitor(appStateObservable()));
        }

        if ((monitorFlags & MONITOR_NETWORK) == MONITOR_NETWORK) {
            monitors.add(new NetworkMonitor(getNetworkActivityObservable()));
        }
        // todo: and so on

        for (Monitor monitor : monitors) {
            eventStore.subscribeToEvents(monitor.getEventObservable());
        }
    }

    /**
     * Marks start of a session, call when a Activity comes to the foreground (Activity.onStart)
     *
     * @param activity
     * @return
     */
    public boolean startSession(Activity activity) {
        onAppStateForeground();
        return true;
    }

    /**
     * * Marks start of a session, call when a Activity is removed from the foreground (Activity.onStop)
     *
     * @param activity
     * @return
     */
    public boolean endSession(Activity activity) {
        onAppStateBackground();
        return true;
    }


    private static volatile FalxApi falxApi = null;

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    @Inject
    Logger logger;

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    List<AppStateListener> appStateListeners;

    private Context application;        // Application application

    private UtilComponent utilComponent;

    private ArrayList<Monitor> monitors = new ArrayList<>();

    private FalxEventStore eventStore;

    private FalxInterceptor falxInterceptor;
    private PublishSubject<NetworkActivity> networkActivitySubject = PublishSubject.create();


    private FalxApi(@NonNull Context context) {
        // Create our UtilComponent module, since it will be only used by FalxApi
        UtilComponent utilComponent = DaggerUtilComponent.builder()
                // list of modules that are part of this component need to be created here too
                .dateTimeModule(new DateTimeModule())
                .loggerModule(new LoggerModule())
                .build();

        Realm.init(context);
        init(context, utilComponent);
    }

    /**
     * Test code can pass in a TestUtilsComponent to this special constructor to inject
     * fake objects instead of what is provided by UtilComponent
     *
     * @param context
     * @param utilComponent
     */
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    FalxApi(@NonNull Context context, @NonNull UtilComponent utilComponent) {

        // Note: Call to Realm.init is omitted as we should not use Realm in unit tests.
        init(context, utilComponent);
    }

    private void init(Context context, UtilComponent utilComponent) {
        this.application = context.getApplicationContext();

        // Create our UtilComponent module, since it will be only used by FalxApi
        this.utilComponent = utilComponent;
        this.utilComponent.inject(this);

        appStateListeners = new ArrayList<>();

        eventStore = new FalxEventStore(new FalxRealm(), context);
    }

    public void addAppStateListener(AppStateListener listener) {
        appStateListeners.add(listener);
    }

    public void removeAppStateListener(AppStateListener listener) {
        appStateListeners.remove(listener);
    }

    public void removeAllAppStateListeners() {
        appStateListeners.clear();
    }

    @VisibleForTesting
    void onAppStateForeground() {
        for (AppStateListener listener : appStateListeners) {
            listener.onEnterForeground();
        }
    }

    @VisibleForTesting
    void onAppStateBackground() {
        for (AppStateListener listener : appStateListeners) {
            listener.onEnterBackground();
        }
    }

    /**
     * Get a Interceptor that can be added to OkHttpClient.
     *
     * @return a instance of FalxInterceptor that will allow the network monitor to log data about network activity
     */
    public FalxInterceptor getInterceptor() {
        if (falxInterceptor == null) {
            falxInterceptor = new FalxInterceptor(getNetworkActivityObserver());
        }

        return falxInterceptor;
    }


    @VisibleForTesting
    Observable<AppState> appStateObservable() {

        return Observable.create(new ObservableOnSubscribe<AppState>() {
            @Override
            public void subscribe(@io.reactivex.annotations.NonNull final ObservableEmitter<AppState> appStateEmitter) throws Exception {

                final AppStateListener appStateListener = new AppStateListener() {
                    @Override
                    public void onEnterBackground() {
                        appStateEmitter.onNext(AppState.BACKGROUND);
                    }

                    @Override
                    public void onEnterForeground() {
                        appStateEmitter.onNext(AppState.FOREGROUND);
                    }
                };

                addAppStateListener(appStateListener);

                appStateEmitter.setCancellable(new Cancellable() {
                    @Override
                    public void cancel() throws Exception {
                        removeAppStateListener(appStateListener);
                    }
                });
            }
        });
    }

    private Observable<NetworkActivity> getNetworkActivityObservable() {
        return networkActivitySubject;
    }

    private Observer<NetworkActivity> getNetworkActivityObserver() {
        return networkActivitySubject;
    }

    /**
     * get aggregated events for the provided event
     *
     * @param eventName name of event
     * @return list of aggregated Falx monitor events
     */
    public List<AggregatedFalxMonitorEvent> aggregateEvents(String eventName) {
        if (eventStore != null) {
            return eventStore.aggregateEvents(eventName);
        }
        return null;
    }

    /**
     * get aggregated events for the provided event, also partial day option is available
     *
     * @param eventName name of event
     * @param allowPartialDays if true partial day's data also included
     * @return list of aggregated Falx monitor events
     */
    public List<AggregatedFalxMonitorEvent> aggregatedEvents(String eventName, boolean allowPartialDays) {
        if (eventStore != null) {
            return eventStore.aggregatedEvents(eventName, allowPartialDays);
        }
        return null;
    }

    /**
     * get all aggregated events
     *
     * @param allowPartialDays if true partial day's data also included
     * @return list of aggregated Falx monitor events
     */
    public List<AggregatedFalxMonitorEvent> allAggregatedEvents(boolean allowPartialDays) {
        if (eventStore != null) {
            return eventStore.allAggregatedEvents(allowPartialDays);
        }
        return null;
    }

    // ** Test function
    public void testStoredData() {
//        realmInstance realm = realmInstance.getDefaultInstance();
//        RealmQuery<FalxData> query = realm.where(FalxData.class);
//
//        RealmResults<FalxData> result = query.findAll();
//
//        logger.d(TAG, "Stored data: ");
//        for (FalxData data : result) {
//            logger.d(TAG, data.toString());
//        }
    }
}
