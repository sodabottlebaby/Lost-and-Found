package com.example.lostfound;

public class LostFoundItems {
    private int id;
    private String advertType;
    private String name;
    private String phone;
    private String description;
    private String date;
    private String location;
    double latitude;
    double longitude;

    //Constructor
    public LostFoundItems(int id, String advertType, String name, String phone, String description, String date, String location, double latitude, double longitude) {
        this.id = id;
        this.advertType = advertType;
        this.name = name;
        this.phone = phone;
        this.description = description;
        this.date = date;
        this.location = location;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    //Getters and setters
    public int getId() {
        return id;
    }
    public String getAdvertType() {
        return advertType;
    }
    public String getName() {
        return name;
    }
    public String getPhone() {
        return phone;
    }
    public String getDescription() {
        return description;
    }
    public String getDate() {
        return date;
    }
    public String getLocation() {
        return location;
    }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }

}
