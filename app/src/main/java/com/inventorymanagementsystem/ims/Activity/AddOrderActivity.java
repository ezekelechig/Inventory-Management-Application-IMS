package com.inventorymanagementsystem.ims.Activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.inventorymanagementsystem.ims.Adapters.FontManager;
import com.inventorymanagementsystem.ims.R;
import com.inventorymanagementsystem.ims.Utils.Utils;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.SendCallback;

import java.util.ArrayList;
import java.util.List;

public class AddOrderActivity extends AppCompatActivity {

    TextView tvCategoryExpand, tvItemExpand, tvItemNumberExpand, tvItemNumber, tvCategory, tvItem;

    LinearLayout llChoiceCategory, llChoiceItem, llChoiceItemNo;

    ArrayList<String> categoryList = new ArrayList<>();
    ArrayList<String> categoryIDList = new ArrayList<>();
    ArrayList<String> categoryItemList = new ArrayList<>();
    ArrayList<String> categoryItemIdList = new ArrayList<>();
    ArrayList<String> categoryItemQuantity = new ArrayList<>();

    String strSelectedCatId, strselectedItemId, strSelectedQuantity, strSelectedItem;

    Toolbar toolbar;

    ActionBar actionBar;

    Button btnOrderConfirm, btnOrderClear;

    ParseQuery<ParseObject> categoryQuery;

    int itemQuantity;

    String owner_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_order);

        categoryQuery = ParseQuery
                .getQuery("Category");


        toolbar = (Toolbar) findViewById(R.id.add_order_toolbar);

        setupActionbar();

        tvCategoryExpand = (TextView) findViewById(R.id.tv_category_expand);
        tvItemExpand = (TextView) findViewById(R.id.tv_item_expand);
        tvItemNumberExpand = (TextView) findViewById(R.id.tv_item_no_expand);
        tvItemNumber = (TextView) findViewById(R.id.tv_item_no);
        llChoiceCategory = (LinearLayout) findViewById(R.id.ll_choose_category);
        llChoiceItem = (LinearLayout) findViewById(R.id.ll_choose_item);
        llChoiceItemNo = (LinearLayout) findViewById(R.id.ll_choose_item_no);
        btnOrderClear = (Button) findViewById(R.id.btn_order_clear);
        btnOrderConfirm = (Button) findViewById(R.id.btn_order_confirm);
        tvCategory = (TextView) findViewById(R.id.tv_category);
        tvItem = (TextView) findViewById(R.id.tv_item);


        Typeface typeface = FontManager.getTypeface(AddOrderActivity.this, FontManager.FONTAWESOME);

        tvCategoryExpand.setTypeface(typeface);
        tvItemExpand.setTypeface(typeface);
        tvItemNumberExpand.setTypeface(typeface);

        llChoiceCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                categoryList.clear();
                categoryIDList.clear();

                if (Utils.isNetworkAvailable(AddOrderActivity.this)) {
                    Utils.showProgressView(AddOrderActivity.this, "Loading...");
                    getCategoryNames();
                } else
                    Utils.noNetMessage(AddOrderActivity.this);
            }
        });

        llChoiceItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                categoryItemList.clear();
                categoryItemIdList.clear();

                if (Utils.isNetworkAvailable(AddOrderActivity.this)) {

                    if (tvCategory.getText().toString() == "")
                        Utils.showToast(AddOrderActivity.this, "Please choose a category first");
                    else {
                        Utils.showProgressView(AddOrderActivity.this, "Loading...");
                        getItemNames();
                    }
                } else
                    Utils.noNetMessage(AddOrderActivity.this);

            }
        });

        llChoiceItemNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (categoryItemList.size() == 0)
                    Utils.showToast(AddOrderActivity.this, "No item selected");
                else {
                    getItemNumber();
                    //showDialog();
                }
            }
        });

        btnOrderClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                clearAllViews();
            }
        });

        btnOrderConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (Utils.isNetworkAvailable(AddOrderActivity.this)) {

                    /*strselectedItemId != "" || strSelectedQuantity != "" || strSelectedCatId != ""*/
                    if (tvCategory.getText().toString()=="")
                        Utils.showToast(AddOrderActivity.this, "Please select a category");
                    else if (tvItem.getText().toString()=="")
                        Utils.showToast(AddOrderActivity.this, "Please select an Item");
                    else if (tvItemNumber.getText().toString()=="")
                        Utils.showToast(AddOrderActivity.this, "Please select number of items");
                    else{
                        Utils.showProgressView(AddOrderActivity.this, "Placing order. Please wait...");
                        setCustomerOrder();
                    }

                } else
                    Utils.noNetMessage(AddOrderActivity.this);
            }
        });
    }

    public void clearAllViews() {

        tvCategory.setText("");
        tvItem.setText("");
        tvItemNumber.setText("");

        strSelectedCatId = null;
        strselectedItemId = null;
    }


    public void getCategoryNames() {

        categoryQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> list, ParseException e) {

                if (list.size() > 0) {

                    for (ParseObject Obj : list) {

                        Log.d("Categories", ": " + Obj.getString("category_title") + Obj.getObjectId());
                        categoryList.add(Obj.getString("category_title"));
                        categoryIDList.add(Obj.getObjectId());
                    }

                    Utils.hideProgressView();
                    showDialogCateGory();
                } else
                    Utils.showToast(AddOrderActivity.this, "No category available");
            }
        });
    }


    public void getItemNames() {

        ParseQuery itemQuery = ParseQuery.getQuery("Item");
        itemQuery.include("category");                  //include category pointer from item table

        itemQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> list, ParseException e) {

                Utils.hideProgressView();

                Log.d("ItemTableSize", ": " + list.size() + " -- " + strSelectedCatId);

                for (ParseObject Obj : list) {

                    final ParseObject category_Obj = Obj.getParseObject("category");

                    Log.d("CategoryID@Item", ": " + category_Obj.getObjectId() + "--" + strSelectedCatId);

                    if (!category_Obj.getObjectId().equals(null)) {

                        if (category_Obj.getObjectId().equals(strSelectedCatId)) {
                            Log.d("ItemList", ": " + Obj.getString("item_name") + " -- " + Obj.getObjectId());
                            categoryItemList.add(Obj.getString("item_name"));
                            categoryItemIdList.add(Obj.getObjectId());
                        } else
                            Log.d("Item", ": not matched");
                    } else {
                        Log.d("Category_Id", ": null");
                    }
                }

                if (categoryItemList.size() == 0)
                    Utils.showToast(AddOrderActivity.this, "No item in this category yet.");
                else
                    showDialogCategoryItems();
            }
        });

    }


    public void getItemNumber() {

        ParseQuery inventoryQuery = ParseQuery.getQuery("Inventory");
        inventoryQuery.include("item_id");

        inventoryQuery.findInBackground(new FindCallback<ParseObject>() {

            @Override
            public void done(List<ParseObject> list, ParseException e) {

                if (e == null) {
                    for (ParseObject Obj : list) {

                        final ParseObject item_obj = Obj.getParseObject("item_id");

                        Log.d("InventoryLength", ": " + list.size() + " -- " + item_obj.getObjectId() + " -- " + strselectedItemId);

                        if (item_obj.getObjectId().equals(strselectedItemId)) {

                            itemQuantity = Obj.getInt("inventory_quantity");
                            Log.d("ItemCount", ": " + Obj.getInt("inventory_quantity"));

                        } else
                            Log.d("InventoryItemNo", ": zero");
                    }

                    for (int i = 1; i <= itemQuantity; i++) {

                        categoryItemQuantity.add(String.valueOf(i));
                    }

                    showDialog();

                } else
                    Log.d("InventoryError", ": " + e.getMessage());
            }
        });

    }


    public void showDialog() {

        android.app.AlertDialog.Builder builderSingle = new android.app.AlertDialog.Builder(AddOrderActivity.this);

        //builderSingle.setIcon(R.drawable.ic_launcher);
        builderSingle.setTitle("Select Number of Items");
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                AddOrderActivity.this,
                android.R.layout.simple_selectable_list_item);


        for (int i = 1; i <= categoryItemQuantity.size(); i++) {

            arrayAdapter.add(String.valueOf(i));
        }


        builderSingle.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //String strName = arrayAdapter.getItem(which);
                        dialog.dismiss();
                        tvItemNumber.setText("");
                    }
                });

        builderSingle.setAdapter(arrayAdapter,
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        strSelectedQuantity = categoryItemQuantity.get(which);
                        tvItemNumber.setText(categoryItemQuantity.get(which));

                    }
                });

        builderSingle.show();
    }


    public void showDialogCateGory() {

        android.app.AlertDialog.Builder builderSingle = new android.app.AlertDialog.Builder(AddOrderActivity.this);

        //builderSingle.setIcon(R.drawable.ic_launcher);
        builderSingle.setTitle("Select Category");
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                AddOrderActivity.this,
                android.R.layout.simple_selectable_list_item);


        for (int i = 0; i < categoryList.size(); i++) {

            arrayAdapter.add(categoryList.get(i));
        }


        builderSingle.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //String strName = arrayAdapter.getItem(which);
                        dialog.dismiss();
                        tvCategory.setText("");
                    }
                });

        builderSingle.setAdapter(arrayAdapter,
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        String selectedCategory = categoryList.get(which);
                        tvCategory.setText(selectedCategory);
                        strSelectedCatId = categoryIDList.get(which);

                    }
                });

        builderSingle.show();
    }


    public void showDialogCategoryItems() {

        android.app.AlertDialog.Builder builderSingle = new android.app.AlertDialog.Builder(AddOrderActivity.this);

        //builderSingle.setIcon(R.drawable.ic_launcher);
        builderSingle.setTitle("Select Item");
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                AddOrderActivity.this,
                android.R.layout.simple_selectable_list_item);


        for (int i = 0; i < categoryItemList.size(); i++) {

            arrayAdapter.add(categoryItemList.get(i));
        }


        builderSingle.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //String strName = arrayAdapter.getItem(which);
                        dialog.dismiss();
                        tvItem.setText("");
                    }
                });

        builderSingle.setAdapter(arrayAdapter,
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        strSelectedItem = categoryItemList.get(which);
                        tvItem.setText(strSelectedItem);
                        strselectedItemId = categoryItemIdList.get(which);

                    }
                });

        builderSingle.show();

    }


    public void setCustomerOrder() {

        Utils.hideProgressView();

        ParseQuery<ParseUser> userQuery = ParseUser.getQuery();
        userQuery.whereEqualTo("is_admin",true);

        userQuery.findInBackground(new FindCallback<ParseUser>() {
            public void done(List<ParseUser> objects, ParseException e) {
                if (e == null) {

                    for(ParseUser user:objects){

                        owner_id = user.getObjectId();
                        Log.d("IdOwner",": "+owner_id);

                        setOrderAndNotification();                      //only one admin, so runs only once
                    }

                } else {
                    Log.d("ParseUserError",": "+e.getMessage());
                }
            }

        });

    }


    public void setOrderAndNotification(){

        ParseObject orderObject = new ParseObject("Order");

        //start Push Query
        ParseQuery pushQuery = ParseInstallation.getQuery();
        //target query to inventory owner
        pushQuery.whereEqualTo("user_objectId", owner_id);

        //Setting up push message
        String pushMsg = ParseUser.getCurrentUser().getUsername() + " has ordered for " + strSelectedItem +
                ". [Quantity - " + strSelectedQuantity+" ]";

        //Setting up Parse Push for push notification to Owner
        ParsePush push = new ParsePush();
        push.setQuery(pushQuery);
        push.setMessage(pushMsg);
        push.sendInBackground(new SendCallback() {
            @Override
            public void done(ParseException e) {

                if (e == null)
                    Log.d("success", " True");
                else
                    Log.d("pushError", ": " + e.getMessage());
            }
        });

        /*Place notification in notification table too*/
        ParseObject notificationObject = new ParseObject("Notification");
        notificationObject.put("notification_msg", pushMsg);
        notificationObject.put("notification_receiver_id", ParseObject.createWithoutData("_User", owner_id));
        notificationObject.put("notification_receiver_name", ParseObject.createWithoutData("_User", "admin"));
        notificationObject.put("notification_sender_name", ParseUser.getCurrentUser().getUsername());
        notificationObject.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    Log.d("Notf upload", "Success!!");
                } else
                    Log.d("Notf Error", ": " + e.getMessage());
            }
        });

        /*Uploading order in Order table parse*/
        orderObject.put("order_owner", ParseObject.createWithoutData("_User", ParseUser.getCurrentUser().getObjectId()));
        orderObject.put("order_item", ParseObject.createWithoutData("Item", strselectedItemId));
        orderObject.put("order_quantity", strSelectedQuantity);
        orderObject.put("order_status", "pending");

        orderObject.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                //e.printStackTrace();
                if (e == null) {
                    Log.d("Order upload", "Success!!");
                    orderSuccessDialog();
                } else
                    Log.d("Order Error", ": " + e.getMessage());
            }

        });
    }


    public void orderSuccessDialog() {

        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(AddOrderActivity.this);

        // set dialog message
        alertDialogBuilder
                .setMessage("Order placed successfully! Want to place more orders?")
                .setCancelable(true)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        clearAllViews();
                        dialog.dismiss();
                    }


                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // if this button is clicked, just close
                        // the dialog box and do nothing
                        dialog.cancel();
                        startActivity(new Intent(AddOrderActivity.this, MainActivity.class));
                    }
                });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }


    private void setupActionbar() {
        setSupportActionBar(toolbar);

        actionBar = getSupportActionBar();

        setTitle("Add Order");

        // Enable the Up button
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);
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
}
