package com.example.r311.numizmatik.data;

import java.io.Serializable;

public class Lokacija implements Serializable {
    private double xCrd;
    private double yCrd;
    //private long crtTime;

    public double getxCrd() {
        return xCrd;
    }

    public void setxCrd(double xCrd) {
        this.xCrd = xCrd;
    }

    public double getyCrd() {
        return yCrd;
    }

    public void setyCrd(double yCrd) {
        this.yCrd = yCrd;
    }

    /*public long getCrtTime() {
        return crtTime;
    }

    public void setCrtTime(long crtTime) {
        this.crtTime = crtTime;
    }*/

    public Lokacija() {
    }

    public Lokacija(double xCrd, double yCrd){
        this.xCrd = xCrd;
        this.yCrd = yCrd;
    }

    /*public Lokacija(double xCrd, double yCrd, long crtTime){
        this.xCrd = xCrd;
        this.yCrd = yCrd;
        this.crtTime = crtTime;
    }*/

    @Override
    public String toString() {
        return "Lokacija{" +
                "xCrd=" + xCrd +
                ", yCrd=" + yCrd +
                //", crtTime=" + crtTime +
                '}';
    }
}