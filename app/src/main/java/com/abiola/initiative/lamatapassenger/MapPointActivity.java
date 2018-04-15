package com.abiola.initiative.lamatapassenger;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.abiola.initiative.lamatapassenger.libraries.DialogCreator;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBufferResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.List;

public class MapPointActivity extends AppCompatActivity implements
        OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener {

    private GoogleMap mMap;
    private SupportMapFragment mapFragment;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private GeoDataClient mGeoDataClient;

    private boolean hasPickup = false;
    private boolean hasDropoff = false;
    private boolean hasDestination = false;
    private int from = -1;
    private String pickupPlaceId = "";
    private String dropoffPlaceId = "";
    private String destinationPlaceId = "";
    private LatLng pickupLatLng;
    private LatLng dropoffLatLng;
    private LatLng destinationLatLng;
    private String pickupName;
    private String dropoffName;
    private String destinationName;
    private Marker pickupMarker;
    private Marker dropoffMarker;
    private Marker destinationMarker;
    private Address bestMatch;
    private LatLng pointLatLng;

    private Button set_button;
    private ImageButton point_img_btn;
    private ImageButton my_location_button;
    private ImageButton zoom_out_button;
    private CardView point_layout;
    private TextView point_txt_vw;

    private String TAG = "MAP_POINT_ACTIVITY_LAMATA";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_point);
        from = getIntent().getIntExtra("from", -1);

        mGeoDataClient = Places.getGeoDataClient(this, null);
        if(getIntent().getStringExtra("destinationPlaceId") != null) {
            hasDestination = true;
            destinationPlaceId = getIntent().getStringExtra("destinationPlaceId");
        } else if(getIntent().getStringExtra("destinationName") != null) {
            hasDestination = true;
            destinationName = getIntent().getStringExtra("destinationName");
            destinationLatLng = new LatLng(getIntent().getDoubleExtra("destinationLat", 0), getIntent().getDoubleExtra("destinationLng", 0));
        } else {
            if(getIntent().getStringExtra("pickupPlaceId") != null) {
                hasPickup = true;
                pickupPlaceId = getIntent().getStringExtra("pickupPlaceId");
            }
            if(getIntent().getStringExtra("dropoffPlaceId") != null) {
                hasDropoff = true;
                dropoffPlaceId = getIntent().getStringExtra("dropoffPlaceId");
            }
            if(getIntent().getStringExtra("pickupName") != null) {
                hasPickup = true;
                pickupName = getIntent().getStringExtra("pickupName");
                pickupLatLng = new LatLng(getIntent().getDoubleExtra("pickupLat", 0), getIntent().getDoubleExtra("pickupLng", 0));
            }

            if(getIntent().getStringExtra("dropoffName") != null) {
                hasDropoff = true;
                dropoffName = getIntent().getStringExtra("dropoffName");
                dropoffLatLng = new LatLng(getIntent().getDoubleExtra("dropoffLat", 0), getIntent().getDoubleExtra("dropoffLng", 0));
            }
        }

        set_button = findViewById(R.id.set_button);
        zoom_out_button = findViewById(R.id.zoom_out_button);
        my_location_button = findViewById(R.id.my_location_button);
        point_txt_vw = findViewById(R.id.point_txt_vw);
        point_img_btn = findViewById(R.id.point_img_btn);
        point_txt_vw.setSelected(true);
        point_layout = findViewById(R.id.point_layout);
        if(from == -1) {
            set_button.setText("SET AS RESERVE POINT");
            setHint(point_txt_vw,"Your Destination");
            point_img_btn.setImageDrawable(getResources().getDrawable(R.drawable.ic_pin_yellow_24dp));
        } else {
            set_button.setText(from == 0 ? "SET AS PICKUP POINT" : "SET AS DROP-OFF POINT");
            setHint(point_txt_vw, from == 0 ? "Your Pickup" : "Your Destination");
            point_img_btn.setImageDrawable(getResources().getDrawable(from == 0 ? R.drawable.ic_pin_yellow_24dp : R.drawable.ic_places_black_24dp));
        }
        set_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendPoint();
            }
        });

        zoom_out_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setMarkers("");
            }
        });

        my_location_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CameraPosition cameraPosition;
                if(mLastLocation != null)
                    cameraPosition = new CameraPosition.Builder()
                            .target(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()))
                            .zoom(14)
                            .bearing(0)
                            .build();
                else
                    cameraPosition = new CameraPosition.Builder()
                            .target(pickupLatLng)
                            .zoom(14)
                            .bearing(0)
                            .build();
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            }
        });

        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    private void sendPoint() {
        Intent intent = new Intent(MapPointActivity.this, LandingActivity.class);
        if(hasDestination) {
            intent.putExtra("destinationName", bestMatch.getAddressLine(0));
            intent.putExtra("destinationLatLng", pointLatLng.latitude + "," + pointLatLng.longitude);
            intent.putExtra("destinationLat", pointLatLng.latitude);
            intent.putExtra("destinationLng", pointLatLng.longitude);
            intent.putExtra("hasDestination", 1);
        } else {
            if(hasPickup)
                intent.putExtra("hasPickup", 1);
            if(hasDropoff)
                intent.putExtra("hasDropoff", 1);
            if(bestMatch != null) {
                if(from == 0) {
                    intent.putExtra("pickupName", bestMatch.getAddressLine(0));
                    intent.putExtra("pickupLatLng", pointLatLng.latitude + "," + pointLatLng.longitude);
                    intent.putExtra("pickupLat", pointLatLng.latitude);
                    intent.putExtra("pickupLng", pointLatLng.longitude);

                    intent.putExtra("hasPickup", 1);
                    if(getIntent().getStringExtra("dropoffPlaceId") != null) {
                        intent.putExtra("dropoffPlaceId", dropoffPlaceId);
                    }
                } else {
                    intent.putExtra("dropoffName", bestMatch.getAddressLine(0));
                    intent.putExtra("dropoffLatLng", pointLatLng.latitude + "," + pointLatLng.longitude);
                    intent.putExtra("dropoffLat", pointLatLng.latitude);
                    intent.putExtra("dropoffLng", pointLatLng.longitude);
                    intent.putExtra("hasDropoff", 1);
                    if(getIntent().getStringExtra("pickupPlaceId") != null) {
                        intent.putExtra("pickupPlaceId", pickupPlaceId);
                    }
                }

            } else {
                if(hasPickup) {
                    if(getIntent().getStringExtra("pickupName") != null) {
                        intent.putExtra("pickupName", pickupName);
                        intent.putExtra("pickupLatLng", pickupLatLng.latitude + "," + pickupLatLng.longitude);
                        intent.putExtra("pickupLat", pickupLatLng.latitude);
                        intent.putExtra("pickupLng", pickupLatLng.longitude);
                    } else {
                        intent.putExtra("pickupPlaceId", pickupPlaceId);
                    }
                    intent.putExtra("hasPickup", 1);
                }

                if(hasDropoff) {
                    if(getIntent().getStringExtra("dropoffName") != null) {
                        intent.putExtra("dropoffName", dropoffName);
                        intent.putExtra("dropoffLatLng", dropoffLatLng.latitude + "," + dropoffLatLng.longitude);
                        intent.putExtra("dropoffLat", dropoffLatLng.latitude);
                        intent.putExtra("dropoffLng", dropoffLatLng.longitude);
                    } else {
                        intent.putExtra("dropoffPlaceId", dropoffPlaceId);
                    }
                    intent.putExtra("hasDropoff", 1);
                }
            }
        }
        startActivity(intent);
        finish();
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    private void setText(TextView txtVw, String str) {
        txtVw.setText(str);
        txtVw.setTextColor(getResources().getColor(R.color.place_autocomplete_search_text));
    }

    private void setHint(TextView txtVw, String str) {
        txtVw.setText(str);
        txtVw.setTextColor(getResources().getColor(R.color.place_autocomplete_search_hint));
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(5000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            if (ActivityCompat.checkSelfPermission(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this,
                            android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                DialogCreator.create(this, "locationPermission")
                        .setMessage("We need to access your location and device state to continue using LÁMÁTÁ.")
                        .setPositiveButton("OK")
                        .show();
                return;
            }
        mMap.setMyLocationEnabled(true);
        LocationServices.FusedLocationApi
                .requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) { }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) { }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        try {
            googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            this, R.raw.mapstyle));
        } catch (Resources.NotFoundException e) { }
        mMap = googleMap;
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.getUiSettings().setZoomControlsEnabled(false);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        mMap.setPadding(0,0, 0, point_layout.getLayoutParams().height);
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                pointLatLng = latLng;
                switch (from) {
                    case 0:
                        if(pickupMarker != null)
                            pickupMarker.remove();

                        pickupMarker = mMap.addMarker(new MarkerOptions()
                                .position(latLng)
                                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.yellow_marker)));
                        break;
                    case 1:
                        if(dropoffMarker != null)
                            dropoffMarker.remove();

                        dropoffMarker = mMap.addMarker(new MarkerOptions()
                                .position(latLng)
                                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.black_marker)));
                        break;
                    case -1:
                        if(destinationMarker != null)
                            destinationMarker.remove();

                        destinationMarker = mMap.addMarker(new MarkerOptions()
                                .position(latLng)
                                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.yellow_marker)));
                        break;
                }
                point_txt_vw.setText("Loading Address...");
                findPlaceByLatLng(latLng);
            }
        });
        if(getIntent().getStringExtra("pickupPlaceId") != null)
            findPlaceById(pickupPlaceId, "pickup");

        if(getIntent().getStringExtra("dropoffPlaceId") != null)
            findPlaceById(dropoffPlaceId, "dropoff");

        if(getIntent().getStringExtra("destinationPlaceId") != null)
            findPlaceById(destinationPlaceId, "destination");

        buildGoogleApiClient();
    }

    private void findPlaceByLatLng(LatLng latLng) {
        Geocoder geoCoder = new Geocoder(this);
        List<Address> matches;
        try {
            matches = geoCoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            bestMatch = (matches.isEmpty() ? null : matches.get(0));
            setText(point_txt_vw, bestMatch == null ? "No Place Found!" : bestMatch.getAddressLine(0));
        } catch (IOException e) { }
    }

    private void findPlaceById(String placeId, final String marker) {
        mGeoDataClient.getPlaceById(placeId).addOnCompleteListener(new OnCompleteListener<PlaceBufferResponse>() {
            @Override
            public void onComplete(@NonNull Task<PlaceBufferResponse> task) {
                if (task.isSuccessful()) {
                    PlaceBufferResponse places = task.getResult();
                    Place myPlace = places.get(0);
                    switch (marker) {
                        case "pickup":
                            pickupLatLng = myPlace.getLatLng();
                            pickupName = myPlace.getName().toString();
                            break;
                        case "dropoff":
                            dropoffLatLng = myPlace.getLatLng();
                            dropoffName = myPlace.getName().toString();
                            break;
                        case "destination":
                            destinationLatLng = myPlace.getLatLng();
                            destinationName = myPlace.getName().toString();
                            setText(point_txt_vw, destinationName);
                            break;
                    }
                    places.release();
                    setMarkers(marker);
                }
            }
        });
    }

    private void setMarkers(String marker) {
        if(marker.equals("destination")) {
            if(destinationMarker != null)
                mMap.clear();

            if(destinationMarker == null)
                destinationMarker = mMap.addMarker(new MarkerOptions()
                        .position(destinationLatLng)
                        .icon(BitmapDescriptorFactory.fromResource(R.mipmap.yellow_marker)));

            mMap.animateCamera(CameraUpdateFactory
                    .newCameraPosition(new CameraPosition.Builder()
                            .target(destinationLatLng)
                            .zoom(14)
                            .bearing(0)
                            .build()));
        } else {
            if(pickupMarker != null && dropoffMarker != null) {
                mMap.clear();
            }
            if(pickupMarker == null && pickupLatLng != null) {
                pickupMarker = mMap.addMarker(new MarkerOptions()
                        .position(pickupLatLng)
                        .icon(BitmapDescriptorFactory.fromResource(R.mipmap.yellow_marker)));
            }

            if(dropoffMarker == null && dropoffLatLng != null) {
                dropoffMarker = mMap.addMarker(new MarkerOptions()
                        .position(dropoffLatLng)
                        .icon(BitmapDescriptorFactory.fromResource(R.mipmap.black_marker)));
            }

            if(dropoffLatLng != null && pickupLatLng != null) {
                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                builder.include(pickupLatLng);
                builder.include(dropoffLatLng);
                LatLngBounds bounds = builder.build();
                mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 200));
            } else {
                mMap.animateCamera(CameraUpdateFactory
                        .newCameraPosition(new CameraPosition.Builder()
                                .target(pickupLatLng)
                                .zoom(14)
                                .bearing(0)
                                .build()));
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(MapPointActivity.this, LandingActivity.class);
        if(hasPickup) {
            intent.putExtra("hasPickup", 1);
            if(getIntent().getStringExtra("pickupName") != null) {
                intent.putExtra("pickupName", getIntent().getStringExtra("pickupName"));
                intent.putExtra("pickupLatLng", getIntent().getStringExtra("pickupLatLng"));
                intent.putExtra("pickupLat", getIntent().getDoubleExtra("pickupLat", 0));
                intent.putExtra("pickupLng", getIntent().getDoubleExtra("pickupLng", 0));
            } else {
                intent.putExtra("pickupPlaceId", pickupPlaceId);
            }
        }
        if(hasDropoff) {
            intent.putExtra("hasDropoff", 1);
            if(getIntent().getStringExtra("dropoffName") != null) {
                intent.putExtra("dropoffName", getIntent().getStringExtra("dropoffName"));
                intent.putExtra("dropoffLatLng", getIntent().getStringExtra("dropoffLatLng"));
                intent.putExtra("dropoffLat", getIntent().getDoubleExtra("dropoffLat", 0));
                intent.putExtra("dropoffLng", getIntent().getDoubleExtra("dropoffLng", 0));
            } else {
                intent.putExtra("dropoffPlaceId", dropoffPlaceId);
            }
        }
        startActivity(intent);
        finish();
    }
}
