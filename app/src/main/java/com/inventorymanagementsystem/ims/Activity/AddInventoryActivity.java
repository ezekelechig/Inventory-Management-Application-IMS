package com.inventorymanagementsystem.ims.Activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.inventorymanagementsystem.ims.R;
import com.inventorymanagementsystem.ims.Utils.Utils;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;


public class AddInventoryActivity extends AppCompatActivity {


    private static final String TAG = "AddInventoryActivity";
    private static final int REQUEST_CODE = 5123;

    Toolbar toolbar;
    ActionBar actionBar;

    EditText itemDescription, itemRate, itemQuantity;

    Spinner categoryName;

    AutoCompleteTextView itemName;

    private List<String> ITEMS = null;

    private List<String> CATEGORY = null;

    ArrayAdapter<String> adapter;

    ArrayAdapter<String> spinnerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_inventory);

        setPrefValues();
        findViewsById();
        setUpCategorySpinner();
        getOldItems();
        setupActionbar();
        setUpProgressDialog();

    }

    private void getOldItems() {

        ParseQuery<ParseObject> query = ParseQuery.getQuery("Item");
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(final List<ParseObject> objects, com.parse.ParseException e) {
                if (e == null) {

                    ITEMS = new ArrayList<>();

                    for (ParseObject parseObject : objects) {
                        ITEMS.add(parseObject.getString("item_name"));
                    }

                    adapter = new ArrayAdapter<>(AddInventoryActivity.this,
                            android.R.layout.simple_dropdown_item_1line, ITEMS);
                    itemName.setAdapter(adapter);

                } else {
                    e.printStackTrace();
                }
            }
        });

    }

    public void setUpCategorySpinner() {

        CATEGORY = new ArrayList<>();

        CATEGORY.add("Select Category");

        spinnerAdapter = new ArrayAdapter<>(AddInventoryActivity.this,
                android.R.layout.simple_dropdown_item_1line, CATEGORY);

        categoryName.setAdapter(spinnerAdapter);

        ParseQuery<ParseObject> query = ParseQuery.getQuery("Category");
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(final List<ParseObject> objects, com.parse.ParseException e) {
                if (e == null) {

                    for (ParseObject parseObject : objects) {
                        CATEGORY.add(parseObject.getString("category_title"));
                    }

                } else {
                    e.printStackTrace();
                }
            }
        });

    }

    private void setUpProgressDialog() {

    }

    private void setupActionbar() {
        setSupportActionBar(toolbar);

        actionBar = getSupportActionBar();

        setTitle("Add Inventory");

        // Enable the Up button
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    private void findViewsById() {

        toolbar = (Toolbar) findViewById(R.id.add_inventory_toolbar);
        itemDescription = (EditText) findViewById(R.id.itemDescription);
        itemRate = (EditText) findViewById(R.id.itemRate);
        itemQuantity = (EditText) findViewById(R.id.itemQuantity);
        itemName = (AutoCompleteTextView) findViewById(R.id.itemName);
        categoryName = (Spinner) findViewById(R.id.categoryName);

    }

    private void setPrefValues() {
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_add, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case android.R.id.home:
                onBackPressed();
                return true;

            case R.id.add:
                if (Utils.isNetworkAvailable(this)) {

                    checkTableForSimilarItem(itemName.getText().toString());

                } else
                    Utils.noNetMessage(this);

                return true;

            case R.id.action_settings:
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private boolean validate() {

        if (itemName.getText().toString().isEmpty()) {
            itemName.setError("Item name cannot be empty");
            return false;

        } else if (itemQuantity.getText().toString().isEmpty()) {
            itemQuantity.setError("Item quantity cannot be empty");
            return false;

        } else if (itemRate.getText().toString().isEmpty()) {
            itemRate.setError("Item rate cannot be empty");
            return false;

        } else if (itemDescription.getText().toString().length() > 140) {
            itemDescription.setError("Maximum characters 140");
            return false;

        } else if (categoryName.getSelectedItem().toString().equals(getString(R.string.select_category))) {
            itemDescription.setError("Please select category");
            return false;

        } else
            return true;

    }

    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    /**
     * Query table for similar item
     * If exists old_item_id value is set
     */
    public void checkTableForSimilarItem(String item_name) {

        ParseQuery<ParseObject> query = ParseQuery.getQuery("Item");
        query.whereEqualTo("item_name", item_name);
        query.getFirstInBackground(new GetCallback<ParseObject>() {
            public void done(ParseObject object, ParseException e) {

                if (object == null) {

                    if (validate())
                        addInventory(itemName.getText().toString(),
                                itemRate.getText().toString(),
                                itemQuantity.getText().toString(),
                                itemDescription.getText().toString(),
                                categoryName.getSelectedItem().toString(),
                                "");

                } else {

                    if (validate())
                        addInventory(itemName.getText().toString(),
                                itemRate.getText().toString(),
                                itemQuantity.getText().toString(),
                                itemDescription.getText().toString(),
                                categoryName.getSelectedItem().toString(),
                                object.getObjectId());
                }

            }
        });

    }

    /**
     * Add item details to 'Item' table and add to owner inventory
     *
     * @param item_name        'item name'
     * @param item_rate        'rate of item'
     * @param item_quantity    'quantity of items added by owner'
     * @param item_description 'description about item (optional)'
     * @param category_name    'category name'
     * @param old_item_id      'old item id if item already exists in table'
     */
    public void addInventory(final String item_name, final String item_rate, final String item_quantity, final String item_description, String category_name, String old_item_id) {


//        If old_item_id is empty it means the item doesn't already exist in the table
        if (old_item_id.isEmpty()) {

//        Find the category object id first then insert into table
            ParseQuery<ParseObject> query = ParseQuery.getQuery("Category");
            query.whereEqualTo("category_title", category_name);
            query.getFirstInBackground(new GetCallback<ParseObject>() {

                public void done(ParseObject object, ParseException e) {

                    if (object != null) {

                        final ParseObject item_object = new ParseObject("Item");
                        final ParseObject inventory_object = new ParseObject("Inventory");

                        item_object.put("item_name", item_name);
                        item_object.put("item_description", item_description);
                        item_object.put("category", ParseObject.createWithoutData("Category", object.getObjectId()));
                        item_object.put("item_rate", Integer.parseInt(item_rate));

//                        inventory_object.put("inventory_rate", Integer.parseInt(item_rate));
                        inventory_object.put("inventory_owner", ParseObject.createWithoutData("_User", ParseUser.getCurrentUser().getObjectId()));
                        inventory_object.put("inventory_quantity", Integer.parseInt(item_quantity));

                        item_object.saveInBackground(new SaveCallback() {
                            public void done(ParseException e) {

                                if (e == null) {

                                    inventory_object.put("item_id", ParseObject.createWithoutData("Item", item_object.getObjectId()));

                                    inventory_object.saveInBackground(new SaveCallback() {
                                        @Override
                                        public void done(ParseException e) {
                                            if (e == null) {

                                                alertSuccess("Item added successfully.\nAdd more ?");

                                            } else

                                                e.printStackTrace();
                                        }
                                    });

                                } else
                                    e.printStackTrace();

                            }
                        });
                    }

                }
            });

        } else {

            updateItem(item_quantity, old_item_id);

        }

    }

    private void updateItem(final String item_quantity, String old_item_id) {

        Toast.makeText(getBaseContext(), "Item already exists", Toast.LENGTH_SHORT).show();

        ParseQuery<ParseObject> query = ParseQuery.getQuery("Inventory");
        query.whereEqualTo("item_id", ParseObject.createWithoutData("Item", old_item_id));
        query.whereEqualTo("inventory_owner", ParseUser.getCurrentUser());
        query.getFirstInBackground(new GetCallback<ParseObject>() {
            public void done(ParseObject object, ParseException e) {
                if (object == null) {
                    Log.d(TAG, "The getFirst request failed.");
                } else {
                    Log.d(TAG, "Retrieved the object.");

                    // Now let's update it with some new data. In this case, only cheatMode and score
                    // will get sent to the Parse Cloud. playerName hasn't changed.

                    int quantity = object.getInt("inventory_quantity") + Integer.parseInt(item_quantity);

//                    object.put("inventory_rate", Integer.parseInt(item_rate));
                    object.put("inventory_quantity", quantity);

                    object.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {

                            if (e == null)
                                alertSuccess("Item quantity updated successfully.\nAdd or Update more ?");
                            else
                                e.printStackTrace();

                        }
                    });
                }
            }
        });

    }

    public void alertSuccess(String message) {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        // set dialog message
        alertDialogBuilder
                .setMessage(message)
                .setCancelable(true)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        startActivity(new Intent(AddInventoryActivity.this, AddInventoryActivity.class));
                        finish();
                    }

                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // if this button is clicked, just close
                        // the dialog box and do nothing
                        dialog.cancel();
                        finish();
                    }
                });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            String item_name = data.getStringExtra("item_name");
            itemName.setText(item_name);
        }

    }
}
