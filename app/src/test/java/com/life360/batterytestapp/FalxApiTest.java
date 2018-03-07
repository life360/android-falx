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
