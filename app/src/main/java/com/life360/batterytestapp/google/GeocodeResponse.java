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

package com.life360.batterytestapp.google;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by remon on 9/19/17.
 */

public class GeocodeResponse {
    @SerializedName("status")
    public String status;

    @SerializedName("results")
    public List<Results> results;

    public static class Results {

        @SerializedName("address_components")
        public List<Results.AddressComponents> addressComponents;

        @SerializedName("formatted_address")
        public String formattedAddress;

        @SerializedName("geometry")
        public Results.Geometry geometry;

        @SerializedName("types")
        public List<String> types;

        @SerializedName("place_id")
        public String placeId;

        public static class AddressComponents {
            @SerializedName("long_name")
            public String longName;
            @SerializedName("short_name")
            public String shortName;
            @SerializedName("types")
            public List<String> types;
        }

        public static class Geometry {
            @SerializedName("location")
            public Results.Geometry.SimpleLocation location;

            @SerializedName("location_type")
            public String locationType;

            @SerializedName("viewport")
            public transient Object viewport;

            public static class SimpleLocation {
                @SerializedName("lat")
                public double lat;
                @SerializedName("lng")
                public double lng;
            }
        }
    }

}
