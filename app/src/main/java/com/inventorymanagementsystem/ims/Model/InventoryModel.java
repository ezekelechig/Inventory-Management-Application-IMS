package com.inventorymanagementsystem.ims.Model;

/**
 * Created by dac-android on 7/14/16.
 */
public class InventoryModel {

    public String _item_id;
    public String _item_name;
    public String _item_quantity;
    public String _item_rate;

    public InventoryModel(String item_id, String item_name, String item_quantity, String item_rate) {
        this._item_id = item_id;
        this._item_name = item_name;
        this._item_quantity = item_quantity;
        this._item_rate = item_rate;
    }
}
