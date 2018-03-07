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

package com.life360.falx.model;

import java.util.Date;
import java.util.Map;


/**
 * Created by Vikas on 9/19/17.
 */

public class FalxMonitorEvent {

    private String name;
    private Date timestamp;
    private Map<String, Double> arguments;
    /* whether this event is just an update to a previously logged event.
     * Used to update the wake lock actual duration when it was initially acquired for a max duration but got released.
     */
    private boolean updated;

    public FalxMonitorEvent(String name, Map<String, Double> arguments) {
        this.name = name;
        this.arguments = arguments;
        this.timestamp = new Date();
    }

    public String getName() {
        return name;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public Map<String, Double> getArguments() {
        return arguments;
    }

    public boolean isUpdate() {return updated;}

    public void updateArgs(Map<String, Double> arguments) {
        this.arguments = arguments;
        updated = true;
    }
}
