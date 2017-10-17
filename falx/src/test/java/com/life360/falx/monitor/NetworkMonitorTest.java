package com.life360.falx.monitor;

import android.content.Context;

import com.life360.falx.dagger.AppModule;
import com.life360.falx.dagger.DaggerTestUtilComponent;
import com.life360.falx.dagger.FakeDateTimeModule;
import com.life360.falx.dagger.TestLoggerModule;
import com.life360.falx.dagger.TestUtilComponent;
import com.life360.falx.model.NetworkActivity;

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
public class NetworkMonitorTest {

    @Mock
    Context mockContext;


    @Test
    public void testObservable() {
        TestUtilComponent testUtilComponent = DaggerTestUtilComponent.builder()
                .appModule(new AppModule(mockContext))
                .fakeDateTimeModule(new FakeDateTimeModule())
                .testLoggerModule(new TestLoggerModule())
                .build();

        PublishSubject<NetworkActivity> networkActivityPublishSubject = PublishSubject.create();

        NetworkMonitor monitor = new NetworkMonitor(testUtilComponent, networkActivityPublishSubject) {
            @Override
            protected void saveToDataStore(NetworkActivity activity) {
                // do nothing.
            }
        };

        // Not much to test here yet as the Monitor is just observing NetworkActivity objects and writing them to the data store.
        Assert.assertNotNull(monitor.networkActivityDisposable);
        Assert.assertFalse(monitor.networkActivityDisposable.isDisposed());

        monitor.stop();
        Assert.assertTrue(monitor.networkActivityDisposable.isDisposed());
    }

}
