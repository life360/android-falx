package com.life360.falx.monitor;

import android.content.Context;

import com.life360.falx.dagger.AppModule;
import com.life360.falx.dagger.DaggerTestUtilComponent;
import com.life360.falx.dagger.FakeDateTimeModule;
import com.life360.falx.dagger.TestLoggerModule;
import com.life360.falx.dagger.TestUtilComponent;
import com.life360.falx.model.RealtimeMessagingActivity;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import io.reactivex.subjects.PublishSubject;

/**
 * Created by remon on 9/19/17.
 */
@RunWith(MockitoJUnitRunner.class)
public class RealtimeMessagingMonitorTest {

    @Mock
    Context mockContext;


    @Test
    public void testObservable() {
        TestUtilComponent testUtilComponent = DaggerTestUtilComponent.builder()
                .appModule(new AppModule(mockContext))
                .fakeDateTimeModule(new FakeDateTimeModule())
                .testLoggerModule(new TestLoggerModule())
                .build();

        PublishSubject<RealtimeMessagingActivity> rtMessagingPublishSubject = PublishSubject.create();

        RealtimeMessagingMonitor monitor = new RealtimeMessagingMonitor(testUtilComponent, rtMessagingPublishSubject) {
            @Override
            protected void saveToDataStore(RealtimeMessagingActivity activity) {
                // do nothing.
            }
        };

        // Not much to test here yet as the Monitor is just observing NetworkActivity objects and writing them to the data store.
        Assert.assertNotNull(monitor.rtMessagingDisposable);
        Assert.assertFalse(monitor.rtMessagingDisposable.isDisposed());

        monitor.stop();
        Assert.assertTrue(monitor.rtMessagingDisposable.isDisposed());
    }

}
