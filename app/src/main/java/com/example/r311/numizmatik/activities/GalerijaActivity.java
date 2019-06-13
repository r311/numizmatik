package com.example.r311.numizmatik.activities;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.Toast;

import com.example.r311.numizmatik.R;
import com.example.r311.numizmatik.adapters.KovanciAdapter;
import com.example.r311.numizmatik.data.Kovanec;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class GalerijaActivity extends AppCompatActivity {

    private ArrayList<Kovanec> kov;

    DatabaseReference dbRef;
    private RecyclerView rvkovanci;
    private RecyclerView.Adapter adapter;
    ArrayList<Kovanec> kovSeznam;
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_galerija);
        context = this;

        dbRef = FirebaseDatabase.getInstance().getReference().child("slike");

        this.rvkovanci = (RecyclerView) findViewById(R.id.rv_kovanci);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        this.rvkovanci.setLayoutManager(mLayoutManager);

        kovSeznam = new ArrayList<Kovanec>();
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot dataSnapshot1: dataSnapshot.getChildren()){
                    Kovanec k = dataSnapshot1.getValue(Kovanec.class);
                    kovSeznam.add(k);
                }
                adapter = new KovanciAdapter(context, kovSeznam);
                rvkovanci.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(GalerijaActivity.this, "Error onCanceled",Toast.LENGTH_SHORT).show();
            }
        });

    }

}
