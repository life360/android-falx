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
        PublishSubject<RealtimeMessagingSession> realtimeMessagingSessionObservable = PublishSubject.create();

        RealtimeMessagingMonitor monitor = new RealtimeMessagingMonitor(testUtilComponent, rtMessagingPublishSubject, realtimeMessagingSessionObservable) {
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
