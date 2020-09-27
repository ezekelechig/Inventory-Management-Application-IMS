package com.inventorymanagementsystem.ims.Activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.inventorymanagementsystem.ims.R;
import com.inventorymanagementsystem.ims.Utils.SessionManager;
import com.inventorymanagementsystem.ims.Utils.Utils;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.List;

public class EditProfileActivity extends AppCompatActivity {

    private static final String TAG = "EditProfileActivity";

    Toolbar toolbar;
    ActionBar actionBar;

    EditText username, firstName, lastName, address;

    SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        sessionManager = new SessionManager(this);

        findViewsById();
        setupActionbar();
        setUpProgressDialog();

        if (Utils.isNetworkAvailable(this)) {
            setValues();
        } else
            Utils.noNetMessage(this);

    }

    private void setUpProgressDialog() {

    }

    private void setupActionbar() {
        setSupportActionBar(toolbar);

        actionBar = getSupportActionBar();

        setTitle("Edit Profile");

        // Enable the Up button
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    private void findViewsById() {

        toolbar = (Toolbar) findViewById(R.id.edit_profile_toolbar);

        username = (EditText) findViewById(R.id.username);
        firstName = (EditText) findViewById(R.id.firstName);
        lastName = (EditText) findViewById(R.id.lastName);
        address = (EditText) findViewById(R.id.address);

    }

    /**
     * Get user details such as first_name , last_name , address from server and populate
     * edit texts
     */
    private void setValues() {
        username.setText(sessionManager.getSessionUserName());

        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereEqualTo("objectId", ParseUser.getCurrentUser().getObjectId());
        query.findInBackground(new FindCallback<ParseUser>() {
            public void done(List<ParseUser> objects, ParseException e) {
                if (e == null) {

                    for (ParseObject parseObject : objects) {

                        firstName.setText(parseObject.getString("first_name"));
                        lastName.setText(parseObject.getString("last_name"));
                        address.setText(parseObject.getString("address"));

                    }

                } else {

                    e.printStackTrace();

                }
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_confirmation, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case android.R.id.home:
                onBackPressed();
                return true;

            case R.id.add:
                if (Utils.isNetworkAvailable(this)) {
                    updateValues(firstName.getText().toString(), lastName.getText().toString(), address.getText().toString());
                } else
                    Utils.noNetMessage(this);
                return true;

            case R.id.action_settings:
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    /**
     * Updates values to parse table "User"
     *
     * @param first_name    "Value of first name"
     * @param last_name     "Value of last name"
     * @param address_value "Value of address"
     */
    public void updateValues(final String first_name, final String last_name, final String address_value) {

        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereEqualTo("objectId", ParseUser.getCurrentUser().getObjectId());
        query.findInBackground(new FindCallback<ParseUser>() {
            public void done(List<ParseUser> objects, ParseException e) {
                if (e == null) {

                    for (ParseObject userObj : objects) {

                        userObj.put("first_name", first_name);
                        userObj.put("last_name", last_name);
                        userObj.put("address", address_value);
                        userObj.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {

                                alert(getString(R.string.profile_updated_successful));

                            }
                        });

                    }

                } else {

                    e.printStackTrace();

                }
            }
        });

    }

    public void alert(String value) {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
        builder1.setMessage(value);
        builder1.setCancelable(false);
        builder1.setNeutralButton(android.R.string.ok,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder1.create();
        alert.show();
    }
}
