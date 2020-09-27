package com.inventorymanagementsystem.ims.Activity;

import android.content.DialogInterface;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.inventorymanagementsystem.ims.Adapters.FontManager;
import com.inventorymanagementsystem.ims.Model.InventoryModel;
import com.inventorymanagementsystem.ims.R;
import com.inventorymanagementsystem.ims.Utils.Utils;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;

public class InventoryActivity extends AppCompatActivity {

    private static final String TAG = "InventoryActivity";
    Toolbar toolbar;
    ActionBar actionBar;

    RecyclerView inventory_rv;

    List<InventoryModel> inventoryModelList = new ArrayList<>();

    InventoryAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);

        findViewsById();
        setupActionbar();

        if (Utils.isNetworkAvailable(getBaseContext()))
            getInventory();
        else
            Utils.noNetMessage(getBaseContext());
    }

    private void setupActionbar() {
        setSupportActionBar(toolbar);

        actionBar = getSupportActionBar();

        setTitle("Your Inventory");

        // Enable the Up button
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    private void findViewsById() {
        toolbar = (Toolbar) findViewById(R.id.inventoryToolbar);
        inventory_rv = (RecyclerView) findViewById(R.id.inventory_rv);

        adapter = new InventoryAdapter(inventoryModelList);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getBaseContext());
        inventory_rv.setLayoutManager(layoutManager);
        inventory_rv.setHasFixedSize(true);
        inventory_rv.setAdapter(adapter);
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

    private void getInventory() {

        Utils.showProgressView(this, "Fetching inventory. Please wait...");

        ParseQuery<ParseObject> inventoryListQuery = ParseQuery.getQuery("Inventory");
        inventoryListQuery.orderByDescending("createdAt");
        inventoryListQuery.include("item_id");
        inventoryListQuery.whereEqualTo("inventory_owner", ParseObject.createWithoutData("_User", ParseUser.getCurrentUser().getObjectId()));
        inventoryListQuery.whereNotEqualTo("inventory_quantity", 0);
        inventoryListQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> list, ParseException e) {

                if (e == null) {

                    if (list.size() == 0)
                        Utils.showToast(getBaseContext(), "You have on inventory items.");

                    for (ParseObject userObj : list) {

                        ParseObject item_obj = userObj.getParseObject("item_id");

                        inventoryModelList.add(new InventoryModel(
                                item_obj.getObjectId(),
                                item_obj.getString("item_name"),
                                userObj.getInt("inventory_quantity") + "",
                                item_obj.getInt("item_rate") + ""
                        ));

                        adapter.notifyDataSetChanged();

                    }

                } else
                    e.printStackTrace();

                Utils.hideProgressView();
            }
        });

    }

    public void showChangeItemRateDialog(int rate, final String item_id, final int position) {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.custom_change_item_rate_dialog, null);
        dialogBuilder.setView(dialogView);

        final EditText editRateEditText = (EditText) dialogView.findViewById(R.id.edit_rate);
        editRateEditText.setText(rate + "");

        dialogBuilder.setTitle("Change Rate");
        dialogBuilder.setPositiveButton("Change", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        if (Utils.isNetworkAvailable(getBaseContext())) {
                            if (!editRateEditText.getText().toString().isEmpty() && Integer.parseInt(editRateEditText.getText().toString()) > 0) {

                                ParseQuery<ParseObject> updateRate = ParseQuery.getQuery("Item");
                                updateRate.getInBackground(item_id, new GetCallback<ParseObject>() {
                                    public void done(ParseObject object, ParseException e) {
                                        if (e == null) {
                                            object.put("item_rate", Integer.parseInt(editRateEditText.getText().toString()));
                                            object.saveInBackground(new SaveCallback() {
                                                @Override
                                                public void done(ParseException e) {
                                                    if (e != null)
                                                        e.printStackTrace();
                                                    else {

                                                        Utils.showToast(getBaseContext(), "Item rate updated.");

                                                        adapter.updateItem(position, editRateEditText.getText().toString());

                                                    }
                                                }
                                            });
                                        } else
                                            e.printStackTrace();

                                    }
                                });

                            } else
                                Utils.showToast(getBaseContext(), "Please enter valid rate.");

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

    class InventoryAdapter extends RecyclerView.Adapter<InventoryAdapter.inventoryHolder> {
        public class inventoryHolder extends RecyclerView.ViewHolder {

            public TextView itemName, itemRate, itemQuantity, itemEdit;

            inventoryHolder(View itemView) {
                super(itemView);
                itemName = (TextView) itemView.findViewById(R.id.itemName);
                itemRate = (TextView) itemView.findViewById(R.id.itemRate);
                itemQuantity = (TextView) itemView.findViewById(R.id.itemQuantity);
                itemEdit = (TextView) itemView.findViewById(R.id.itemEdit);
            }
        }

        List<InventoryModel> inventoryModelList;

        public InventoryAdapter(List<InventoryModel> inventoryModelList) {
            this.inventoryModelList = inventoryModelList;
        }

        @Override
        public inventoryHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.inventory_item, viewGroup, false);
            return new inventoryHolder(v);
        }

        @Override
        public void onAttachedToRecyclerView(RecyclerView recyclerView) {
            super.onAttachedToRecyclerView(recyclerView);
        }

        @Override
        public void onBindViewHolder(final inventoryHolder receivedRequestHolder, int i) {

            if (ParseUser.getCurrentUser().getUsername().equals("admin")) {
                receivedRequestHolder.itemEdit.setVisibility(View.VISIBLE);
                receivedRequestHolder.itemEdit.setTypeface(FontManager.getTypeface(getBaseContext(), FontManager.FONTAWESOME));
                receivedRequestHolder.itemEdit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showChangeItemRateDialog(Integer.parseInt(inventoryModelList.get(receivedRequestHolder.getAdapterPosition())._item_rate), inventoryModelList.get(receivedRequestHolder.getAdapterPosition())._item_id, receivedRequestHolder.getAdapterPosition());
                    }
                });
            } else
                receivedRequestHolder.itemEdit.setVisibility(View.GONE);

//            For item name
            receivedRequestHolder.itemName.setText(inventoryModelList.get(i)._item_name);

//            For quantity
            receivedRequestHolder.itemQuantity.setText(inventoryModelList.get(i)._item_quantity);

//            For rate
            receivedRequestHolder.itemRate.setText(inventoryModelList.get(i)._item_rate);
        }

        @Override
        public int getItemCount() {
            return inventoryModelList.size();
        }

        public void removeItem(int position) {
            inventoryModelList.remove(position);
            notifyItemRemoved(position);
        }

        public void updateItem(int position, String new_rate) {

            inventoryModelList.get(position)._item_rate = new_rate;
            adapter.notifyDataSetChanged();

        }
    }
}
