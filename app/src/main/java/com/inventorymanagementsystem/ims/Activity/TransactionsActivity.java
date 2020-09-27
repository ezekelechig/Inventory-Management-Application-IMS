package com.inventorymanagementsystem.ims.Activity;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.inventorymanagementsystem.ims.Model.OrderHistoryModel;
import com.inventorymanagementsystem.ims.Model.TransactionModel;
import com.inventorymanagementsystem.ims.Model.TransactionModel;
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

public class TransactionsActivity extends AppCompatActivity {

    //Activity Java class to show current user's completed transactions ( for both admin and user)
    private static final String TAG = "TransactionsActivity";

    Toolbar toolbar;
    ActionBar actionBar;

    RecyclerView transaction_rv;

    List<TransactionModel> transactionModelList = new ArrayList<>();

    TransactionAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transactions);


        setPrefValues();
        findViewsById();
        setupActionbar();

        //first check internet connection
        //Then check if current user is admin or customer and call method likewise
        if (Utils.isNetworkAvailable(getBaseContext()))
            if (ParseUser.getCurrentUser().getString("username").equals("admin"))
                getTransactionsAdmin();                         //for admin
            else
                getOrderHistoryNonAdmin();
        else
            Utils.noNetMessage(getBaseContext());               //for customer

    }

    private void getOrderHistoryNonAdmin() {

        Utils.showProgressView(this, "Fetching transactions. Please wait...");

        //Querying Parse Order Table
        ParseQuery<ParseObject> parseQuery = ParseQuery.getQuery("Order");

        //Grabbing list in ascending order of updated date
        parseQuery.orderByDescending("updatedAt");

        //Grabbing order Item name and details (For orders whose status has been approved)
        parseQuery.include("order_item");
        parseQuery.whereEqualTo("order_owner", ParseObject.createWithoutData("_User", ParseUser.getCurrentUser().getObjectId()));
        parseQuery.whereEqualTo("order_status", "approved");
        parseQuery.findInBackground(new FindCallback<ParseObject>() {

            @Override
            public void done(List<ParseObject> list, ParseException e) {

                if (e == null) {

                    if (list.size() == 0)
                        Utils.showToast(getBaseContext(), "You have no transactions.");

                    for (ParseObject object : list) {

                        ParseObject order_item_obj = object.getParseObject("order_item");

                        //Getting total price of order approved items to show transaction details
                        int total = Integer.parseInt(object.getString("order_quantity")) * order_item_obj.getInt("item_rate");

                        transactionModelList.add(new TransactionModel(
                                object.getObjectId(),
                                new Utils().convertDateFormat(object.getUpdatedAt()),
                                order_item_obj.getString("item_name"),
                                object.getString("order_quantity"),
                                "$ " + order_item_obj.getInt("item_rate"),
                                "$ " + total
                        ));

                        adapter.notifyDataSetChanged();
                    }
                } else
                    e.printStackTrace();

                Utils.hideProgressView();

            }
        });

    }

    private void getTransactionsAdmin() {

        Utils.showProgressView(this, "Fetching transactions. Please wait...");

        //Querying Parse Order Table
        ParseQuery<ParseObject> parseQuery = ParseQuery.getQuery("Order");
        //Grabbing list in ascending order of updated date
        parseQuery.orderByDescending("updatedAt");

        //Grabbing order Item name and details (For orders whose status has been approved)
        parseQuery.include("order_item");
        parseQuery.whereEqualTo("order_status", "approved");
        parseQuery.findInBackground(new FindCallback<ParseObject>() {

            @Override
            public void done(List<ParseObject> list, ParseException e) {

                if (e == null) {

                    if (list.size() == 0)
                        Utils.showToast(getBaseContext(), "You have no transactions.");

                    for (ParseObject object : list) {

                        ParseObject order_item_obj = object.getParseObject("order_item");

                        int total = Integer.parseInt(object.getString("order_quantity")) * order_item_obj.getInt("item_rate");

                        transactionModelList.add(new TransactionModel(
                                object.getObjectId(),
                                new Utils().convertDateFormat(object.getUpdatedAt()),
                                order_item_obj.getString("item_name"),
                                object.getString("order_quantity"),
                                "$ " + order_item_obj.getInt("item_rate"),
                                "$ " + total
                        ));

                        adapter.notifyDataSetChanged();
                    }
                } else
                    e.printStackTrace();

                Utils.hideProgressView();

            }
        });

    }

    private void setupActionbar() {
        setSupportActionBar(toolbar);

        actionBar = getSupportActionBar();

        setTitle("Transactions");

        // Enable the Up button
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    private void findViewsById() {
        toolbar = (Toolbar) findViewById(R.id.transactionToolbar);
        transaction_rv = (RecyclerView) findViewById(R.id.transaction_rv);

        adapter = new TransactionAdapter(transactionModelList);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getBaseContext());
        transaction_rv.setLayoutManager(layoutManager);
        transaction_rv.setHasFixedSize(true);
        transaction_rv.setAdapter(adapter);

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


    //Set up a recycler view to show each approved transaction for both admin and customer
    class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.transactionHolder> {
        public class transactionHolder extends RecyclerView.ViewHolder {

            public TextView itemName, approved_at, itemQuantity, itemRate, itemTotal;

            transactionHolder(View itemView) {
                super(itemView);
                itemName = (TextView) itemView.findViewById(R.id.itemName);
                itemQuantity = (TextView) itemView.findViewById(R.id.itemQuantity);
                itemRate = (TextView) itemView.findViewById(R.id.itemRate);
                itemTotal = (TextView) itemView.findViewById(R.id.itemTotal);
                approved_at = (TextView) itemView.findViewById(R.id.approved_at);
            }
        }

        List<TransactionModel> transactionModelList;

        public TransactionAdapter(List<TransactionModel> transactionModelList) {
            this.transactionModelList = transactionModelList;
        }

        @Override
        public transactionHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.transaction_item, viewGroup, false);
            return new transactionHolder(v);
        }

        @Override
        public void onAttachedToRecyclerView(RecyclerView recyclerView) {
            super.onAttachedToRecyclerView(recyclerView);
        }

        @Override
        public void onBindViewHolder(final transactionHolder receivedRequestHolder, int i) {

//            For item name
            receivedRequestHolder.itemName.setText(transactionModelList.get(i)._item_name);

//            For time
            receivedRequestHolder.approved_at.setText(transactionModelList.get(i)._approved_time);

//            For quantity
            receivedRequestHolder.itemQuantity.setText(transactionModelList.get(i)._quantity);

//            For rate
            receivedRequestHolder.itemRate.setText(transactionModelList.get(i)._rate);

//            For total
            receivedRequestHolder.itemTotal.setText(transactionModelList.get(i)._total);
        }

        @Override
        public int getItemCount() {
            return transactionModelList.size();
        }

        public void removeItem(int position) {
            transactionModelList.remove(position);
            notifyItemRemoved(position);
        }
    }
}
