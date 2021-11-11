/*Copyright 2021 GastroRice

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/


package com.example.gatromanagerclient;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gatromanagerclient.ui.settings.SettingsActivity;
import com.gastromanager.models.OrderItem;
import com.gastromanager.models.OrderItemInfo;

import java.util.List;

public class OrderListAdapter extends RecyclerView.Adapter<OrderListAdapter.ViewHolder> {


    private List<OrderItemInfo> orderItems;
    private OrderItemClickListener orderItemClickListener;

    public OrderListAdapter(List<OrderItemInfo> orderItems, OrderItemClickListener orderItemClickListener) {
        this.orderItems = orderItems;
        this.orderItemClickListener = orderItemClickListener;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void updateOrderItemList(List<OrderItemInfo> orderItemInfoList) {
        orderItems.clear();
        if(orderItemInfoList != null && orderItemInfoList.size() > 0) {
            orderItems.addAll(orderItemInfoList);
            orderItems.sort(new OrderItemComparator());
        }
        notifyDataSetChanged();
    }

    public List<OrderItemInfo> getOrderItems() {
        return orderItems;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        // Inflate the custom layout
        View listItemView = inflater.inflate(R.layout.menu_row_item, parent, false);

        // Return a new holder instance
        ViewHolder listItemViewHolder = new ViewHolder(listItemView, orderItemClickListener);
        return listItemViewHolder;
    }

    // Involves populating data into the item through holder
    @SuppressLint("ResourceAsColor")
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // Get the data model based on position
        OrderItemInfo orderItem = (OrderItemInfo) orderItems.get(position);
        String xmlString = orderItem.getXmlText();

        holder.ivEditBtn.setVisibility(View.GONE);
        holder.ivDeleteBtn.setVisibility(View.GONE);
        holder.ivPushBtn.setVisibility(View.GONE);

        holder.tvItemName.setText(xmlString);
        holder.tvItemPrice.setText(String.format("%s "+ SettingsActivity.currency, orderItem.getPrice().toString()));
        if(orderItem.getPayed() == 0 && orderItem.getPrintStatus() != 1 ){
            holder.llRootLayout.setClickable(true);
            holder.llRootLayout.setBackgroundResource(R.drawable.black_rounder_rectangle);
        } else {
            holder.llRootLayout.setClickable(false);
            holder.ivPushBtn.setVisibility(View.INVISIBLE);
            holder.llRootLayout.setBackgroundResource(R.drawable.yellow_rounder_rectangle);
        }
    }

    public void showMessage(String message, OrderItemInfo orderItem, View view) {

        AlertDialog.Builder alert = new AlertDialog.Builder(view.getContext());
        alert.setTitle(message);
        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            public void onClick(DialogInterface dialog, int whichButton) {
                orderItems.remove(orderItem);
                updateOrderItemList(orderItems);
            }

        });

       alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int whichButton) {
            //what you need to do after click "Cancel"
        }
    });
            alert.show();
}


    @Override
    public int getItemCount() {
        return (orderItems == null) ? 0 :orderItems.size();
    }

    // Provide a direct reference to each of the views within a data item
    // Used to cache the views within the item layout for fast access

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView tvItemName;
        public TextView tvItemPrice;
        public ImageView ivPushBtn;
        public ImageView ivEditBtn;
        public ImageView ivDeleteBtn;
        public LinearLayout llRootLayout;
        public OrderItemClickListener orderItemClickListener;

        // We also create a constructor that accepts the entire item row
        // and does the view lookups to find each subview
        public ViewHolder(View itemView, OrderItemClickListener orderItemClickListener) {
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);
            this.tvItemName = itemView.findViewById(R.id.tv_item_name);
            this.tvItemPrice = itemView.findViewById(R.id.tv_item_price);
            this.llRootLayout = itemView.findViewById(R.id.root_layout);
            this.ivPushBtn = itemView.findViewById(R.id.iv_push_btn);
            this.ivEditBtn = itemView.findViewById(R.id.iv_edit_btn);
            this.ivDeleteBtn = itemView.findViewById(R.id.iv_delete_btn);
            this.orderItemClickListener = orderItemClickListener;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            orderItemClickListener.onOrderItemClick(getAdapterPosition());
        }
    }

    public interface OrderItemClickListener {
        void onOrderItemClick(int position);
    }
}
