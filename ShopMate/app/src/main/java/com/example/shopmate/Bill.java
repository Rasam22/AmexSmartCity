package com.example.shopmate;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class Bill extends Activity {

    Button pay1;
    private TextView txtInput;
    DatabaseReference databaseRef;
    ListView listviewitems;

    List<Item> itemList;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bill_layout);
        txtInput = (TextView)findViewById(R.id.bill_cartno);
        FirebaseApp.initializeApp(this);
        databaseRef = FirebaseDatabase.getInstance().getReference("Carts");
        listviewitems = (ListView) findViewById(R.id.listViewItems);
        itemList=new ArrayList<>();
        Item item1=new Item("Maggi Noodles",2,10);
        Item item2=new Item("Soap",2,16);
        Item item3=new Item("Washing powder",1,100);
        Item item4=new Item("Lays",5,20);
        itemList.add(item1);
        itemList.add(item2);
        itemList.add(item3);
        itemList.add(item4);
        ItemsList adapter = new ItemsList(Bill.this,itemList);
        listviewitems.setAdapter(adapter);
        Intent i = getIntent();
        final String input = i.getStringExtra("input");
        txtInput.setText("Cart Number : " + input);
        pay1 = (Button) findViewById(R.id.pay1);
        pay1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent i = new Intent(Bill.this, MakePayment.class);
                i.putExtra("input",input);
                startActivity(i);
            }
        });
    }
    @Override
    protected void onStart() {
        super.onStart();
        try{
            databaseRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    for(DataSnapshot artistSnapshot : dataSnapshot.getChildren())
                    {
                        Toast.makeText(Bill.this,"Enter Cart Number...",Toast.LENGTH_SHORT).show();
                        Item itms = artistSnapshot.getValue(Item.class);

                        itemList.add(itms);
                        Toast.makeText(Bill.this,"Enter Cart Number..."+itemList,Toast.LENGTH_SHORT).show();
                    }

                    ItemsList adapter = new ItemsList(Bill.this,itemList);
                    listviewitems.setAdapter(adapter);

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });}
        catch (Exception e){
            Log.println(1,"error","exception occured");
        }
    }




}
