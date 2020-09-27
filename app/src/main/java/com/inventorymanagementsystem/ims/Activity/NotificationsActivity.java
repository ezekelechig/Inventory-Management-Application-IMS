package com.inventorymanagementsystem.ims.Activity;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import com.inventorymanagementsystem.ims.Adapters.NotificationAdapter;
import com.inventorymanagementsystem.ims.Model.NotificationModel;
import com.inventorymanagementsystem.ims.R;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dac-android on 6/13/16.
 */
public class NotificationsActivity extends AppCompatActivity {

    //Java activity class that fetches current user's notifications from Parse Notification Table

    private static final String TAG = "Notifications";
    Toolbar toolbar;
    ActionBar actionBar;
    ArrayList<NotificationModel> notificationList = new ArrayList();
    ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        setPrefValues();
        findViewsById();
        setupActionbar();
        getNotifications();
    }

    //Method to grab current user's notification from parse Notification table
    private void getNotifications() {

        //Querying the parse Notification table
        ParseQuery<ParseObject> notificationQuery = ParseQuery.getQuery("Notification");

        //Grabbing row values based on latest created date
        notificationQuery.orderByDescending("createdAt");

        //Query based on whether receiver ID matches current user ID
        notificationQuery.include("notification_receiver_id");
        notificationQuery.whereEqualTo("notification_receiver_id", ParseObject.createWithoutData("_User", ParseUser.getCurrentUser().getObjectId()));

        //Starting the query in background thread
        notificationQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> list, ParseException e) {

                if (e == null) {

                    for (ParseObject Obj : list) {
                        //for each instance where IDs match, add values to notificationList
                        notificationList.add(new NotificationModel(Obj.getString("notification_msg"), Obj.getString("notification_sender_name"), Obj.getCreatedAt()));
                    }

                    //Set notificationList Array to notification adapter and then setAdapter to notification listview
                    NotificationAdapter notificationAdapter = new NotificationAdapter(NotificationsActivity.this, notificationList);
                    listView.setAdapter(notificationAdapter);
                } else
                    e.printStackTrace();

            }

        });

    }

    private void setupActionbar() {
        setSupportActionBar(toolbar);

        actionBar = getSupportActionBar();

        setTitle("Notifications");

        // Enable the Up button
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    private void findViewsById() {
        toolbar = (Toolbar) findViewById(R.id.notificationsToolbar);
        listView = (ListView) findViewById(R.id.notification_ListView);
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
