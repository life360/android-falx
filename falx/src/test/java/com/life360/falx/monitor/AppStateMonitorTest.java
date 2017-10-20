package com.life360.falx.monitor;

import android.content.Context;

import com.life360.falx.dagger.AppModule;
import com.life360.falx.dagger.DaggerTestUtilComponent;
import com.life360.falx.dagger.FakeDateTimeModule;
import com.life360.falx.dagger.TestLoggerModule;
import com.life360.falx.dagger.TestUtilComponent;
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

/**
 * Created by remon on 7/26/17.
 */
@RunWith(MockitoJUnitRunner.class)
public class AppStateMonitorTest {

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

        AppStateMonitor monitor = new AppStateMonitor(testUtilComponent, appStateObservable()) {
            @Override
            protected void saveToDataStore() {
                // do nothing.
            }
        };

        TestClock testClock = (TestClock) monitor.clock;

        final long startTime = 100L;
        long currentTime = startTime;

        testClock.setCurrentTimeMillis(currentTime);
        Assert.assertEquals(testClock.currentTimeMillis(), currentTime);

        monitor.onForeground();

        currentTime += 500L;
        testClock.setCurrentTimeMillis(currentTime);

        monitor.onBackground();

        Assert.assertNotNull(monitor.sessionEndTimer);

        monitor.onSessionEnded();

        Assert.assertEquals(monitor.lastSessionData.startTime, startTime);
        Assert.assertEquals(monitor.lastSessionData.getDuration(), currentTime - startTime);

        monitor.stop();
    }

    @Test
    public void sessionTestChained() throws Exception {
        TestUtilComponent testUtilComponent = DaggerTestUtilComponent.builder()
                .appModule(new AppModule(mockContext))
                .fakeDateTimeModule(new FakeDateTimeModule())
                .build();

        AppStateMonitor monitor = new AppStateMonitor(testUtilComponent, appStateObservable()) {
            @Override
            protected void saveToDataStore() {
                // do nothing.
            }
        };

        TestClock testClock = (TestClock) monitor.clock;

        final long firstSessionStartTime = 100L;
        long currentTime = firstSessionStartTime;
        testClock.setCurrentTimeMillis(currentTime);

        Assert.assertEquals(testClock.currentTimeMillis(), currentTime);

        monitor.onForeground();

        currentTime += 500L;
        testClock.setCurrentTimeMillis(currentTime);

        monitor.onBackground();

        Assert.assertNotNull(monitor.sessionEndTimer);

        currentTime += (AppStateMonitor.TIME_BETWEEN_ACTIVITY_TRANSITION - 1);
        testClock.setCurrentTimeMillis(currentTime);

        monitor.onForeground();

        currentTime += 500L;
        testClock.setCurrentTimeMillis(currentTime);

        monitor.onBackground();
        monitor.onSessionEnded();

        Assert.assertEquals(monitor.lastSessionData.startTime, firstSessionStartTime);
        Assert.assertEquals(monitor.lastSessionData.getDuration(), currentTime - firstSessionStartTime);

        // Start another session after the time between session is expired
        currentTime += (AppStateMonitor.TIME_BETWEEN_ACTIVITY_TRANSITION + 1);
        testClock.setCurrentTimeMillis(currentTime);

        final long secondSessionStartTime = currentTime;

        monitor.onForeground();
        currentTime += 500L;
        testClock.setCurrentTimeMillis(currentTime);

        monitor.onBackground();
        monitor.onSessionEnded();

        Assert.assertEquals(monitor.lastSessionData.startTime, secondSessionStartTime);
        Assert.assertEquals(monitor.lastSessionData.getDuration(), currentTime - secondSessionStartTime);

        monitor.stop();
    }

    @Test
    public void sessionTestErrorCase() throws Exception {
        TestUtilComponent testUtilComponent = DaggerTestUtilComponent.builder()
                .appModule(new AppModule(mockContext))
                .fakeDateTimeModule(new FakeDateTimeModule())
                .build();

        AppStateMonitor monitor = new AppStateMonitor(testUtilComponent, appStateObservable()) {
            @Override
            protected void saveToDataStore() {
                // do nothing.
            }
        };

        TestClock testClock = (TestClock) monitor.clock;

        final long firstSessionStartTime = 100L;
        long currentTime = firstSessionStartTime;
        testClock.setCurrentTimeMillis(currentTime);

        Assert.assertEquals(testClock.currentTimeMillis(), currentTime);

        // We did not get a onForeground, but get a onBackground
        monitor.onBackground();
        monitor.onSessionEnded();

        Assert.assertEquals(monitor.lastSessionData.startTime, 0);
        // If the startTime was never set, then lastSessionData should record 0 as the session time
        Assert.assertEquals(monitor.lastSessionData.getDuration(), 0);

    }

        // Dummy Observable for tests
    private Observable<AppState> appStateObservable() {

        return Observable.create(new ObservableOnSubscribe<AppState>() {
            @Override
            public void subscribe(@io.reactivex.annotations.NonNull final ObservableEmitter<AppState> appStateEmitter) throws Exception {
            }
        });
    }

}
