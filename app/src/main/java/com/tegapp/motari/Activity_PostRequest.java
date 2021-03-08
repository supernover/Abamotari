package com.tegapp.motari;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * @author Joyce, ...(who did the fare part!!!)
 * enable posting request for riders
 */
public class Activity_PostRequest extends AppCompatActivity  {
    private FusedLocationProviderClient fusedLocationProviderClient;
    private TextInputLayout textInputLayout_from;
    private Fragment editText_from;
    private TextInputLayout textInputLayout_to;
    private Fragment editText_to;
    private Geocoder geocoder;
    private String start_location, end_location;
    private TextView fare_estimate;
    private EditText fare;
    private float final_fare;
    private Button post_button;
    private ImageButton decrease_fare;
    private ImageButton increase_fare;
    private static Bundle coords_bun;
    GoogleMap mMap;



    private ImageView mGps,mPicker;

    private Boolean mLocationPermissionsGranted = false;







    ImageButton mapIcon;


    String TAG = "Sample";
    private LatLng srcLagLng;
    private LatLng destLagLng;
//    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.post_request);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if(!Places.isInitialized()){
            Places.initialize(getApplicationContext(),"AIzaSyAi2HCUbkEb3CMKN3ndwnkFWGZJ11gWXWU");
        }

        //get the current location latitude and longitude and current address
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

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                mMap = googleMap;

                if (ActivityCompat.checkSelfPermission(Activity_PostRequest.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(Activity_PostRequest.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                mMap.setMyLocationEnabled(true);

                mMap.getUiSettings().setMyLocationButtonEnabled(false);
            }
        });

        //textInputLayout_from = findViewById(R.id.from_textField);
        //editText_from = findViewById(R.id.from_editText);
        fare_estimate = findViewById(R.id.fair_fare_text);
        fare = findViewById(R.id.offered_payment_text);

        mGps = findViewById(R.id.ic_gps);
        //mPicker = findViewById(R.id.ic_picker);
        mGps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShowLocation();
            }
        });



        //click the map icon to specify start location on map
        AutocompleteSupportFragment autocompleteFragment2 = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.from_editText);
        autocompleteFragment2.setHint("Where are you?");
        // Specify the types of place data to return.
        assert autocompleteFragment2 != null;
        autocompleteFragment2.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME));

        // Set up a PlaceSelectionListener to handle the response.
        autocompleteFragment2.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                // Get info about the selected place.
                // destinationText = place.getAddress();
                start_location = place.getName();

                //geoLocate(start_location,"src");
                Log.i(TAG, "Place: " + place.getName() + ", " + place.getId());
                //update fair fare
                getFare(start_location, end_location);

            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error
                Log.i(TAG, "An error occurred: " + status);
            }
        });

        // Initialize the AutocompleteSupportFragment.
        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.to_editText);
        autocompleteFragment.setHint("Where are you going?");
        // Specify the types of place data to return.
        assert autocompleteFragment != null;
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME));

        // Set up a PlaceSelectionListener to handle the response.
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                // Get info about the selected place.
                //destinationText = place.getAddress();
                end_location = place.getName();
                //update fair fare
                getFare(start_location, end_location);

                //LatLng address = place.getLatLng();
                //geoLocate(end_location,"dest");
                Log.i(TAG, "Place: " + place.getName() + ", " + place.getId());

            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i(TAG, "An error occurred: " + status);
            }
        });



        //click the map icon to show the start and end location on the map





        /*textInputLayout_from.setEndIconOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                start_location = editText_from.getText().toString();
                if (start_location.matches("")) {

                    Toast.makeText(getApplicationContext(), "Please Enter the Start Location", Toast.LENGTH_LONG).show();
                } else {
                    if (isAddressValid(start_location)) {
                        Intent intent = new Intent(v.getContext(), Activity_RiderMapDisplay.class);
                        intent.putExtra("START LOCATION", start_location);
                        intent.putExtra("TYPE", "from");
                        //get the lat and lng from MapDisplay.class
                        startActivity(intent);
                        //update fare fair
                        getFare(start_location, end_location);
                    } else {
                        Toast.makeText(getApplicationContext(), "Invalid Start Location Address", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });*/

        //textInputLayout_to = findViewById(R.id.to_textField);
        //editText_to = findViewById(R.id.to_editText);

        //click the map icon to specify end location on map
        /*textInputLayout_to.setEndIconOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                end_location = editText_to.getText().toString();
                if (end_location.matches("")) {
                    Toast.makeText(getApplicationContext(), "Please Enter the End Location", Toast.LENGTH_LONG).show();
                } else {
                    if (isAddressValid(end_location)) {
                        Intent intent = new Intent(v.getContext(), Activity_RiderMapDisplay.class);
                        intent.putExtra("END LOCATION", end_location);
                        intent.putExtra("TYPE", "to");
                        startActivity(intent);
                        //update fair fare
                        getFare(start_location, end_location);

                    } else {
                        Toast.makeText(getApplicationContext(), "Invalid End Location Address", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });*/



        decrease_fare = findViewById(R.id.minus_payment_button);
        increase_fare = findViewById(R.id.add_payment_button);

        //edit amount of money
        decrease_fare.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("DefaultLocale")
            @Override
            public void onClick(View v) {
                final_fare = Float.parseFloat(fare.getText().toString());
                final_fare -= 1.00;
                fare.setText(String.format("%.2f", final_fare));
            }
        });
        increase_fare.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("DefaultLocale")
            @Override
            public void onClick(View v) {
                final_fare = Float.parseFloat(fare.getText().toString());
                final_fare += 1.00;
                fare.setText(String.format("%.2f", final_fare));
            }
        });




        //post and save the information of ride, end the activity
        post_button = findViewById(R.id.post_button);
        post_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // check whether the input address is valid


                    LatLng pickup = new LatLng(getLat(start_location), getLng(start_location));
                    LatLng dropoff = new LatLng(getLat(end_location), getLng(end_location));

                    final_fare = Float.parseFloat(fare.getText().toString());
                    Log.d("LASTFARE", "last final fare: " + fare.getText().toString());
                    Activity_Request post = new Activity_Request(pickup, dropoff, final_fare);
                    finish();

                    Intent current_request_intent = new Intent(v.getContext(), CurrentRequest.class); //opens current request activity
                    Bundle coords = new Bundle();
                    coords.putParcelable("PICKUP", pickup);   //https://stackoverflow.com/questions/16134682/how-to-send-a-latlng-instance-to-new-intent
                    coords.putParcelable("DROPOFF", dropoff);
                    current_request_intent.putExtra("COORDS", coords);
                    coords_bun = coords;
                    startActivity(current_request_intent);
                }

        });

        ShowLocation();

    }

    /**
     * Returns the amount of the fare based on a fair calculation.
     * Fares calculated by base amount + price per KM.
     * @param start string address of starting/pickup location
     * @param end string address of end/dropoff location
     */
    @SuppressLint("DefaultLocale")
    private void getFare(String start, String end) {
        DecimalFormat rounded = new DecimalFormat("0.00");
        float[] distance = new float[1];
        Double startLat = getLat(start);
        Double startLng = getLng(start);
        Double endLat = getLat(end);
        Double endLng = getLng(end);
        double perKM = 300.00;
        double base = 5.00;
        double amount = 0;

        if (!(startLat == null) & !(startLng == null) & !(endLat == null) & !(endLng == null)) { //calculates the fare
            Location.distanceBetween(startLat, startLng, endLat, endLng, distance);
            Log.d("distance", "Distance: " + (distance[0]));
            amount = base + perKM * distance[0] / 1000;
        }

        String strAmount = String.format("%.2f", amount);
        fare_estimate.setText(strAmount);
        fare.setText(strAmount);
        this.final_fare = Float.parseFloat(strAmount);
    }

    /**
     * get the address of current location
     */
    private void getLastLocation() {
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
                    //editText_from.setText(address);
                } else {
                    Toast.makeText(getApplicationContext(), "Turn on the Location Permission", Toast.LENGTH_LONG).show();
                }
            }
        });
    }





    /**
     * check if the location is valid
     * @param location
     * @return true/false
     */
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

    /**
     * get the latitude of location
     * @param location
     * @return latitude
     */
    public Double getLat(String location) {
        Address address;
        Double lat = null;
        try {
            List<Address> addresses = geocoder.getFromLocationName(location, 5);
            if (addresses != null && addresses.size() > 0) {
                address = addresses.get(0);
                lat = address.getLatitude();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lat;
    }

    /**
     * get the longitude of location
     * @param location
     * @return longitude
     */
    public Double getLng(String location) {
        Address address;
        Double lng = null;
        try {
            List<Address> addresses = geocoder.getFromLocationName(location, 5);
            if (addresses != null && addresses.size() > 0) {
                address = addresses.get(0);
                lng = address.getLongitude();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lng;
    }
    private void ShowLocation() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(Activity_PostRequest.this);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                if (ActivityCompat.checkSelfPermission(Activity_PostRequest.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(Activity_PostRequest.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 13));

                            CameraPosition cameraPosition = new CameraPosition.Builder()
                                    .target(new LatLng(location.getLatitude(), location.getLongitude()))      // Sets the center of the map to location user
                                    .zoom(17)                   // Sets the zoom
                                    .bearing(90)                // Sets the orientation of the camera to east
                                    .tilt(40)                   // Sets the tilt of the camera to 30 degrees
                                    .build();                   // Creates a CameraPosition from the builder
                            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

                        } else {

                        }
                    }
                });
            }
        }, 1500);
    }



    /**
     * this return method is primarily for creating current request activity in Activity_MainMenuR.java
     * values within bundle are checked for coords; if coords exists, then create activity (in MainMenuR)
     * @return bundle of coordinates
     */

}


