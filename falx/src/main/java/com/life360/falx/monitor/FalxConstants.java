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

/**
 * Created by remon on 10/20/17.
 */

public class FalxConstants {

    // Event names
    public static final String EVENT_FOREGROUND = "foreground";
    public static final String EVENT_BACKGROUND = "background";
    public static final String EVENT_NETWORK = "falx-network";
    public static final String EVENT_REALTIME_MESSAGING = "falx-realtime-messaging";
    public static final String EVENT_REALTIME_MESSAGING_SESSION = "falx-realtime-messaging-session";

    // Event property names
    public static final String PROP_DURATION = "duration";
    public static final String PROP_BYTES_RECEIVED = "bytesReceived";
    public static final String PROP_COUNT = "count";
    public static final String PROP_PROTOCOL = "protocol";
}
