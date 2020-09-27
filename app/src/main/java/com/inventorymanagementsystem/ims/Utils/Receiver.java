package com.inventorymanagementsystem.ims.Utils;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.inventorymanagementsystem.ims.Activity.MainActivity;
import com.parse.ParsePushBroadcastReceiver;

public class Receiver extends ParsePushBroadcastReceiver {

    @Override
    public void onPushOpen(Context context, Intent intent) {

        Bundle extras = intent.getExtras();

        String jsonData = extras.getString("com.parse.Data");
        //System.out.println("pushData:"+jsonData);
        Log.e("Push", intent.getData() + "Clicked" + jsonData);
        Intent i = new Intent(context, MainActivity.class);
        i.putExtras(intent.getExtras());
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i);
    }
}
