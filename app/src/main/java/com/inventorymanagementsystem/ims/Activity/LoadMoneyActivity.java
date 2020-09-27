package com.inventorymanagementsystem.ims.Activity;

import android.content.DialogInterface;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.inventorymanagementsystem.ims.R;
import com.inventorymanagementsystem.ims.Utils.Utils;
import com.parse.GetCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

public class LoadMoneyActivity extends AppCompatActivity {

    Toolbar toolbar;

    ActionBar actionBar;

    LinearLayout pay_pal, master_card;

    Integer intCurrentBlnc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_load_money);

        findViewsById();
        setupActionbar();

    }

    private void setupActionbar() {
        setSupportActionBar(toolbar);

        actionBar = getSupportActionBar();

        setTitle("Load Money");

        // Enable the Up button
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    private void findViewsById() {
        toolbar = (Toolbar) findViewById(R.id.loadMoneyToolbar);
        pay_pal = (LinearLayout) findViewById(R.id.pay_pal);
        master_card = (LinearLayout) findViewById(R.id.master_card);

        pay_pal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                intCurrentBlnc = ParseUser.getCurrentUser().getInt("account");
                Log.d("CurrentBlnc",": "+ParseUser.getCurrentUser().getInt("account"));
                showDialog();
            }
        });

        master_card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intCurrentBlnc = ParseUser.getCurrentUser().getInt("account");
                Log.d("CurrentBlnc",": "+ParseUser.getCurrentUser().getInt("account"));
                showDialog();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void showDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.custom_load_money_dialog, null);
        dialogBuilder.setView(dialogView);

        final EditText amountEditText = (EditText) dialogView.findViewById(R.id.amount);

        dialogBuilder.setTitle("Amount");
        dialogBuilder.setPositiveButton("Load", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        if (Utils.isNetworkAvailable(getBaseContext())) {
                            if (!amountEditText.getText().toString().isEmpty() && Integer.parseInt(amountEditText.getText().toString()) > 0) {

                                int loadedAmount = Integer.parseInt(amountEditText.getText().toString());

                                final int newBalance = loadedAmount + intCurrentBlnc;

                                ParseQuery<ParseObject> updateAmount = ParseQuery.getQuery("_User");
//                                updateAmount.whereEqualTo("objectId", ParseObject.createWithoutData("_User", ParseUser.getCurrentUser().getObjectId()));
                                updateAmount.getInBackground(ParseUser.getCurrentUser().getObjectId(), new GetCallback<ParseObject>() {
                                    public void done(ParseObject object, ParseException e) {
                                        if (e == null) {
                                            object.put("account", newBalance);
                                            object.saveInBackground(new SaveCallback() {
                                                @Override
                                                public void done(ParseException e) {
                                                    if (e != null)
                                                        e.printStackTrace();
                                                    else {

                                                        Utils.showToast(getBaseContext(), "Amount loaded to account.");
                                                    }
                                                }
                                            });
                                        } else
                                            e.printStackTrace();

                                    }
                                });

                            } else
                                Utils.showToast(getBaseContext(), "Please enter valid amount.");

                        } else
                            Utils.noNetMessage(getBaseContext());

                        dialog.dismiss();
                    }
                }

        );

        dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener()

                {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.dismiss();
                    }
                }

        );
        AlertDialog b = dialogBuilder.create();
        b.show();
    }

}
