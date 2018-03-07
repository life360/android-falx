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
import android.support.annotation.NonNull;

import com.life360.falx.FalxApi;
import com.life360.falx.dagger.UtilComponent;
import com.life360.falx.monitor_store.AggregatedFalxMonitorEvent;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

import java.util.List;

/**
 * Created by remon on 10/9/17.
 */
@Implements(FalxApi.class)
public class ShadowFalxApi {

    private Context context;
    private static FalxApi falxApi;

    public void __constructor__(@NonNull Context context) {
        this.context = context;
    }

    @Implementation
    private void init(Context context, UtilComponent utilComponent) {
        // do nothing
    }

    @Implementation
    public void addMonitors(int monitorFlags) {
        // do nothing
    }

    @Implementation
    public List<AggregatedFalxMonitorEvent> aggregateEvents(String eventName) {
        return null;
    }

    @Implementation
    public List<AggregatedFalxMonitorEvent> aggregatedEvents(String eventName, boolean allowPartialDays) {
        return null;
    }

    @Implementation
    public List<AggregatedFalxMonitorEvent> allAggregatedEvents(boolean allowPartialDays) {
        return null;
    }
}
