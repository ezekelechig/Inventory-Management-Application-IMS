package com.inventorymanagementsystem.ims.Fragment;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.inventorymanagementsystem.ims.Activity.CreditInfoActivity;
import com.inventorymanagementsystem.ims.Activity.DebitInfoActivity;
import com.inventorymanagementsystem.ims.Activity.EditProfileActivity;
import com.inventorymanagementsystem.ims.Activity.InventoryActivity;
import com.inventorymanagementsystem.ims.Activity.LoginActivity;
import com.inventorymanagementsystem.ims.Activity.OrderHistoryActivity;
import com.inventorymanagementsystem.ims.Activity.TransactionsActivity;
import com.inventorymanagementsystem.ims.Adapters.FontManager;
import com.inventorymanagementsystem.ims.Model.TransactionModel;
import com.inventorymanagementsystem.ims.R;
import com.inventorymanagementsystem.ims.Utils.SessionManager;
import com.inventorymanagementsystem.ims.Utils.Utils;
import com.parse.CountCallback;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.List;


public class DashboardFragment extends Fragment {

    private static final String TAG = "DashboardFragment";
    View view;

    TextView edit_profile, logout, inventoryArrow, orderPendingArrow, tvUname, tvUemail, transactionArrow, creditValue, debitValue, pendingOrderCount, inventoryCount, balanceValue;

    LinearLayout logout_layout, order_pending_layout, transaction_layout, view_credit_details_layout, view_debit_details_layout, inventory_layout;

    SessionManager sessionManager;

    ParseUser parseUser;
    ParseInstallation installation;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        sessionManager = new SessionManager(getActivity());

        parseUser = ParseUser.getCurrentUser();
        if (parseUser != null && parseUser.getSessionToken() != null) {
            installation = ParseInstallation.getCurrentInstallation();
            installation.put("user_objectId", parseUser.getObjectId());
            installation.saveInBackground();
            //getUserDetailsFromParse();
        } else {
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            startActivity(intent);
            getActivity().finish();
        }

        findViewsById();

        tvUname.setText(sessionManager.getSessionUserName());
        tvUemail.setText(sessionManager.getSessionUserEmail());

        /*We can also do this by simply creating a parseuser and calling current credentials
        * But I have a hunch we will need to maintain a session for different purposes, do used it here too.
        * */

        if (Utils.isNetworkAvailable(getContext())) {

            setupInventoryCount();
            setupBalance();

            if (ParseUser.getCurrentUser().getString("username").equals("admin")) {
                setupPendingOrderCountAdmin();
                setupTransactionValuesAdmin();
            } else {
                setupPendingOrderCountNonAdmin();
                setupTransactionValuesNonAdmin();
            }

        } else
            Utils.noNetMessage(getContext());

        return view;
    }

    private void setupPendingOrderCountNonAdmin() {

        ParseQuery<ParseObject> query = ParseQuery.getQuery("Order");
        query.whereEqualTo("order_status", "pending");
        query.whereEqualTo("order_owner", ParseObject.createWithoutData("_User", ParseUser.getCurrentUser().getObjectId()));
        query.countInBackground(new CountCallback() {
            public void done(int count, ParseException e) {
                if (e == null) {
                    pendingOrderCount.setText(count + "");
                } else {
                    e.printStackTrace();
                }
            }
        });

    }

    private void setupPendingOrderCountAdmin() {

        ParseQuery<ParseObject> query = ParseQuery.getQuery("Order");
        query.whereEqualTo("order_status", "pending");
        query.countInBackground(new CountCallback() {
            public void done(int count, ParseException e) {
                if (e == null) {
                    pendingOrderCount.setText(count + "");
                } else {
                    e.printStackTrace();
                }
            }
        });

    }

    public void setupBalance() {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("_User");
        query.whereEqualTo("objectId", ParseUser.getCurrentUser().getObjectId());
        query.getFirstInBackground(new GetCallback<ParseObject>() {
            public void done(ParseObject object, ParseException e) {
                if (object == null) {
                    Log.d(TAG, "The getFirst request failed.");
                } else {
                    if (object.getInt("account") > 0)
                        balanceValue.setText("$" + object.getInt("account"));
                }
            }
        });
    }

    private void setupInventoryCount() {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Inventory");
        query.whereEqualTo("inventory_owner", ParseObject.createWithoutData("_User", ParseUser.getCurrentUser().getObjectId()));
        query.countInBackground(new CountCallback() {
            public void done(int count, ParseException e) {
                if (e == null) {
                    inventoryCount.setText(count + "");
                } else {
                    e.printStackTrace();
                }
            }
        });
    }

    private void setupTransactionValuesAdmin() {

        ParseQuery<ParseObject> parseQuery = ParseQuery.getQuery("Order");
        parseQuery.orderByDescending("updatedAt");
        parseQuery.include("order_item");
        parseQuery.whereEqualTo("order_status", "approved");
        parseQuery.findInBackground(new FindCallback<ParseObject>() {

            @Override
            public void done(List<ParseObject> list, ParseException e) {

                if (e == null) {
                    long credit = 0;

                    if (list.size() == 0) {
                        debitValue.setText("N/A");
                        creditValue.setText("N/A");
                    }

                    for (ParseObject object : list) {

                        ParseObject order_item_obj = object.getParseObject("order_item");

                        int total = Integer.parseInt(object.getString("order_quantity")) * order_item_obj.getInt("item_rate");

                        credit = credit + total;

                        Log.d(TAG, "" + credit);

                        String credit_value = "$" + credit;
                        creditValue.setText(credit_value);

                    }

                } else
                    e.printStackTrace();

            }
        });

    }

    private void setupTransactionValuesNonAdmin() {
        ParseQuery<ParseObject> parseQuery = ParseQuery.getQuery("Order");
        parseQuery.orderByDescending("updatedAt");
        parseQuery.include("order_item");
        parseQuery.include("order_owner");
        parseQuery.whereEqualTo("order_owner", ParseObject.createWithoutData("_User", ParseUser.getCurrentUser().getObjectId()));
        parseQuery.whereEqualTo("order_status", "approved");
        parseQuery.findInBackground(new FindCallback<ParseObject>() {

            @Override
            public void done(List<ParseObject> list, ParseException e) {

                if (e == null) {
                    long debit = 0;

                    if (list.size() == 0) {
                        debitValue.setText("N/A");
                        creditValue.setText("N/A");
                    }

                    for (ParseObject object : list) {

                        ParseObject order_item_obj = object.getParseObject("order_item");
                        ParseObject order_owner = object.getParseObject("order_owner");

                        int total = Integer.parseInt(object.getString("order_quantity")) * order_item_obj.getInt("item_rate");

                        debit = debit + total;

                        Log.d(TAG, "" + debit);

                        long value = debit - order_owner.getInt("debit_clearance");

                        if (value == 0)
                            debitValue.setText("N/A");
                        else {
                            String debit_value = "$" + value;
                            debitValue.setText(debit_value);
                        }

                    }

                } else
                    e.printStackTrace();

            }
        });
    }

    private void findViewsById() {
        edit_profile = (TextView) view.findViewById(R.id.edit_profile);
        logout = (TextView) view.findViewById(R.id.logout);
        inventoryArrow = (TextView) view.findViewById(R.id.inventoryArrow);
        orderPendingArrow = (TextView) view.findViewById(R.id.orderPendingArrow);
        tvUname = (TextView) view.findViewById(R.id.tv_uname_db);
        tvUemail = (TextView) view.findViewById(R.id.tv_uemail_db);
        transactionArrow = (TextView) view.findViewById(R.id.transactionArrow);
        creditValue = (TextView) view.findViewById(R.id.creditValue);
        debitValue = (TextView) view.findViewById(R.id.debitValue);
        inventoryCount = (TextView) view.findViewById(R.id.inventoryCount);
        pendingOrderCount = (TextView) view.findViewById(R.id.pendingOrderCount);
        balanceValue = (TextView) view.findViewById(R.id.balanceValue);

        logout_layout = (LinearLayout) view.findViewById(R.id.logout_layout);
        order_pending_layout = (LinearLayout) view.findViewById(R.id.order_pending_layout);
        transaction_layout = (LinearLayout) view.findViewById(R.id.transaction_layout);
        view_credit_details_layout = (LinearLayout) view.findViewById(R.id.view_credit_details_layout);
        view_debit_details_layout = (LinearLayout) view.findViewById(R.id.view_debit_details_layout);
        inventory_layout = (LinearLayout) view.findViewById(R.id.inventory_layout);

        edit_profile.setTypeface(FontManager.getTypeface(getContext(), FontManager.FONTAWESOME));
        logout.setTypeface(FontManager.getTypeface(getContext(), FontManager.FONTAWESOME));
        inventoryArrow.setTypeface(FontManager.getTypeface(getContext(), FontManager.FONTAWESOME));
        orderPendingArrow.setTypeface(FontManager.getTypeface(getContext(), FontManager.FONTAWESOME));
        transactionArrow.setTypeface(FontManager.getTypeface(getContext(), FontManager.FONTAWESOME));


        edit_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (Utils.isNetworkAvailable(getActivity())) {
                    Intent intent = new Intent(getActivity(), EditProfileActivity.class);
                    startActivity(intent);
                } else
                    Utils.noNetMessage(getActivity());

            }
        });


        order_pending_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (Utils.isNetworkAvailable(getActivity())) {
                    Intent intent = new Intent(getActivity(), OrderHistoryActivity.class);
                    startActivity(intent);
                } else
                    Utils.noNetMessage(getActivity());

            }
        });


        logout_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());

                // set dialog message
                alertDialogBuilder
                        .setMessage("Are you sure you want to logout?")
                        .setCancelable(true)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                ParseInstallation parseInstallation = ParseInstallation.getCurrentInstallation();
                                parseInstallation.deleteInBackground();
                                parseUser.logOut();
                                startActivity(new Intent(getActivity(), LoginActivity.class));
                            }


                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // if this button is clicked, just close
                                // the dialog box and do nothing
                                dialog.cancel();
                            }
                        });

                // create alert dialog
                AlertDialog alertDialog = alertDialogBuilder.create();

                // show it
                alertDialog.show();
            }
        });

        transaction_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), TransactionsActivity.class));
            }
        });

        view_credit_details_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), CreditInfoActivity.class));
            }
        });

        view_debit_details_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), DebitInfoActivity.class));
            }
        });

        inventory_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), InventoryActivity.class));
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        if (Utils.isNetworkAvailable(getContext())) {

            setupInventoryCount();
            setupBalance();

            if (ParseUser.getCurrentUser().getString("username").equals("admin")) {
                setupPendingOrderCountAdmin();
                setupTransactionValuesAdmin();
            } else {
                setupPendingOrderCountNonAdmin();
                setupTransactionValuesNonAdmin();
            }

        } else
            Utils.noNetMessage(getContext());

    }
}
