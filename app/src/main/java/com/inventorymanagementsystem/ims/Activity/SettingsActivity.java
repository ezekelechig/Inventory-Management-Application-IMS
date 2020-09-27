package com.inventorymanagementsystem.ims.Activity;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.inventorymanagementsystem.ims.Adapters.FontManager;
import com.inventorymanagementsystem.ims.R;
import com.inventorymanagementsystem.ims.Utils.Utils;
import com.parse.ParseUser;


public class SettingsActivity extends AppCompatActivity {

    //Java activity class for Settings

    Toolbar toolbar;
    ActionBar actionBar;
    TextView iconAboutUs, iconNotification, iconEmail, iconPassword;

    AlertDialog alertDialog;

    LinearLayout llChangePassword;

    String strNewPass, strNewPassConfirm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        setPrefValues();
        findViewsById();
        setupActionbar();
        setUpProgressDialog();

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
                break;

            case R.id.action_settings:
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void setUpProgressDialog() {

    }

    private void setupActionbar() {
        setSupportActionBar(toolbar);

        actionBar = getSupportActionBar();

        setTitle("Settings");

        // Enable the Up button
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    private void findViewsById() {
        Typeface iconFont = FontManager.getTypeface(getBaseContext(), FontManager.FONTAWESOME);

        toolbar = (Toolbar) findViewById(R.id.settingsToolbar);

//        TextViews
        iconAboutUs = (TextView) findViewById(R.id.iconAboutUs);
        iconNotification = (TextView) findViewById(R.id.iconNotification);
        iconEmail = (TextView) findViewById(R.id.iconEmail);
        iconPassword = (TextView) findViewById(R.id.iconPassword);

//        Set Icon TypeFace
        iconAboutUs.setTypeface(iconFont);
        iconNotification.setTypeface(iconFont);
        iconEmail.setTypeface(iconFont);
        iconPassword.setTypeface(iconFont);

        llChangePassword = (LinearLayout) findViewById(R.id.changePassword);


        //On clicking option to change password
        llChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                changeCurrentUserPassword();
            }
        });
    }

    private void setPrefValues() {
    }

    public void onBackPressed(){
        super.onBackPressed();
        finish();
    }

    public void changeCurrentUserPassword(){

        //show a diaglog box where user has to enter a new password and then confirm the change in password

        alertDialog = new android.support.v7.app.AlertDialog.Builder(SettingsActivity.this).create();
        alertDialog.setCancelable(true);

        LayoutInflater inflater = getLayoutInflater();
        final View dialogLayout = inflater.inflate(R.layout.dialog_change_password, null);
        alertDialog.setView(dialogLayout);

        TextView tvPassChange = (TextView) dialogLayout.findViewById(R.id.tv_login_pass_change);

        final EditText edNewPass = (EditText) dialogLayout.findViewById(R.id.ed_new_pass);
        final EditText edNewPassConfirm = (EditText) dialogLayout.findViewById(R.id.ed_new_pass_confirm);


        tvPassChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                strNewPass = edNewPass.getText().toString();
                strNewPassConfirm = edNewPassConfirm.getText().toString();

                if(strNewPass.equals(""))
                    Utils.showToast(SettingsActivity.this, "Please enter new password");
                else if(strNewPassConfirm.equals(""))
                    Utils.showToast(SettingsActivity.this, "Please confirm new password");
                else if(!strNewPass.equals(strNewPassConfirm))
                    Utils.showToast(SettingsActivity.this, "New and confirm password don't match");
                else
                    methodToChangePass();

            }
        });

        alertDialog.show();

    }

    //Method to change current user password
    public void methodToChangePass(){

        alertDialog.cancel();

        //check if internet is available, if yes, do as below
        if(Utils.isNetworkAvailable(SettingsActivity.this)) {


            //gets current user's parse info
            ParseUser user = ParseUser.getCurrentUser();
            //sets the new password into current user's password value in parse
            user.setPassword(strNewPass);
            //saves the task
            user.saveInBackground();
            //show a toast message saying change in password successful

            Utils.showToast(SettingsActivity.this, "Password changed successfully");
        }else {
            //in case of no internet, show no internet toast message
            Utils.noNetMessage(SettingsActivity.this);
        }

    }
}
