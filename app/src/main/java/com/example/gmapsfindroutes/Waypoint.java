package com.example.gmapsfindroutes;

import com.google.android.gms.maps.model.LatLng;

// Define a Waypoint class to hold the location name and LatLng
public class Waypoint {
    private String name;
    private LatLng latLng;

    public Waypoint(String name, LatLng latLng) {
        this.name = name;
        this.latLng = latLng;
    }
    public Waypoint() {

    }

    public String getName(LatLng latLng) {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LatLng getLatLng() {
        return latLng;
    }

    public void setLatLng(LatLng latLng) {
        this.latLng = latLng;
    }
}

