package com.inventorymanagementsystem.ims.Utils;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by swift on 6/20/16.
 */
public class Utils {

    public static ProgressDialog dialog;

    //Method to show progress Dialogue
    public static void showProgressView(android.content.Context context, String message) {
        dialog = new ProgressDialog(context);
        dialog.setMessage(message);
        dialog.setCancelable(false);
        dialog.setIndeterminate(true);
        dialog.show();
    }

    public static void hideProgressView() {
        if (dialog != null) {
            dialog.dismiss();
        }
    }

    //Method to check Internet Availability
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    //No internet Toast
    public static void noNetMessage(Context context){
        Toast.makeText(context,"No internet connection",Toast.LENGTH_SHORT).show();
    }

    //Custom message Toast
    public static void showToast(Context context, String msg){
        Toast.makeText(context,msg,Toast.LENGTH_SHORT).show();
    }

    public String convertDateFormat(Date date){
        try {
            @SuppressLint("SimpleDateFormat") SimpleDateFormat standardForm = new SimpleDateFormat("MMM dd, yyyy hh:mm a");
            return standardForm.format(date);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
