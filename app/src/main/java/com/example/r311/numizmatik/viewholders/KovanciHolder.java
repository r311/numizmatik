package com.example.r311.numizmatik.viewholders;

import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.r311.numizmatik.R;
//import com.example.r311.numizmatik.adapters.KovanciAdapter;

public class KovanciHolder extends RecyclerView.ViewHolder {

    public View view;
    public TextView drzava;
    public TextView vrednost;
    public ImageView slika;
    public ConstraintLayout cLayout;

    public KovanciHolder(View view){
        super(view);
        this.view = view;
        drzava = view.findViewById(R.id.textDrzava);
        vrednost = view.findViewById(R.id.textVrednost);
        slika = view.findViewById(R.id.imageKovanec);
        cLayout = view.findViewById(R.id.cLayout);
    }

    @NonNull
    public KovanciHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        View v = (View) LayoutInflater.from(parent.getContext()).inflate(R.layout.item_kovanec, parent, false);
        return new KovanciHolder(view);
    }
    public  void onBindViewHolder(@NonNull KovanciHolder holder, int position) {
    }
}
