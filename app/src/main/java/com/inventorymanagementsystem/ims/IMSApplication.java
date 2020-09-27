package com.inventorymanagementsystem.ims;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseFacebookUtils;
import com.parse.ParseInstallation;


/**
 * Created by swift on 6/20/16.
 */
public class IMSApplication extends Application {

    public static IMSApplication app;


    @Override
    public void onCreate() {
        super.onCreate();

        app = this;

        Parse.enableLocalDatastore(this);

        // Parse Initialization Code - Application ID, Client ID
        Parse.initialize(this, "3cqhZyFcyxMqLHDsoE0SVheXBbB2gdiDeUs1YGOT", "BwBdnfEKuGY94oI6m1URq6FNGUa0gjWggeTHQ8HA");

        //For Facebook Login
        ParseFacebookUtils.initialize(this);
    }

}
