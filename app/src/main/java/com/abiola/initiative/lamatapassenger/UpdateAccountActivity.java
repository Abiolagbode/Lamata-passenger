package com.abiola.initiative.lamatapassenger;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.abiola.initiative.lamatapassenger.libraries.SnackBarCreator;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.apache.commons.lang3.text.WordUtils;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class UpdateAccountActivity extends AppCompatActivity {

    private TextView name_txt_vw;
    private EditText mobnum_edit_txt;
    private EditText email_edit_txt;
    private ImageView profpic_img_vw;
    private Button submit_btn;
    private Button change_trusted_btn;

    private String name;
    private String email;
    private String mobnum;
    private String profpic;
    private String trusted_id;
    private String auth;
    private String database_id;
    private Uri resultUri;
    private CountDownTimer typing;

    private boolean mobnum_error_empty = false;
    private boolean mobnum_error_match = false;
    private boolean email_error_empty = false;
    private boolean email_error_match = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_account);
        setDetails();

        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        profpic_img_vw = findViewById(R.id.profpic_img_vw);
        name_txt_vw = findViewById(R.id.name_txt_vw);
        mobnum_edit_txt = findViewById(R.id.mobnum_edit_txt);
        email_edit_txt = findViewById(R.id.email_edit_txt);
        submit_btn = findViewById(R.id.submit_btn);
        change_trusted_btn = findViewById(R.id.change_trusted_btn);

        typing = new CountDownTimer(1000, 1000) {
            @Override
            public void onTick(long l) { }

            @Override
            public void onFinish() {
                checkErrors();
            }
        };

       profpic_img_vw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(auth.equals("mobile")) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        if(ActivityCompat.checkSelfPermission(UpdateAccountActivity.this,
                                android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(UpdateAccountActivity.this,
                                    new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},
                                    1);
                            return;
                        }
                    }
                    Intent intent = new Intent(Intent.ACTION_PICK);
                    intent.setType("image/*");
                    startActivityForResult(intent, 1);
                } else {
                    SnackBarCreator.show(view);
                    SnackBarCreator.set("You cannot update your profile picture.");
                }
            }
        });

        mobnum_edit_txt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                mobnum = mobnum_edit_txt.getText().toString().isEmpty() ? "" : mobnum_edit_txt.getText().toString();
                typing.start();
            }

            @Override
            public void afterTextChanged(Editable editable) { }
        });

        email_edit_txt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                email = email_edit_txt.getText().toString().isEmpty() ? "" : email_edit_txt.getText().toString();
                typing.start();
            }

            @Override
            public void afterTextChanged(Editable editable) { }
        });

        submit_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(checkErrors())
                    updateDatabase();
            }
        });

        change_trusted_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(UpdateAccountActivity.this, TrustedContactActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean checkErrors() {
        if(mobnum.isEmpty()) {
            mobnum_error_empty = true;
        } else {
            mobnum_error_empty = false;
        }

        if(mobnum.matches("^(09|\\+234)\\d{9}$")) {
            mobnum_error_match = false;
        } else {
            mobnum_error_match = true;
        }

        if(email.isEmpty()) {
            email_error_empty = true;
        } else {
            email_error_empty = false;
        }

        if(email.matches("^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$")) {
            email_error_match = false;
        } else {
            email_error_match = true;
        }
        putErrors();
        return !(mobnum_error_empty && mobnum_error_match && email_error_empty && email_error_empty);
    }

    private void putErrors() {
        if(mobnum_error_empty)
            mobnum_edit_txt.setError("Mobile Number is required.", getResources().getDrawable(R.drawable.ic_warning_red_24dp));
        else if(mobnum_error_match)
            mobnum_edit_txt.setError("Not a valid Nigeria Number.", getResources().getDrawable(R.drawable.ic_warning_red_24dp));

        if(email_error_empty)
            email_edit_txt.setError("Email is required.", getResources().getDrawable(R.drawable.ic_warning_red_24dp));
        else if(email_error_match)
            email_edit_txt.setError("Not a valid Email Address.", getResources().getDrawable(R.drawable.ic_warning_red_24dp));

        if(!(mobnum_error_empty && mobnum_error_match)) {
          mobnum_edit_txt.setError(null);
        } else if(!(email_error_empty && email_error_empty)) {
          email_edit_txt.setError(null);
        }
    }

    private void updateDatabase() {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Uploading Data...");
        progressDialog.setIndeterminate(false);
        progressDialog.setCancelable(false);
        progressDialog.show();
        if(resultUri != null) {
            profpic = resultUri.toString();
            StorageReference file = FirebaseStorage
                    .getInstance()
                    .getReference("profile_images/" + FirebaseAuth.getInstance().getUid());
            Bitmap bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getApplication().getContentResolver(), resultUri);
            } catch(IOException e) {
                e.printStackTrace();
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 20, baos);
            byte[] data = baos.toByteArray();
            UploadTask uploadTask = file.putBytes(data);

            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Uri downloadUrl = taskSnapshot.getDownloadUrl();
                    DatabaseReference pssngrdetails = FirebaseDatabase
                            .getInstance()
                            .getReference("users/passenger/" + FirebaseAuth.getInstance().getUid());
                    pssngrdetails.child("mobnum").setValue(mobnum);
                    pssngrdetails.child("email").setValue(email);
                    pssngrdetails.child("profile_pic").setValue(downloadUrl.toString());
                    pssngrdetails.child("trusted").setValue(trusted_id);
                    progressDialog.dismiss();
                    saveUserDetails();
                }
            });

            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressDialog.dismiss();
                    Toast.makeText(UpdateAccountActivity.this, "Error when uploading data.", Toast.LENGTH_SHORT).show();
                    finish();
                }
            });
        } else {
            DatabaseReference pssngrdetails = FirebaseDatabase
                    .getInstance()
                    .getReference("users/passenger/" + FirebaseAuth.getInstance().getUid());
            pssngrdetails.child("mobnum").setValue(mobnum);
            pssngrdetails.child("email").setValue(email);
            pssngrdetails.child("profile_pic").setValue(profpic);
            pssngrdetails.child("trusted").setValue(trusted_id);
            progressDialog.dismiss();
            saveUserDetails();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        setDetails();
    }

    private void setDetails() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String JSON_DETAILS_KEY = "userDetails";
        String userDetails = sharedPref.getString(JSON_DETAILS_KEY, "{ \"name\" : NULL }");
        try {
            JSONObject jsonObject = new JSONObject(userDetails);
            if(!jsonObject.getString("name").equals("NULL")) {
                name = jsonObject.getString("name");
                email = jsonObject.getString("email");
                mobnum = jsonObject.getString("mobnum");
                profpic = jsonObject.getString("profile_pic");
                trusted_id = jsonObject.getString("trusted_id");
                database_id = jsonObject.getString("database_id");
                auth = jsonObject.getString("auth");
                if(!profpic.equals("default") && resultUri == null) {
                    Glide.with(this)
                            .load(profpic)
                            .apply(RequestOptions.circleCropTransform())
                            .into(profpic_img_vw);
                }

                name_txt_vw.setText(name);
                email_edit_txt.setText(email.equals("null") ? "" : email);
                mobnum_edit_txt.setText(mobnum.equals("null") ? "" : mobnum);
            }
        } catch (Exception e) { }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, 1);
                break;
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
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1 && resultCode == Activity.RESULT_OK){
            final Uri imageUri = data.getData();
            resultUri = imageUri;
            Glide.with(UpdateAccountActivity.this)
                    .load(resultUri)
                    .apply(RequestOptions.circleCropTransform())
                    .into(profpic_img_vw);
        }
    }
}
