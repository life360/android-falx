package com.life360.falx;

import android.content.Context;

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
    public void sessionTest() throws Exception {
        TestUtilComponent testUtilComponent = DaggerTestUtilComponent.builder()
                .fakeDateTimeModule(new FakeDateTimeModule())
                .testLoggerModule(new TestLoggerModule())
                .build();

        FalxApi api = new FalxApi(mockContext, testUtilComponent);
        Assert.assertNotNull(api);

        TestClock testClock = (TestClock) api.clock;

        final long startTime = 100L;
        long currentTime = startTime;

        testClock.setCurrentTimeMillis(currentTime);
        Assert.assertEquals(testClock.currentTimeMillis(), currentTime);

        api.startSession(null);

        currentTime += 500L;
        testClock.setCurrentTimeMillis(currentTime);

        api.endSession(null);

        Assert.assertNotNull(api.sessionEndTimer);

        api.onSessionEnded();

        Assert.assertEquals(api.lastSessionData.startTime, startTime);
        Assert.assertEquals(api.lastSessionData.duration, currentTime - startTime);
    }

    @Test
    public void sessionTestChained() throws Exception {
        TestUtilComponent testUtilComponent = DaggerTestUtilComponent.builder()
                .fakeDateTimeModule(new FakeDateTimeModule())
                .build();

        FalxApi api = new FalxApi(mockContext, testUtilComponent);
        Assert.assertNotNull(api);

        TestClock testClock = (TestClock) api.clock;

        final long firstSessionStartTime = 100L;
        long currentTime = firstSessionStartTime;
        testClock.setCurrentTimeMillis(currentTime);

        Assert.assertEquals(testClock.currentTimeMillis(), currentTime);

        api.startSession(null);

        currentTime += 500L;
        testClock.setCurrentTimeMillis(currentTime);

        api.endSession(null);

        Assert.assertNotNull(api.sessionEndTimer);

        currentTime += (FalxApi.TIME_BETWEEN_ACTIVITY_TRANSITION - 1);
        testClock.setCurrentTimeMillis(currentTime);

        api.startSession(null);

        currentTime += 500L;
        testClock.setCurrentTimeMillis(currentTime);

        api.endSession(null);
        api.onSessionEnded();

        Assert.assertEquals(api.lastSessionData.startTime, firstSessionStartTime);
        Assert.assertEquals(api.lastSessionData.duration, currentTime - firstSessionStartTime);

        // Start another session after the time between session is expired
        currentTime += (FalxApi.TIME_BETWEEN_ACTIVITY_TRANSITION + 1);
        testClock.setCurrentTimeMillis(currentTime);

        final long secondSessionStartTime = currentTime;

        api.startSession(null);
        currentTime += 500L;
        testClock.setCurrentTimeMillis(currentTime);

        api.endSession(null);
        api.onSessionEnded();

        Assert.assertEquals(api.lastSessionData.startTime, secondSessionStartTime);
        Assert.assertEquals(api.lastSessionData.duration, currentTime - secondSessionStartTime);
    }
}