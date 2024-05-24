package com.example.lostfound;

import java.util.List;

public class GeocodingResponse {
    public List<Result> results;

    public static class Result {
        public Geometry geometry;
        public String formattedAddress;

        public static class Geometry {
            public Location location;

            public static class Location {
                public double lat;
                public double lng;
            }
        }
    }
}
