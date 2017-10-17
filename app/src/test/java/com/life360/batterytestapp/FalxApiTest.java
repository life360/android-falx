package com.life360.batterytestapp;

import android.content.Context;

import com.life360.falx.FalxApi;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

/**
 * Created by remon on 10/9/17.
 */

@RunWith(RobolectricTestRunner.class)
public class FalxApiTest {

    @Test
    @Config(shadows={ShadowFalxApi.class})
    public void testFalxApiInit() {
        final Context context = RuntimeEnvironment.application;
        FalxApi.getInstance(context).addMonitors(0);
        Assert.assertNull(FalxApi.getInstance(context).aggregateEvents("none"));
    }
}
