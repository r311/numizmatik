package com.example.r311.numizmatik.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.example.r311.numizmatik.R;
import com.example.r311.numizmatik.data.Kovanci;
import com.example.r311.numizmatik.data.SeznamKovancev;

public class StartActivity extends AppCompatActivity {
    public static void main(String s[]){

    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.btnOpenColection);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(StartActivity.this, GalerijaActivity.class));
            }
        });

        FloatingActionButton kamera = (FloatingActionButton) findViewById(R.id.btnOpenCamera);
        kamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(StartActivity.this, SlikajActivity.class));
            }
        });
    }


    private SeznamKovancev initSeznamKovanci(){
        SeznamKovancev seznam = new SeznamKovancev("Euro","bankovci");

        Kovanci k1 = new Kovanci("38828282", "Slovenia", 2, "1");
        Kovanci k2 = new Kovanci("55353443", "Avstrija", 2, "4");

        seznam.dodaj(k1);
        seznam.dodaj(k1);
        seznam.dodaj(k2);

        return seznam;
    }
}
