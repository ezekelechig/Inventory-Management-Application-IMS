package com.inventorymanagementsystem.ims.Model;

/**
 * Created by dac-android on 7/9/16.
 */
public class OrderHistoryModel {
    public String _id;
    public String _time;
    public String _name;
    public String _quantity;
    public String _status;
    public String _order_owner_name;
    public String _order_owner_id;

    public OrderHistoryModel(String id, String time, String name, String quantity,
                             String order_owner_name, String status, String order_owner_id) {
        this._id = id;
        this._time = time;
        this._name = name;
        this._quantity = quantity;
        this._order_owner_name = order_owner_name;
        this._status = status;
        this._order_owner_id = order_owner_id;
    }

}
