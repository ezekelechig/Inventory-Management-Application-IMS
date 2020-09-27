package com.inventorymanagementsystem.ims.Activity;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;

import com.inventorymanagementsystem.ims.R;
import com.inventorymanagementsystem.ims.Utils.Constants;
import com.inventorymanagementsystem.ims.Utils.Utils;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

import eu.inmite.android.lib.validations.form.FormValidator;
import eu.inmite.android.lib.validations.form.annotations.NotEmpty;
import eu.inmite.android.lib.validations.form.annotations.RegExp;
import eu.inmite.android.lib.validations.form.callback.SimpleErrorPopupCallback;

public class RegisterActivity extends AppCompatActivity {


    //Defining Regular expression for email validation
    final String EMAIL = "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" +
            "\\@" +
            "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
            "(" +
            "\\." +
            "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
            ")+";

    //to check if username empty or not
    @NotEmpty(messageId = R.string.null_username, order = 1)
    AutoCompleteTextView tvUsername;


    //Calling Regular expression validation, order 1 means this will be checked first
    @RegExp(value = EMAIL, messageId = R.string.invalid_email, order = 2)
    AutoCompleteTextView tvUserEmail;

    //To check if set password is empty or not
    @NotEmpty(messageId = R.string.null_pass, order = 3)
    AutoCompleteTextView tvPassword;

    //To check if confirm password if empty or not
    @NotEmpty(messageId = R.string.null_confirm_pass, order = 4)
    AutoCompleteTextView tvConfirmPassword;


    private static final String TAG = "RegisterActivity";
    Toolbar toolbar;
    ActionBar actionBar;


    Button btnRegister;


    //Default Parse User table Object
    ParseUser user;

    Boolean isValid;

    String strUserName, strUserEmail, strPassword;

    AppCompatCheckBox terms_and_conditions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        setPrefValues();
        findViewsById();
        setupActionbar();

        user = new ParseUser();

        //Action when user presses the register button
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //sets a boolean value true to isValid if all validation conditions are satisfied
                isValid = FormValidator.validate(RegisterActivity.this, new SimpleErrorPopupCallback(getApplicationContext(), true));

                //if validation true, registration works, else it shows error msg in each textbox
                if (isValid && terms_and_conditions.isChecked()) {

                    //once all fields and terms and conditions checked, check if internet connection is made
                    if (Utils.isNetworkAvailable(RegisterActivity.this)) {

                        if (tvPassword.getText().toString().equals(tvConfirmPassword.getText().toString())) {

                            Utils.showToast(RegisterActivity.this, "Registering. Please wait...");

                            //set username, useremail and password in user table in parse
                            strUserName = tvUsername.getText().toString();
                            strUserEmail = tvUserEmail.getText().toString();
                            strPassword = tvPassword.getText().toString();

                            user.setUsername(strUserName);
                            user.setEmail(strUserEmail);
                            user.setPassword(strPassword);

                            //complete new user registration in parse user table
                            user.signUpInBackground(new SignUpCallback() {
                                @Override
                                public void done(ParseException e) {

                                    Utils.hideProgressView();

                                    if (e == null) {

                                        System.out.println("Sign up successful:");
                                        Utils.showToast(RegisterActivity.this, "Registration Successful!");
                                        startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                                    } else {

                                        System.out.println("Sign up error:" + e);
                                        Utils.showToast(RegisterActivity.this, "Oops! Something went wrong.");
                                        // Sign up didn't succeed. Look at the ParseException
                                        // to figure out what went wrong
                                    }

                                }
                            });
                        } else
                            Utils.showToast(RegisterActivity.this, "Passwords did not match");
                    } else
                        Utils.noNetMessage(RegisterActivity.this);
                } else {
                    alert("Please check terms and conditions.");
                }
            }
        });

    }


    /**
     * Creates an alert dialog with specific string
     *
     * @param value
     */
    public void alert(String value) {
        AlertDialog.Builder alert_dialog = new AlertDialog.Builder(this);
        alert_dialog.setMessage(value);
        alert_dialog.setCancelable(false);
        alert_dialog.setNeutralButton(android.R.string.ok,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = alert_dialog.create();
        alert.show();
    }

    private void setUpProgressDialog() {

    }

    private void setupActionbar() {
        setSupportActionBar(toolbar);

        actionBar = getSupportActionBar();

        setTitle("Client Registration");

        // Enable the Up button
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    private void findViewsById() {
        toolbar = (Toolbar) findViewById(R.id.registerToolbar);
        btnRegister = (Button) findViewById(R.id.btn_register);
        tvUsername = (AutoCompleteTextView) findViewById(R.id.tv_username);
        tvUserEmail = (AutoCompleteTextView) findViewById(R.id.tv_email);
        tvPassword = (AutoCompleteTextView) findViewById(R.id.tv_password);
        tvConfirmPassword = (AutoCompleteTextView) findViewById(R.id.tv_confirm_password);
        terms_and_conditions = (AppCompatCheckBox) findViewById(R.id.terms_and_conditions);
    }

    private void setPrefValues() {
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_basic, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case android.R.id.home:
                onBackPressed();
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
}
