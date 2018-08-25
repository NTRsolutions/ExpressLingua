package com.neosolusi.expresslingua.data.entity;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

public class MemberParcel implements Parcelable {

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<MemberParcel> CREATOR = new Parcelable.Creator<MemberParcel>() {
        @Override
        public MemberParcel createFromParcel(Parcel in) {
            return new MemberParcel(in);
        }

        @Override
        public MemberParcel[] newArray(int size) {
            return new MemberParcel[size];
        }
    };

    private long id;
    private long group_id;
    private String user_id;
    private String url;
    private int approved;
    private int permission;
    private boolean uploaded;
    private Date datecreated;
    private Date datemodified;

    public MemberParcel() {
        // Empty constructor
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

    public boolean isUploaded() {
        return uploaded;
    }

    public void setUploaded(boolean uploaded) {
        this.uploaded = uploaded;
    }

    public MemberParcel(Parcel in) {
        id = in.readLong();
        group_id = in.readLong();
        user_id = in.readString();
        url = in.readString();
        approved = in.readInt();
        permission = in.readInt();
        uploaded = in.readByte() != 0x00;
        long tmpDatecreated = in.readLong();
        datecreated = tmpDatecreated != -1 ? new Date(tmpDatecreated) : null;
        long tmpDatemodified = in.readLong();
        datemodified = tmpDatemodified != -1 ? new Date(tmpDatemodified) : null;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeLong(group_id);
        dest.writeString(user_id);
        dest.writeString(url);
        dest.writeInt(approved);
        dest.writeInt(permission);
        dest.writeByte((byte) (uploaded ? 0x01 : 0x00));
        dest.writeLong(datecreated != null ? datecreated.getTime() : -1L);
        dest.writeLong(datemodified != null ? datemodified.getTime() : -1L);
    }

    @Override
    public String toString() {
        return "MemberParcel{" +
                "id='" + id + '\'' +
                ", group_id='" + group_id + '\'' +
                ", user_id='" + user_id + '\'' +
                ", url='" + url + '\'' +
                ", approved='" + approved + '\'' +
                ", permission='" + permission + '\'' +
                ", datecreated='" + datecreated + '\'' +
                ", datemodified='" + datemodified + '\'' +
                '}';
    }

}
