package com.example.lostfound;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class GeocodingResponse {
    public List<Result> results;

    public static class Result {
        @SerializedName("formatted_address")
        public String formattedAddress;

        @SerializedName("geometry")
        public Geometry geometry;
    }

    public static class Geometry {
        @SerializedName("location")
        public Location location;
    }

    public static class Location {
        public double lat;
        public double lng;
    }
}
