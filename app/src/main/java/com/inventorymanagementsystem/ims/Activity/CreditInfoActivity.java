package com.inventorymanagementsystem.ims.Activity;

import android.support.v7.app.ActionBar;
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
import android.widget.TextView;

import com.inventorymanagementsystem.ims.Model.OrderHistoryModel;
import com.inventorymanagementsystem.ims.Model.CreditModel;
import com.inventorymanagementsystem.ims.Model.TransactionModel;
import com.inventorymanagementsystem.ims.R;
import com.inventorymanagementsystem.ims.Utils.Utils;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class CreditInfoActivity extends AppCompatActivity {

    private static final String TAG = "CreditInfoActivity";
    Toolbar toolbar;
    ActionBar actionBar;

    RecyclerView credit_info_rv;

    List<CreditModel> creditModelList = new ArrayList<>();

    CreditInfoAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_credit_info);

        findViewsById();
        setupActionbar();

        if (Utils.isNetworkAvailable(getBaseContext()))
            if (ParseUser.getCurrentUser().getString("username").equals("admin"))
                getCreditInfoAdmin();
            else
                getCreditInfoNonAdmin();
        else
            Utils.noNetMessage(getBaseContext());
    }

    private void setupActionbar() {
        setSupportActionBar(toolbar);

        actionBar = getSupportActionBar();

        setTitle("Account Credit Info");

        // Enable the Up button
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    private void findViewsById() {
        toolbar = (Toolbar) findViewById(R.id.creditInfoToolbar);
        credit_info_rv = (RecyclerView) findViewById(R.id.credit_info_rv);

        adapter = new CreditInfoAdapter(creditModelList);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getBaseContext());
        credit_info_rv.setLayoutManager(layoutManager);
        credit_info_rv.setHasFixedSize(true);
        credit_info_rv.setAdapter(adapter);
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

    private void getCreditInfoNonAdmin() {
        Utils.showToast(getBaseContext(),"You have no credit.");
    }

    private void getCreditInfoAdmin() {

        Utils.showProgressView(this, "Fetching account credit info. Please wait...");

        ParseQuery<ParseObject> getUserListQuery = ParseQuery.getQuery("_User");
        getUserListQuery.orderByDescending("username");
        getUserListQuery.whereNotEqualTo("username", "admin");
        getUserListQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> list, ParseException e) {

                if (e == null) {

                    String owner_object;
                    String owner_name;

                    for (ParseObject userObj : list) {

                        owner_object = userObj.getObjectId();
                        owner_name = userObj.getString("username");

                        ParseQuery<ParseObject> parseQuery = ParseQuery.getQuery("Order");
                        parseQuery.include("order_item");
                        parseQuery.whereEqualTo("order_status", "approved");
                        parseQuery.whereEqualTo("order_owner", ParseObject.createWithoutData("_User", userObj.getObjectId()));

                        final String finalOwner_object = owner_object;
                        final String finalOwner_name = owner_name;

                        parseQuery.findInBackground(new FindCallback<ParseObject>() {

                            @Override
                            public void done(List<ParseObject> list, ParseException e) {

                                if (e == null) {

                                    long credit = 0;
                                    int total;

                                    for (ParseObject object : list) {

                                        ParseObject order_item_obj = object.getParseObject("order_item");

                                        total = Integer.parseInt(object.getString("order_quantity")) * order_item_obj.getInt("item_rate");

                                        credit = credit + total;
                                    }

                                    if (credit != 0)
                                        creditModelList.add(new CreditModel(
                                                finalOwner_object,
                                                finalOwner_name,
                                                "$" + credit
                                        ));

                                    adapter.notifyDataSetChanged();

                                } else
                                    e.printStackTrace();
                            }
                        });

                    }

                } else
                    e.printStackTrace();

                Utils.hideProgressView();
            }
        });

    }

    class CreditInfoAdapter extends RecyclerView.Adapter<CreditInfoAdapter.creditInfoHolder> {
        public class creditInfoHolder extends RecyclerView.ViewHolder {

            public TextView username, amount;

            creditInfoHolder(View itemView) {
                super(itemView);
                username = (TextView) itemView.findViewById(R.id.username);
                amount = (TextView) itemView.findViewById(R.id.amount);
            }
        }

        List<CreditModel> creditModelList;

        public CreditInfoAdapter(List<CreditModel> creditModelList) {
            this.creditModelList = creditModelList;
        }

        @Override
        public creditInfoHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.credit_info_item, viewGroup, false);
            return new creditInfoHolder(v);
        }

        @Override
        public void onAttachedToRecyclerView(RecyclerView recyclerView) {
            super.onAttachedToRecyclerView(recyclerView);
        }

        @Override
        public void onBindViewHolder(final creditInfoHolder receivedRequestHolder, int i) {

//            For owner name
            receivedRequestHolder.username.setText(creditModelList.get(i)._owner_name);

//            For amount
            receivedRequestHolder.amount.setText(creditModelList.get(i)._amount);
        }

        @Override
        public int getItemCount() {
            return creditModelList.size();
        }

        public void removeItem(int position) {
            creditModelList.remove(position);
            notifyItemRemoved(position);
        }
    }
}
