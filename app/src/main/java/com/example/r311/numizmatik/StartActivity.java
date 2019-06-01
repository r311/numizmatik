package com.example.r311.numizmatik;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

public class StartActivity extends AppCompatActivity {
    public static void main(String s[]){
        SeznamKovancev seznam = new SeznamKovancev("Euro","bankovci");

        Kovanci k1 = new Kovanci(38828282, "Slovenia", 2, "eur", false, 1);
        Kovanci k2 = new Kovanci(55353443, "Avstrija", 2, "cent", false, 1);

        seznam.dodaj(k1);
        seznam.dodaj(k1);
        seznam.dodaj(k2);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

}
