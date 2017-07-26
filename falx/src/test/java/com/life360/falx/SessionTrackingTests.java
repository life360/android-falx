package com.life360.falx;

import android.content.Context;

import com.life360.falx.dagger.DaggerTestUtilComponent;
import com.life360.falx.dagger.FakeDateTimeModule;
import com.life360.falx.dagger.TestLoggerModule;
import com.life360.falx.dagger.TestUtilComponent;
import com.life360.falx.monitor.AppState;
import com.life360.falx.monitor.AppStateMonitor;
import com.life360.falx.util.TestClock;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import io.reactivex.Observable;
import io.reactivex.observers.TestObserver;

/**
 * Unit tests for session tracking.
 */
@RunWith(MockitoJUnitRunner.class)
public class SessionTrackingTests {

    @Mock
    Context mockContext;

    @Before
    public void initMocks() {
    }

    @Test
    public void testAppStateObserver() {
        TestUtilComponent testUtilComponent = DaggerTestUtilComponent.builder()
                .fakeDateTimeModule(new FakeDateTimeModule())
                .build();

        FalxApi api = new FalxApi(mockContext, testUtilComponent);
        Assert.assertNotNull(api);

        Assert.assertEquals(api.appStateListeners.size(), 0);

        Observable<AppState> appStateObservable = api.appStateObservable();

        TestObserver<AppState> testObserver = new TestObserver<>();
        appStateObservable.subscribeWith(testObserver);

        Assert.assertEquals(api.appStateListeners.size(), 1);
        testObserver.assertNoValues();

        api.onAppStateForeground();
        testObserver.assertValueCount(1);
        testObserver.assertValue(AppState.FOREGROUND);

        api.onAppStateBackground();
        testObserver.assertValueCount(2);
        testObserver.assertValues(AppState.FOREGROUND, AppState.BACKGROUND);

        api.onAppStateForeground();
        testObserver.assertValueCount(3);
        testObserver.assertValues(AppState.FOREGROUND, AppState.BACKGROUND, AppState.FOREGROUND);
    }
}