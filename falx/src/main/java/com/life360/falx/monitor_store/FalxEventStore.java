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

package com.life360.falx.monitor_store;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.util.Log;

import com.life360.falx.model.EventArgument;
import com.life360.falx.model.FalxEventEntity;
import com.life360.falx.model.FalxMonitorEvent;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.realm.OrderedRealmCollectionSnapshot;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;
import io.realm.Sort;

/**
 * Created by Vikas on 9/19/17.
 */

public class FalxEventStore implements FalxEventStorable {

    private static final String TAG = FalxEventStore.class.getSimpleName();

    private static final String FALX_EVENT_STORE_SYNC_DATE_KEY = "FalxEventStoreSyncDatekey";
    private static final String LOOPER_THREAD_NAME = "FalxEventStoreThread";
    //TODO move to network monitor
    public static final String FALX_URL_PREFIX = "URL=";

    protected Context context;
    private RealmStore realmStore;
    private CompositeDisposable compositeDisposable;
    private final HandlerThread handlerThread;
    private final Scheduler looperScheduler;

    public FalxEventStore(RealmStore store, Context context) {
        this.realmStore = store;
        this.context = context;
        compositeDisposable = new CompositeDisposable();

        handlerThread = new HandlerThread(LOOPER_THREAD_NAME);
        handlerThread.start();
        synchronized (handlerThread) {
            looperScheduler = AndroidSchedulers.from(handlerThread.getLooper());
        }
        Realm.init(context);
    }

    public Date getSyncDate() {
        SharedPreferences preferences = context.getSharedPreferences(FALX_EVENT_STORE_SYNC_DATE_KEY, Context.MODE_PRIVATE);
        return new Date(preferences.getLong(FALX_EVENT_STORE_SYNC_DATE_KEY, 0));
    }

    public void setSyncDate(Date syncDate) {
        SharedPreferences preferences = context.getSharedPreferences(FALX_EVENT_STORE_SYNC_DATE_KEY, Context.MODE_PRIVATE);
        preferences.edit().putLong(FALX_EVENT_STORE_SYNC_DATE_KEY, syncDate.getTime()).apply();
    }

    @Override
    public void save(final FalxMonitorEvent event) {
        if (Thread.currentThread() == handlerThread) {
            saveEvent(event);
        } else {
            Flowable.just(1)
                    .subscribeOn(looperScheduler)
                    .observeOn(looperScheduler)
                    .doOnNext(new Consumer<Integer>() {
                        @Override
                        public void accept(Integer integer) throws Exception {
                            saveEvent(event);
                        }
                    })
                    .subscribe();
        }
    }

    private void saveEvent(final FalxMonitorEvent event) {
        Realm realm = null;
        try {
            realm = realmStore.realmInstance();
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(@NonNull Realm realm) {
                    if (event.isUpdate()) {
                        /*
                        * If this event was an update to previous event, find it and delete it from DB.
                        * Used to update wakelock actual duration when initially acquired for max duration but was manually released.
                        */
                        FalxEventEntity oldEntity = realm.where(FalxEventEntity.class)
                                .equalTo(FalxEventEntity.KEY_NAME, event.getName())
                                .equalTo(FalxEventEntity.KEY_TIMESTAMP, event.getTimestamp())
                                .findAll().sort(FalxEventEntity.KEY_TIMESTAMP).first(null);
                        if (oldEntity != null) {
                            for (EventArgument argument : oldEntity.getArguments()) {
                                if (event.getArguments().containsKey(argument.getKey())) {
                                    argument.setValue(event.getArguments().get(argument.getKey()));
                                }
                            }
                        }
                    } else {
                        FalxEventEntity entity = new FalxEventEntity(event);
                        realm.insert(entity);
                    }
                }
            });
        } finally {
            if (realm != null) {
                realm.close();
            }
        }
    }

    @Override
    public void deleteOldEvents() {
        if (Thread.currentThread() == handlerThread) {
            deleteOldEventsFromDataStore();
        } else {
            Flowable.just(1)
                    .subscribeOn(looperScheduler)
                    .observeOn(looperScheduler)
                    .doOnNext(new Consumer<Integer>() {
                        @Override
                        public void accept(Integer integer) throws Exception {
                            deleteOldEventsFromDataStore();
                        }
                    })
                    .blockingFirst();
        }
    }

    private void deleteOldEventsFromDataStore() {
        Calendar sixDaysAgoCal = Calendar.getInstance();
        sixDaysAgoCal.add(Calendar.DATE, -6);
        sixDaysAgoCal.set(Calendar.HOUR_OF_DAY, 0);
        sixDaysAgoCal.set(Calendar.MINUTE, 0);
        sixDaysAgoCal.set(Calendar.SECOND, 0);
        Realm realm = null;

        try {
            realm = realmStore.realmInstance();
            final Date beginningOfSixDaysAgo = sixDaysAgoCal.getTime();

            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(@NonNull Realm realm) {
                    RealmResults<FalxEventEntity> olderEvents = realm
                            .where(FalxEventEntity.class)
                            .lessThan(FalxEventEntity.KEY_TIMESTAMP, beginningOfSixDaysAgo)
                            .findAll();

                    // Use a snapshot of the arguments to remove all arguments from inside each element
                    for (FalxEventEntity event : olderEvents) {
                        RealmList<EventArgument> arguments = event.getArguments();
                        OrderedRealmCollectionSnapshot<EventArgument> argsSnapshot = arguments.createSnapshot();
                        argsSnapshot.deleteAllFromRealm();
                    }

                    olderEvents.deleteAllFromRealm();
                }
            });
        } finally {
            if (realm != null) {
                realm.close();
            }
        }
    }

    @Override
    public void deleteAllEvents() {
        if (Thread.currentThread() == handlerThread) {
            deleteAllEventsFromDataStore();
        } else {
            Flowable.just(1)
                    .subscribeOn(looperScheduler)
                    .observeOn(looperScheduler)
                    .doOnNext(new Consumer<Integer>() {
                        @Override
                        public void accept(Integer integer) throws Exception {
                            deleteAllEventsFromDataStore();
                        }
                    })
                    .blockingFirst();
        }
    }

    private void deleteAllEventsFromDataStore() {
        Realm realm = null;
        try {
            realm = realmStore.realmInstance();
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(@NonNull Realm realm) {
                    realm.deleteAll();
                }
            });
        } finally {
            if (realm != null) {
                realm.close();
            }
        }
    }

    @Override
    public List<AggregatedFalxMonitorEvent> aggregateEvents(String eventName) {
        return aggregatedEvents(eventName, true);
    }

    /**
     * Get aggregated argument values for a list of events.
     *
     * @param eventName
     * @param allowPartialDays
     * @return a list of events, can be empty if no stored events found
     */
    @Override
    public List<AggregatedFalxMonitorEvent> aggregatedEvents(final String eventName, final boolean allowPartialDays) {
        if (Thread.currentThread() == handlerThread) {
            return aggregatedEventsFromDataStore(eventName, allowPartialDays);
        } else {
            return Flowable.just(1)
                    .subscribeOn(looperScheduler)
                    .observeOn(looperScheduler)
                    .map(new Function<Integer, List<AggregatedFalxMonitorEvent>>() {
                        @Override
                        public List<AggregatedFalxMonitorEvent> apply(Integer integer) throws Exception {
                            return aggregatedEventsFromDataStore(eventName, allowPartialDays);
                        }
                    })
                    .blockingFirst();
        }
    }

    private List<AggregatedFalxMonitorEvent> aggregatedEventsFromDataStore(String eventName, final boolean allowPartialDays) {
        Realm realm = null;
        try {
            final List<AggregatedFalxMonitorEvent> aggregatedFalxEvents = new ArrayList<>();
            realm = realmStore.realmInstance();
            final RealmResults<FalxEventEntity> events = realm.where(FalxEventEntity.class)
                    .equalTo(FalxEventEntity.KEY_NAME, eventName)
                    .equalTo(FalxEventEntity.KEY_PROCESSED_FOR_AGGREGATION, false)
                    .findAllSorted(FalxEventEntity.KEY_TIMESTAMP, Sort.ASCENDING);
            int count = events.size();
            if (count <= 0) {
                return aggregatedFalxEvents;
            }
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    RealmList<FalxEventEntity> processedEvents = new RealmList<>();
                    Date beginWindowDate = events.first().getTimestamp();
                    Map<String, Double> aggregatedArguments = new HashMap<>();

                    for (FalxEventEntity event : events) {
                        Date currentEventDate = event.getTimestamp();
                        Calendar calBegin = Calendar.getInstance();
                        calBegin.setTime(beginWindowDate);
                        Calendar calCurrent = Calendar.getInstance();
                        calCurrent.setTime(currentEventDate);
                        if (isSameDate(calBegin, calCurrent)) {
                            aggregateCurrentEvent(
                                    event,
                                    processedEvents,
                                    aggregatedArguments);
                        } else {
                            appendEvent(
                                    beginWindowDate,
                                    processedEvents,
                                    aggregatedArguments,
                                    aggregatedFalxEvents);
                            //for next event
                            processedEvents.clear();
                            aggregatedArguments.clear();
                            beginWindowDate = currentEventDate;
                            aggregateCurrentEvent(
                                    event,
                                    processedEvents,
                                    aggregatedArguments);
                        }
                    }
                    if (allowPartialDays) {
                        appendEvent(
                                beginWindowDate,
                                processedEvents,
                                aggregatedArguments,
                                aggregatedFalxEvents);
                    }
                }
            });
            return aggregatedFalxEvents;
        } finally {
            if (realm != null) {
                realm.close();
            }
        }
    }

    /*
     * Must be called inside a transaction
     */
    private void aggregateCurrentEvent(FalxEventEntity currentEntity,
                                       List<FalxEventEntity> processedEvents,
                                       Map<String, Double> aggregatedArguments) {
        processedEvents.add(currentEntity);
        Map<String, Double> currentArguments = currentEntity.getArgumentsMap();
        for (String key : currentArguments.keySet()) {
            if (!key.contains(FALX_URL_PREFIX)) {
                if (aggregatedArguments.containsKey(key)) {
                    aggregatedArguments.put(key, aggregatedArguments.get(key) + currentArguments.get(key));
                } else {
                    aggregatedArguments.put(key, currentArguments.get(key));
                }
            }
        }
    }

    private AggregatedFalxMonitorEvent createAggregatedEvents(final RealmList<FalxEventEntity> events,
                                                              Map<String, Double> aggregatedArguments,
                                                              long timestamp) {
        if (events == null || events.size() == 0) {
            return null;
        }
        AggregatedFalxMonitorEvent aggregatedEvent = new AggregatedFalxMonitorEvent(
                events.first().getName(),
                events.size(),
                timestamp);
        for (Map.Entry<String, Double> entry : aggregatedArguments.entrySet()) {
            aggregatedEvent.putArgument(entry.getKey(), entry.getValue());
        }
        for (FalxEventEntity event : events) {
            event.setProcessedForAggregation(true);
        }
        return aggregatedEvent;
    }

    private void appendEvent(Date beginWindowDate,
                             final RealmList<FalxEventEntity> processedEvents,
                             Map<String, Double> aggregatedArguments,
                             List<AggregatedFalxMonitorEvent> aggregatedFalxEvents) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(beginWindowDate);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        Date startOfDay = cal.getTime();
        AggregatedFalxMonitorEvent aggregatedFalxMonitorEvent =
                createAggregatedEvents(processedEvents, aggregatedArguments, startOfDay.getTime());
        if (aggregatedFalxMonitorEvent != null) {
            aggregatedFalxEvents.add(aggregatedFalxMonitorEvent);
        }
    }

    @Override
    public List<AggregatedFalxMonitorEvent> allAggregatedEvents(final boolean allowPartialDays) {
        if (Thread.currentThread() == handlerThread) {
            return allAggregatedEventsFromDatastore(allowPartialDays);
        } else {
            return Flowable.just(1)
                    .subscribeOn(looperScheduler)
                    .observeOn(looperScheduler)
                    .map(new Function<Integer, List<AggregatedFalxMonitorEvent>>() {
                        @Override
                        public List<AggregatedFalxMonitorEvent> apply(Integer integer) throws Exception {
                            return allAggregatedEventsFromDatastore(allowPartialDays);
                        }
                    })
                    .blockingFirst();
        }
    }

    private List<AggregatedFalxMonitorEvent> allAggregatedEventsFromDatastore(boolean allowPartialDays) {
        Realm realm = null;
        try {
            realm = realmStore.realmInstance();
            RealmResults<FalxEventEntity> allDistinctNameEvents = realm.where(FalxEventEntity.class).distinct(FalxEventEntity.KEY_NAME);
            List<AggregatedFalxMonitorEvent> allEvents = new ArrayList<>();
            for (int i = 0; i < allDistinctNameEvents.size(); i++) {
                List<AggregatedFalxMonitorEvent> events = aggregatedEvents(
                        allDistinctNameEvents.get(i).getName(),
                        allowPartialDays);
                allEvents.addAll(events);
            }
            setSyncDate(new Date());
            deleteOldEvents();
            return allEvents;
        } finally {
            if (realm != null) {
                realm.close();
            }
        }
    }

    @Override
    public URI writeEventsToJSONFile(@NonNull final String fileName) {
        //using dummy URI so we are not passing null in reactive stream
        final URI dummyUri = URI.create("file://dummy");
        URI uri;
        if (Thread.currentThread() == handlerThread) {
            uri = getURIForJSONFile(fileName, dummyUri);
        } else {
            uri = Flowable.just(1)
                    .subscribeOn(looperScheduler)
                    .observeOn(looperScheduler)
                    .map(new Function<Integer, URI>() {
                        @Override
                        public URI apply(Integer integer) throws Exception {
                            return getURIForJSONFile(fileName, dummyUri);
                        }
                    })
                    .blockingFirst();
        }

        return uri == dummyUri ? null : uri;
    }

    private URI getURIForJSONFile(String fileName, URI dummyUri) {
        Realm realm = null;
        try {
            deleteOldEvents();
            if (fileName.length() == 0) {
                return dummyUri;
            }
            File jsonFile = new File(context.getCacheDir(), fileName);
            realm = realmStore.realmInstance();
            RealmResults<FalxEventEntity> allRealmEvents = realm.where(FalxEventEntity.class)
                    .findAllSorted(FalxEventEntity.KEY_TIMESTAMP, Sort.ASCENDING);
            if (allRealmEvents.size() <= 0) {
                return dummyUri;
            }
            JSONArray jsonEvents = new JSONArray();
            for (FalxEventEntity falxEventEntity : allRealmEvents) {
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put(FalxEventEntity.KEY_NAME, falxEventEntity.getName());
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(falxEventEntity.getTimestamp());
                    jsonObject.put(FalxEventEntity.KEY_TIMESTAMP, Long.toString(cal.getTimeInMillis()));
                    for (Map.Entry<String, Double> entry : falxEventEntity.getArgumentsMap().entrySet()) {
                        jsonObject.put(entry.getKey(), Double.toString(entry.getValue()));
                    }
                } catch (JSONException e) {
                    Log.d(TAG, e.getMessage());
                    return dummyUri;
                }
                jsonEvents.put(jsonObject);
            }
            FileOutputStream fileOutputStream = null;
            try {
                fileOutputStream = new FileOutputStream(jsonFile, false);
                byte[] content = jsonEvents.toString().getBytes();
                fileOutputStream.write(content);
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
                return dummyUri;
            } finally {
                if (fileOutputStream != null)
                    try {
                        fileOutputStream.flush();
                        fileOutputStream.close();
                    } catch (IOException e) {
                        Log.e(TAG, "problem while flushing", e);
                    }
            }
            return jsonFile.toURI();
        } finally {
            if (realm != null) {
                realm.close();
            }
        }
    }

    @Override
    public void subscribeToEvents(Observable<FalxMonitorEvent> observable) {
        compositeDisposable.add(observable
                .observeOn(looperScheduler)
                .subscribe(new Consumer<FalxMonitorEvent>() {
                    @Override
                    public void accept(FalxMonitorEvent event) throws Exception {
                        FalxEventStore.this.save(event);
                    }
                }));
    }

    @Override
    public void clearSubscriptions() {
        compositeDisposable.clear();
    }

    private boolean isSameDate(Calendar cal1, Calendar cal2) {
        return (cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH) &&
                cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH));
    }

    // Test function
    public void testFunction() {
        Realm realm = null;
        try {
            realm = realmStore.realmInstance();
            RealmResults<FalxEventEntity> entities = realm.where(FalxEventEntity.class).findAll();
            for (FalxEventEntity entity : entities) {
                Log.d(TAG, entity.toString() + "----" + entity.getArgumentsMap().toString());
            }
        } finally {
            if (realm != null) {
                realm.close();
            }
        }
    }
}
