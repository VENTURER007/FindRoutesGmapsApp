package com.example.gmapsfindroutes;


import com.google.android.gms.maps.model.LatLng;

import java.util.List;

public class RouteData {
    private LatLng start;

    public LatLng getStart() {
        return start;
    }

    public LatLng getEnd() {
        return end;
    }

    public void setStart(LatLng start) {
        this.start = start;
    }

    public void setEnd(LatLng end) {
        this.end = end;
    }

    public void setWaypoints(List<LatLng> waypoints) {
        this.waypoints = waypoints;
    }

    public List<LatLng> getWaypoints() {
        return waypoints;
    }

    private LatLng end;
    private List<LatLng> waypoints;
}



