package com.abiola.initiative.lamatapassenger;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;

public class ReservationActivity extends AppCompatActivity {

    private GoogleMap mMap;
    private SupportMapFragment mapFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reservation);

        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(ReservationActivity.this, LandingActivity.class);
        if(getIntent().getStringExtra("destinationPlaceId") != null) {
            intent.putExtra("destinationPlaceId", getIntent().getStringExtra("destinationPlaceId"));
        }

        if(getIntent().getStringExtra("destinationName") != null) {
            intent.putExtra("destinationName", getIntent().getStringExtra("destinationName"));
            intent.putExtra("destinationLatLng", getIntent().getStringExtra("destinationLatLng"));
            intent.putExtra("destinationLat", getIntent().getDoubleExtra("destinationLat", 0));
            intent.putExtra("destinationLng", getIntent().getDoubleExtra("destinationLng", 0));

        }
        intent.putExtra("hasDestination", 1);
        startActivity(intent);
        finish();
    }
}
