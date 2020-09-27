package com.inventorymanagementsystem.ims.Fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.inventorymanagementsystem.ims.Model.OrderHistoryModel;
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


public class OrderFragment extends Fragment {

    //Fragment to show non-pending i.e. either approved/rejected orders (for both customer and owner)

    private static final String TAG = "OrderFragment";
    View view;

    RecyclerView order_history_rv;

    List<OrderHistoryModel> orderHistoryModelList = new ArrayList<>();

    OrderHistoryAdapter adapter;

    String strCustomerName, strCustomerId;

    String strItemName, strItemCount;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_order_history, container, false);

        findViewsById();

        //to check whether the given view is for admin or for a normal customer
        if (Utils.isNetworkAvailable(getContext()))
            if (ParseUser.getCurrentUser().getString("username").equals("admin"))
                getOrderHistoryAdmin();         //method called for admin
            else
                getOrderHistoryNonAdmin();      //method called for customer

        return view;
    }

    private void findViewsById() {
        order_history_rv = (RecyclerView) view.findViewById(R.id.order_history_rv);

        adapter = new OrderHistoryAdapter(orderHistoryModelList);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        order_history_rv.setLayoutManager(layoutManager);
        order_history_rv.setHasFixedSize(true);
        order_history_rv.setAdapter(adapter);

    }

    public void getOrderHistoryAdmin() {

        ParseQuery<ParseObject> parseQuery = ParseQuery.getQuery("Order");
        parseQuery.orderByDescending("createdAt");
        parseQuery.include("order_item");
        parseQuery.include("order_owner");
        parseQuery.whereNotEqualTo("order_status", "pending");
        parseQuery.findInBackground(new FindCallback<ParseObject>() {

            @Override
            public void done(List<ParseObject> list, ParseException e) {

                if (e == null) {

                    if (list.size() == 0)
                        Utils.showToast(getContext(), "You have no orders.");

                    for (ParseObject object : list) {

                        ParseObject order_item_obj = object.getParseObject("order_item");
                        ParseObject order_owner_obj = object.getParseObject("order_owner");

                        orderHistoryModelList.add(new OrderHistoryModel(
                                object.getObjectId(),
                                "",
                                order_item_obj.getString("item_name"),
                                object.getString("order_quantity"),
                                order_owner_obj.getString("username"),
                                object.getString("order_status"),
                                order_owner_obj.getObjectId()
                        ));

                        adapter.notifyDataSetChanged();
                    }
                } else
                    e.printStackTrace();

            }
        });
    }

    public void getOrderHistoryNonAdmin() {

        ParseQuery<ParseObject> owner_query = ParseQuery.getQuery("Order");
        owner_query.whereEqualTo("order_owner", ParseObject.createWithoutData("_User", ParseUser.getCurrentUser().getObjectId()));

        ParseQuery<ParseObject> status_query = ParseQuery.getQuery("Order");
        status_query.whereNotEqualTo("order_status", "pending");


        List<ParseQuery<ParseObject>> queries = new ArrayList<>();
        queries.add(owner_query);
        queries.add(status_query);

        ParseQuery<ParseObject> parseQuery = ParseQuery.or(queries);
        parseQuery.include("order_item");
        parseQuery.include("order_owner");
        parseQuery.findInBackground(new FindCallback<ParseObject>() {

            @Override
            public void done(List<ParseObject> list, ParseException e) {

                if (e == null) {

                    if (list.size() == 0)
                        Utils.showToast(getContext(), "You have no orders.");

                    for (ParseObject object : list) {

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

                        adapter.notifyDataSetChanged();
                    }
                } else
                    e.printStackTrace();

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
//                In recycle view it is neccessary to un hide and hide components that change visibility in different items
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

            receivedRequestHolder.accept.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (Utils.isNetworkAvailable(getContext())) {

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
                                                            Log.d(TAG, "Transaction Success");
                                                            receivedRequestHolder.accept.setVisibility(View.GONE);
                                                            receivedRequestHolder.reject.setVisibility(View.GONE);
                                                            receivedRequestHolder.pending.setVisibility(View.GONE);
                                                            receivedRequestHolder.approved.setVisibility(View.VISIBLE);
                                                            receivedRequestHolder.rejected.setVisibility(View.GONE);
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

                    } else
                        Utils.noNetMessage(getContext());

                }
            });


            receivedRequestHolder.reject.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (Utils.isNetworkAvailable(getContext())) {

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
                                                receivedRequestHolder.accept.setVisibility(View.GONE);
                                                receivedRequestHolder.reject.setVisibility(View.GONE);
                                                receivedRequestHolder.pending.setVisibility(View.GONE);
                                                receivedRequestHolder.approved.setVisibility(View.GONE);
                                                receivedRequestHolder.rejected.setVisibility(View.VISIBLE);
                                            } else
                                                e.printStackTrace();

                                        }
                                    });
                                }
                            }
                        });

                    } else {
                        Utils.noNetMessage(getContext());
                    }

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
}
