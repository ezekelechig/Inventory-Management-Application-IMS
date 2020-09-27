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
import com.inventorymanagementsystem.ims.R;
import com.inventorymanagementsystem.ims.Utils.Utils;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.Parse;
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
import java.util.StringTokenizer;

public class OrderHistoryActivity extends AppCompatActivity {

    //Java Activity class to show list of orders that are yet pending to be approved/disapproved
    //Admin view - admin gets to set action for whether they approve/disapprove an order
    //Customer view - orders are appending with a "pending" status

    private static final String TAG = "OrderHistoryActivity";
    Toolbar toolbar;
    ActionBar actionBar;

    RecyclerView order_history_rv;

    List<OrderHistoryModel> orderHistoryModelList = new ArrayList<>();

    OrderHistoryAdapter adapter;

    String strCustomerName, strCustomerId;

    String strItemName, strItemCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_history);

        setPrefValues();
        findViewsById();
        setupActionbar();

        //Checks internet connections and also whether current user is an admin or customer
        Log.d("CurrentUser",": "+ParseUser.getCurrentUser().getString("username"));

        if (ParseUser.getCurrentUser().getString("username").equals("admin"))
            getOrderHistoryAdmin();

        if (Utils.isNetworkAvailable(getBaseContext()))
            if (ParseUser.getCurrentUser().getString("username").equals("admin"))
                getOrderHistoryAdmin();
            else
                getOrderHistoryNonAdmin();

        else
            Utils.noNetMessage(getBaseContext());
    }

    private void setupActionbar() {
        setSupportActionBar(toolbar);

        actionBar = getSupportActionBar();

        setTitle("Pending Orders");

        // Enable the Up button
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    private void findViewsById() {
        toolbar = (Toolbar) findViewById(R.id.orderHistoryToolbar);
        order_history_rv = (RecyclerView) findViewById(R.id.order_history_rv);

        adapter = new OrderHistoryAdapter(orderHistoryModelList);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getBaseContext());
        order_history_rv.setLayoutManager(layoutManager);
        order_history_rv.setHasFixedSize(true);
        order_history_rv.setAdapter(adapter);

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

    //method called to setup up pending orders view for admin
    public void getOrderHistoryAdmin() {

        //Utils.showProgressView(this, "Fetching orders. Please wait...");

        //Queries parse Order table
        //Calls all orders placed to owner with status 'Pending' by all customers
        ParseQuery<ParseObject> parseQuery = ParseQuery.getQuery("Order");
        parseQuery.orderByDescending("createdAt");
        parseQuery.include("order_item");
        parseQuery.include("order_owner");
        parseQuery.whereEqualTo("order_status", "pending");
        parseQuery.findInBackground(new FindCallback<ParseObject>() {

            @Override
            public void done(List<ParseObject> list, ParseException e) {

                if (e == null) {

                    if (list.size() == 0)
                        Utils.showToast(getBaseContext(), "You have no orders.");

                    for (ParseObject object : list) {

                        ParseObject order_item_obj = object.getParseObject("order_item");
                        ParseObject order_owner_obj = object.getParseObject("order_owner");

                        //Grabs all pending order details and adds order object into an arrayList
                        orderHistoryModelList.add(new OrderHistoryModel(
                                object.getObjectId(),
                                "",
                                order_item_obj.getString("item_name"),
                                object.getString("order_quantity"),
                                order_owner_obj.getString("username"),
                                object.getString("order_status"),
                                order_owner_obj.getObjectId()
                        ));

                        //sets array list into adapter - -  which inturn sends all order details into view
                        //Utils.hideProgressView();
                        adapter.notifyDataSetChanged();
                    }
                } else
                    e.printStackTrace();


            }
        });
    }

    //Setup up pending orders view for non-admin i.e. normal customers
    public void getOrderHistoryNonAdmin() {

        Utils.showProgressView(this, "Fetching orders. Please wait...");

        //Queries parse Order table
        //Calls all orders placed to owner with status 'Pending' by current customer
        ParseQuery<ParseObject> parseQuery = ParseQuery.getQuery("Order");
        parseQuery.include("order_item");
        parseQuery.include("order_owner");
        parseQuery.whereEqualTo("order_owner", ParseObject.createWithoutData("_User", ParseUser.getCurrentUser().getObjectId()));
        parseQuery.whereEqualTo("order_status", "pending");
        parseQuery.findInBackground(new FindCallback<ParseObject>() {

            @Override
            public void done(List<ParseObject> list, ParseException e) {

                if (e == null) {

                    //in case list size is null, ,show message with no pending orders
                    if (list.size() == 0)
                        Utils.showToast(getBaseContext(), "You have no pending orders.");

                    for (ParseObject object : list) {

                        // incase list has a size, grab all order object values and add them to arraylist
                        ParseObject order_item_obj = object.getParseObject("order_item");

                        orderHistoryModelList.add(new OrderHistoryModel(
                                object.getObjectId(),
                                "",
                                order_item_obj.getString("item_name"),
                                object.getString("order_quantity"),
                                ParseUser.getCurrentUser().getUsername(),
                                object.getString("order_status"),
                                ""
                        ));

                        //set arraylist to adapter and then into view
                        adapter.notifyDataSetChanged();
                    }
                } else
                    e.printStackTrace();

                Utils.hideProgressView();

            }
        });

    }

    class OrderHistoryAdapter extends RecyclerView.Adapter<OrderHistoryAdapter.orderHistoryHolder> {
        public class orderHistoryHolder extends RecyclerView.ViewHolder {

            public TextView item_name, time, itemQuantity, accept, reject, order_owner, pending, approved, rejected;

            orderHistoryHolder(View itemView) {
                super(itemView);
                item_name = (TextView) itemView.findViewById(R.id.item_name);
                itemQuantity = (TextView) itemView.findViewById(R.id.itemQuantity);
                time = (TextView) itemView.findViewById(R.id.time);
                accept = (TextView) itemView.findViewById(R.id.accept);
                reject = (TextView) itemView.findViewById(R.id.reject);
                order_owner = (TextView) itemView.findViewById(R.id.order_owner);
                pending = (TextView) itemView.findViewById(R.id.pending);
                approved = (TextView) itemView.findViewById(R.id.approved);
                rejected = (TextView) itemView.findViewById(R.id.rejected);
            }
        }

        List<OrderHistoryModel> orderHistoryModelList;

        public OrderHistoryAdapter(List<OrderHistoryModel> orderHistoryModelList) {
            this.orderHistoryModelList = orderHistoryModelList;
        }

        @Override
        public orderHistoryHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.order_history_item, viewGroup, false);
            return new orderHistoryHolder(v);
        }

        @Override
        public void onAttachedToRecyclerView(RecyclerView recyclerView) {
            super.onAttachedToRecyclerView(recyclerView);
        }

        @Override
        public void onBindViewHolder(final orderHistoryHolder receivedRequestHolder, int i) {

//            For item name
            receivedRequestHolder.item_name.setText(orderHistoryModelList.get(i)._name);

//            For time
            receivedRequestHolder.time.setText(orderHistoryModelList.get(i)._time);

//            For quantity
            receivedRequestHolder.itemQuantity.setText(orderHistoryModelList.get(i)._quantity);

//            For order owner
            receivedRequestHolder.order_owner.setText(orderHistoryModelList.get(i)._order_owner_name);

            if (orderHistoryModelList.get(i)._status.equals("pending")) {

//                If user is admin show accept and reject else show pending
//                In recycle view it is neccesary to unhide and hide components that change visibility in different items
                if (ParseUser.getCurrentUser().getUsername().equals("admin")) {
                    receivedRequestHolder.accept.setVisibility(View.VISIBLE);
                    receivedRequestHolder.reject.setVisibility(View.VISIBLE);
                    receivedRequestHolder.pending.setVisibility(View.GONE);
                    receivedRequestHolder.approved.setVisibility(View.GONE);
                    receivedRequestHolder.rejected.setVisibility(View.GONE);
                } else {
                    receivedRequestHolder.accept.setVisibility(View.GONE);
                    receivedRequestHolder.reject.setVisibility(View.GONE);
                    receivedRequestHolder.pending.setVisibility(View.VISIBLE);
                    receivedRequestHolder.approved.setVisibility(View.GONE);
                    receivedRequestHolder.rejected.setVisibility(View.GONE);
                }

            } else if (orderHistoryModelList.get(i)._status.equals("approved")) {

                receivedRequestHolder.accept.setVisibility(View.GONE);
                receivedRequestHolder.reject.setVisibility(View.GONE);
                receivedRequestHolder.pending.setVisibility(View.GONE);
                receivedRequestHolder.approved.setVisibility(View.VISIBLE);
                receivedRequestHolder.rejected.setVisibility(View.GONE);

            } else if (orderHistoryModelList.get(i)._status.equals("rejected")) {

                receivedRequestHolder.accept.setVisibility(View.GONE);
                receivedRequestHolder.reject.setVisibility(View.GONE);
                receivedRequestHolder.pending.setVisibility(View.GONE);
                receivedRequestHolder.approved.setVisibility(View.GONE);
                receivedRequestHolder.rejected.setVisibility(View.VISIBLE);

            }


            //Action when owner accepts an order
            receivedRequestHolder.accept.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Log.d("accept",": clicked");

                    if (Utils.isNetworkAvailable(getBaseContext())) {

                        strCustomerName = orderHistoryModelList.get(receivedRequestHolder.getAdapterPosition())._order_owner_name;
                        strCustomerId = orderHistoryModelList.get(receivedRequestHolder.getAdapterPosition())._order_owner_id;
                        strItemName = orderHistoryModelList.get(receivedRequestHolder.getAdapterPosition())._name;
                        strItemCount = orderHistoryModelList.get(receivedRequestHolder.getAdapterPosition())._quantity;

                        Log.d("ItemDetailsAccept", ": " + strCustomerName + " -- " + strItemName + " -- " + strItemCount);

                        ParseQuery<ParseObject> query = ParseQuery.getQuery("Order");
                        query.whereEqualTo("objectId", orderHistoryModelList.get(receivedRequestHolder.getAdapterPosition())._id);
                        Log.d("AcceptedOrderId", ": " + orderHistoryModelList.get(receivedRequestHolder.getAdapterPosition())._id);
                        query.getFirstInBackground(new GetCallback<ParseObject>() {
                            public void done(final ParseObject object, ParseException e) {
                                if (object == null) {
                                    Log.d(TAG, "The getFirst request failed.");
                                } else {

                                    final String order_owner_value = object.getObjectId();
                                    final String order_item_id = object.getParseObject("order_item").getObjectId();
                                    final String order_item_quantity = object.getString("order_quantity").trim();

                                    Log.d(TAG, "Retrieved the object.");

                                    object.put("order_status", "approved");

                                    object.saveInBackground(new SaveCallback() {
                                        @Override
                                        public void done(ParseException e) {

                                            if (e == null) {

                                                ParseObject inventory_object = new ParseObject("Inventory");
                                                inventory_object.put("inventory_owner", ParseObject.createWithoutData("_User", order_owner_value));
                                                inventory_object.put("item_id", ParseObject.createWithoutData("Item", order_item_id));
                                                inventory_object.put("inventory_quantity", Integer.parseInt(order_item_quantity));
                                                inventory_object.put("inventory_rate", Integer.parseInt("0"));

                                                inventory_object.saveInBackground(new SaveCallback() {
                                                    @Override
                                                    public void done(ParseException e) {
                                                        if (e == null) {

                                                            Utils.showToast(getBaseContext(), "Order approved");
                                                            removeItem(receivedRequestHolder.getAdapterPosition());

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

                        sendNotification("accepted");

                    } else
                        Utils.noNetMessage(getBaseContext());

                }
            });


            //action when onwer rejects an order
            receivedRequestHolder.reject.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (Utils.isNetworkAvailable(getBaseContext())) {

                        strCustomerName = orderHistoryModelList.get(receivedRequestHolder.getAdapterPosition())._order_owner_name;
                        strCustomerId = orderHistoryModelList.get(receivedRequestHolder.getAdapterPosition())._order_owner_id;
                        strItemName = orderHistoryModelList.get(receivedRequestHolder.getAdapterPosition())._name;
                        strItemCount = orderHistoryModelList.get(receivedRequestHolder.getAdapterPosition())._quantity;

                        ParseQuery<ParseObject> query = ParseQuery.getQuery("Order");
                        query.whereEqualTo("objectId", orderHistoryModelList.get(receivedRequestHolder.getAdapterPosition())._id);
                        query.getFirstInBackground(new GetCallback<ParseObject>() {
                            public void done(ParseObject object, ParseException e) {
                                if (object == null) {
                                    Log.d(TAG, "The getFirst request failed.");
                                } else {
                                    Log.d(TAG, "Retrieved the object.");

                                    object.put("order_status", "rejected");

                                    object.saveInBackground(new SaveCallback() {
                                        @Override
                                        public void done(ParseException e) {

                                            if (e == null) {
                                                Utils.showToast(getBaseContext(), "Order rejected");
                                                removeItem(receivedRequestHolder.getAdapterPosition());
                                            } else
                                                e.printStackTrace();

                                        }
                                    });
                                }
                            }
                        });

                    } else {
                        Utils.noNetMessage(getBaseContext());
                    }

                    sendNotification("rejected");

                }
            });

        }

        @Override
        public int getItemCount() {
            return orderHistoryModelList.size();
        }

        public void removeItem(int position) {
            orderHistoryModelList.remove(position);
            notifyItemRemoved(position);
        }


        
    }


    //method to send notification when owner accepts or rejects an order to the customer who placed order
    public void sendNotification(String status) {

        //Place push notification to customer
        //Update on notification table

        ParseQuery pushQuery = ParseInstallation.getQuery();                //start Push Query
        pushQuery.whereEqualTo("user_objectId", strCustomerId);              //target query to inventory owner
        Log.d("NoftCustId", ": " + strCustomerId);

        String pushMsg = "Your order for " + strItemName + " has been " + status + " by owner. [ Quantity: " + strItemCount + " ]";

        Log.d("OwnerPushMsg", ": " + pushMsg);

        ParsePush push = new ParsePush();
        push.setQuery(pushQuery);                                       // Set our Installation query
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

        //Place notification in notification table too
        ParseObject notificationObject = new ParseObject("Notification");
        notificationObject.put("notification_msg", pushMsg);
        notificationObject.put("notification_receiver_id", ParseObject.createWithoutData("_User", strCustomerId));
        notificationObject.put("notification_receiver_name", ParseObject.createWithoutData("_User", strCustomerName));
        notificationObject.put("notification_sender_name", "admin");
        notificationObject.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    Log.d("Notf upload", "Success!!");
                } else
                    Log.d("Notf Error", ": " + e.getMessage());
            }
        });
    }
}
