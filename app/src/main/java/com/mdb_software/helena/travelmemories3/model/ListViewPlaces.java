package com.mdb_software.helena.travelmemories3.model;

import java.util.Date;

/**
 * Created by Helena on 5/10/2015.
 */
public class ListViewPlaces {
    private String imageUrl;
    private String title;
    private Date datum;
    private int ID;
    private double longitude, latitude;


    public ListViewPlaces(){

    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;

    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public ListViewPlaces (String imageUrl, String title, Date datum, int ID, double longitude, double latitude){
        this.imageUrl = imageUrl;
        this.ID = ID;
        this.title = title;
        this.datum = datum;
        this.longitude = longitude;
        this.latitude=latitude;

    }

    public Date getDatum() {
        return datum;
    }

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public String getImageUrl(){
        return imageUrl;
    }

    public void setImageUrl(String imageUrl){
        this.imageUrl = imageUrl;
    }
    public String getTitle(){
        return title;
    }

    public void setTitle(String title){
        this.title = title;
    }


}
