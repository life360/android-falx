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
 * Created by sudheer on 11/22/17.
 */
@RunWith(MockitoJUnitRunner.class)
public class WakelockMonitorTest {

    public static final String MONITOR_LABEL_WAKELOCK = "Wakelock";

    /* Monitor for use by the legacy location code */
    public static final String MONITOR_LABEL_WAKELOCK_2 = "Wakelock2";

    public static final String EVENT_WAKELOCK_ACQUIRED = "wakelock-acq";
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

        WakelockMonitor monitor = new WakelockMonitor(testUtilComponent, stateObservable(), EVENT_WAKELOCK_ACQUIRED, MONITOR_LABEL_WAKELOCK);

        TestObserver<FalxMonitorEvent> testObserver = monitor.getEventObservable().test();
        TestClock testClock = (TestClock) monitor.clock;

        final long startTime = 100L;
        long currentTime = startTime;

        testClock.setCurrentTimeMillis(currentTime);
        Assert.assertEquals(testClock.currentTimeMillis(), currentTime);

        monitor.acquired();

        currentTime += 500L;
        testClock.setCurrentTimeMillis(currentTime);

        monitor.released();

        FalxMonitorEvent event = testObserver.values().get(0);
        Assert.assertEquals(EVENT_WAKELOCK_ACQUIRED, event.getName());
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

        WakelockMonitor monitor1 = new WakelockMonitor(testUtilComponent, stateObservable(), EVENT_WAKELOCK_ACQUIRED, MONITOR_LABEL_WAKELOCK);
        WakelockMonitor monitor2 = new WakelockMonitor(testUtilComponent, stateObservable(), EVENT_WAKELOCK_ACQUIRED, MONITOR_LABEL_WAKELOCK_2);

        TestObserver<FalxMonitorEvent> observer1 = monitor1.getEventObservable().test();
        TestObserver<FalxMonitorEvent> observer2 = monitor2.getEventObservable().test();
        TestClock clock1 = (TestClock) monitor1.clock;
        TestClock clock2 = (TestClock) monitor2.clock;

        final long startTime = 100L;
        long currentTime = startTime;

        clock1.setCurrentTimeMillis(currentTime);
        Assert.assertEquals(clock1.currentTimeMillis(), currentTime);

        monitor1.acquired();

        currentTime += 500L;
        final long wakelock2AcqTime = currentTime;
        clock2.setCurrentTimeMillis(currentTime);
        monitor2.acquired();

        currentTime += 500L;
        clock1.setCurrentTimeMillis(currentTime);

        monitor1.released();

        FalxMonitorEvent event = observer1.values().get(0);
        Assert.assertEquals(EVENT_WAKELOCK_ACQUIRED, event.getName());
        Assert.assertEquals(currentTime - startTime, (long)(event.getArguments().get(FalxConstants.PROP_DURATION) * DateUtils.SECOND_IN_MILLIS));
        monitor1.stop();

        currentTime += 500L;
        clock2.setCurrentTimeMillis(currentTime);

        monitor2.released();

        event = observer2.values().get(0);
        Assert.assertEquals(EVENT_WAKELOCK_ACQUIRED, event.getName());
        Assert.assertEquals(currentTime - wakelock2AcqTime, (long)(event.getArguments().get(FalxConstants.PROP_DURATION) * DateUtils.SECOND_IN_MILLIS));
        monitor2.stop();

    }

    @Test
    public void sessionTestChained() throws Exception {
        TestUtilComponent testUtilComponent = DaggerTestUtilComponent.builder()
                .appModule(new AppModule(mockContext))
                .fakeDateTimeModule(new FakeDateTimeModule())
                .build();

        WakelockMonitor monitor = new WakelockMonitor(testUtilComponent, stateObservable(), EVENT_WAKELOCK_ACQUIRED, MONITOR_LABEL_WAKELOCK);
        TestObserver<FalxMonitorEvent> testObserver = monitor.getEventObservable().test();

        TestClock testClock = (TestClock) monitor.clock;

        final long firstSessionStartTime = 100L;
        long currentTime = firstSessionStartTime;
        testClock.setCurrentTimeMillis(currentTime);

        Assert.assertEquals(testClock.currentTimeMillis(), currentTime);

        monitor.acquired();

        currentTime += 500L;
        testClock.setCurrentTimeMillis(currentTime);

        monitor.released();

        testClock.setCurrentTimeMillis(currentTime);
        final long secondSessionStartTime = currentTime;

        monitor.acquired();

        currentTime += 500L;
        testClock.setCurrentTimeMillis(currentTime);

        monitor.released();

        FalxMonitorEvent event = testObserver.values().get(1);
        Assert.assertEquals(EVENT_WAKELOCK_ACQUIRED, event.getName());
        Assert.assertEquals(currentTime - secondSessionStartTime, (long)(event.getArguments().get(FalxConstants.PROP_DURATION) * DateUtils.SECOND_IN_MILLIS));

        testClock.setCurrentTimeMillis(currentTime);

        final long thirdSessionStartTime = currentTime;

        monitor.acquired(DateUtils.MINUTE_IN_MILLIS);

        event = testObserver.values().get(2);
        Assert.assertEquals(EVENT_WAKELOCK_ACQUIRED, event.getName());
        Assert.assertEquals(DateUtils.MINUTE_IN_MILLIS, (long)(event.getArguments().get(FalxConstants.PROP_DURATION) * DateUtils.SECOND_IN_MILLIS));

        currentTime += 500L;
        testClock.setCurrentTimeMillis(currentTime);
        monitor.released();

        event = testObserver.values().get(0);
        Assert.assertEquals(EVENT_WAKELOCK_ACQUIRED, event.getName());
        Assert.assertEquals(currentTime - thirdSessionStartTime, (long)(event.getArguments().get(FalxConstants.PROP_DURATION) * DateUtils.SECOND_IN_MILLIS));

        monitor.stop();
    }

    @Test
    public void sessionTestErrorCase() throws Exception {
        TestUtilComponent testUtilComponent = DaggerTestUtilComponent.builder()
                .appModule(new AppModule(mockContext))
                .fakeDateTimeModule(new FakeDateTimeModule())
                .build();

        WakelockMonitor monitor = new WakelockMonitor(testUtilComponent, stateObservable(), EVENT_WAKELOCK_ACQUIRED, MONITOR_LABEL_WAKELOCK);
        TestObserver<FalxMonitorEvent> testObserver = monitor.getEventObservable().test();

        TestClock testClock = (TestClock) monitor.clock;

        final long firstSessionStartTime = 100L;
        long currentTime = firstSessionStartTime;
        testClock.setCurrentTimeMillis(currentTime);

        Assert.assertEquals(testClock.currentTimeMillis(), currentTime);

        // We did not get a turnedOn, but got a turnedOff
        monitor.released();

        FalxMonitorEvent event = testObserver.values().get(0);
        Assert.assertEquals(EVENT_WAKELOCK_ACQUIRED, event.getName());
        Assert.assertEquals(0, (long)(event.getArguments().get(FalxConstants.PROP_DURATION) * DateUtils.SECOND_IN_MILLIS));

    }

    // Dummy Observable for tests
    private Observable<WakelockState> stateObservable() {

        return Observable.create(new ObservableOnSubscribe<WakelockState>() {
            @Override
            public void subscribe(@io.reactivex.annotations.NonNull final ObservableEmitter<WakelockState> stateEmitter) throws Exception {
            }
        });
    }

}
