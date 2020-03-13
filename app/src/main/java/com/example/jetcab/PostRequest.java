package com.example.jetcab;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class PostRequest extends AppCompatActivity {
    FusedLocationProviderClient fusedLocationProviderClient;
    TextInputLayout textInputLayout_from;
    TextInputEditText editText_from;
    TextInputLayout textInputLayout_to;
    TextInputEditText editText_to;
    Button post_button;
    Geocoder geocoder;
    String start_location, end_location;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.post_request);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        //get the current location latitude and longitude
        //get the current address
        editText_from = findViewById(R.id.from_editText);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        geocoder = new Geocoder(this, Locale.getDefault());
        //check whether the location permission is open
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            getLastLocation();
        } else {
            Toast.makeText(getApplicationContext(), "Turn on the Location Permission", Toast.LENGTH_LONG).show();
        }
        //click the map icon to specify start location on map
        textInputLayout_from = findViewById(R.id.from_textField);
        textInputLayout_from.setEndIconOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                start_location = editText_from.getText().toString();
                if (start_location.matches("")) {
                    Toast.makeText(getApplicationContext(), "Please Enter the Start Location", Toast.LENGTH_LONG).show();
                } else {
                    if (isAddressValid(start_location)) {
                        Intent intent = new Intent(v.getContext(), MapDisplay.class);
                        intent.putExtra("START LOCATION", start_location);
                        intent.putExtra("TYPE", "from");
                        startActivity(intent);
                    } else {
                        Toast.makeText(getApplicationContext(), "Invalid Start Location Address", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
        //click the map icon to specify end location on map
        editText_to = findViewById(R.id.to_editText);
        textInputLayout_to = findViewById(R.id.to_textField);
        textInputLayout_to.setEndIconOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                end_location = editText_to.getText().toString();
                if (end_location.matches("")) {
                    Toast.makeText(getApplicationContext(), "Please Enter the End Location", Toast.LENGTH_LONG).show();
                } else {
                    if (isAddressValid(end_location)) {
                        Intent intent = new Intent(v.getContext(), MapDisplay.class);
                        intent.putExtra("END LOCATION", end_location);
                        intent.putExtra("TYPE", "to");
                        startActivity(intent);
                    } else {
                        Toast.makeText(getApplicationContext(), "Invalid End Location Address", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
        //post and save the information of ride, end the activity
        post_button = findViewById(R.id.post_button);
        post_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // check whether the input address is valid
                if (isAddressValid(editText_from.getText().toString()) && !start_location.matches("") && !end_location.matches("")) {
                    //...
                    finish();
                } else {
                    Toast.makeText(getApplicationContext(), "Invalid Start Location Address", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void getLastLocation() {
        fusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                //get the latitude and longitude of current location
                Location location = task.getResult();
                if (location != null) {
                    String address = null;
                    Double curr_latitude, curr_longitude;
                    curr_latitude = location.getLatitude();
                    curr_longitude = location.getLongitude();

                    //get the address of current location
                    try {
                        List<Address> addresses = geocoder.getFromLocation(curr_latitude, curr_longitude, 1);
                        address = addresses.get(0).getAddressLine(0);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    editText_from.setText(address);
                } else {
                    Toast.makeText(getApplicationContext(), "Turn on the Location Permission", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public boolean isAddressValid(String location) {
        Geocoder coder = new Geocoder(getApplicationContext(), Locale.getDefault());
        try {
            List<Address> addressList = coder.getFromLocationName(location, 1);
            if (addressList == null && addressList.size() < 1) {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }
}
