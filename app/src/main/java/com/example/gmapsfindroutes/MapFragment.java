package com.example.gmapsfindroutes;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MapFragment extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener, RoutingListener {

    //google map object
    private GoogleMap mMap;

    //current and destination location objects
    Location myLocation = null;
    Location destinationLocation = null;
    protected LatLng start;
    protected LatLng end;

    List<LatLng> waypoints = new ArrayList<>();
    List<Waypoint> waypointsList = new ArrayList<>();

    Waypoint wb = new Waypoint();

    //to get location permissions.
    private final static int LOCATION_REQUEST_CODE = 23;
    boolean locationPermission = false;

    //polyline object
    private List<Polyline> polylines = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_map);



        //request location permission.
        requestPermision();

        //init google map fragment to show map.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    private void requestPermision() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_REQUEST_CODE);
        } else {
            locationPermission = true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case LOCATION_REQUEST_CODE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //if permission granted.
                    locationPermission = true;
                    getMyLocation();

                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
        }
    }

    //to get user location
    private void getMyLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        //search location
        // Get references to your start and end location EditText views
        // Convert the start and end location strings to LatLng objects
        LatLng startLatLng = getLocationFromAddress("Alappuzha");
        LatLng endLatLng = getLocationFromAddress("Thrissur");
        start = startLatLng;
        end = endLatLng;

        // Define your waypoints
        List<LatLng> waypoints = new ArrayList<>();
        waypointsList.add(new Waypoint("cherthala",getLocationFromAddress("cherthala")));
        waypointsList.add(new Waypoint("ezhuppunna",getLocationFromAddress("ezhuppunna")));
        waypointsList.add(new Waypoint("thoppumpadi",getLocationFromAddress("thoppumpadi")));
//                waypoints.add(startLatLng);
        waypoints.add(getLocationFromAddress("cherthala"));
        waypoints.add(getLocationFromAddress("ezhuppunna"));
        waypoints.add(getLocationFromAddress("thoppumpadi"));
//                waypoints.add(endLatLng);

        // Call Findroutes with the start and end locations

        Findroutes(startLatLng, endLatLng,waypoints);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if(locationPermission) {
            getMyLocation();
        }

    }


    // function to find Routes.
    public void Findroutes(LatLng Start, LatLng End,List<LatLng> waypoints)
    {
        if(Start==null || End==null) {
            start = Start;
            end = End;
            Toast.makeText(MapFragment.this,"Unable to get location", Toast.LENGTH_LONG).show();
        }
        else
        {
            // Add Marker on start location
            MarkerOptions startMarker = new MarkerOptions();
            startMarker.position(Start);
            startMarker.title("Start Location");
            mMap.addMarker(startMarker);

            // Add Marker on end location
            MarkerOptions endMarker = new MarkerOptions();
            endMarker.position(End);
            endMarker.title("End Location");
            mMap.addMarker(endMarker);

            // Add Marker for waypoints
            int i =0;
//            waypoints.clear();


            for (LatLng waypoint : waypoints) {
                MarkerOptions waypointMarker = new MarkerOptions();
                waypointMarker.position(waypoint);
                waypointMarker.title(waypointsList.get(i).getName(waypoint));
                mMap.addMarker(waypointMarker);
                i++;
            }

            waypoints.add(0,Start);
            waypoints.add(End);

            Routing.Builder routingBuilder = new Routing.Builder()
                    .travelMode(AbstractRouting.TravelMode.DRIVING)
                    .withListener(this)
                    .alternativeRoutes(false)
                    .waypoints(waypoints.toArray(new LatLng[0]))
                    .key("API_KEY");  //also define your api key here.
            if (waypoints != null && !waypoints.isEmpty()) {
                routingBuilder.waypoints(waypoints);
            }
            Routing routing =routingBuilder.build();
            routing.execute();
        }
    }

    //Routing call back functions.
    @Override
    public void onRoutingFailure(RouteException e) {
        View parentLayout = findViewById(android.R.id.content);
        Snackbar snackbar= Snackbar.make(parentLayout, e.toString(), Snackbar.LENGTH_LONG);
        snackbar.show();
        Findroutes(start,end,waypoints);
    }

    @Override
    public void onRoutingStart() {
        Toast.makeText(MapFragment.this,"Finding Route...",Toast.LENGTH_LONG).show();
    }

    //If Route finding success..
    @Override
    public void onRoutingSuccess(ArrayList<Route> route, int shortestRouteIndex) {
        polylines = new ArrayList<>();



        // Add route(s) to the map using polyline
        for (int i = 0; i < route.size(); i++) {
            PolylineOptions polyOptions = new PolylineOptions();
            polyOptions.color(ContextCompat.getColor(this, R.color.purple_200));
            polyOptions.width(7);
            polyOptions.addAll(route.get(i).getPoints());
            Polyline polyline = mMap.addPolyline(polyOptions);
            polylines.add(polyline);
        }

        // Move camera to show the entire route and markers
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(start); // Include start marker
        builder.include(end); // Include end marker

        // Include waypoints if available
        for (LatLng waypoint : waypoints) {
            builder.include(waypoint);
        }

        LatLngBounds bounds = builder.build();
        int padding = 100; // Padding around the route (in pixels)
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, padding);
        mMap.animateCamera(cameraUpdate);
    }




    @Override
    public void onRoutingCancelled() {
        Findroutes(start, end,waypoints);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Findroutes(start,end,waypoints);

    }
    private LatLng getLocationFromAddress(String location) {
        Geocoder geocoder = new Geocoder(this);
        List<Address> addressList;
        LatLng latLng = null;

        try {
            addressList = geocoder.getFromLocationName(location, 1);
            if (addressList != null && !addressList.isEmpty()) {
                Address address = addressList.get(0);
                latLng = new LatLng(address.getLatitude(), address.getLongitude());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return latLng;
    }
}