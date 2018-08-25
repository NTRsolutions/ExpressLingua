package com.neosolusi.expresslingua.data.entity;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Group extends RealmObject {

    @PrimaryKey
    @SerializedName("groupId")
    private long id;

    @SerializedName("groupName")
    private String name;

    @SerializedName("remarks")
    private String description;

    @SerializedName("img_url")
    private String url;

    @SerializedName("groupOwner")
    private String admin;

    @SerializedName("groupPrivacy")
    private int privacy;

    @SerializedName("groupTranslate")
    private int translate;

    private int member_count;

    private Date datecreated;
    private Date datemodified;

    public Group() {
        // Required empty constructor
    }

    public Group(GroupParcel parcel) {
        id = parcel.getId();
        name = parcel.getName();
        description = parcel.getDescription();
        url = parcel.getUrl();
        admin = parcel.getAdmin();
        privacy = parcel.getPrivacy();
        translate = parcel.getTranslate();
        member_count = parcel.getMember_count();
        datecreated = parcel.getDatecreated();
        datemodified = parcel.getDatemodified();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getAdmin() {
        return admin;
    }

    public void setAdmin(String admin) {
        this.admin = admin;
    }

    public int getMember_count() {
        return member_count;
    }

    public void setMember_count(int member_count) {
        this.member_count = member_count;
    }

    public int getPrivacy() {
        return privacy;
    }

    public void setPrivacy(int privacy) {
        this.privacy = privacy;
    }

    public int getTranslate() {
        return translate;
    }

    public void setTranslate(int translate) {
        this.translate = translate;
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
}
