package com.inventorymanagementsystem.ims.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.inventorymanagementsystem.ims.Activity.OrderHistoryActivity;
import com.inventorymanagementsystem.ims.Model.NotificationModel;
import com.inventorymanagementsystem.ims.R;
import com.inventorymanagementsystem.ims.Utils.Utils;

import java.util.ArrayList;

/**
 * Created by swift on 7/12/16.
 */
public class NotificationAdapter extends BaseAdapter {

    //Adapter Java class to send current user notification values into view

    private final Context context;
    private final ArrayList<NotificationModel> notificationArrayList;
    private LayoutInflater layoutInflater;

    public NotificationAdapter(Context context, ArrayList<NotificationModel> notificationArrayList) {
        this.context = context;
        layoutInflater = LayoutInflater.from(context);
        this.notificationArrayList = notificationArrayList;
    }

    @Override
    public int getCount() {
        return notificationArrayList.size();
    }

    @Override
    public Object getItem(int position) {
        return notificationArrayList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        final ViewHolder holder;

        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.notification_item, null);             //inflate notification layout
            holder = new ViewHolder();
            holder.notificationMsg = (TextView) convertView.findViewById(R.id.tv_nofitication_msg);
            holder.notificationSender = (TextView) convertView.findViewById(R.id.tv_notification_sender);
            holder.notificationTime = (TextView) convertView.findViewById(R.id.tv_notification_time);
            holder.notificationLayout = (LinearLayout) convertView.findViewById(R.id.ll_Notification);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.notificationMsg.setText(notificationArrayList.get(position)._msg);       //set Notification message
        holder.notificationSender.setText(notificationArrayList.get(position)._sender); //set Notification sender name
        holder.notificationTime.setText(new Utils().convertDateFormat(notificationArrayList.get(position)._createdAt)); //set Notification sender name


        //ON CLICKING EACH INDIVIDUAL NOTIFICATION
        holder.notificationLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //CALLS OrderhistoryActivity
                Intent intent = new Intent(context, OrderHistoryActivity.class);
                context.startActivity(intent);

            }
        });

        return convertView;
    }

    static class ViewHolder {
        TextView notificationMsg;
        TextView notificationSender;
        TextView notificationTime;
        LinearLayout notificationLayout;
    }
}
