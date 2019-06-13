package com.example.r311.numizmatik.data;

import java.io.Serializable;

public class Kovanec implements Serializable {
    String uuid;
    String slika;
    String drzava;
    int vrednost;
    int	stevec;
    Lokacija lokacija;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getDrzava() {
        return drzava;
    }

    public void setDrzava(String drzava) {
        this.drzava = drzava;
    }

    public int getVrednost() {
        return vrednost;
    }

    public void setVrednost(int vrednost) {
        this.vrednost = vrednost;
    }

    public String getSlika() {
        return slika;
    }

    public void setSlika(String slika) {
        this.slika = slika;
    }

    public int getStevec() {
        return stevec;
    }

    public void setStevec(int stevec) {
        this.stevec = stevec;
    }

    public Lokacija getLokacija() {
        return lokacija;
    }

    public Kovanec() {
    }

    public Kovanec(String uuid, String drzava, int vrednost, String slika, Lokacija lok) {
        this.uuid = uuid;
        this.drzava = drzava;
        this.vrednost = vrednost;
        this.slika = slika;
        this.lokacija = lok;
    }

    public Kovanec(String drzava, int vrednost, String slika) {
        this.drzava = drzava;
        this.vrednost = vrednost;
        this.slika = slika;
    }

}
