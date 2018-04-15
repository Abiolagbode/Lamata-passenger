package com.abiola.initiative.lamatapassenger;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.abiola.initiative.lamatapassenger.adapter.TrustedContactAdapter;
import com.abiola.initiative.lamatapassenger.libraries.DialogCreator;
import com.abiola.initiative.lamatapassenger.libraries.SimpleDividerItemDecoration;
import com.abiola.initiative.lamatapassenger.object.TrustedContactObject;

import org.apache.commons.lang3.text.WordUtils;
import org.json.JSONObject;

import java.util.ArrayList;

public class TrustedContactActivity extends AppCompatActivity implements
        TrustedContactAdapter.TrustedContactAdapterInterface,
        DialogCreator.DialogActionListener {

    private RecyclerView userList;
    private TrustedContactAdapter mAdapter;
    private EditText search_edit_text;

    private String name;
    private String email;
    private String mobnum;
    private String profpic;
    private String trusted_id;
    private String auth;
    private String database_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trusted_contact);
        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        setDetails();
        mAdapter = new TrustedContactAdapter(this, this);
        userList = findViewById(R.id.userList);
        userList.setHasFixedSize(true);
        userList.setLayoutManager(new LinearLayoutManager(this));
        userList.setAdapter(mAdapter);
        userList.addItemDecoration(new SimpleDividerItemDecoration(this));

        search_edit_text = findViewById(R.id.search_edit_text);
        search_edit_text.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                mAdapter.getFilter().filter(charSequence);
            }

            @Override
            public void afterTextChanged(Editable editable) { }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.trusted_contact_menu, menu);
        return true;
    }

    @Override
    public void onUserClick(ArrayList<TrustedContactObject> mResultList, int position) {
        TrustedContactObject contact = mResultList.get(position);
        trusted_id = contact.getUid();
        DialogCreator.create(TrustedContactActivity.this, "trustedContact")
                .setTitle("Are you sure?")
                .setMessage(contact.getName() + " as your trusted contact?")
                .setPositiveButton("YES")
                .setNegativeButton("NO")
                .show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home) {
            finish();
        } else if(item.getItemId() == R.id.delete) {
            trusted_id = "null";
            DialogCreator.create(TrustedContactActivity.this, "trustedContact")
                    .setTitle("Are you sure?")
                    .setMessage("To delete your trusted contact?")
                    .setPositiveButton("YES")
                    .setNegativeButton("NO")
                    .show();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClickPositiveButton(String actionId) {
        if(actionId.equals("trustedContact")) {
            saveUserDetails();
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
            }
        } catch (Exception e) { }
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
}
