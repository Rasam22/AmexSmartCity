package com.example.shopmate;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Bill extends Activity {

    Button pay1,scanBt;
    private TextView txtInput;
    DatabaseReference databaseRef;
    ListView listviewitems;

    List<Item> itemList;
    DatabaseReference productRef;
    BottomSheetDialog dialog;

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

        findViewById(R.id.scanBt).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showScanner();
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
//            Log.println(,"error","exception occured");
        }
    }

    private void showScanner() {

        SurfaceView surfaceView;
        TextView txtBarcodeValue;
        BarcodeDetector barcodeDetector;
        CameraSource cameraSource;
        final int REQUEST_CAMERA_PERMISSION = 201;
        final String[] intentData = {""};
        boolean isEmail = false;

        dialog = new BottomSheetDialog(Bill.this);
        View sheetView = getLayoutInflater().inflate(R.layout.activity_scanner,null);
        dialog.setContentView(sheetView);

        txtBarcodeValue = dialog.findViewById(R.id.txtBarcodeValue);
        surfaceView = dialog.findViewById(R.id.surfaceView);

        barcodeDetector = new BarcodeDetector.Builder(Bill.this)
                .setBarcodeFormats(Barcode.ALL_FORMATS)
                .build();

        cameraSource = new CameraSource.Builder(Bill.this, barcodeDetector)
                .setRequestedPreviewSize(1920, 1080)
                .setAutoFocusEnabled(true) //you should add this feature
                .build();

        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                try {
                    if (ActivityCompat.checkSelfPermission(Bill.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                        cameraSource.start(surfaceView.getHolder());
                    } else {
                        ActivityCompat.requestPermissions(Bill.this, new
                                String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) { }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                cameraSource.stop();
            }
        });

        barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {
//                Toast.makeText(getContext(), "To prevent memory leaks barcode scanner has been stopped", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                final SparseArray<Barcode> barcodes = detections.getDetectedItems();
                if (barcodes.size() != 0) {

                    txtBarcodeValue.post(new Runnable() {

                        @Override
                        public void run() {

                            txtBarcodeValue.removeCallbacks(null);
                            intentData[0] = barcodes.valueAt(0).displayValue;
                            txtBarcodeValue.setText(intentData[0]);
                            Log.d("Bill","fetch data");

                            fetchData(intentData[0]);
                            dialog.cancel();

                            //TODO data is present here

                        }
                    });
                }
            }
        });

        dialog.show();

    }

    private void fetchData(String id) {

        productRef = FirebaseDatabase.getInstance().getReference().child("Products").child(id);
        Log.d("Bill","fetching data");

        productRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Log.d("Bill","existsssss");
                    String price = dataSnapshot.child("price").getValue().toString();
                    String quantity = dataSnapshot.child("quantity").getValue().toString();
                    String name = dataSnapshot.child("name").getValue().toString();

                    Log.d("Bill",name + price + quantity);

                    Item item1= new Item(name,Integer.parseInt(quantity),Integer.parseInt(price));
                    if(!itemList.contains(item1)){
                        itemList.add(item1);
                    }

                    Log.d("Bill","item added");

                    ItemsList adapter = new ItemsList(Bill.this,itemList);
                    listviewitems.setAdapter(adapter);
                    Log.d("Bill","adapter set");

                    dialog.cancel();

                    Toast.makeText(Bill.this, "Item added", Toast.LENGTH_SHORT).show();

                }else{
                    Log.d("Bill","doesnt exist");
                    Toast.makeText(Bill.this, "Item not present!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("Bill",databaseError.getMessage());
                Log.d("Bill",databaseError.getDetails());
            }
        });
    }

}
