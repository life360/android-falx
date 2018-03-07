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

package com.life360.falx;

import android.content.Context;

import com.life360.falx.dagger.AppModule;
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
                .appModule(new AppModule(mockContext))                          // Can we have a FakeModule that can work without a Context?
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