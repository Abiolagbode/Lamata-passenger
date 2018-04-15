package com.abiola.initiative.lamatapassenger;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.abiola.initiative.lamatapassenger.config.TaskConfig;
import com.abiola.initiative.lamatapassenger.libraries.SnackBarCreator;
import com.abiola.initiative.lamatapassenger.tasks.SuperTask;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import org.apache.commons.lang3.text.WordUtils;
import org.json.JSONObject;

import java.util.Map;

public class SignUpActivity extends AppCompatActivity implements SuperTask.TaskListener {

    private Button mobLogin;
    private Button googLogin;
    private Button faceLogin;
    private GoogleSignInClient mGoogleSignInClient;
    private LoginButton loginButton;
    private ProgressDialog progressDialog;


    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private String email = "null";
    private String name = "null";
    private String profpic = "null";
    private String mobnum = "null";
    private String trusted_id = "null";
    private String database_id = "null";
    private String auth;
    private static final int RC_SIGN_IN = 1;

    private CallbackManager mCallbackManager;

  Button btnSignUp,btnRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);




        mobLogin = findViewById(R.id.mobLogin);
        googLogin = findViewById(R.id.googLogin);
        faceLogin = findViewById(R.id.faceLogin);

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Login");
        progressDialog.setIndeterminate(false);
        progressDialog.setCancelable(false);

        mCallbackManager = CallbackManager.Factory.create();
        loginButton = findViewById(R.id.login_button);

        if(getIntent().getIntExtra("loginError", -1) == 1) {
            SnackBarCreator.set("Sorry! You're not a Passenger.");
            SnackBarCreator.show(mobLogin);
        }

        loginButton.setReadPermissions("email", "public_profile");
        loginButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                facebookSignin(loginResult.getAccessToken());
                progressDialog.setMessage("Logging in with Facebook...");
                progressDialog.show();
            }

            @Override
            public void onCancel() {
                Log.e("fbLogin", "facebookLoginCancel");
            }

            @Override
            public void onError(FacebookException error) {
                Log.e("fbLogin", "facebookLoginError: " + error.getMessage());
            }
        });

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        mobLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SignUpActivity.this, LandingActivity.class);
                startActivity(intent);
                finish();
                return;
            }
        });

        googLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                googleSignin();
            }
        });

        faceLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginButton.performClick();
            }
        });

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if(mAuth.getCurrentUser() != null) {
                    registerUser();
                    return;
                }
            }
        };
    }

    private void googleSignin() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void facebookSignin(AccessToken token) {
        GraphRequest request = GraphRequest.newMeRequest(
                token,
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(
                            JSONObject object,
                            GraphResponse response) {
                        try {
                            if(object.has("email")) email = object.getString("email");
                            if(object.has("id")) profpic = "https://graph.facebook.com/"
                                    + object.getString("id") + "/picture?width=500&height=500";
                            if(object.has("name")) name = object.getString("name");
                            auth = "facebook";
                        } catch (Exception ignored) { }
                    }
                });
        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,name,email");
        request.setParameters(parameters);
        request.executeAsync();
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.getException() instanceof FirebaseAuthUserCollisionException) {
                            progressDialog.dismiss();
                            LoginManager.getInstance().logOut();
                            SnackBarCreator.set("Email is in-use");
                            SnackBarCreator.show(mobLogin);
                        } else if(!task.isSuccessful()) {
                            Log.e("firebaseFacebook", task.getException().toString());
                        }
                    }
                });
    }

    private void registerUser() {
        String user_id = mAuth.getCurrentUser().getUid();
        final DatabaseReference dbRef = FirebaseDatabase.getInstance()
                .getReference("users/passenger/"+user_id);
        dbRef.child("name").setValue(WordUtils.capitalize(name.toLowerCase()));
        dbRef.child("auth").setValue(auth);
        dbRef.child("profile_pic").setValue(profpic);
        dbRef.child("token").setValue(FirebaseInstanceId.getInstance().getToken());
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    Map<String, Object> value = (Map<String, Object>) dataSnapshot.getValue();
                    if(value.get("mobnum") != null) {
                        if(!value.get("mobnum").toString().equals("null")) {
                            mobnum = value.get("mobnum").toString();
                        } else {
                            mobnum = "null";
                        }
                    } else {
                        dbRef.child("mobnum").setValue(mobnum);
                    }

                    if(value.get("email") != null) {
                        if(!value.get("email").toString().equals("null")) {
                            email = value.get("email").toString();
                        } else {
                            email= "null";
                        }
                    } else {
                        dbRef.child("email").setValue(email);
                    }
                    if(value.get("trusted") != null) {
                        if(!value.get("trusted").toString().equals("null")) {
                            trusted_id = value.get("trusted").toString();
                        } else {
                            trusted_id = "null";
                        }
                    } else {
                        dbRef.child("trusted").setValue(trusted_id);
                    }
                    SuperTask.execute(SignUpActivity.this,
                            TaskConfig.REGISTER_USER_URL,
                            "register");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) { }
        });
    }

    @Override
    public void onTaskRespond(String json, String id) {
        switch (id) {
            case "register":
                try {
                    JSONObject jsonObject = new JSONObject(json);
                    String status = jsonObject.getString("status");
                    if(status.equals("success")) {
                        database_id = jsonObject.getString("database_id");
                        saveUserDetails();
                    }
                } catch (Exception e) { }
                break;
        }
    }

    @Override
    public ContentValues setRequestValues(ContentValues contentValues, String id) {
        switch (id) {
            case "register":
                contentValues.put("android", 1);
                contentValues.put("firebase_uid", FirebaseAuth.getInstance().getCurrentUser().getUid());
                contentValues.put("name", name);
                contentValues.put("email", email);
                contentValues.put("contact", mobnum);
                contentValues.put("img_path", profpic);
                contentValues.put("token", TaskConfig.CURRENT_TOKEN);
                return contentValues;
            default:
                return null;
        }
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
        progressDialog.dismiss();
        Intent intent = new Intent(SignUpActivity.this, LandingActivity.class);
        startActivity(intent);
        finish();
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(!task.isSuccessful()) { }
                    }
                });
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                name = account.getDisplayName();
                email = account.getEmail();
                profpic = account.getPhotoUrl().toString();
                auth = "google";
                progressDialog.setMessage("Logging in with Google...");
                progressDialog.show();
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) { }
        }

        mCallbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(AccessToken.getCurrentAccessToken() != null)
            LoginManager.getInstance().logOut();
    }
}