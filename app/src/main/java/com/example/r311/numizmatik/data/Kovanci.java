package com.example.r311.numizmatik.data;

public class Kovanci {
    String uuid;
    String slika;
    String drzava;
    int vrednost;
    int	stevec;

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

    public Kovanci() {
    }

    public Kovanci(String uuid, String drzava, int vrednost, String slika) {
        this.uuid = uuid;
        this.drzava = drzava;
        this.vrednost = vrednost;
        this.slika = slika;
    }

    public Kovanci(String drzava, int vrednost, String slika) {
        this.drzava = drzava;
        this.vrednost = vrednost;
        this.slika = slika;
    }
}
