package com.example.r311.numizmatik;

public class Kovanci {
    int uuid;
    String drzava;
    int vrednost;
    String slika;
    Boolean imam;
    int	stevec;

    public int getUuid() {
        return uuid;
    }

    public void setUuid(int uuid) {
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

    public Boolean getImam() {
        return imam;
    }

    public void setImam(Boolean imam) {
        this.imam = imam;
    }

    public int getStevec() {
        return stevec;
    }

    public void setStevec(int stevec) {
        this.stevec = stevec;
    }

    public Kovanci(int uuid, String drzava, int vrednost, String slika, Boolean imam, int stevec) {
        this.uuid = uuid;
        this.drzava = drzava;
        this.vrednost = vrednost;
        this.slika = slika;
        this.imam = imam;
        this.stevec = stevec;
    }
}
