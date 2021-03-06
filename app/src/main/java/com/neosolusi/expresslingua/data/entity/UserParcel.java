package com.neosolusi.expresslingua.data.entity;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.Gson;

public class UserParcel implements Parcelable {

    private long id;
    private String userid;
    private String email;
    private String password;
    private int commercial_status;
    private String cell_no;
    private String address;
    private String city;
    private String province;
    private String country;
    private double gps_lat;
    private double gps_lng;
    private boolean active;
    private String manufacture;
    private String api_version;
    private String app_version;
    private String avatar;

    public UserParcel(User user) {
        this.id = user.getId();
        this.userid = user.getUserid();
        this.email = user.getEmail();
        this.password = user.getPassword();
        this.commercial_status = user.getCommercial_status();
        this.cell_no = user.getCell_no();
        this.address = user.getAddress();
        this.city = user.getCity();
        this.province = user.getProvince();
        this.country = user.getCountry();
        this.gps_lat = user.getGps_lat();
        this.gps_lng = user.getGps_lng();
        this.active = user.isActive();
        this.manufacture = user.getManufacture();
        this.api_version = user.getApi_version();
        this.app_version = user.getApp_version();
        this.avatar = user.getAvatar();
    }

    protected UserParcel(Parcel in) {
        id = in.readLong();
        userid = in.readString();
        email = in.readString();
        password = in.readString();
        commercial_status = in.readInt();
        cell_no = in.readString();
        address = in.readString();
        city = in.readString();
        province = in.readString();
        country = in.readString();
        gps_lat = in.readDouble();
        gps_lng = in.readDouble();
        active = in.readByte() != 0x00;
        manufacture = in.readString();
        api_version = in.readString();
        app_version = in.readString();
        avatar = in.readString();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getCommercial_status() {
        return commercial_status;
    }

    public void setCommercial_status(int commercial_status) {
        this.commercial_status = commercial_status;
    }

    public String getCell_no() {
        return cell_no;
    }

    public void setCell_no(String cell_no) {
        this.cell_no = cell_no;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public double getGps_lat() {
        return gps_lat;
    }

    public void setGps_lat(double gps_lat) {
        this.gps_lat = gps_lat;
    }

    public double getGps_lng() {
        return gps_lng;
    }

    public void setGps_lng(double gps_lng) {
        this.gps_lng = gps_lng;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getManufacture() {
        return manufacture;
    }

    public void setManufacture(String manufacture) {
        this.manufacture = manufacture;
    }

    public String getApi_version() {
        return api_version;
    }

    public void setApi_version(String api_version) {
        this.api_version = api_version;
    }

    public String getApp_version() {
        return app_version;
    }

    public void setApp_version(String app_version) {
        this.app_version = app_version;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    @Override public int describeContents() {
        return 0;
    }

    @Override public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(userid);
        dest.writeString(email);
        dest.writeString(password);
        dest.writeInt(commercial_status);
        dest.writeString(cell_no);
        dest.writeString(address);
        dest.writeString(city);
        dest.writeString(province);
        dest.writeString(country);
        dest.writeDouble(gps_lat);
        dest.writeDouble(gps_lng);
        dest.writeByte((byte) (active ? 0x01 : 0x00));
        dest.writeString(manufacture);
        dest.writeString(api_version);
        dest.writeString(app_version);
        dest.writeString(avatar);
    }

    public static final Parcelable.Creator<UserParcel> CREATOR = new Parcelable.Creator<UserParcel>() {
        @Override
        public UserParcel createFromParcel(Parcel in) {
            return new UserParcel(in);
        }

        @Override
        public UserParcel[] newArray(int size) {
            return new UserParcel[size];
        }
    };

    @Override public String toString() {
        return new Gson().toJson(this);
    }

}
