package com.life360.falx;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;

import com.life360.falx.dagger.AppModule;
import com.life360.falx.dagger.DaggerUtilComponent;
import com.life360.falx.dagger.DateTimeModule;
import com.life360.falx.dagger.FalxStoreModule;
import com.life360.falx.dagger.LoggerModule;
import com.life360.falx.dagger.UtilComponent;
import com.life360.falx.model.NetworkActivity;
import com.life360.falx.monitor.AppState;
import com.life360.falx.monitor.AppStateListener;
import com.life360.falx.monitor.AppStateMonitor;
import com.life360.falx.monitor.GpsMonitor;
import com.life360.falx.monitor.GpsState;
import com.life360.falx.monitor.GpsStateListener;
import com.life360.falx.monitor.Monitor;
import com.life360.falx.monitor.NetworkMonitor;
import com.life360.falx.monitor_store.AggregatedFalxMonitorEvent;
import com.life360.falx.monitor_store.FalxEventStorable;
import com.life360.falx.network.FalxInterceptor;
import com.life360.falx.util.Logger;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.functions.Cancellable;
import io.reactivex.subjects.PublishSubject;

/**
 * Created by remon on 6/27/17.
 */
public class FalxApi {

    public static final int MONITOR_APP_STATE = 0x01;
    public static final int MONITOR_NETWORK = 0x02;
    public static final int MONITOR_GPS = 0x04;
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
     * If a monitor was added before it will remain added, and only one insteance of a monitor
     * shall exist with the FalxApi singleton.
     *
     * @param monitorFlags
     */
    public void addMonitors(int monitorFlags) {

        if ((monitorFlags & MONITOR_APP_STATE) == MONITOR_APP_STATE) {
            if (!monitors.containsKey(MONITOR_APP_STATE)) {
                AppStateMonitor appStateMonitor = new AppStateMonitor(utilComponent, appStateObservable());
                monitors.put(MONITOR_APP_STATE, appStateMonitor);
                eventStorable.subscribeToEvents(appStateMonitor.getEventObservable());
            }
        }

        if ((monitorFlags & MONITOR_NETWORK) == MONITOR_NETWORK) {
            if (!monitors.containsKey(MONITOR_NETWORK)) {
                NetworkMonitor networkMonitor = new NetworkMonitor(utilComponent, getNetworkActivityObservable());
                monitors.put(MONITOR_NETWORK, networkMonitor);
                eventStorable.subscribeToEvents(networkMonitor.getEventObservable());
            }
        }

        if ((monitorFlags & MONITOR_GPS) == MONITOR_GPS) {
            if (!monitors.containsKey(MONITOR_GPS)) {
                GpsMonitor gpsMonitor = new GpsMonitor(utilComponent, getGpsStateObservable());
                monitors.put(MONITOR_GPS, gpsMonitor);
                eventStorable.subscribeToEvents(gpsMonitor.getEventObservable());
            }
        }
        // todo: and so on
    }

    public void removeAllMonitors() {
        if (!monitors.isEmpty()) {

            for (Monitor monitor : monitors.values()) {
                monitor.stop();
            }

            monitors.clear();
        }

        eventStorable.clearSubscriptions();
    }

    public boolean isMonitorActive(int monitorId) {
        return monitors.containsKey(monitorId);
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

    public void onGpsOn() {
        if (gpsStateListener != null) {
            gpsStateListener.onGpsOn();
        }
    }

    public void onGpsOff() {
        if (gpsStateListener != null) {
            gpsStateListener.onGpsOff();
        }

    }

    private static volatile FalxApi falxApi = null;


    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    @Inject
    Logger logger;

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    List<AppStateListener> appStateListeners;

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    @Inject
    FalxEventStorable eventStorable;

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    GpsStateListener gpsStateListener;

    @Inject
    Context application;        // Application application

    private UtilComponent utilComponent;

    private boolean loggingEnabled;


    // Maps MonitorId -> Monitor
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    protected HashMap<Integer, Monitor> monitors = new HashMap<>();

    private FalxInterceptor falxInterceptor;
    private PublishSubject<NetworkActivity> networkActivitySubject = PublishSubject.create();


    private FalxApi(@NonNull Context context) {
        // Create our UtilComponent module, since it will be only used by FalxApi
        UtilComponent utilComponent = DaggerUtilComponent.builder()
                // list of modules that are part of this component need to be created here too
                .appModule(new AppModule(context.getApplicationContext()))
                .dateTimeModule(new DateTimeModule())
                .loggerModule(new LoggerModule())
                .falxStoreModule(new FalxStoreModule())
                .build();


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

    public void enableLogging(boolean enable) {
        logger.setEnabled(enable);
        if (falxInterceptor != null) {
            falxInterceptor.enableLogging(enable);
        }
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
            falxInterceptor = new FalxInterceptor(application, getNetworkActivityObserver());
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

    @VisibleForTesting
    Observable<GpsState> getGpsStateObservable() {
        return Observable.create(new ObservableOnSubscribe<GpsState>() {

            @Override
            public void subscribe(@io.reactivex.annotations.NonNull final ObservableEmitter<GpsState> gpsStateEmitter) throws Exception {
                gpsStateListener = new GpsStateListener() {

                    @Override
                    public void onGpsOn() {
                        gpsStateEmitter.onNext(GpsState.ON);
                    }

                    @Override
                    public void onGpsOff() {
                        gpsStateEmitter.onNext(GpsState.OFF);
                    }
                };
                gpsStateEmitter.setCancellable(new Cancellable() {
                    @Override
                    public void cancel() throws Exception {
                        gpsStateListener = null;
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
        if (eventStorable != null) {
            return eventStorable.aggregateEvents(eventName);
        }
        return null;
    }

    /**
     * get aggregated events for the provided event, also partial day option is available
     *
     * @param eventName        name of event
     * @param allowPartialDays if true partial day's data also included
     * @return list of aggregated Falx monitor events
     */
    public List<AggregatedFalxMonitorEvent> aggregatedEvents(String eventName, boolean allowPartialDays) {
        if (eventStorable != null) {
            return eventStorable.aggregatedEvents(eventName, allowPartialDays);
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
        if (eventStorable != null) {
            return eventStorable.allAggregatedEvents(allowPartialDays);
        }
        return null;
    }

    /**
     * get JSON file URI which contains all Falx events
     *
     * @return URI of file
     */
    public URI eventToJSON() {
        if (eventStorable != null) {
            return eventStorable.eventToJSONFile();
        }
        return null;
    }
}
