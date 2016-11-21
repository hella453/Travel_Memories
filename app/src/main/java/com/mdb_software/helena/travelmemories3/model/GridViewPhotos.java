package com.mdb_software.helena.travelmemories3.model;

/**
 * Created by Helena on 6/5/2015.
 */
public class GridViewPhotos  {
    int photoID;
    String photoUrl;

    public GridViewPhotos(int photoID, String photoUrl){
        this.photoID=photoID;
        this.photoUrl=photoUrl;
       }
    public GridViewPhotos(){

    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public int getPhotoID() {
        return photoID;
    }

    public void setPhotoID(int photoID) {
        this.photoID = photoID;
    }
}
