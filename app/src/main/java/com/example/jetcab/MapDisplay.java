package com.example.jetcab;

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;
import java.util.Locale;

public class MapDisplay extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    String start_location, end_location;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_display);
        MapDisplay.this.setTitle("Map View");

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker to start location
        Intent intent = getIntent();
        start_location = intent.getStringExtra("START LOCATION");
        getLatLng(start_location, getApplicationContext(), new StartGeocoderHandler());

        // Add a marker to to end location
        Intent intent1 = getIntent();
        end_location = intent1.getStringExtra("END LOCATION");
        getLatLng(end_location, getApplicationContext(), new EndGeocoderHandler());
    }

    public static void getLatLng(final String location, final Context context, final Handler handler) {
        Thread thread = new Thread() {
            @Override
            public void run() {
                Geocoder geocoder = new Geocoder(context, Locale.getDefault());
                Address address;
                Double lat = null;
                Double lng = null;
                try {
                    List<Address> addresses = geocoder.getFromLocationName(location, 5);
                    if (addresses != null && addresses.size() > 0) {
                        address = addresses.get(0);
                        lat = address.getLatitude();
                        lng = address.getLongitude();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    Message message = Message.obtain();
                    message.setTarget(handler);
                    if (lat != null && lng != null) {
                        Bundle bundle = new Bundle();
                        bundle.putDouble("LATITUDE", lat);
                        bundle.putDouble("LONGITUDE", lng);
                        message.setData(bundle);
                    }
                    message.sendToTarget();
                }
            }
        };
        thread.start();
    }

    private class StartGeocoderHandler extends Handler {
        public Double start_lat, start_lng;

        @Override
        public void handleMessage(Message message) {
            Bundle bundle = message.getData();
            start_lat = bundle.getDouble("LATITUDE");
            start_lng = bundle.getDouble("LONGITUDE");

            //add start marker
            if (start_lat != null && start_lng != null) {
                LatLng from = new LatLng(start_lat, start_lng);
                mMap.addMarker(new MarkerOptions().
                        position(from).title("Start Location")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET)));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(from));
            }
        }
    }

    private class EndGeocoderHandler extends Handler {
        public Double end_lat, end_lng;

        @Override
        public void handleMessage(Message message) {
            Bundle bundle = message.getData();
            end_lat = bundle.getDouble("LATITUDE");
            end_lng = bundle.getDouble("LONGITUDE");

            //add end marker
            if (end_lat != null && end_lng != null) {
                LatLng from = new LatLng(end_lat, end_lng);
                mMap.addMarker(new MarkerOptions()
                        .position(from).title("End Location")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(from));
            }
        }
    }

}