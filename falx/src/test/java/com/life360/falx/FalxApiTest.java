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
import com.life360.falx.dagger.TestUtilComponent;
import com.life360.falx.network.FalxInterceptor;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * Created by remon on 10/17/17.
 */
@RunWith(MockitoJUnitRunner.class)
public class FalxApiTest {

    @Mock
    Context mockContext;

    @Test
    public void testConstruction() {

        TestUtilComponent testUtilComponent = DaggerTestUtilComponent.builder()
                .appModule(new AppModule(mockContext))                          // Can we have a FakeModule that can work without a Context?
                .fakeDateTimeModule(new FakeDateTimeModule())
                .build();

        FalxApi api = new FalxApi(mockContext, testUtilComponent);
        Assert.assertNotNull(api);
        Assert.assertNotNull((api.eventStorable));

        api.addMonitors(FalxApi.MONITOR_APP_STATE);
        Assert.assertTrue(api.isMonitorActive(FalxApi.MONITOR_APP_STATE));
        Assert.assertEquals(api.monitors.size(), 1);

        api.addMonitors(FalxApi.MONITOR_APP_STATE | FalxApi.MONITOR_NETWORK);
        Assert.assertTrue(api.isMonitorActive(FalxApi.MONITOR_APP_STATE));
        Assert.assertTrue(api.isMonitorActive(FalxApi.MONITOR_NETWORK));
        Assert.assertEquals(api.monitors.size(), 2);

        Assert.assertTrue(api.isMonitorActive(FalxApi.MONITOR_NETWORK));
        Assert.assertEquals(api.monitors.size(), 2);


        api.removeAllMonitors();
        Assert.assertFalse(api.isMonitorActive(FalxApi.MONITOR_APP_STATE));
        Assert.assertFalse(api.isMonitorActive(FalxApi.MONITOR_NETWORK));
        Assert.assertEquals(api.monitors.size(), 0);
    }

    @Test
    public void testEnableLogging() {
        TestUtilComponent testUtilComponent = DaggerTestUtilComponent.builder()
                .appModule(new AppModule(mockContext))
                .fakeDateTimeModule(new FakeDateTimeModule())
                .build();

        FalxApi api = new FalxApi(mockContext, testUtilComponent);
        Assert.assertNotNull(api);

        api.enableLogging(true);
        FalxInterceptor interceptor = api.getInterceptor();
        Assert.assertFalse(interceptor.logger.isEnabled());

        api.enableLogging(true);
        Assert.assertTrue(interceptor.logger.isEnabled());
    }
}
