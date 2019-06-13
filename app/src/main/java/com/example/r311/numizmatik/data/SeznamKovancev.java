package com.example.r311.numizmatik.data;

import java.util.ArrayList;

public class SeznamKovancev {
    String imeSeznama;
    String vrstaSeznama;
    public ArrayList<Kovanec> arrayKovanci;

    public SeznamKovancev(String imeSeznama, String vrstaSeznama) {
        this.imeSeznama = imeSeznama;
        this.vrstaSeznama = vrstaSeznama;
        arrayKovanci = new ArrayList<>();
    }

    public String getImeSeznama() {
        return imeSeznama;
    }

    public void setImeSeznama(String imeSeznama) {
        this.imeSeznama = imeSeznama;
    }

    public String getVrstaSeznama() {
        return vrstaSeznama;
    }

    public void setVrstaSeznama(String vrstaSeznama) {
        this.vrstaSeznama = vrstaSeznama;
    }

    public ArrayList<Kovanec> getArrayKovanci() {
        return arrayKovanci;
    }

    public void setArrayKovanci(ArrayList<Kovanec> arrayKovanci) {
        this.arrayKovanci = arrayKovanci;
    }


    public void dodaj(Kovanec k){arrayKovanci.add(k);}

    @Override
    public String toString() {
        return super.toString();
    }
}
