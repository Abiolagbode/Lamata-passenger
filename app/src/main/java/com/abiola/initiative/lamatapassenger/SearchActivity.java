package com.abiola.initiative.lamatapassenger;

import android.content.Intent;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.abiola.initiative.lamatapassenger.adapter.PlaceAutocompleteAdapter;
import com.abiola.initiative.lamatapassenger.object.PlaceAutocompleteObject;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Places;

import java.util.ArrayList;

public class SearchActivity extends AppCompatActivity implements PlaceAutocompleteAdapter.PlaceAutoCompleteInterface {

    private EditText searchET;
    private ImageButton clearImgVw;
    private ImageButton backImgVw;
    private LinearLayout openMap;
    private RelativeLayout loadingView;
    private RelativeLayout blankView;
    private RecyclerView listRecVw;
    private PlaceAutocompleteAdapter mAdapter;

    private GeoDataClient mGeoDataClient;
    private String pickupPlaceId = "";
    private String dropoffPlaceId = "";
    private String destinationPlaceId = "";
    private int hasPickup = -1;
    private int hasDropoff = -1;
    private int from;
    private CharSequence text;
    private CountDownTimer timer;

    private String TAG = "SEARCH_ACTIVTY_LAMATA";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        from = getIntent().getIntExtra("from", -1);

        if(getIntent().getIntExtra("hasDestination", -1) == 1) {
            destinationPlaceId = getIntent().getStringExtra("destinationPlaceId");
        } else {
            if(getIntent().getIntExtra("hasPickup", -1) == 1) {
                hasPickup = 1;
                pickupPlaceId = getIntent().getStringExtra("pickupPlaceId");
            }

            if(getIntent().getIntExtra("hasDropoff", -1) == 1){
                hasDropoff = 1;
                dropoffPlaceId = getIntent().getStringExtra("dropoffPlaceId");
            }
        }

        searchET = findViewById(R.id.searchET);
        clearImgVw = findViewById(R.id.clearImgVw);
        listRecVw = findViewById(R.id.listRecVw);
        listRecVw.setHasFixedSize(true);
        listRecVw.setLayoutManager(new LinearLayoutManager(this));
        backImgVw = findViewById(R.id.backImgVw);
        openMap = findViewById(R.id.openMap);
        loadingView = findViewById(R.id.loading_view);
        blankView = findViewById(R.id.blank_view);
        noLocationFound(true);

        timer = new CountDownTimer(1500, 1000) {

            @Override
            public void onTick(long l) {

            }

            @Override
            public void onFinish() {
                mAdapter.getFilter().filter(text);
                finishedLoading(true);
            }
        };


        if(from == 0) {
            searchET.setHint("Enter Pick-up point.");
        } else {
            searchET.setHint("Where are you going?");
        }

        mGeoDataClient = Places.getGeoDataClient(this, null);
        AutocompleteFilter typeFilter = new AutocompleteFilter.Builder()
                .setCountry("PH")
                .build();

        mAdapter = new PlaceAutocompleteAdapter(this, mGeoDataClient, typeFilter);
        listRecVw.setAdapter(mAdapter);

        searchET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                text = charSequence;
                finishedLoading(false);
                noLocationFound(false);
                timer.cancel();

                if(!text.toString().isEmpty()) {
                    clearImgVw.setVisibility(View.VISIBLE);
                } else {
                    clearImgVw.setVisibility(View.GONE);
                    noLocationFound(true);
                    if(mAdapter != null) mAdapter.clearList();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                timer.start();
            }
        });

        clearImgVw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mAdapter != null)
                    mAdapter.clearList();
                searchET.setText("");
                noLocationFound(true);
            }
        });

        backImgVw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        openMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SearchActivity.this, MapPointActivity.class);
                if(getIntent().getStringExtra("destinationPlaceId") != null) {
                    intent.putExtra("destinationPlaceId", destinationPlaceId);
                } else if(getIntent().getStringExtra("destinationName") != null) {
                    if(getIntent().getStringExtra("destinationName") != null) {
                        intent.putExtra("destinationName", getIntent().getStringExtra("destinationName"));
                        intent.putExtra("destinationLatLng", getIntent().getStringExtra("destinationLatLng"));
                        intent.putExtra("destinationLat", getIntent().getDoubleExtra("destinationLat", 0));
                        intent.putExtra("destinationLng", getIntent().getDoubleExtra("destinationLng", 0));
                    }

                } else {
                    if(getIntent().getStringExtra("pickupPlaceId") != null)
                        intent.putExtra("pickupPlaceId", pickupPlaceId);
                    if(getIntent().getStringExtra("dropoffPlacePlaceId") != null)
                        intent.putExtra("dropoffPlaceId", dropoffPlaceId);

                    if(getIntent().getStringExtra("pickupName") != null) {
                        intent.putExtra("pickupName", getIntent().getStringExtra("pickupName"));
                        intent.putExtra("pickupLatLng", getIntent().getStringExtra("pickupLatLng"));
                        intent.putExtra("pickupLat", getIntent().getDoubleExtra("pickupLat", 0));
                        intent.putExtra("pickupLng", getIntent().getDoubleExtra("pickupLng", 0));
                    }

                    if(getIntent().getStringExtra("dropoffName") != null) {
                        intent.putExtra("dropoffName", getIntent().getStringExtra("dropoffName"));
                        intent.putExtra("dropoffLatLng", getIntent().getStringExtra("dropoffLatLng"));
                        intent.putExtra("dropoffLat", getIntent().getDoubleExtra("dropoffLat", 0));
                        intent.putExtra("dropoffLng", getIntent().getDoubleExtra("dropoffLng", 0));
                    }
                    intent.putExtra("from", from);
                }
                startActivity(intent);
                finish();
            }
        });
    }

    private void noLocationFound(boolean notFound) {
        blankView.setVisibility(View.GONE);
        if(notFound) {
            blankView.setVisibility(View.VISIBLE);
            listRecVw.setVisibility(View.GONE);
        }
    }

    private void finishedLoading(boolean isFinished) {
        if(isFinished) {
            listRecVw.setVisibility(View.VISIBLE);
            loadingView.setVisibility(View.GONE);
        } else {
            listRecVw.setVisibility(View.GONE);
            loadingView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onPlaceClick(ArrayList<PlaceAutocompleteObject> mResultList, int position) {
        Intent intent = new Intent(SearchActivity.this, LandingActivity.class);
        if(getIntent().getIntExtra("hasDestination", -1) == 1) {
            intent.putExtra("hasDestination", 1);
            intent.putExtra("destinationPlaceId", mResultList.get(position).getPlaceId());
        } else {
            if(from == 0) {
                if(hasPickup == 1) {
                    intent.putExtra("hasPickup", 1);
                    intent.putExtra("pickupPlaceId", mResultList.get(position).getPlaceId());
                }

                if(hasDropoff == 1) {
                    intent.putExtra("hasDropoff", 1);
                    intent.putExtra("dropoffPlaceId", dropoffPlaceId);
                }
            } else {
                if(hasPickup == 1) {
                    intent.putExtra("hasPickup", 1);
                    intent.putExtra("pickupPlaceId", pickupPlaceId);
                }

                if(hasDropoff == 1) {
                    intent.putExtra("hasDropoff", 1);
                    intent.putExtra("dropoffPlaceId", mResultList.get(position).getPlaceId());
                } else {
                    intent.putExtra("hasDropoff", 1);
                    intent.putExtra("dropoffPlaceId", mResultList.get(position).getPlaceId());
                }
            }
        }
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(SearchActivity.this, LandingActivity.class);
        if(getIntent().getIntExtra("hasDestination", -1) == 1) {
            intent.putExtra("hasDestination", 1);
            if(getIntent().getStringExtra("destinationName") != null) {
                intent.putExtra("destinationName", getIntent().getStringExtra("destinationName"));
                intent.putExtra("destinationLatLng", getIntent().getStringExtra("destinationLatLng"));
                intent.putExtra("destinationLat", getIntent().getDoubleExtra("destinationLat", 0));
                intent.putExtra("destinationLng", getIntent().getDoubleExtra("destinationLng", 0));
            } else {
                intent.putExtra("destinationPlaceId", destinationPlaceId);
            }
        } else {
            if(hasPickup == 1) {
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
            if(hasDropoff == 1) {
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
        }
        startActivity(intent);
        finish();
    }
}
