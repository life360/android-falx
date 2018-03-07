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

import io.realm.RealmObject;

/**
 * Store a key-value pair where the Key is a string and the value is a floating point.
 * <p>
 * Created by Vikas on 9/21/17.
 */

public class EventArgument extends RealmObject {
    private String key;
    private Double value;

    public EventArgument() {
    }

    public EventArgument(String key, Double value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "EventArgument{" +
                "key='" + key + '\'' +
                ", value=" + value +
                '}';
    }
}
