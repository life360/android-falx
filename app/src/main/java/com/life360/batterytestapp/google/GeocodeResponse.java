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
