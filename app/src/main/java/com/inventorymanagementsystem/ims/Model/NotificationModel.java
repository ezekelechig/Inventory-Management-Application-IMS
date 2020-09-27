package com.inventorymanagementsystem.ims.Model;

import java.util.Date;

/**
 * Created by swift on 7/12/16.
 */

public class NotificationModel {

    public String _msg;
    public String _sender;
    public Date _createdAt;

    public NotificationModel(String msg, String sender, Date createdAt) {
        this._msg = msg;
        this._sender = sender;
        this._createdAt = createdAt;
    }
}
