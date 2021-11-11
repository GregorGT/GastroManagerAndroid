/*Copyright 2021 GastroRice

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/


package com.example.gatromanagerclient.ui.payment;

import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;
import com.example.gatromanagerclient.R;
import com.example.gatromanagerclient.ui.settings.SettingsActivity;
import com.gastromanager.models.OrderItemInfo;
import java.util.ArrayList;
import java.util.List;

public class MenuItemsAdapter extends RecyclerView.Adapter<MenuItemsAdapter.ViewHolder> {

    private List<OrderItemInfo> menuItems;
    private List<OrderItemInfo> selectedMenuItems = new ArrayList<>();
    public final MenuItemClickListener myListener;

    public MenuItemsAdapter(List<OrderItemInfo> orderItems, MenuItemClickListener menuItemClickListener) {
        this.menuItems = orderItems;
        this.myListener = menuItemClickListener;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void updateOrderItemList(List<OrderItemInfo> orderItemInfoList) {
        this.menuItems = orderItemInfoList;
        notifyDataSetChanged();
    }

    public void clearList(){
        menuItems.clear();
        notifyDataSetChanged();
    }

    public List<OrderItemInfo> getOrderItems() {
        return menuItems;
    }

    public void removeItem(OrderItemInfo orderItemInfo){
        menuItems.remove(orderItemInfo);
        notifyDataSetChanged();
    }

    public void addItemToList(OrderItemInfo orderItemInfo){
        menuItems.add(orderItemInfo);
        notifyDataSetChanged();
    }

    public List<OrderItemInfo> getSelectedMenuItems() {
        return selectedMenuItems;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View listItemView = inflater.inflate(R.layout.menu_row_item, parent, false);
        ViewHolder listItemViewHolder = new ViewHolder(listItemView);
        return listItemViewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        OrderItemInfo orderItem = menuItems.get(position);
        String xmlString = orderItem.getXmlText();
        String[] splitString = xmlString.split("\\s+");

        holder.ivEditBtn.setVisibility(View.GONE);
        holder.ivDeleteBtn.setVisibility(View.GONE);
        holder.ivPushBtn.setVisibility(View.VISIBLE);

        holder.tvItemName.setText(xmlString);

//        if (splitString.length >= 4) {
//            holder.tvItemName.setText(String.format("%s %s", splitString[0], splitString[2]));
//        } else {
//            holder.tvItemName.setText(String.format("%s ", splitString[0]));
//        }
        holder.tvItemPrice.setText(String.format("%s " + SettingsActivity.currency, orderItem.getPrice().toString()));
        if(orderItem.getPayed() == 0){
            holder.llRootLayout.setClickable(true);
            holder.llRootLayout.setBackgroundResource(R.drawable.black_rounder_rectangle);
        } else {
            holder.llRootLayout.setClickable(false);
            holder.ivPushBtn.setVisibility(View.INVISIBLE);
            holder.llRootLayout.setBackgroundResource(R.drawable.yellow_rounder_rectangle);
        }

        if(holder.llRootLayout.isClickable()){
            holder.ivPushBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(holder.llRootLayout.isClickable())
                        myListener.onMenuItemClickListener(orderItem, false);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return (menuItems == null) ? 0 : menuItems.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView tvItemName;
        public TextView tvItemPrice;
        public ImageView ivPushBtn;
        public ImageView ivEditBtn;
        public ImageView ivDeleteBtn;
        public LinearLayout llRootLayout;

        public ViewHolder(View itemView) {
            super(itemView);
            this.tvItemName = itemView.findViewById(R.id.tv_item_name);
            this.tvItemPrice = itemView.findViewById(R.id.tv_item_price);
            this.llRootLayout = itemView.findViewById(R.id.root_layout);
            this.ivPushBtn = itemView.findViewById(R.id.iv_push_btn);
            this.ivEditBtn = itemView.findViewById(R.id.iv_edit_btn);
            this.ivDeleteBtn = itemView.findViewById(R.id.iv_delete_btn);
        }
    }
}
