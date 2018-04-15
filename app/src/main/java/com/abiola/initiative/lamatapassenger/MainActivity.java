package com.abiola.initiative.lamatapassenger;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.abiola.initiative.lamatapassenger.config.TaskConfig;
import com.abiola.initiative.lamatapassenger.libraries.DialogCreator;
import com.abiola.initiative.lamatapassenger.tasks.SuperTask;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import org.apache.commons.lang3.text.WordUtils;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity implements DialogCreator.DialogActionListener,
        SuperTask.TaskListener {

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private String name;
    private String email;
    private String mobnum;
    private String profpic;
    private String trusted_id = "null";
    private String database_id;
    private String auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ImageView imageView = findViewById(R.id.loader);

        TaskConfig.CURRENT_TOKEN = FirebaseInstanceId.getInstance().getToken();

        Glide.with(this).load(getImage("loader")).into(imageView);

        mAuth = FirebaseAuth.getInstance();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if(user == null) {
                    Intent intent = new Intent(MainActivity.this, SignUpActivity.class);
                    startActivity(intent);
                    finish();
                    return;
                } else {
                    if(SuperTask.isNetworkAvailable(MainActivity.this)) {
                        SuperTask.execute(MainActivity.this,
                                TaskConfig.CHECK_CONNECTION_URL,
                                "check_connection");
                    } else {
                        DialogCreator.create(MainActivity.this, "connectionError")
                                .setCancelable(false)
                                .setTitle("No Internet Connection")
                                .setMessage("This application needs an Internet Connection.")
                                .setPositiveButton("Exit")
                                .show();
                    }
                }
            }
        };
    }

    private void updateUserdetails() {
        DatabaseReference dbRef = FirebaseDatabase.getInstance()
                .getReference("users/passenger/"+FirebaseAuth.getInstance().getUid());
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot != null) {
                    for(DataSnapshot details : dataSnapshot.getChildren()) {
                        if(details.getKey().equals("auth")) auth = details.getValue().toString();
                        if(details.getKey().equals("email")) email = details.getValue().toString();
                        if(details.getKey().equals("mobnum")) mobnum = details.getValue().toString();
                        if(details.getKey().equals("name")) name = details.getValue().toString();
                        if(details.getKey().equals("profile_pic")) profpic = details.getValue().toString();
                        if(details.getKey().equals("trusted")) trusted_id = details.getValue().toString();
                    }
                    SuperTask.execute(MainActivity.this,
                            TaskConfig.REGISTER_USER_URL,
                            "register");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) { }
        });

    }

    private void saveUserDetails() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = sharedPref.edit();
        String JSON_DETAILS_KEY = "userDetails";
        String jsonDetails = "{ \"name\" : \"" + WordUtils.capitalize(name.toLowerCase()) + "\", " +
                "\"email\" : \"" + email + "\", " +
                "\"mobnum\" : \"" + mobnum + "\", " +
                "\"profile_pic\" : \"" + profpic + "\", " +
                "\"trusted_id\" : \"" + trusted_id + "\", " +
                "\"auth\" : \"" + auth + "\", " +
                "\"database_id\": \""+ database_id +"\"}";
        editor.putString(JSON_DETAILS_KEY, jsonDetails);
        editor.apply();
        Intent intent = new Intent(MainActivity.this, LandingActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onTaskRespond(String json, String id) {
        switch (id) {
            case "register":
                try {
                    JSONObject jsonObject = new JSONObject(json);
                    if(jsonObject.getString("status").equals("success")) {
                        database_id = jsonObject.getString("database_id");
                        saveUserDetails();
                    }
                } catch (Exception e) { }
                break;
            case "check_connection":
                try {
                    JSONObject jsonObject = new JSONObject(json);
                    if(jsonObject.getString("status").equals("success")) {
                        updateUserdetails();
                    }
                } catch (NullPointerException e) {
                    DialogCreator.create(MainActivity.this, "connectionError")
                            .setCancelable(false)
                            .setTitle("No Internet Connection")
                            .setMessage("This application needs an Internet Connection.")
                            .setPositiveButton("Exit")
                            .show();
                } catch (Exception e) { }
                break;
        }
    }

    @Override
    public ContentValues setRequestValues(ContentValues contentValues, String id) {
        contentValues.put("android", 1);
        switch (id) {
            case "register":
                contentValues.put("firebase_uid", FirebaseAuth.getInstance().getCurrentUser().getUid());
                contentValues.put("name", name);
                contentValues.put("email", email);
                contentValues.put("contact", mobnum);
                contentValues.put("img_path", profpic);
                contentValues.put("token", TaskConfig.CURRENT_TOKEN);
                return contentValues;
        }
        return contentValues;
    }

    public int getImage(String imageName) {
        int drawableResourceId = this.getResources()
                .getIdentifier(imageName, "drawable", this.getPackageName());
        return drawableResourceId;
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mAuth.removeAuthStateListener(mAuthListener);
    }

    @Override
    public void onClickPositiveButton(String actionId) {
        switch (actionId) {
            case "connectionError":
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
    public void onCreateDialogView(String actionId, View view) { }
}
