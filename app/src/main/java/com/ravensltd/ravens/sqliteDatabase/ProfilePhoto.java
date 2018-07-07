package com.ravensltd.ravens.sqliteDatabase;

/**
 * Created by jatin on 9/10/17.
 */

public class ProfilePhoto {

    String ID;
    byte [] img;

    public ProfilePhoto() {
    }

    public ProfilePhoto(String ID, byte[] img) {
        this.ID = ID;
        this.img = img;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public byte[] getImg() {
        return img;
    }

    public void setImg(byte[] img) {
        this.img = img;
    }
}
