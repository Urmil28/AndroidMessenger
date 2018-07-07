package com.ravensltd.ravens.sqliteDatabase;

/**
 * Created by jatin on 9/10/17.
 */

public class ProfileInfo {

    private String userName,status,phone,uId;
    private byte[] profilePhotoID;

    public ProfileInfo() {
    }

    public ProfileInfo(String userName, String status, String phone, String uId, byte[] profilePhotoID) {
        this.userName = userName;
        this.status = status;
        this.phone = phone;
        this.uId = uId;
        this.profilePhotoID = profilePhotoID;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getuId() {
        return uId;
    }

    public void setuId(String uId) {
        this.uId = uId;
    }

    public byte[] getProfilePhotoID() {
        return profilePhotoID;
    }

    public void setProfilePhotoID(byte[] profilePhotoID) {
        this.profilePhotoID = profilePhotoID;
    }
}
