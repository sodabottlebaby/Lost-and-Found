package com.example.lostfound;

public class ItemsPreview {
    private int id;
    private String type;
    private String name;
    private double latitude;
    private double longitude;
    private String location;

    // Constructor
    public ItemsPreview(int id, String advertType, String name, double latitude, double longitude, String location) {
        this.id = id;
        this.type = advertType;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.location = location;
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getAdvertType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getLocation() {
        return location;
    }

    // Setters
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}
