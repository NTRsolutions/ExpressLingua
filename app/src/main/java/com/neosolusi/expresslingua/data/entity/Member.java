package com.neosolusi.expresslingua.data.entity;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Member extends RealmObject {

    @PrimaryKey
    private long id;

    @SerializedName("groupId")
    private long group_id;

    @SerializedName("userid")
    private String user_id;

    @SerializedName("avatar")
    private String url;

    private int approved;
    private int permission;
    private boolean uploaded;
    private Date datecreated;
    private Date datemodified;

    public Member() {
        // Required empty constructor
    }

    public Member(long id, long group, String user, String url, boolean uploaded) {
        this.setId(id);
        this.setGroup_id(group);
        this.setUser_id(user);
        this.setUrl(url);
        this.setApproved(0);
        this.setPermission(0);
        this.setUploaded(uploaded);
        this.setDatecreated(new Date());
        this.setDatemodified(new Date());
    }

    public Member(MemberParcel parcel) {
        id = parcel.getId();
        group_id = parcel.getGroup_id();
        user_id = parcel.getUser_id();
        url = parcel.getUrl();
        approved = parcel.getApproved();
        permission = parcel.getPermission();
        uploaded = parcel.isUploaded();
        datecreated = parcel.getDatecreated();
        datemodified = parcel.getDatemodified();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getGroup_id() {
        return group_id;
    }

    public void setGroup_id(long group_id) {
        this.group_id = group_id;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Date getDatecreated() {
        return datecreated;
    }

    public void setDatecreated(Date datecreated) {
        this.datecreated = datecreated;
    }

    public Date getDatemodified() {
        return datemodified;
    }

    public void setDatemodified(Date datemodified) {
        this.datemodified = datemodified;
    }

    public int getApproved() {
        return approved;
    }

    public void setApproved(int approved) {
        this.approved = approved;
    }

    public int getPermission() {
        return permission;
    }

    public void setPermission(int permission) {
        this.permission = permission;
    }

    public boolean isUploaded() {
        return uploaded;
    }

    public void setUploaded(boolean uploaded) {
        this.uploaded = uploaded;
    }
}
