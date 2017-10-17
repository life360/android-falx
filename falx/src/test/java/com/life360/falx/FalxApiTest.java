package com.life360.falx;

import android.content.Context;

import com.life360.falx.dagger.AppModule;
import com.life360.falx.dagger.DaggerTestUtilComponent;
import com.life360.falx.dagger.FakeDateTimeModule;
import com.life360.falx.dagger.TestUtilComponent;

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

}
