package com.inventorymanagementsystem.ims.Model;

/**
 * Created by dac-android on 7/13/16.
 */
public class TransactionModel {
    public String _id;
    public String _approved_time;
    public String _item_name;
    public String _quantity;
    public String _rate;
    public String _total;

    public TransactionModel(String id, String approved_time, String item_name, String quantity, String rate, String total) {
        this._id = id;
        this._approved_time = approved_time;
        this._item_name = item_name;
        this._quantity = quantity;
        this._rate = rate;
        this._total = total;
    }

}