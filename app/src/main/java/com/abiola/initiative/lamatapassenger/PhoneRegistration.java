package com.abiola.initiative.lamatapassenger;

import android.content.Intent;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

public class PhoneRegistration extends AppCompatActivity {

    private EditText mobET;
    private EditText nameET;
    private EditText emailET;
    private TextInputLayout mobTL;
    private TextInputLayout nameTL;
    private TextInputLayout emailTL;

    private String mobnum = "";
    private String name = "";
    private String email = "";
    private boolean mobET_error_empty = false;
    private boolean nameET_error_empty = false;
    private boolean emailET_error_empty = false;
    private boolean mobET_error_match = false;
    private boolean nameET_error_match = false;
    private boolean emailET_error_match = false;
    private boolean mobile_check = false;
    private boolean name_check = false;
    private boolean email_check = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_registration);
        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        mobET = findViewById(R.id.mobET);
        nameET = findViewById(R.id.nameET);
        emailET = findViewById(R.id.emailET);

        mobTL = findViewById(R.id.mobTL);
        nameTL = findViewById(R.id.nameTl);
        emailTL = findViewById(R.id.emailTL);

        mobET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {  }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                mobnum = mobET.getText().toString().isEmpty() ? "" : mobET.getText().toString();
                checkMobErrors();
            }

            @Override
            public void afterTextChanged(Editable editable) { }
        });

        nameET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                name = nameET.getText().toString().isEmpty() ? "" : nameET.getText().toString();
                checkNameErrors();
            }

            @Override
            public void afterTextChanged(Editable editable) { }
        });

        emailET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                email = emailET.getText().toString().isEmpty() ? "" : emailET.getText().toString();
                checkEmailErrors();
            }

            @Override
            public void afterTextChanged(Editable editable) { }
        });
    }

    private void checkMobErrors() {
        if(mobnum.isEmpty()) {
            mobET_error_empty = true;
        } else {
            mobET_error_empty = false;
        }

        if(mobnum.matches("^(09|\\+234)\\d{9}$")) {
            mobET_error_match = false;
        } else {
            mobET_error_match = true;
        }
        putMobErrors();
    }

    private void checkNameErrors() {
        if(name.isEmpty()) {
            nameET_error_empty = true;
        } else {
            nameET_error_empty = false;
        }

        if(name.matches(".*\\d+.*")) {
            nameET_error_match = false;
        } else {
            nameET_error_match = true;
        }
        putNameErrors();
    }

    private void checkEmailErrors() {
        if(email.isEmpty()) {
            emailET_error_empty = true;
        } else {
            emailET_error_empty = false;
        }

        if(email.matches("^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$")) {
            emailET_error_match = false;
        } else {
            emailET_error_match = true;
        }
        putEmailErrors();
    }

    private void putMobErrors() {
        if(mobET_error_empty) {
            setError("Enter your mobile number.", mobTL, mobET);
            mobile_check = false;
            return;
        } else {
            clearError(mobTL, mobET);
        }

        if(mobET_error_match) {
            setError("Enter a valid Nigeria mobile number.", mobTL, mobET);
            mobile_check = false;
            return;
        } else {
            clearError(mobTL, mobET);
        }

        mobile_check = true;
    }

    private void putEmailErrors() {
        if(emailET_error_empty) {
            setError("Enter your email address.", emailTL, emailET);
            email_check = false;
            return;
        } else {
            clearError(emailTL, emailET);
        }

        if(emailET_error_match) {
            setError("Enter a valid email address.", emailTL, emailET);
            email_check = false;
            return;
        } else {
            clearError(emailTL, emailET);
        }

        email_check = true;
    }

    private void putNameErrors() {
        if(nameET_error_empty) {
            setError("Enter your name.", nameTL, nameET);
            name_check = false;
            return;
        } else {
            clearError(nameTL, nameET);
        }

        if(!nameET_error_match) {
            setError("Name contains a number.", nameTL, nameET);
            name_check = false;
            return;
        } else {
            clearError(nameTL, nameET);
        }

        name_check = true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.next:
                Intent intent = new Intent(PhoneRegistration.this, PhoneAuthentication.class);
                intent.putExtra("mobnum", mobnum);
                intent.putExtra("name", name);
                intent.putExtra("email", email);
                intent.putExtra("phoneReg", true);
                finish();
                startActivity(intent);
                break;
        }
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if(name_check && mobile_check && email_check) {
            menu.findItem(R.id.next).setEnabled(true);
        } else {
            menu.findItem(R.id.next).setEnabled(false);
        }
        return true ;
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(PhoneRegistration.this, SignUpActivity.class);
        finish();
        startActivity(intent);
        return;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.phone_reg, menu);
        return super.onCreateOptionsMenu(menu);
    }

    private void setError(String message, TextInputLayout tl, EditText et) {
        tl.setError(message);
        et.setCompoundDrawablesWithIntrinsicBounds(0,0, R.drawable.ic_warning_red_24dp,0);
        invalidateOptionsMenu();
    }

    private void clearError(TextInputLayout tl, EditText et) {
        tl.setError(null);
        tl.setErrorEnabled(false);
        et.setCompoundDrawablesWithIntrinsicBounds(0,0, 0,0);
        invalidateOptionsMenu();
    }
}