package com.inventorymanagementsystem.ims.Activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.inventorymanagementsystem.ims.R;
import com.inventorymanagementsystem.ims.Utils.SessionManager;
import com.inventorymanagementsystem.ims.Utils.Utils;
import com.parse.LogInCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseInstallation;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.Arrays;
import java.util.List;

import eu.inmite.android.lib.validations.form.FormValidator;
import eu.inmite.android.lib.validations.form.annotations.NotEmpty;
import eu.inmite.android.lib.validations.form.annotations.RegExp;
import eu.inmite.android.lib.validations.form.callback.SimpleErrorPopupCallback;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    final String EMAIL = "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" +
            "\\@" +
            "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
            "(" +
            "\\." +
            "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
            ")+";

    //Calling Regular expression validation, order 1 means this will be checked first
    //@RegExp(value = EMAIL, messageId = R.string.invalid_email, order = 1)
    EditText tvLoginUsername;

    //To check if set password is empty or not
    @NotEmpty(messageId = R.string.null_pass, order = 1)
    EditText edLoginPass;


    Button email_sign_in_button, btnFbLogin;
    TextView sign_up_button;

    List<String> permissions = Arrays.asList("public_profile", "email");

    ParseUser loginUser;

    //For forgot password option
    //ParseQuery<ParseUser> loginQuery;

    Boolean isValid;

    String strLoginUName, strLoginUPass;

    SessionManager sessionManager;

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        sharedPreferences = this.getSharedPreferences("IMSPreferences", 0);

        sessionManager = new SessionManager(this);

        email_sign_in_button = (Button) findViewById(R.id.email_sign_in_button);
        sign_up_button = (TextView) findViewById(R.id.sign_up_button);
        tvLoginUsername = (EditText) findViewById(R.id.tv_login_username);
        edLoginPass = (EditText) findViewById(R.id.tv_login_password);
        btnFbLogin = (Button) findViewById(R.id.btn_fb_login);

        loginUser = new ParseUser();

        //loginQuery = ParseUser.getQuery();


        //Method call for when Login Button is pressed
        email_sign_in_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                login();

            }
        });

        //Listens if user edits value in the login Pass Edit text
        edLoginPass.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                if (actionId == EditorInfo.IME_ACTION_UNSPECIFIED) {

                    login();
                    return true;
                }

                return false;
            }
        });


        //Call register page when sign up button is pressed
        sign_up_button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

        //Method call for when Facebook Login Button is pressed
        btnFbLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ParseFacebookUtils.logInWithReadPermissionsInBackground(LoginActivity.this, permissions, new LogInCallback() {
                    @Override
                    public void done(ParseUser parseUser, ParseException e) {

                        if (parseUser == null)
                            Log.d("FbLogin: ", "User cancelled FB Login " + parseUser.getUsername() + "--" + parseUser.getEmail());

                        else if (parseUser.isNew()) {
                            Log.d("FbLogin: ", "User signed up and logged in thru FB " + parseUser.getString("email") + "--" + parseUser.getEmail() + "--" + parseUser);
                            getUserDetailsFromFb();
                        } else {
                            Log.d("FbLogin: ", "User logged in thru FB ");
                            getUserDetailsFromFb();
                        }

                    }
                });
            }
        });

    }

    //Method to access user's FB account basic info (Email and username) for FB login purpose
    public void getUserDetailsFromFb() {

        Bundle parameters = new Bundle();
        parameters.putString("fields", "email, name");

        new GraphRequest(AccessToken.getCurrentAccessToken(), "/me", parameters, HttpMethod.GET, new GraphRequest.Callback() {

            @Override
            public void onCompleted(GraphResponse response) {


                try {

                    Log.d("FbUserMail", ": " + response.toString());

                    loginUser = new ParseUser();

                    Log.d("FbUserMail", ": " + response.getJSONObject().getString("email"));
                    Log.d("FbUserName", ": " + response.getJSONObject().getString("name"));

                    //email is username
                    saveNewUser(response.getJSONObject().getString("email"),
                            response.getJSONObject().getString("name"));

                } catch (Exception e) {
                    e.printStackTrace();
                }


            }
        }).executeAsync();

    }

    //Method called once a new user tries to login using Facebook account
    private void saveNewUser(final String email, String username) {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Logging in...");
        progressDialog.show();

        if (email != null && !email.isEmpty()) {
            loginUser = ParseUser.getCurrentUser();
            installDevice();
            loginUser.setEmail(email);              //sets user Email in db from fb

            if (username != null)
                loginUser.setUsername(username);    //sets user Name in db from fb

            loginUser.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {

                    //Login Successful, now open the HomePage
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    Toast.makeText(LoginActivity.this, email + " signed up", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(LoginActivity.this, "Sorry, sign up failed! Please, try again.", Toast.LENGTH_SHORT).show();
        }
        progressDialog.dismiss();
    }

    //Installing a new device ID in case of login through facebook
    void installDevice() {
        ParseInstallation installation = ParseInstallation.getCurrentInstallation();
        installation.put("usrObjId", loginUser.getObjectId());
        installation.saveInBackground();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        ParseFacebookUtils.onActivityResult(requestCode, resultCode, data);
    }


    ////Method to get User Login details from Parse DB and authenticate login - - called when user tries manual login
    public void login() {

        hideSoftKeyboard();

        //sets a boolean value true to isValid if all validation conditions are satisfied
        isValid = FormValidator.validate(LoginActivity.this, new SimpleErrorPopupCallback(getApplicationContext(), true));

        //sets a boolean value true to isValid if all validation conditions are satisfied
        if (isValid) {

            //Check if internet connection is available, if yes, proceed
            if (Utils.isNetworkAvailable(LoginActivity.this)) {

                Utils.showProgressView(LoginActivity.this, "Signing in. Please wait...");

                strLoginUName = tvLoginUsername.getText().toString();
                strLoginUPass = edLoginPass.getText().toString();

                Log.d("LoginCreds", ": " + strLoginUName + "--" + strLoginUPass);

                ParseUser.logInInBackground(strLoginUName, strLoginUPass, new LogInCallback() {
                    @Override
                    public void done(ParseUser parseUser, ParseException e) {

                        Utils.hideProgressView();

                        if (parseUser != null) {
                            Boolean isAdmin = parseUser.getBoolean("is_admin");

                            editor = sharedPreferences.edit();
                            editor.putBoolean("is_admin", isAdmin);
                            editor.apply();

                            System.out.println(sharedPreferences.getBoolean("is_admin", false));
                            System.out.println("login_Success");

                            sessionManager.createLoginSession(strLoginUName, parseUser.getEmail());
                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                            finish();
                        } else {
                            System.out.println("login error:" + e);
                            Utils.showToast(LoginActivity.this, e.getMessage().toUpperCase());
                        }

                    }
                });

            } else {
                //if no internet connection, show no net available toast
                Utils.noNetMessage(LoginActivity.this);
            }
        }

    }

    /**
     * Hides the soft keyboard
     */
    public void hideSoftKeyboard() {
        if (getCurrentFocus() != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }
}

