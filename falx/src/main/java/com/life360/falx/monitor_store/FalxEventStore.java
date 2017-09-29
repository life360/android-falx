package com.life360.falx.monitor_store;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.life360.falx.model.FalxEventEntity;
import com.life360.falx.model.FalxMonitorEvent;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import io.reactivex.Observable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;
import io.realm.Sort;

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

/**
 * Created by Vikas on 9/19/17.
 */

public class FalxEventStore implements FalxEventStorable {

    private static final String TAG = FalxEventStore.class.getSimpleName();

    private static final String FALX_EVENT_STORE_SYNC_DATE_KEY = "FalxEventStoreSyncDatekey";
    private static final String FALX_LOG_SYNC_PREF_FILE = "falx_sync_log_pref_file";

    //TODO move to network monitor
    public static final String FALX_URL_PREFIX = "URL=";
    /**
     * sync value is in hrs
     */
    private static final int SYNC_INTERVAL = 24;

    private Context context;

    private RealmStore realmStore;

    public FalxEventStore(RealmStore store, Context context) {
        this.realmStore = store;
        this.context = context;
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
    public void save(FalxMonitorEvent event) {
        FalxEventEntity entity = new FalxEventEntity(event);

        Realm realm = realmStore.realmInstance();

        realm.beginTransaction();
        realm.copyToRealm(entity);
        realm.commitTransaction();

        this.testFunction();
    }

    @Override
    public void deleteOldEvents() {
        Calendar sixDaysAgoCal = Calendar.getInstance();
        sixDaysAgoCal.add(Calendar.DATE, -6);
        sixDaysAgoCal.set(Calendar.HOUR_OF_DAY, 0);
        sixDaysAgoCal.set(Calendar.MINUTE, 0);
        sixDaysAgoCal.set(Calendar.SECOND, 0);

        Realm realm = realmStore.realmInstance();
        Date beginingOfSixDaysAgo = sixDaysAgoCal.getTime();
        final RealmResults<FalxEventEntity> olderEvents = realm
                .where(FalxEventEntity.class)
                .lessThan("timestamp", beginingOfSixDaysAgo)
                .findAll();

        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                olderEvents.deleteAllFromRealm();
            }
        });
    }

    @Override
    public List<AggregatedFalxMonitorEvent> aggregateEvents(String eventName) {
        return aggregatedEvents(eventName, true);
    }

    @Override
    public List<AggregatedFalxMonitorEvent> aggregatedEvents(String eventName, boolean allowPartialDays) {

        Realm realm = realmStore.realmInstance();

        RealmResults<FalxEventEntity> events = realm.where(FalxEventEntity.class)
                .equalTo("name", eventName)
                .equalTo("processedByAgreegated", false)
                .findAllSorted("timestamp", Sort.ASCENDING);


        int count = events.size();

        if (count <= 0) {
            return null;
        }

        List<AggregatedFalxMonitorEvent> aggregatedFalxEvents = new ArrayList<>();

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
                this.aggregateCurrentEvent(
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
                this.aggregateCurrentEvent(
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
        if (aggregatedFalxEvents.size() > 0) {
            return aggregatedFalxEvents;
        } else {
            return null;
        }

    }

    private void aggregateCurrentEvent(FalxEventEntity currentEntity,
                                       List<FalxEventEntity> processedEvents,
                                       Map<String, Double> aggregatedArguments) {
        processedEvents.add(currentEntity);

        Map<String, Double> currentAruments = currentEntity.getArguments();

        for (String key : currentAruments.keySet()) {
            if (!key.contains(FALX_URL_PREFIX)) {
                if (aggregatedArguments.containsKey(key)) {
                    aggregatedArguments.put(key, aggregatedArguments.get(key) + currentAruments.get(key));
                } else {
                    aggregatedArguments.put(key, currentAruments.get(key));
                }
            }
        }

    }

    private AggregatedFalxMonitorEvent createAggregatedEvents(final RealmList<FalxEventEntity> events,
                                                              Map<String, Double> aggregatedArguments,
                                                              Date timestamp) {
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


        Realm realm = realmStore.realmInstance();

        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                for (FalxEventEntity event : events) {
                    event.setProcessedByAgregated(true);
                }
            }
        });

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
                createAggregatedEvents(processedEvents, aggregatedArguments, startOfDay);

        if (aggregatedFalxMonitorEvent != null) {
            aggregatedFalxEvents.add(aggregatedFalxMonitorEvent);
        }
    }

    @Override
    public List<AggregatedFalxMonitorEvent> allAggregatedEvents(boolean allowPartialDays) {
        Realm realm = realmStore.realmInstance();

        RealmResults<FalxEventEntity> allDistinctNameEvents = realm.where(FalxEventEntity.class).distinct("name");

        List<AggregatedFalxMonitorEvent> allEvents = new ArrayList<>();
        for (int i = 0; i < allDistinctNameEvents.size(); i++) {
            List<AggregatedFalxMonitorEvent> events = this.aggregatedEvents(
                    allDistinctNameEvents.get(i).getName(),
                    allowPartialDays);
            allEvents.addAll(events);

        }
        this.setSyncDate(new Date());
        this.deleteOldEvents();
        return allEvents;
    }

    @Override
    public URI eventToJSONFile() {
        this.deleteOldEvents();

        String fileName = "FalxEvents";

        File jsonFile = null;
        try {
            jsonFile = File.createTempFile(fileName, null, context.getCacheDir());
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
            return null;
        }

        Realm realm = realmStore.realmInstance();

        RealmResults<FalxEventEntity> allRealmEvents = realm.where(FalxEventEntity.class)
                .findAllSorted("timestamp", Sort.ASCENDING);

        if (allRealmEvents.size() <= 0) {
            return null;
        }

        JSONArray jsonEvents = new JSONArray();

        for (FalxEventEntity falxEventEntity : allRealmEvents) {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("name", falxEventEntity.getName());

                Calendar cal = Calendar.getInstance();
                cal.setTime(falxEventEntity.getTimestamp());
                jsonObject.put("timestamp", Long.toString(cal.getTimeInMillis()));

                for (Map.Entry<String, Double> entry : falxEventEntity.getArguments().entrySet()) {
                    jsonObject.put(entry.getKey(), Double.toString(entry.getValue()));
                }
            } catch (JSONException e) {
                Log.d(TAG,e.getMessage());
                return null;
            }

            jsonEvents.put(jsonObject);
        }
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(jsonFile);
            byte[] content = jsonEvents.toString().getBytes();

            fileOutputStream.write(content);
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
            return null;
        } finally {
            if (fileOutputStream != null)
                try {
                    fileOutputStream.flush();
                    fileOutputStream.close();
                } catch (IOException e) {
                    Log.d(TAG,e.getMessage());
                    return null;
                }
        }

        return jsonFile.toURI();
    }

    @Override
    public void subscribeToEvents(Observable<FalxMonitorEvent> observable) {
        //TODO make sure to add it to disposable bag to dispose
        observable
                .subscribeOn(Schedulers.io())
                .subscribe(new Consumer<FalxMonitorEvent>() {
                    @Override
                    public void accept(FalxMonitorEvent event) throws Exception {
                        FalxEventStore.this.save(event);
                    }
                });
    }

    private boolean isSameDate(Calendar cal1, Calendar cal2) {
        return (cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH) &&
                cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH));

    }

    // ** Test function
    public void testFunction() {
//        deleteOldEvents();
//        Log.d(TAG, "deleted old events");
        Realm realm = realmStore.realmInstance();

        RealmResults<FalxEventEntity> entities = realm.where(FalxEventEntity.class).findAll();

        for (FalxEventEntity entity : entities) {
            Log.d(TAG, entity.toString() + "----" + entity.getArguments().toString());
        }

//        List<AggregratedFalxMonitorEvent> list = this.allAggregatedEvents(true);
//        Log.d(TAG,list.toString());
//        this.eventToJSONFile();
    }
}
