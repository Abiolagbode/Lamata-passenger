package com.abiola.initiative.lamatapassenger;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.location.Location;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.abiola.initiative.lamatapassenger.config.TaskConfig;
import com.abiola.initiative.lamatapassenger.libraries.DialogCreator;
import com.abiola.initiative.lamatapassenger.tasks.SuperTask;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONObject;

import java.util.List;
import java.util.Map;

public class DriverAcceptedActivity extends AppCompatActivity
        implements
        OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener,
        SuperTask.TaskListener,
        DialogCreator.DialogActionListener {

    private GoogleMap mMap;
    private CameraPosition cameraPosition;
    private Marker driverMarker;
    private SupportMapFragment mapFragment;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;

    private Toolbar toolbar;
    private Button cancel_btn;
    private LinearLayout informationLayout;
    private ProgressDialog progressDialog;
    private FloatingActionButton alert_trusted_btn;

    private ImageView driver_prof_pic;
    private TextView driver_name_txt_vw;
    private TextView driver_mob_num_txt_vw;
    private TextView driver_plate_number_txt_vw;

    private String driverId = null;
    private String driver_name = null;
    private String driver_plate = null;
    private String driver_mobnum = null;
    private String driver_profpic = "default";

    private String user_id;
    private String user_name;
    private String user_mobnum;
    private String user_email;
    private String user_pic = "default";
    private String user_trusted_id;
    private String user_trusted_name;
    private String user_reason = "";
    private float rating = 0;

    private String dropoffName;
    private String dropoffLat;
    private String dropoffLng;
    private String pickupName;
    private String pickupLat;
    private String pickupLng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_accepted);
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Fetching Data...");
        progressDialog.setIndeterminate(false);
        progressDialog.setCancelable(false);
        progressDialog.show();
        toolbar = findViewById(R.id.toolbar_driver_accepted);
        toolbar.setTitle("Driver on the way");
        setSupportActionBar(toolbar);
        setBookingDetails();
        informationLayout = findViewById(R.id.information_layout);
        alert_trusted_btn = findViewById(R.id.alert_trusted_btn);
        user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();
        driverId = getIntent().getStringExtra("driver_id");

        setDetails();

        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);

        driver_prof_pic = findViewById(R.id.driver_prof_pic);
        driver_name_txt_vw = findViewById(R.id.driver_name_txt_vw);
        driver_mob_num_txt_vw = findViewById(R.id.driver_mob_num_txt_vw);
        driver_plate_number_txt_vw = findViewById(R.id.driver_plate_number_txt_vw);
        cancel_btn = findViewById(R.id.cancel_btn);

        cancel_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogCreator.create(DriverAcceptedActivity.this, "cancel")
                        .setMessage("Are you sure to cancel this trip?")
                        .setCancelable(false)
                        .setPositiveButton("Yes")
                        .setNegativeButton("No")
                        .show();
            }
        });

        getDriverDetails();

        final DatabaseReference inTransit = FirebaseDatabase.getInstance()
                .getReference("services/booking/"+user_id+"/in_transit");
        inTransit.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    toolbar.setTitle("In-transit");
                    SuperTask.execute(DriverAcceptedActivity.this,
                            TaskConfig.SEND_NOTIFICATION,
                            "notification_in_transit");
                    inTransit.removeEventListener(this);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) { }
        });

        final DatabaseReference endTrip = FirebaseDatabase.getInstance()
                .getReference("services/booking/"+user_id+"/end");
        endTrip.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    SuperTask.execute(DriverAcceptedActivity.this,
                            TaskConfig.SEND_NOTIFICATION,
                            "notification_dropoff");
                    endTrip.removeEventListener(this);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) { }
        });
    }

    private void setBookingDetails() {
        DatabaseReference bookingDetails = FirebaseDatabase.getInstance()
                .getReference("services/booking/"+user_id);
        bookingDetails.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot bookingDetails : dataSnapshot.getChildren()) {
                    switch (bookingDetails.getKey()) {
                        case "dropoff":
                            for(DataSnapshot dropoffDetails : bookingDetails.getChildren()) {
                                switch (dropoffDetails.getKey()) {
                                    case "lat":
                                        dropoffLat = dropoffDetails.getValue().toString();
                                        break;
                                    case "lng":
                                        dropoffLng = dropoffDetails.getValue().toString();
                                        break;
                                    case "name":
                                        dropoffName = dropoffDetails.getValue().toString();
                                        break;
                                }
                            }
                            break;
                        case "pickup":
                            for(DataSnapshot pickupDetails : bookingDetails.getChildren()) {
                                switch (pickupDetails.getKey()) {
                                    case "lat":
                                        pickupLat = pickupDetails.getValue().toString();
                                        break;
                                    case "lng":
                                        pickupLng = pickupDetails.getValue().toString();
                                        break;
                                    case "name":
                                        pickupName = pickupDetails.getValue().toString();
                                        break;
                                }
                            }
                            break;
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) { }
        });
    }

    private void setDetails() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        final String JSON_DETAILS_KEY = "userDetails";
        String userDetails = sharedPref.getString(JSON_DETAILS_KEY, "{ \"name\" : NULL }");
        try {
            JSONObject jsonObject = new JSONObject(userDetails);
            if(!jsonObject.getString("name").equals("NULL")) {
                if(!jsonObject.getString("profile_pic").equals("default")) {
                    user_pic = jsonObject.getString("profile_pic");
                }
                user_name = jsonObject.getString("name");
                user_email = jsonObject.getString("email").equals("null") ?
                        "None" : jsonObject.getString("email");
                user_mobnum = jsonObject.getString("mobnum").equals("null") ?
                        "None" : jsonObject.getString("mobnum");
                user_trusted_id = jsonObject.getString("trusted_id").equals("null") ?
                        "None" : jsonObject.getString("trusted_id");

                if(user_trusted_id.equals("None")) {
                    alert_trusted_btn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Toast.makeText(DriverAcceptedActivity.this,
                                    "You don't have a Trusted Contact to alert.", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    alert_trusted_btn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            SuperTask.execute(DriverAcceptedActivity.this,
                                    TaskConfig.SEND_NOTIFICATION,
                                    "notification_alert");
                        }
                    });
                }
            }
        } catch (Exception e) { }
    }

    @Override
    public void onTaskRespond(String json, String id) {
        try {
            JSONObject jsonObject = new JSONObject(json);
            if(jsonObject.getInt("success") == 1) {
                Toast.makeText(this, "We have sent your notification.",
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Something went wrong.",
                        Toast.LENGTH_SHORT).show();
            }
            switch (id) {
                case "notification_dropoff":
                    DialogCreator.create(this, "end")
                            .setTitle("We need your feedback:")
                            .setView(R.layout.dialog_feedback)
                            .setPositiveButton("Send")
                            .setNegativeButton("Cancel")
                            .show();
                    break;
            }
        } catch (Exception e) { }
    }

    @Override
    public ContentValues setRequestValues(ContentValues contentValues, String id) {
        contentValues.put("android", 1);
        switch (id) {
            case "notification_alert":
                contentValues.put("notif_type", "emergency");
                break;
            case "notification_in_transit":
                contentValues.put("notif_type", "in-transit");
                break;
            case "notification_dropoff":
                contentValues.put("notif_type", "dropoff");
                break;
            case "end":
                contentValues.put("comment", user_reason);
                contentValues.put("rating", rating);
                contentValues.put("driver_uid", driverId);
                break;
        }
        contentValues.put("sender_uid", user_id);
        contentValues.put("receiver_uid", user_trusted_id);
        Log.d("DRIVERACCEPTED_AC", contentValues+"");
        return contentValues;
    }

    private void getDriverDetails() {
        DatabaseReference driverDetails = FirebaseDatabase.getInstance().getReference("users/driver/" + driverId);
        driverDetails.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String, Object> data = (Map<String, Object>) dataSnapshot.getValue();
                if(data.get("profile_pic") != null) {
                    if(data.get("profile_pic").equals("default")) {
                        driver_profpic = "default";
                    } else {
                        driver_profpic = data.get("profile_pic").toString();
                    }
                }

                if(data.get("name") != null)
                    driver_name = data.get("name").toString();

                if(data.get("plate") != null)
                    driver_plate = data.get("plate").toString();

                if(data.get("mobnum") != null)
                    driver_mobnum = data.get("mobnum").toString();

                if(!driver_profpic.equals("default"))
                    Glide.with(DriverAcceptedActivity.this)
                            .load(driver_profpic)
                            .apply(RequestOptions.circleCropTransform())
                            .into(driver_prof_pic);

                if(driver_name != null)
                    driver_name_txt_vw.setText(driver_name);

                if(driver_mobnum != null)
                    driver_mob_num_txt_vw.setText(driver_mobnum);

                if(driver_plate != null)
                    driver_plate_number_txt_vw.setText(driver_plate);


                showDriverLocation();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) { }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.driver_accepted_activity_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.message:

                break;
        }
        return true;
    }

    private void showDriverLocation() {
        final DatabaseReference driverLoc = FirebaseDatabase.getInstance().getReference("available_drivers/" + driverId + "/l");
        driverLoc.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<Object> map = (List<Object>) dataSnapshot.getValue();
                double drvLat = 0;
                double drvLng = 0;

                if(dataSnapshot.getValue() != null) {
                    if(map.get(0) != null){
                        drvLat = Double.parseDouble(map.get(0).toString());
                    }
                    if(map.get(1) != null){
                        drvLng = Double.parseDouble(map.get(1).toString());
                    }
                }

                LatLng drvLatLng = new LatLng(drvLat, drvLng);

                if(driverMarker != null) {
                    driverMarker.remove();
                }

                Location drvLoc = new Location("");
                drvLoc.setLatitude(drvLat);
                drvLoc.setLongitude(drvLng);

                Location pickupLoc = new Location("");
                pickupLoc.setLatitude(getIntent().getDoubleExtra("pickupLat", 0));
                pickupLoc.setLongitude(getIntent().getDoubleExtra("pickupLng", 0));

                driverMarker = mMap.addMarker(new MarkerOptions().position(drvLatLng));
                loadMarkerIcon(driverMarker);

                if(progressDialog.isShowing())
                    progressDialog.dismiss();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) { }
        });
    }

    private void loadMarkerIcon(final Marker marker) {
        if(driver_profpic.equals("default"))
            Glide.with(getApplicationContext()).asBitmap()
                    .load(getImage("placeholder"))
                    .apply(RequestOptions.circleCropTransform())
                    .into(new SimpleTarget<Bitmap>(150,150) {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource,
                                                    @Nullable Transition<? super Bitmap> transition) {
                            BitmapDescriptor icon = BitmapDescriptorFactory.fromBitmap(resource);
                            marker.setIcon(icon);
                        }
                    });
        else
            Glide.with(getApplicationContext()).asBitmap()
                .load(driver_profpic)
                .apply(RequestOptions.circleCropTransform())
                .into(new SimpleTarget<Bitmap>(100,100) {
            @Override
            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                    BitmapDescriptor icon = BitmapDescriptorFactory.fromBitmap(resource);
                    marker.setIcon(icon);
                }
            });
    }

    public int getImage(String imageName) {
        int drawableResourceId = this.getResources()
                .getIdentifier(imageName, "drawable", this.getPackageName());
        return drawableResourceId;
    }

    @Override
    public void onLocationChanged(Location location) {
        if(getApplicationContext() != null) {
            mLastLocation = location;
        }
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
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.setPadding(0,0,0,informationLayout.getLayoutParams().height);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            DialogCreator.create(this, "locationPermission")
                    .setMessage("We need to access your location and device state to continue using LÁMÁTÁ.")
                    .setPositiveButton("OK")
                    .show();
            return;
        }
        buildGoogleApiClient();
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
    public void onConnected(@Nullable Bundle bundle) {
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(5000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
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
    protected void onStop() {
        super.onStop();
        if(mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
    }

    @Override
    public void onClickPositiveButton(String actionId) {
        Intent intent;
        switch (actionId) {
            case "locationPermission":
                ActivityCompat.requestPermissions(DriverAcceptedActivity
                                .this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        1);
                break;
            case "cancel":
                DialogCreator.create(this, "reason")
                        .setTitle("State your reason:")
                        .setView(R.layout.dialog_reason)
                        .setPositiveButton("Send")
                        .setNegativeButton("Cancel")
                        .show();
                break;
            case "reason":
                DatabaseReference dbRef = FirebaseDatabase.getInstance()
                        .getReference("services/booking/" + user_id +"/cancelled_by");
                dbRef.child("passenger").setValue(true);
                dbRef.child("reason").setValue(user_reason);
                intent = new Intent(DriverAcceptedActivity.this, LandingActivity.class);
                startActivity(intent);
                finish();
                break;
            case "end":
                SuperTask.execute(DriverAcceptedActivity.this,
                        TaskConfig.SEND_FEEDBACK,
                        "end");
                intent  = new Intent(DriverAcceptedActivity.this, LandingActivity.class);
                startActivity(intent);
                finish();
                break;
        }
    }

    @Override
    public void onClickNegativeButton(String actionId) { }

    @Override
    public void onClickNeutralButton(String actionId) { }

    @Override
    public void onClickMultiChoiceItem(String actionId, int which, boolean isChecked) { }

    @Override
    public void onCreateDialogView(String actionId, View view) {
        switch (actionId) {
            case "reason":
                final TextInputEditText reason_et = view.findViewById(R.id.reason_et);
                reason_et.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                        user_reason = reason_et.getText().toString();
                    }

                    @Override
                    public void afterTextChanged(Editable editable) { }
                });
                break;
            case "end":
                final TextInputEditText comment_et = view.findViewById(R.id.reason_et);
                comment_et.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                        user_reason = comment_et.getText().toString();
                    }

                    @Override
                    public void afterTextChanged(Editable editable) { }
                });
                RatingBar ratingBar = view.findViewById(R.id.ratingBar);
                ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
                    @Override
                    public void onRatingChanged(RatingBar ratingBar, float v, boolean b) {
                        rating = v;
                    }
                });
                break;
        }
    }

    @Override
    public void onConnectionSuspended(int i) { }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) { }
}
