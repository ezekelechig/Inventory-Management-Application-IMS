package com.inventorymanagementsystem.ims.Activity;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.inventorymanagementsystem.ims.R;
import com.parse.ParseInstallation;
import com.parse.ParseUser;

public class SplashScreenActivity extends AppCompatActivity {

    ParseUser parseUser;
    ParseInstallation installation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_sreen);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                parseUser = ParseUser.getCurrentUser();

                /**
                 * Check Session if exists open Main activity else open login activity
                 */
                if (parseUser != null && parseUser.getSessionToken() != null) {

                    installation = ParseInstallation.getCurrentInstallation();
                    installation.put("user_objectId", parseUser.getObjectId());
                    installation.saveInBackground();

                    final Intent intent = new Intent(SplashScreenActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();

                } else {

                    final Intent intent = new Intent(SplashScreenActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();

                }

            }
        }, 2000);
    }
}
