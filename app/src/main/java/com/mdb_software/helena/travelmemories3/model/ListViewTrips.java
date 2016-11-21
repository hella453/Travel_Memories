package com.mdb_software.helena.travelmemories3.model;

import java.util.Date;

/**
 * Created by Helena on 5/10/2015.
 */
public class ListViewTrips {
    private String imageUrl;
    private String title;
    private Date startDate;
    private Date endDate;
    private int ID;
    private int brojLokacija;


    public ListViewTrips(){

    }
    public ListViewTrips (String imageUrl, String title, Date startDate, Date endDate, int ID, int brojLokacija){
        this.imageUrl = imageUrl;
        this.ID = ID;
        this.title = title;
        this.startDate = startDate;
        this.endDate = endDate;
        this.brojLokacija=brojLokacija;
    }
    public ListViewTrips (String title,  int ID){
        this.ID = ID;
        this.title = title;

    }

    public int getBrojLokacija() {
        return brojLokacija;
    }

    public Date getStartDate() {
        return startDate;
    }

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public Date getEndDate() {
        return endDate;

    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
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
