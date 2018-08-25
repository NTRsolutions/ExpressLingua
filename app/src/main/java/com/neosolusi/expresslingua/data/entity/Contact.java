package com.neosolusi.expresslingua.data.entity;

import android.support.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Contact extends RealmObject implements Comparable {

    @PrimaryKey
    private long id;
    @SerializedName("cell_no")
    private String phone;
    private String name;
    private String imageUrl;
    private boolean isActive;
    private Date lastUpdate;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public Date getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Date lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    @Override public int compareTo(@NonNull Object o) {
        Contact compare = (Contact) o;

        if (compare.getId() == this.getId()) {
            return 0;
        } else {
            return 1;
        }
    }
}
