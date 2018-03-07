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

import android.content.Context;
import android.text.format.DateUtils;

import com.life360.falx.dagger.AppModule;
import com.life360.falx.dagger.DaggerTestUtilComponent;
import com.life360.falx.dagger.FakeDateTimeModule;
import com.life360.falx.dagger.TestLoggerModule;
import com.life360.falx.dagger.TestUtilComponent;
import com.life360.falx.model.FalxMonitorEvent;
import com.life360.falx.util.TestClock;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.observers.TestObserver;

/**
 * Created by sudheer on 11/10/17.
 */
@RunWith(MockitoJUnitRunner.class)
public class OnOffMonitorTest {

    public static final String MONITOR_LABEL_GPS = "GPS";

    /* Monitor for use by the legacy location code */
    public static final String MONITOR_LABEL_ACTIVITY_DETECTION = "ActivityDetection";

    public static final String EVENT_ACTIVITY_DETECTION_ON = "activities-on";
    public static final String EVENT_GPS_ON = "gps-on";
    @Mock
    Context mockContext;

    @Before
    public void initMocks() {
    }

    @Test
    public void sessionTest() throws Exception {
        TestUtilComponent testUtilComponent = DaggerTestUtilComponent.builder()
                .appModule(new AppModule(mockContext))
                .fakeDateTimeModule(new FakeDateTimeModule())
                .testLoggerModule(new TestLoggerModule())
                .build();

        OnOffMonitor monitor = new OnOffMonitor(testUtilComponent, stateObservable(), EVENT_GPS_ON, MONITOR_LABEL_GPS);

        TestObserver<FalxMonitorEvent> testObserver = monitor.getEventObservable().test();
        TestClock testClock = (TestClock) monitor.clock;

        final long startTime = 100L;
        long currentTime = startTime;

        testClock.setCurrentTimeMillis(currentTime);
        Assert.assertEquals(testClock.currentTimeMillis(), currentTime);

        monitor.turnedOn();

        currentTime += 500L;
        testClock.setCurrentTimeMillis(currentTime);

        monitor.turnedOff();

        FalxMonitorEvent event = testObserver.values().get(0);
        Assert.assertEquals(EVENT_GPS_ON, event.getName());
        Assert.assertEquals(currentTime - startTime, (long)(event.getArguments().get(FalxConstants.PROP_DURATION) * DateUtils.SECOND_IN_MILLIS));
        monitor.stop();
    }

    /*
     * Make sure if we use multiple on off monitors, they work correctly independent of each other.
     */
    @Test
    public void sessionTwoMonitors() throws Exception {
        TestUtilComponent testUtilComponent = DaggerTestUtilComponent.builder()
                .appModule(new AppModule(mockContext))
                .fakeDateTimeModule(new FakeDateTimeModule())
                .testLoggerModule(new TestLoggerModule())
                .build();

        OnOffMonitor gpsMonitor = new OnOffMonitor(testUtilComponent, stateObservable(), EVENT_GPS_ON, MONITOR_LABEL_GPS);
        OnOffMonitor activitiesMonitor = new OnOffMonitor(testUtilComponent, stateObservable(), EVENT_ACTIVITY_DETECTION_ON, MONITOR_LABEL_ACTIVITY_DETECTION);

        TestObserver<FalxMonitorEvent> gpsObserver = gpsMonitor.getEventObservable().test();
        TestObserver<FalxMonitorEvent> activitiesObserver = activitiesMonitor.getEventObservable().test();
        TestClock gpsClock = (TestClock) gpsMonitor.clock;
        TestClock activitiesClock = (TestClock) activitiesMonitor.clock;

        final long startTime = 100L;
        long currentTime = startTime;

        gpsClock.setCurrentTimeMillis(currentTime);
        Assert.assertEquals(gpsClock.currentTimeMillis(), currentTime);

        gpsMonitor.turnedOn();

        currentTime += 500L;
        final long activitiesStartTime = currentTime;
        activitiesClock.setCurrentTimeMillis(currentTime);
        activitiesMonitor.turnedOn();

        currentTime += 500L;
        gpsClock.setCurrentTimeMillis(currentTime);

        gpsMonitor.turnedOff();

        FalxMonitorEvent event = gpsObserver.values().get(0);
        Assert.assertEquals(EVENT_GPS_ON, event.getName());
        Assert.assertEquals(currentTime - startTime, (long)(event.getArguments().get(FalxConstants.PROP_DURATION) * DateUtils.SECOND_IN_MILLIS));
        gpsMonitor.stop();

        currentTime += 500L;
        activitiesClock.setCurrentTimeMillis(currentTime);

        activitiesMonitor.turnedOff();

        event = activitiesObserver.values().get(0);
        Assert.assertEquals(EVENT_ACTIVITY_DETECTION_ON, event.getName());
        Assert.assertEquals(currentTime - activitiesStartTime, (long)(event.getArguments().get(FalxConstants.PROP_DURATION) * DateUtils.SECOND_IN_MILLIS));
        activitiesMonitor.stop();

    }

    @Test
    public void sessionTestChained() throws Exception {
        TestUtilComponent testUtilComponent = DaggerTestUtilComponent.builder()
                .appModule(new AppModule(mockContext))
                .fakeDateTimeModule(new FakeDateTimeModule())
                .build();

        OnOffMonitor monitor = new OnOffMonitor(testUtilComponent, stateObservable(), EVENT_GPS_ON, MONITOR_LABEL_GPS);
        TestObserver<FalxMonitorEvent> testObserver = monitor.getEventObservable().test();

        TestClock testClock = (TestClock) monitor.clock;

        final long firstSessionStartTime = 100L;
        long currentTime = firstSessionStartTime;
        testClock.setCurrentTimeMillis(currentTime);

        Assert.assertEquals(testClock.currentTimeMillis(), currentTime);

        monitor.turnedOn();

        currentTime += 500L;
        testClock.setCurrentTimeMillis(currentTime);

        monitor.turnedOff();

        testClock.setCurrentTimeMillis(currentTime);
        final long secondSessionStartTime = currentTime;

        monitor.turnedOn();

        currentTime += 500L;
        testClock.setCurrentTimeMillis(currentTime);

        monitor.turnedOff();

        FalxMonitorEvent event = testObserver.values().get(0);
        Assert.assertEquals(EVENT_GPS_ON, event.getName());
        Assert.assertEquals(currentTime - secondSessionStartTime, (long)(event.getArguments().get(FalxConstants.PROP_DURATION) * DateUtils.SECOND_IN_MILLIS));

        testClock.setCurrentTimeMillis(currentTime);

        final long thirdSessionStartTime = currentTime;

        monitor.turnedOn();
        currentTime += 500L;
        testClock.setCurrentTimeMillis(currentTime);

        monitor.turnedOff();

        event = testObserver.values().get(0);
        Assert.assertEquals(EVENT_GPS_ON, event.getName());
        Assert.assertEquals(currentTime - thirdSessionStartTime, (long)(event.getArguments().get(FalxConstants.PROP_DURATION) * DateUtils.SECOND_IN_MILLIS));

        monitor.stop();
    }

    @Test
    public void sessionTestErrorCase() throws Exception {
        TestUtilComponent testUtilComponent = DaggerTestUtilComponent.builder()
                .appModule(new AppModule(mockContext))
                .fakeDateTimeModule(new FakeDateTimeModule())
                .build();

        OnOffMonitor monitor = new OnOffMonitor(testUtilComponent, stateObservable(), EVENT_GPS_ON, MONITOR_LABEL_GPS);
        TestObserver<FalxMonitorEvent> testObserver = monitor.getEventObservable().test();

        TestClock testClock = (TestClock) monitor.clock;

        final long firstSessionStartTime = 100L;
        long currentTime = firstSessionStartTime;
        testClock.setCurrentTimeMillis(currentTime);

        Assert.assertEquals(testClock.currentTimeMillis(), currentTime);

        // We did not get a turnedOn, but got a turnedOff
        monitor.turnedOff();

        FalxMonitorEvent event = testObserver.values().get(0);
        Assert.assertEquals(EVENT_GPS_ON, event.getName());
        Assert.assertEquals(0, (long)(event.getArguments().get(FalxConstants.PROP_DURATION) * DateUtils.SECOND_IN_MILLIS));

    }

    // Dummy Observable for tests
    private Observable<Boolean> stateObservable() {

        return Observable.create(new ObservableOnSubscribe<Boolean>() {
            @Override
            public void subscribe(@io.reactivex.annotations.NonNull final ObservableEmitter<Boolean> stateEmitter) throws Exception {
            }
        });
    }

}
