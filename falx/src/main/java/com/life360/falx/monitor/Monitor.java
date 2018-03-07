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

import com.life360.falx.model.FalxMonitorEvent;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;

/**
 * Created by remon on 7/19/17.
 */

public class Monitor {

    /**
     * Stops the Monitor from observing and collecting data.
     */
    public void stop() {}

    /**
     * Get a observable where the monitor can publish events.
     */
    public Observable<FalxMonitorEvent> getEventObservable() {
        return eventPublishSubject;
    }

    protected PublishSubject<FalxMonitorEvent> eventPublishSubject = PublishSubject.create();
}
