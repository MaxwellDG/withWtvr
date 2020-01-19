package com.example.myapplication.the_near_me;

public class NearbyPlace {

    private String name;
    private String lat;
    private String lng;

    NearbyPlace(){

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    String getLat() {
        return lat;
    }

    void setLat(String lat) {
        this.lat = lat;
    }

    String getLng() {
        return lng;
    }

    void setLng(String lng) {
        this.lng = lng;
    }
}
