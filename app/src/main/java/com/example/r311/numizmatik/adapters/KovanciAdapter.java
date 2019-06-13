package com.example.r311.numizmatik.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.r311.numizmatik.R;
import com.example.r311.numizmatik.activities.KovanecActivity;
import com.example.r311.numizmatik.data.Kovanec;
import com.example.r311.numizmatik.viewholders.KovanciHolder;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class KovanciAdapter extends RecyclerView.Adapter<KovanciHolder> {

    private ArrayList<Kovanec> kov;
    Context context;

    public KovanciAdapter(Context c, ArrayList<Kovanec> kov) {
        this.context = c;
        this.kov = kov;
    }

    private static void startDView(Kovanec kov, Context ac) {
        Intent i = new Intent(ac, KovanecActivity.class);
        i.putExtra("KOVANEC", kov);
        ac.startActivity(i);
    }

    @NonNull
    @Override
    public KovanciHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = (View) LayoutInflater.from(parent.getContext()).inflate(R.layout.item_kovanec, parent, false);
        KovanciHolder vh = new KovanciHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(KovanciHolder holder, int position) {
        final Kovanec kovanc = kov.get(position);
        Log.d("kovanc", kovanc.getDrzava());
        Log.d("kovanc", String.valueOf(kovanc.getVrednost()));
        holder.drzava.setText(kovanc.getDrzava());
        holder.vrednost.setText(String.valueOf(kovanc.getVrednost()));
        Picasso.with(context).load(kovanc.getSlika()).fit().into(holder.slika);

        holder.cLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                KovanciAdapter.startDView(kovanc, context);
            }
        });
    }

    @Override
    public int getItemCount() {
        if( kov != null){
            return kov.size();
        }
        else{
            return 0;
        }
    }

    class myViewHolder extends RecyclerView.ViewHolder {

        public myViewHolder(View itemView) {
            super(itemView);
        }
    }

}
