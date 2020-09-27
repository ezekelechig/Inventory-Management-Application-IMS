package com.inventorymanagementsystem.ims.Activity;

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
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.inventorymanagementsystem.ims.Model.CreditModel;
import com.inventorymanagementsystem.ims.Model.DebitModel;
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

public class DebitInfoActivity extends AppCompatActivity {

    private static final String TAG = "DebitInfoActivity";
    Toolbar toolbar;
    ActionBar actionBar;

    RecyclerView debit_info_rv;

    List<DebitModel> debitModelList = new ArrayList<>();

    DebitInfoAdapter adapter;

    Button btnPaydebit;

    String strCurrentDebit;

    Integer intAvailableBalance, intNewBalance;

    AlertDialog alertDialog;

    int debit_clearance_value = 0;

    Boolean hasDebt = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debit_info);

        findViewsById();
        setupActionbar();

        if (Utils.isNetworkAvailable(getBaseContext()))
            if (ParseUser.getCurrentUser().getString("username").equals("admin"))
                getDebitInfoAdmin();
            else
                getDebitInfoNonAdmin();
        else
            Utils.noNetMessage(getBaseContext());

        btnPaydebit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (hasDebt) {
                    intAvailableBalance = ParseUser.getCurrentUser().getInt("account");

                    if (ParseUser.getCurrentUser().getUsername().equals("admin"))
                        Utils.showToast(getBaseContext(), "Admin account can clear debts.");
                    else
                        payCustomerDebt();
                } else {
                    Utils.showToast(getBaseContext(), "You have no debts.");
                }

            }
        });
    }

    private void setupActionbar() {
        setSupportActionBar(toolbar);

        actionBar = getSupportActionBar();

        setTitle("Account Debit Info");

        // Enable the Up button
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    private void findViewsById() {
        toolbar = (Toolbar) findViewById(R.id.debitInfoToolbar);
        debit_info_rv = (RecyclerView) findViewById(R.id.debit_info_rv);

        adapter = new DebitInfoAdapter(debitModelList);

        btnPaydebit = (Button) findViewById(R.id.btn_pay_debit);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getBaseContext());
        debit_info_rv.setLayoutManager(layoutManager);
        debit_info_rv.setHasFixedSize(true);
        debit_info_rv.setAdapter(adapter);
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

    private void getDebitInfoAdmin() {
        Utils.showToast(getBaseContext(), "You have no debts.");
    }

    private void getDebitInfoNonAdmin() {

        Utils.showProgressView(this, "Fetching account debit info. Please wait...");

        ParseQuery<ParseObject> parseQuery = ParseQuery.getQuery("Order");
        parseQuery.include("order_item");
        parseQuery.include("order_owner");
        parseQuery.whereEqualTo("order_status", "approved");
        parseQuery.whereEqualTo("order_owner", ParseObject.createWithoutData("_User", ParseUser.getCurrentUser().getObjectId()));

        parseQuery.findInBackground(new FindCallback<ParseObject>() {

            @Override
            public void done(List<ParseObject> list, ParseException e) {

                if (e == null) {

                    long debit = 0;
                    int total;

                    if (list.size() == 0) {
                        hasDebt = false;
                        Utils.showToast(getBaseContext(), "You have no debts.");
                    }

                    for (ParseObject object : list) {

                        ParseObject order_item_obj = object.getParseObject("order_item");
                        ParseObject order_owner = object.getParseObject("order_owner");

                        debit_clearance_value = order_owner.getInt("debit_clearance");

                        total = Integer.parseInt(object.getString("order_quantity")) * order_item_obj.getInt("item_rate");

                        debit = debit + total;
                    }

                    if (debit != 0) {

                        if (debit_clearance_value != 0)
                            debit = debit - debit_clearance_value;

                        if (debit != 0)
                            debitModelList.add(new DebitModel(
                                    getString(R.string.admin_id),
                                    "admin",
                                    "" + debit
                            ));
                        else {
                            hasDebt = false;
                            Utils.showToast(getBaseContext(), "You have no debts.");
                        }
                    }

                    adapter.notifyDataSetChanged();

                } else
                    e.printStackTrace();

                Utils.hideProgressView();
            }
        });

    }

    class DebitInfoAdapter extends RecyclerView.Adapter<DebitInfoAdapter.debitInfoHolder> {
        public class debitInfoHolder extends RecyclerView.ViewHolder {

            public TextView username, amount;

            debitInfoHolder(View itemView) {
                super(itemView);
                username = (TextView) itemView.findViewById(R.id.username);
                amount = (TextView) itemView.findViewById(R.id.amount);
            }
        }

        List<DebitModel> debitModelList;

        public DebitInfoAdapter(List<DebitModel> debitModelList) {
            this.debitModelList = debitModelList;
        }

        @Override
        public debitInfoHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.debit_info_item, viewGroup, false);
            return new debitInfoHolder(v);
        }

        @Override
        public void onAttachedToRecyclerView(RecyclerView recyclerView) {
            super.onAttachedToRecyclerView(recyclerView);
        }

        @Override
        public void onBindViewHolder(final debitInfoHolder receivedRequestHolder, int i) {

//            For owner name
            receivedRequestHolder.username.setText(debitModelList.get(i)._owner_name);

//            For amount
            receivedRequestHolder.amount.setText("$" + debitModelList.get(i)._amount);
            strCurrentDebit = debitModelList.get(i)._amount;
        }

        @Override
        public int getItemCount() {
            return debitModelList.size();
        }

        public void removeItem(int position) {
            debitModelList.remove(position);
            notifyItemRemoved(position);
        }

        public void updateItem(int position) {
            debitModelList.get(position)._amount = "0";
            notifyDataSetChanged();
        }
    }

    public void payCustomerDebt() {

        alertDialog = new android.support.v7.app.AlertDialog.Builder(DebitInfoActivity.this).create();
        alertDialog.setCancelable(true);

        LayoutInflater inflater = getLayoutInflater();
        final View dialogLayout = inflater.inflate(R.layout.dialog_debit_payment, null);
        alertDialog.setView(dialogLayout);

        TextView tvPayDebit = (TextView) dialogLayout.findViewById(R.id.tv_confirm_debit_pay);
        TextView tvCurrentDebit = (TextView) dialogLayout.findViewById(R.id.tv_current_debit_value);
        TextView tvCurrentBlnc = (TextView) dialogLayout.findViewById(R.id.tv_current_blnc_value);

        tvCurrentDebit.setText("$" + strCurrentDebit);
        tvCurrentBlnc.setText("$" + intAvailableBalance);

//        final EditText edPaidDebit = (EditText) dialogLayout.findViewById(R.id.ed_pay_amount);


        tvPayDebit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final Integer currentDebit = Integer.valueOf(strCurrentDebit);

                if (currentDebit > intAvailableBalance)
                    Utils.showToast(DebitInfoActivity.this, "Payable amount cannot exceed available balance");
                else {
                    intNewBalance = intAvailableBalance - currentDebit;

                    //Need to reduce the debit amount here first

                    ParseQuery<ParseObject> updateAmount = ParseQuery.getQuery("_User");
                    updateAmount.whereEqualTo("objectId", ParseUser.getCurrentUser().getObjectId());
                    updateAmount.getInBackground(ParseUser.getCurrentUser().getObjectId(), new GetCallback<ParseObject>() {
                        public void done(ParseObject object, ParseException e) {
                            if (e == null) {
                                int oldBalance = object.getInt("debit_clearance");

                                object.put("account", intNewBalance);
                                object.put("debit_clearance", oldBalance + currentDebit);
                                object.saveInBackground(new SaveCallback() {
                                    @Override
                                    public void done(ParseException e) {
                                        if (e != null)
                                            e.printStackTrace();
                                        else {

                                            //TODO this condition is only true when only one admin is present
                                            adapter.updateItem(0);
                                            hasDebt = false;
                                            Utils.showToast(getBaseContext(), "Payment Successful");

                                        }
                                    }
                                });
                            } else
                                e.printStackTrace();

                        }
                    });

                    alertDialog.dismiss();
                }


            }
        });

        alertDialog.show();

    }
}
