package com.example.shopmate;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class ItemsList extends ArrayAdapter<Item> {

    private Activity context;
    private List<Item> itemsLists;
    public ItemsList(Activity context,List<Item> itemsLists){
        super(context,R.layout.mylist,itemsLists);
        this.context=context;
        this.itemsLists=itemsLists;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View listViewItem = inflater.inflate(R.layout.mylist,null,true);

        TextView item = (TextView) listViewItem.findViewById(R.id.item);
        TextView quantity = (TextView) listViewItem.findViewById(R.id.quantity);
        TextView price = (TextView) listViewItem.findViewById(R.id.price);

        Item items = itemsLists.get(position);
        price.setText(String.valueOf(items.getPrice()));
        quantity.setText(String.valueOf(items.getQuantity()));
        item.setText(items.getItem());



        return listViewItem;
    }
}
