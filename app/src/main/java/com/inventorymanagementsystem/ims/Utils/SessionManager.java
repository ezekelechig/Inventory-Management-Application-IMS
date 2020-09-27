package com.inventorymanagementsystem.ims.Utils;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.inventorymanagementsystem.ims.Activity.LoginActivity;

/**
 * Created by swift on 6/20/16.
 */
public class SessionManager {

    private SharedPreferences prefs;

    Context _context;

    public SessionManager(Context context) {
        this._context = context;
        prefs = PreferenceManager.getDefaultSharedPreferences(_context);
    }

    public void createLoginSession(String userName, String userEmail) {

        prefs.edit().putString("userName", userName).commit();
        prefs.edit().putString("userEmail", userEmail).commit();
    }


    public void sessionOut(){

        prefs.edit().remove("userName").commit();
        prefs.edit().remove("userEmail").commit();

        _context.startActivity(new Intent(_context, LoginActivity.class));

    }

    public String getSessionUserName() {
        String name = prefs.getString("userName","");
        return name;
    }

    public String getSessionUserEmail() {
        String email = prefs.getString("userEmail","");
        return email;
    }
}
