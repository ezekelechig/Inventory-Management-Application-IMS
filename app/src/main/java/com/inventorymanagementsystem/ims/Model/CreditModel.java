package com.inventorymanagementsystem.ims.Model;

import java.util.Date;

/**
 * Created by dac-android on 7/13/16.
 */
public class CreditModel {

    public String _owner_id;
    public String _owner_name;
    public String  _amount;

    public CreditModel(String owner_id, String owner_name, String amount) {
        this._owner_id = owner_id;
        this._owner_name = owner_name;
        this._amount = amount;
    }
}
