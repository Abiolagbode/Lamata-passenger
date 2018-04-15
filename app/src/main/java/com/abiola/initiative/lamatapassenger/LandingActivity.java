package com.abiola.initiative.lamatapassenger;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.facebook.AccessToken;
import com.facebook.login.LoginManager;
import com.abiola.initiative.lamatapassenger.adapter.HistoryAdapter;
import com.abiola.initiative.lamatapassenger.config.TaskConfig;
import com.abiola.initiative.lamatapassenger.libraries.DialogCreator;
import com.abiola.initiative.lamatapassenger.libraries.SimpleDividerItemDecoration;
import com.abiola.initiative.lamatapassenger.libraries.SnackBarCreator;
import com.abiola.initiative.lamatapassenger.object.HistoryObject;
import com.abiola.initiative.lamatapassenger.tasks.SuperTask;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBufferResponse;
import com.google.android.gms.location.places.PlaceDetectionClient;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBufferResponse;
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
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Map;

import me.grantland.widget.AutofitHelper;

public class LandingActivity extends AppCompatActivity
        implements DialogCreator.DialogActionListener,
        NavigationView.OnNavigationItemSelectedListener,
        OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener,
        HistoryAdapter.HistoryAdapterListener,
        SuperTask.TaskListener {

    private DrawerLayout drawer;
    private TextView name_header_txt_vw;
    private TextView email_header_txt_vw;
    private TextView pickup_txt_vw;
    private TextView dropoff_txt_vw;
    private TextView fare_details_txt_vw;
    private TextView distance_details_txt_vw;
    private TextView duration_details_txt_vw;
    private ImageView profile_pic_header_img_vw;
    private CardView booking_details_card_vw;
    private CardView fare_details_card_vw;
    private Button booking_btn;
    private ImageButton go_to_my_location;
    private ImageButton show_traffic;
    private LinearLayout pickup_layout;
    private LinearLayout dropoff_layout;
    private MenuItem clear_history;
    private GoogleMap mMap;
    private CameraPosition cameraPosition;
    private SupportMapFragment bookingmapFragment;
    private SupportMapFragment reservationmapFragment;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private GeoDataClient mGeoDataClient;
    private Location mLastLocation;
    private LatLng pickupLocation;
    private LatLng dropoffLocation;
    private LatLng destinationLocation;
    private HistoryAdapter historyAdapter;
    private DatabaseReference historyRef;
    private int hasPickup = -1;
    private int hasDropoff = -1;
    private int hasDestination;
    private int noDriver = -1;
    private boolean cameraUpdated = false;
    private boolean is_traffic_shown = false;
    private boolean from_update_activity = false;
    private boolean google_client_built = false;
    private String uid;
    private String destinationName;
    private String pickupName;
    private String dropoffName;
    private String destinationPlaceId;
    private String pickupPlaceId;
    private String dropoffPlaceId;
    private String user_name;
    private String user_mobnum;
    private String user_email;
    private String user_pic;
    private String user_trusted_id;
    private String user_trusted_name;
    private String user_database_id;
    private String taxi_fare = "0.00";
    private String duration = "0KM";
    private String distance = "0M";
    private ViewFlipper viewFlipper;
    private Toolbar toolbar;
    final int LOCATION_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing);

        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        final DatabaseReference bookRef = FirebaseDatabase.getInstance().getReference("services/booking/" + uid);
        bookRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean cancelled = false;
                String driver_id = "";
                for (DataSnapshot driverId : dataSnapshot.getChildren()) {
                    if (driverId.getKey().equals("accepted_by")) {
                        driver_id = driverId.getValue().toString();
                    }

                    if(driverId.getKey().equals("cancelled_by")) {
                        cancelled = true;
                        bookRef.removeValue();
                    }

                    if(driverId.getKey().equals("in_transit")) {
                        cancelled = false;
                    }

                    if(driver_id.isEmpty()) {
                        cancelled = true;
                    }
                }

                if(!cancelled && dataSnapshot.exists()) {
                    Intent intent = new Intent(LandingActivity.this, DriverAcceptedActivity.class);
                    intent.putExtra("driver_id", driver_id);
                    startActivity(intent);
                    finish();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) { }
        });

        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Booking");
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        final NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.getMenu().getItem(0).setChecked(true);
        View v = navigationView.getHeaderView(0);
        name_header_txt_vw = v.findViewById(R.id.txtVw_name);
        email_header_txt_vw = v.findViewById(R.id.email_txt_vw);
        profile_pic_header_img_vw = v.findViewById(R.id.imgVw_profile_pic);
        AutofitHelper.create(name_header_txt_vw);
        profile_pic_header_img_vw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setAccountProfile();
                drawer.closeDrawer(GravityCompat.START);
                drawer = findViewById(R.id.drawer_layout);
                navigationView.getMenu().getItem(3).setChecked(true);
            }
        });
        Glide.with(this)
                .load(getImage("placeholder"))
                .apply(RequestOptions.circleCropTransform())
                .into(profile_pic_header_img_vw);
        setDetails();

        mGeoDataClient = Places.getGeoDataClient(this, null);
        viewFlipper = findViewById(R.id.app_bar_include).findViewById(R.id.viewFlipper);
        booking_btn = findViewById(R.id.booking_btn);
        fare_details_card_vw = findViewById(R.id.fare_details_card_vw);
        booking_details_card_vw = findViewById(R.id.booking_details_card_vw);
        pickup_layout = findViewById(R.id.pickup_layout);
        dropoff_layout = findViewById(R.id.dropoff_layout);
        pickup_txt_vw = findViewById(R.id.pickup_txt_vw);
        dropoff_txt_vw = findViewById(R.id.dropoff_txt_vw);
        fare_details_txt_vw = findViewById(R.id.fare_details_txt_vw);
        distance_details_txt_vw = findViewById(R.id.distance_details_txt_vw);
        duration_details_txt_vw = findViewById(R.id.duration_details_txt_vw);
        go_to_my_location = findViewById(R.id.go_to_my_location);
        show_traffic = findViewById(R.id.show_traffic);
        pickup_txt_vw.setSelected(true);
        dropoff_txt_vw.setSelected(true);
        fare_details_txt_vw.setText(taxi_fare);
        distance_details_txt_vw.setText(distance);
        duration_details_txt_vw.setText(duration);

        if (!locationEnabled())
            DialogCreator.create(this, "requestLocation")
                    .setTitle("Access Location")
                    .setMessage("Turn on your location settings to be able to get location data.")
                    .setPositiveButton("Go to Settings")
                    .show();

        hasPickup = getIntent().getIntExtra("hasPickup", -1);
        hasDropoff = getIntent().getIntExtra("hasDropoff", -1);
        hasDestination = getIntent().getIntExtra("hasDestination", -1);
        noDriver = getIntent().getIntExtra("noDriver", -1);

        if (noDriver == 1) {
            DialogCreator.create(this, "noDriverFound")
                    .setTitle("No Driver Found")
                    .setMessage("Drivers are currently busy right now. Please try again later.")
                    .setPositiveButton("OK")
                    .show();
        }

        if (hasDestination == 1) {
            navigationView.getMenu().getItem(1).setChecked(true);
            setReservation();
        } else {
            bookingmapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);

            bookingmapFragment.getMapAsync(this);

            if (getIntent().getStringExtra("pickupName") != null) {
                setText(pickup_txt_vw, pickupName = getIntent().getStringExtra("pickupName"));
                pickupLocation = new LatLng(getIntent().getDoubleExtra("pickupLat", 0),
                        getIntent().getDoubleExtra("pickupLng", 0));
            } else {
                if (hasPickup == 1) {
                    pickupPlaceId = getIntent().getStringExtra("pickupPlaceId");
                    findPlaceById(getIntent().getStringExtra("pickupPlaceId"), 0);
                }
            }

            if (getIntent().getStringExtra("dropoffName") != null) {
                setText(dropoff_txt_vw, dropoffName = getIntent().getStringExtra("dropoffName"));
                dropoffLocation = new LatLng(getIntent().getDoubleExtra("dropoffLat", 0),
                        getIntent().getDoubleExtra("dropoffLng", 0));
                changeBookBtn();
            } else {
                if (hasDropoff == 1) {
                    cameraUpdated = true;
                    dropoffPlaceId = getIntent().getStringExtra("dropoffPlaceId");
                    findPlaceById(getIntent().getStringExtra("dropoffPlaceId"), 1);
                }
                changeBookBtn();
            }

            if (hasPickup == 1 && hasDropoff == 1) {
                if (getIntent().getStringExtra("pickupName") != null || getIntent().getStringExtra("dropoffName") != null) {
                    SuperTask.execute(this,
                            TaskConfig.CREATE_TAXI_FARE_URL,
                            "get_fare_map_point",
                            "Calculating Fare...");
                } else {
                    setFare();
                }
            }
        }

        pickup_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LandingActivity.this, SearchActivity.class);
                intent.putExtra("from", 0);
                if (pickupPlaceId != null) {
                    intent.putExtra("pickupPlaceId", pickupPlaceId);
                    intent.putExtra("hasPickup", 1);
                }

                if (dropoffPlaceId != null) {
                    intent.putExtra("dropoffPlaceId", dropoffPlaceId);
                    intent.putExtra("hasDropoff", 1);
                }

                if (getIntent().getStringExtra("pickupName") != null) {
                    intent.putExtra("pickupName", pickupName);
                    intent.putExtra("pickupLatLng", pickupLocation.latitude + "," + pickupLocation.longitude);
                    intent.putExtra("pickupLat", pickupLocation.latitude);
                    intent.putExtra("pickupLng", pickupLocation.longitude);
                    intent.putExtra("hasPickup", 1);
                }

                if (getIntent().getStringExtra("dropoffName") != null) {
                    intent.putExtra("dropoffName", dropoffName);
                    intent.putExtra("dropoffLatLng", dropoffLocation.latitude + "," + dropoffLocation.longitude);
                    intent.putExtra("dropoffLat", dropoffLocation.latitude);
                    intent.putExtra("dropoffLng", dropoffLocation.longitude);
                    intent.putExtra("hasDropoff", 1);
                }
                startActivity(intent);
                finish();
            }
        });

        dropoff_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setDropoff();
            }
        });

        go_to_my_location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (locationEnabled()) {
                    if (mLastLocation != null)
                        cameraPosition = new CameraPosition.Builder()
                                .target(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()))
                                .zoom(14)
                                .bearing(0)
                                .build();
                    else
                        cameraPosition = new CameraPosition.Builder()
                                .target(pickupLocation)
                                .zoom(14)
                                .bearing(0)
                                .build();
                    mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                }
            }
        });

        show_traffic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                is_traffic_shown = !is_traffic_shown;
                mMap.setTrafficEnabled(is_traffic_shown);
            }
        });
    }

    private void setReservation() {
        cameraUpdated = false;
        viewFlipper.setDisplayedChild(1);
        toolbar.setTitle("Reservation");
        final CardView reservation_details_card_vw = findViewById(R.id.reseravation_details_card_vw);
        final TextView destination_txt_vw = findViewById(R.id.destination_txt_vw);
        Button reserve_btn = findViewById(R.id.reservation_btn);
        LinearLayout destination_layout = findViewById(R.id.destination_layout);
        destination_txt_vw.setSelected(true);

        reservationmapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.reserve_map);

        reservationmapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                try {
                    googleMap.setMapStyle(
                            MapStyleOptions.loadRawResourceStyle(
                                    LandingActivity.this, R.raw.mapstyle));
                } catch (Resources.NotFoundException e) {
                }
                mMap = googleMap;
                mMap.getUiSettings().setMapToolbarEnabled(false);
                mMap.getUiSettings().setZoomControlsEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                mMap.setPadding(0, 0,
                        0, reservation_details_card_vw.getLayoutParams().height);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
                    if (ActivityCompat.checkSelfPermission(LandingActivity.this,
                            android.Manifest.permission.ACCESS_FINE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED &&
                            ActivityCompat.checkSelfPermission(LandingActivity.this,
                                    android.Manifest.permission.ACCESS_COARSE_LOCATION)
                                    != PackageManager.PERMISSION_GRANTED) {
                        DialogCreator.create(LandingActivity.this, "locationPermission")
                                .setMessage("We need to access your location and " +
                                        "device state to continue using LÁMÁTÁ.")
                                .setPositiveButton("OK")
                                .show();
                        return;
                    }
                mMap.setMyLocationEnabled(true);
                mMap.clear();
                if (hasDestination == -1) {
                    PlaceDetectionClient mPlaceDetectionClient =
                            Places.getPlaceDetectionClient(LandingActivity.this,
                            null);
                    final Task<PlaceLikelihoodBufferResponse> placeResult =
                            mPlaceDetectionClient.getCurrentPlace(null);
                    placeResult.addOnCompleteListener
                            (new OnCompleteListener<PlaceLikelihoodBufferResponse>() {
                                @Override
                                public void onComplete(@NonNull Task<PlaceLikelihoodBufferResponse> task) {
                                    if (task.isSuccessful() && task.getResult() != null) {
                                        PlaceLikelihoodBufferResponse likelyPlaces = task.getResult();
                                        for (PlaceLikelihood placeLikelihood : likelyPlaces) {
                                            destinationName = placeLikelihood.getPlace().getName().toString();
                                            destinationLocation = placeLikelihood.getPlace().getLatLng();
                                            destinationPlaceId = placeLikelihood.getPlace().getId();
                                            setText(destination_txt_vw, destinationName);
                                            if (!cameraUpdated) {
                                                cameraPosition = new CameraPosition.Builder()
                                                        .target(destinationLocation)
                                                        .zoom(14)
                                                        .bearing(0)
                                                        .build();
                                                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                                                cameraUpdated = true;
                                            }
                                        }
                                        likelyPlaces.release();
                                        mMap.addMarker(new MarkerOptions()
                                                .position(destinationLocation)
                                                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.yellow_marker)));
                                    }
                                }
                            });
                } else {
                    if (getIntent().getStringExtra("destinationName") != null) {
                        destinationLocation = new LatLng(getIntent().getDoubleExtra("destinationLat", 0),
                                getIntent().getDoubleExtra("destinationLng", 0));
                        destinationName = getIntent().getStringExtra("destinationName");
                        setText(destination_txt_vw, destinationName);
                        mMap.addMarker(new MarkerOptions()
                                .position(destinationLocation)
                                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.yellow_marker)));
                        cameraPosition = new CameraPosition.Builder()
                                .target(destinationLocation)
                                .zoom(14)
                                .bearing(0)
                                .build();
                        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                    } else {
                        destinationPlaceId = getIntent().getStringExtra("destinationPlaceId");
                        findPlaceById(destinationPlaceId, 2);
                    }
                }
            }
        });

        reserve_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                reserveLocation();
            }
        });

        destination_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setDestination();
            }
        });

        destination_txt_vw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setDestination();
            }
        });
    }

    private void reserveLocation() {
        Intent intent = new Intent(LandingActivity.this, ReservationActivity.class);
        if (destinationPlaceId != null) {
            intent.putExtra("destinationPlaceId", destinationPlaceId);
            intent.putExtra("hasDestination", 1);
        }

        if (getIntent().getStringExtra("destinationName") != null) {
            intent.putExtra("destinationName", destinationName);
            intent.putExtra("destinationLatLng", destinationLocation.latitude
                    + "," + destinationLocation.longitude);
            intent.putExtra("destinationLat", destinationLocation.latitude);
            intent.putExtra("destinationLng", destinationLocation.longitude);
            intent.putExtra("hasDestination", 1);
        }
        startActivity(intent);
        finish();
    }

    private void setDestination() {
        Intent intent = new Intent(LandingActivity.this, SearchActivity.class);
        intent.putExtra("from", 1);
        if (destinationPlaceId != null) {
            intent.putExtra("destinationPlaceId", destinationPlaceId);
            intent.putExtra("hasDestination", 1);
        }

        if (getIntent().getStringExtra("destinationName") != null) {
            intent.putExtra("destinationName", destinationName);
            intent.putExtra("destinationLatLng", destinationLocation.latitude
                    + "," + destinationLocation.longitude);
            intent.putExtra("destinationLat", destinationLocation.latitude);
            intent.putExtra("destinationLng", destinationLocation.longitude);
            intent.putExtra("hasDestination", 1);
        }
        startActivity(intent);
        finish();
    }

    private void changeBookBtn() {
        if (hasDropoff == 1) {
            booking_btn.setText("Book");
            booking_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    prepareBooking(false);
                }
            });
        } else {
            booking_btn.setText("Set Drop-off Point");
            booking_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    setDropoff();
                }
            });
        }
    }

    private void setHistoryList() {
        viewFlipper.setDisplayedChild(2);
        toolbar.setTitle("History");
        showOptionsMenu(R.id.history);
        final RecyclerView history_rec_vw = findViewById(R.id.history_rec_vw);
        final RelativeLayout loading_view = findViewById(R.id.loading_view);
        final RelativeLayout blank_view = findViewById(R.id.blank_view);
        blank_view.setVisibility(View.VISIBLE);
        historyAdapter = new HistoryAdapter(this, this);
        history_rec_vw.setAdapter(historyAdapter);
        history_rec_vw.setHasFixedSize(true);
        history_rec_vw.setLayoutManager(new LinearLayoutManager(this));
        history_rec_vw.addItemDecoration(new SimpleDividerItemDecoration(this));
        historyRef = FirebaseDatabase.getInstance().getReference("history/" + uid);
        historyRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                historyAdapter.clearHistory();
                if (dataSnapshot != null) {
                    history_rec_vw.setVisibility(View.GONE);
                    loading_view.setVisibility(View.VISIBLE);
                    for (DataSnapshot historyIds : dataSnapshot.getChildren()) {
                        String history_id = historyIds.getKey();
                        String driver_id = "";
                        String pickup_name = "";
                        LatLng pickup_location = new LatLng(0, 0);
                        String dropoff_name = "";
                        LatLng dropoff_location = new LatLng(0, 0);
                        int driver_rating = 0;
                        String date = "";
                        String time = "";
                        double lat = 0;
                        double lng = 0;
                        for (DataSnapshot userHistory : historyIds.getChildren()) {
                            switch (userHistory.getKey()) {
                                case "driver":
                                    for (DataSnapshot driverDetails : userHistory.getChildren()) {
                                        switch (driverDetails.getKey()) {
                                            case "id":
                                                driver_id = driverDetails.getValue().toString();
                                                break;
                                            case "rating":
                                                driver_rating = Integer.parseInt(driverDetails.getValue().toString());
                                                break;
                                        }
                                    }
                                    driver_id = userHistory.getValue().toString();
                                    break;
                                case "dropoff":
                                    for (DataSnapshot dropoffDetails : userHistory.getChildren()) {
                                        switch (dropoffDetails.getKey()) {
                                            case "name":
                                                dropoff_name = dropoffDetails.getValue().toString();
                                                break;
                                            case "lat":
                                                lat = Double.parseDouble(dropoffDetails.getValue().toString());
                                                break;
                                            case "lng":
                                                lng = Double.parseDouble(dropoffDetails.getValue().toString());
                                                break;
                                        }
                                    }
                                    dropoff_location = new LatLng(lat, lng);
                                    break;
                                case "pickup":
                                    for (DataSnapshot pickupDetails : userHistory.getChildren()) {
                                        switch (pickupDetails.getKey()) {
                                            case "name":
                                                pickup_name = pickupDetails.getValue().toString();
                                                break;
                                            case "lat":
                                                lat = Double.parseDouble(pickupDetails.getValue().toString());
                                                break;
                                            case "lng":
                                                lng = Double.parseDouble(pickupDetails.getValue().toString());
                                                break;
                                        }
                                    }
                                    pickup_location = new LatLng(lat, lng);
                                    break;
                                case "date":
                                    date = userHistory.getValue().toString();
                                    break;
                                case "time":
                                    time = userHistory.getValue().toString();
                                    break;
                            }
                        }
                        HistoryAdapter.historyList.add(new HistoryObject(history_id,
                                driver_id, dropoff_name, pickup_name,
                                pickup_location, dropoff_location, date, time, driver_rating));
                        historyAdapter.notifyDataSetChanged();
                        blank_view.setVisibility(View.GONE);
                    }
                }
                loading_view.setVisibility(View.GONE);
                history_rec_vw.setVisibility(View.VISIBLE);
                historyRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.history_menu, menu);
        clear_history = menu.findItem(R.id.clear_history);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.clear_history:
                DialogCreator.create(this, "clearHistory")
                        .setTitle("Clear History")
                        .setMessage("Are you sure?")
                        .setPositiveButton("OK")
                        .setNegativeButton("CANCEL")
                        .setCancelable(false)
                        .show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onHistoryClick(ArrayList<HistoryObject> resultList, int position) {
        //new activity
    }

    private void showOptionsMenu(int id) {
        clear_history.setVisible(false);
        switch (id) {
            case R.id.history:
                clear_history.setVisible(true);
                break;
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        showOptionsMenu(item.getItemId());
        switch (item.getItemId()) {
            case R.id.booking:
                bookingmapFragment = (SupportMapFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.map);

                bookingmapFragment.getMapAsync(this);
                viewFlipper.setDisplayedChild(0);
                toolbar.setTitle("Booking");
                break;
            case R.id.reservation:
                setReservation();
                break;
            case R.id.history:
                setHistoryList();
                break;
            case R.id.profile:
                setAccountProfile();
                break;
            case R.id.settings:
                viewFlipper.setDisplayedChild(4);
                toolbar.setTitle("Settings");
                EditText token_et = findViewById(R.id.token_et);
                token_et.setText(TaskConfig.CURRENT_TOKEN);
                break;
            case R.id.logout:
                String providerid = "";
                for (UserInfo user : FirebaseAuth.getInstance().getCurrentUser().getProviderData()) {
                    providerid = user.getProviderId();
                }
                if (AccessToken.getCurrentAccessToken() != null && providerid.equals("facebook.com")) {
                    LoginManager.getInstance().logOut();
                } else if (providerid.equals("google.com")) {
                    GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                            .requestIdToken(getString(R.string.default_web_client_id))
                            .requestEmail()
                            .build();

                    GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

                    mGoogleSignInClient.signOut();
                }
                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.clear();
                editor.commit();
                FirebaseAuth.getInstance().signOut();
                SuperTask.execute(LandingActivity.this,
                        TaskConfig.SIGNOUT_URL,
                        "signout");
                Intent intent = new Intent(LandingActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
                break;
        }

        drawer.closeDrawer(GravityCompat.START);
        drawer = findViewById(R.id.drawer_layout);
        return true;
    }

    private void setAccountProfile() {
        setDetails();
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Fetching Data...");
        progressDialog.setIndeterminate(false);
        progressDialog.setCancelable(false);
        progressDialog.show();
        final TextView nameTxtVw = findViewById(R.id.name_txt_vw);
        final TextView emailTxtVw = findViewById(R.id.email_edit_txt);
        final TextView mobnumTxtVw = findViewById(R.id.mobnum_edit_txt);
        final TextView trustedTxtVw = findViewById(R.id.change_trusted_btn);
        final ImageView profpicImgVw = findViewById(R.id.profpic_img_vw);
        Button update = findViewById(R.id.update_btn);

        Glide.with(this)
                .load(user_pic)
                .apply(RequestOptions.circleCropTransform())
                .into(profpicImgVw);

        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                from_update_activity = true;
                Intent intent = new Intent(LandingActivity.this, UpdateAccountActivity.class);
                startActivity(intent);
            }
        });
        if (!user_trusted_id.equals("None")) {
            DatabaseReference pssngr = FirebaseDatabase.getInstance()
                    .getReference("users/passenger/" + user_trusted_id);
            pssngr.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Map<String, Object> data = (Map<String, Object>) dataSnapshot.getValue();
                    user_trusted_name = data.get("name") != null ? data.get("name").toString() : "None";
                    trustedTxtVw.setText(user_trusted_name);
                    viewFlipper.setDisplayedChild(3);
                    toolbar.setTitle("Profile");
                    progressDialog.dismiss();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
        } else {
            trustedTxtVw.setText(user_trusted_id);
            viewFlipper.setDisplayedChild(3);
            toolbar.setTitle("Profile");
            progressDialog.dismiss();
        }
        nameTxtVw.setText(user_name);
        emailTxtVw.setText(user_email);
        mobnumTxtVw.setText(user_mobnum);
    }

    private void prepareBooking(boolean notrusted) {
        if (checkDetails() == 1 || notrusted) {
            Intent intent = new Intent(LandingActivity.this, FindNearbyDriverActivity.class);
            intent.putExtra("pickupName", pickupName);
            intent.putExtra("pickupLat", pickupLocation.latitude);
            intent.putExtra("pickupLng", pickupLocation.longitude);
            intent.putExtra("pickupPlaceId", pickupPlaceId);
            intent.putExtra("dropoffName", dropoffName);
            intent.putExtra("dropoffLat", dropoffLocation.latitude);
            intent.putExtra("dropoffLng", dropoffLocation.longitude);
            intent.putExtra("dropoffPlaceId", dropoffPlaceId);
            intent.putExtra("fare", taxi_fare);
            startActivity(intent);
            finish();
        } else if (checkDetails() == -1) {
            showSnackbarMessage(booking_btn, "Please set your Mobile Number.");
        } else if (checkDetails() == 2) {
            showSnackbarMessage(booking_btn, "Please set your Email Address.");
        } else if (checkDetails() == -3) {
            showSnackbarMessage(booking_btn, "Please set your Mobile Number and Email Address.");
        } else if (checkDetails() == -4) {
            DialogCreator.create(this, "noTrusted")
                    .setTitle("No Trusted Contact")
                    .setMessage("Are you sure to proceed?")
                    .setCancelable(false)
                    .setPositiveButton("BOOK")
                    .setNegativeButton("CANCEL")
                    .show();
        }
    }

    private void setDropoff() {
        Intent intent = new Intent(LandingActivity.this, SearchActivity.class);
        intent.putExtra("from", 1);
        if (pickupPlaceId != null) {
            intent.putExtra("pickupPlaceId", pickupPlaceId);
            intent.putExtra("hasPickup", 1);
        }

        if (dropoffPlaceId != null) {
            intent.putExtra("dropoffPlaceId", dropoffPlaceId);
            intent.putExtra("hasDropoff", 1);
        }

        if (getIntent().getStringExtra("pickupName") != null) {
            intent.putExtra("pickupName", pickupName);
            intent.putExtra("pickupLatLng", pickupLocation.latitude + "," + pickupLocation.longitude);
            intent.putExtra("pickupLat", pickupLocation.latitude);
            intent.putExtra("pickupLng", pickupLocation.longitude);
            intent.putExtra("hasPickup", 1);
        }

        if (getIntent().getStringExtra("dropoffName") != null) {
            intent.putExtra("dropoffName", dropoffName);
            intent.putExtra("dropoffLatLng", dropoffLocation.latitude + "," + dropoffLocation.longitude);
            intent.putExtra("dropoffLat", dropoffLocation.latitude);
            intent.putExtra("dropoffLng", dropoffLocation.longitude);
            intent.putExtra("hasDropoff", 1);
        }
        startActivity(intent);
        finish();
    }

    private void setFare() {
        SuperTask.execute(this,
                TaskConfig.CREATE_TAXI_FARE_URL,
                "get_fare",
                "Calculating Fare...");
    }

    @Override
    public void onTaskRespond(String json, String id) {
        try {
            JSONObject jsonObject = new JSONObject(json);
            switch (id) {
                case "get_fare_map_point":
                case "get_fare":
                    if (jsonObject.getString("status").equals("OK")) {
                        taxi_fare = jsonObject.getString("fare");
                        distance = jsonObject.getString("distance");
                        duration = jsonObject.getString("duration");

                        fare_details_txt_vw.setText(taxi_fare);
                        distance_details_txt_vw.setText(distance);
                        duration_details_txt_vw.setText(duration);
                    } else if (jsonObject.getString("status").equals("STATUS: SERVER ERROR!")) {
                        SnackBarCreator.set("Sorry! Place is not available right now.");
                        SnackBarCreator.show(booking_btn);
                    }

                    LatLngBounds.Builder builder = new LatLngBounds.Builder();
                    builder.include(pickupLocation);
                    builder.include(dropoffLocation);
                    LatLngBounds bounds = builder.build();
                    mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 130));
                    break;
            }
        } catch (Exception e) {
        }
    }

    @Override
    public ContentValues setRequestValues(ContentValues contentValues, String id) {
        contentValues.put("android", 1);
        switch (id) {
            case "get_fare":
                contentValues.put("origins", pickupPlaceId);
                contentValues.put("destinations", dropoffPlaceId);
                break;
            case "get_fare_map_point":
                if (getIntent().getStringExtra("pickupName") != null) {
                    contentValues.put("pickupLatLng", 1);
                    contentValues.put("origins", getIntent().getStringExtra("pickupLatLng"));
                } else {
                    contentValues.put("origins", pickupPlaceId);
                }

                if (getIntent().getStringExtra("dropoffName") != null) {
                    contentValues.put("dropoffLatLng", 1);
                    contentValues.put("destinations", getIntent().getStringExtra("dropoffLatLng"));
                } else {
                    contentValues.put("destinations", dropoffPlaceId);
                }
                break;
            case "signout":
                contentValues.put("database_id", user_database_id);
                break;
        }
        return contentValues;
    }

    private void findPlaceById(String placeId, final int from) {
        mGeoDataClient.getPlaceById(placeId).addOnCompleteListener(new OnCompleteListener<PlaceBufferResponse>() {
            @Override
            public void onComplete(@NonNull Task<PlaceBufferResponse> task) {
                if (task.isSuccessful()) {
                    PlaceBufferResponse places = task.getResult();
                    Place myPlace = places.get(0);
                    if (from == 0) {
                        pickupName = myPlace.getName().toString();
                        setText(pickup_txt_vw, pickupName);
                        pickupLocation = myPlace.getLatLng();
                    } else if (from == 1) {
                        dropoffName = myPlace.getName().toString();
                        setText(dropoff_txt_vw, dropoffName);
                        dropoffLocation = myPlace.getLatLng();
                        setFare();
                    } else if (from == 2) {
                        destinationName = myPlace.getName().toString();
                        destinationLocation = myPlace.getLatLng();
                        TextView destination_txt_vw = findViewById(R.id.destination_txt_vw);
                        places.release();
                        setText(destination_txt_vw, destinationName);
                        mMap.addMarker(new MarkerOptions()
                                .position(destinationLocation)
                                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.yellow_marker)));
                        cameraPosition = new CameraPosition.Builder()
                                .target(destinationLocation)
                                .zoom(14)
                                .bearing(0)
                                .build();
                        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                        cameraUpdated = true;
                        return;
                    }
                    setMarkers(false);
                    places.release();
                }
            }
        });
    }

    private void setMarkers(boolean autoMarker) {
        if (autoMarker) mMap.clear();

        if (pickupLocation != null) {
            mMap.addMarker(new MarkerOptions()
                    .position(pickupLocation)
                    .icon(BitmapDescriptorFactory.fromResource(R.mipmap.yellow_marker)));
            if (hasDropoff == -1) {
                cameraPosition = new CameraPosition.Builder()
                        .target(pickupLocation)
                        .zoom(14)
                        .bearing(0)
                        .build();
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                cameraUpdated = true;
            }
        }
        if (dropoffLocation != null) {
            mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(dropoffLocation.latitude, dropoffLocation.longitude))
                    .icon(BitmapDescriptorFactory.fromResource(R.mipmap.black_marker)));
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        google_client_built = true;
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(5000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            if (ActivityCompat.checkSelfPermission(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this,
                            android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                DialogCreator.create(this, "locationPermission")
                        .setMessage("We need to access your location and device state to continue using FROURÁ.")
                        .setPositiveButton("OK")
                        .show();
                return;
            }
        mMap.setMyLocationEnabled(true);
        LocationServices.FusedLocationApi
                .requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        if (hasPickup == -1) {
            PlaceDetectionClient mPlaceDetectionClient = Places.getPlaceDetectionClient(this, null);
            final Task<PlaceLikelihoodBufferResponse> placeResult =
                    mPlaceDetectionClient.getCurrentPlace(null);
            placeResult.addOnCompleteListener
                    (new OnCompleteListener<PlaceLikelihoodBufferResponse>() {
                        @Override
                        public void onComplete(@NonNull Task<PlaceLikelihoodBufferResponse> task) {
                            if (task.isSuccessful() && task.getResult() != null) {
                                PlaceLikelihoodBufferResponse likelyPlaces = task.getResult();
                                for (PlaceLikelihood placeLikelihood : likelyPlaces) {
                                    pickupName = placeLikelihood.getPlace().getName().toString();
                                    pickupLocation = placeLikelihood.getPlace().getLatLng();
                                    pickupPlaceId = placeLikelihood.getPlace().getId();
                                    setText(pickup_txt_vw, pickupName);
                                    setMarkers(true);
                                    if (!cameraUpdated) {
                                        cameraPosition = new CameraPosition.Builder()
                                                .target(pickupLocation)
                                                .zoom(14)
                                                .bearing(0)
                                                .build();
                                        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                                        cameraUpdated = true;
                                    }
                                }
                                likelyPlaces.release();
                            }
                        }
                    });
        }
    }

    private void setDetails() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String JSON_DETAILS_KEY = "userDetails";
        String userDetails = sharedPref.getString(JSON_DETAILS_KEY, "{ \"name\" : NULL }");
        try {
            JSONObject jsonObject = new JSONObject(userDetails);
            if (!jsonObject.getString("name").equals("NULL")) {
                if (!jsonObject.getString("profile_pic").equals("default")) {
                    user_pic = jsonObject.getString("profile_pic");
                    Glide.with(this)
                            .load(user_pic)
                            .apply(RequestOptions.circleCropTransform())
                            .into(profile_pic_header_img_vw);
                }
                user_name = jsonObject.getString("name");
                user_email = jsonObject.getString("email").equals("null") ?
                        "None" : jsonObject.getString("email");
                user_mobnum = jsonObject.getString("mobnum").equals("null") ?
                        "None" : jsonObject.getString("mobnum");
                user_trusted_id = jsonObject.getString("trusted_id").equals("null") ?
                        "None" : jsonObject.getString("trusted_id");
                user_database_id = jsonObject.getString("database_id");
                name_header_txt_vw.setText(jsonObject.getString("name"));
                email_header_txt_vw.setText(user_email);
            }
        } catch (Exception e) {
        }
    }

    private boolean locationEnabled() {
        int locationMode = 0;
        String locationProviders;
        boolean isAvailable;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            try {
                locationMode = Settings.Secure.getInt(getContentResolver(), Settings.Secure.LOCATION_MODE);
            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
            }

            isAvailable = (locationMode != Settings.Secure.LOCATION_MODE_OFF);
        } else {
            locationProviders = Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            isAvailable = !TextUtils.isEmpty(locationProviders);
        }

        return isAvailable;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        try {
            googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            this, R.raw.mapstyle));
        } catch (Resources.NotFoundException e) {
        }
        mMap = googleMap;
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.getUiSettings().setZoomControlsEnabled(false);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        mMap.setPadding(0,
                hasDropoff == 1 ? fare_details_card_vw.getLayoutParams().height : 0,
                0, booking_details_card_vw.getLayoutParams().height);
        if (!google_client_built) buildGoogleApiClient();
        setMarkers(false);
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
    }

    private void setText(TextView txtVw, String str) {
        txtVw.setText(str);
        txtVw.setTextColor(getResources().getColor(R.color.place_autocomplete_search_text));
    }

    private void runAutomaticPickup() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);
        LocationServices.FusedLocationApi
                .requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        if(hasPickup == -1) {
            PlaceDetectionClient mPlaceDetectionClient = Places.getPlaceDetectionClient(this, null);
            final Task<PlaceLikelihoodBufferResponse> placeResult =
                    mPlaceDetectionClient.getCurrentPlace(null);
            placeResult.addOnCompleteListener
                    (new OnCompleteListener<PlaceLikelihoodBufferResponse>() {
                        @Override
                        public void onComplete(@NonNull Task<PlaceLikelihoodBufferResponse> task) {
                            if (task.isSuccessful() && task.getResult() != null) {
                                PlaceLikelihoodBufferResponse likelyPlaces = task.getResult();
                                for (PlaceLikelihood placeLikelihood : likelyPlaces) {
                                    pickupName = placeLikelihood.getPlace().getName().toString();
                                    pickupLocation = placeLikelihood.getPlace().getLatLng();
                                    pickupPlaceId = placeLikelihood.getPlace().getId();
                                    setText(pickup_txt_vw, pickupName);
                                    setMarkers(true);
                                    if(!cameraUpdated) {
                                        cameraPosition = new CameraPosition.Builder()
                                                .target(pickupLocation)
                                                .zoom(14)
                                                .bearing(0)
                                                .build();
                                        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                                        cameraUpdated = true;
                                    }
                                }
                                likelyPlaces.release();
                            }
                        }
                    });
        }
    }

    @Override
    public void onBackPressed() {
        drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case LOCATION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    runAutomaticPickup();
                } else {
                    DialogCreator.create(this,"locationPermission")
                            .setMessage("We need to access your location and device state to continue " +
                                    "using FROURÁ. Ask permission again?")
                            .setPositiveButton("YES")
                            .setNegativeButton("NO")
                            .setCancelable(false)
                            .show();
                }
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
    }

    @Override
    public void onClickPositiveButton(String actionId) {
        switch (actionId) {
            case "requestLocation":
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
                break;
            case "locationPermission":
                ActivityCompat.requestPermissions(LandingActivity.this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        LOCATION_REQUEST_CODE);
                break;
            case "noTrusted":
                prepareBooking(true);
                break;
            case "clearHistory":
                historyAdapter.clearHistory();
                historyRef.removeValue();
                break;
        }
    }

    @Override
    public void onClickNegativeButton(String actionId) {
        switch (actionId) {
            case "locationPermission":
                finish();
                break;
        }
    }

    public int getImage(String imageName) {
        int drawableResourceId = this.getResources()
                .getIdentifier(imageName, "drawable", this.getPackageName());
        return drawableResourceId;
    }

    private void askPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            if (ActivityCompat.checkSelfPermission(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this,
                            android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(LandingActivity.this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        LOCATION_REQUEST_CODE);
            } else {
                Toast.makeText(this, "All Permissions granted.", Toast.LENGTH_SHORT).show();
            }
    }

    private void permissionDenied() {
        booking_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSnackbarMessage(booking_btn, "Permissions denied.");
            }
        });
    }

    private int checkDetails() {
        if(user_mobnum.equals("None") && user_email.equals("None")) {
            return  -3;
        }
        if(user_mobnum.equals("None")) {
            return -1;
        }
        if(user_email.equals("None")) {
            return -2;
        }
        if(user_trusted_id.equals("None")) {
            return -4;
        }
        return 1;
    }

    private void setHint(TextView txtVw, String str) {
        txtVw.setText(str);
        txtVw.setTextColor(getResources().getColor(R.color.place_autocomplete_search_hint));
    }

    private void showSnackbarMessage(View view, String msg) {
        SnackBarCreator.set(msg);
        SnackBarCreator.show(view);
    }


    @Override
    protected void onResume() {
        super.onResume();
        if(from_update_activity) {
            setAccountProfile();
            from_update_activity = false;
        }
    }

    @Override
    public void onConnectionSuspended(int i) { }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) { }

    @Override
    public void onClickNeutralButton(String actionId) { }

    @Override
    public void onClickMultiChoiceItem(String actionId, int which, boolean isChecked) { }

    @Override
    public void onCreateDialogView(String actionId, View view) { }
}
