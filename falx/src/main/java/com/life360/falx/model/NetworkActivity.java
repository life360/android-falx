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

/**
 * Created by remon on 9/19/17.
 */

public class NetworkActivity {
    int count;
    int bytesReceived;
    double responseDuration;
    String url;

    public NetworkActivity(int count, int bytesReceived, double responseDuration, String url) {
        this.count = count;
        this.bytesReceived = bytesReceived;
        this.responseDuration = responseDuration;
        this.url = url;
    }

    @Override
    public String toString() {
        return "NetworkActivity{" +
                "count=" + count +
                ", bytesReceived=" + bytesReceived +
                ", responseDuration=" + responseDuration +
                ", url='" + url + '\'' +
                '}';
    }

    public int getCount() {
        return count;
    }

    public int getBytesReceived() {
        return bytesReceived;
    }

    public double getResponseDuration() {
        return responseDuration;
    }

    public String getUrl() {
        return url;
    }
}
