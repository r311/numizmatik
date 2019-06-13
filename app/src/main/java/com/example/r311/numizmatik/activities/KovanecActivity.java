package com.example.r311.numizmatik.activities;

import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.example.r311.numizmatik.R;
import com.example.r311.numizmatik.data.Kovanec;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class KovanecActivity extends FragmentActivity implements OnMapReadyCallback {//AppCompatActivity ,  FragmentActivity

    Location currentLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kovanec);

        Kovanec kov = (Kovanec) getIntent().getSerializableExtra("KOVANEC");
        currentLocation = new Location("dummyprovider");
        currentLocation.setLatitude(kov.getLokacija().getxCrd());
        currentLocation.setLongitude(kov.getLokacija().getyCrd());
        Log.d("Lokacija", String.valueOf(kov.getLokacija().getxCrd()));
        Log.d("Lokacija", String.valueOf(kov.getLokacija().getyCrd()));

        SupportMapFragment supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.google_map);
        supportMapFragment.getMapAsync(KovanecActivity.this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        LatLng lokacija = new LatLng(currentLocation.getLatitude(),currentLocation.getLongitude());
        MarkerOptions mo = new MarkerOptions().position(lokacija).title("kovanec");
        googleMap.animateCamera(CameraUpdateFactory.newLatLng(lokacija));
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(lokacija,15));
        googleMap.addMarker(mo);
    }
}
