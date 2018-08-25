package com.neosolusi.expresslingua.data.entity;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

public class GroupParcel implements Parcelable {

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<GroupParcel> CREATOR = new Parcelable.Creator<GroupParcel>() {
        @Override
        public GroupParcel createFromParcel(Parcel in) {
            return new GroupParcel(in);
        }

        @Override
        public GroupParcel[] newArray(int size) {
            return new GroupParcel[size];
        }
    };
    private long id;
    private String name;
    private String description;
    private String url;
    private String admin;
    private int privacy;
    private int translate;
    private int member_count;
    private Date datecreated;
    private Date datemodified;

    public GroupParcel() {
        // Empty constructor
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

    public int getMember_count() {
        return member_count;
    }

    public void setMember_count(int member_count) {
        this.member_count = member_count;
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

    protected GroupParcel(Parcel in) {
        id = in.readLong();
        name = in.readString();
        description = in.readString();
        url = in.readString();
        admin = in.readString();
        privacy = in.readInt();
        translate = in.readInt();
        member_count = in.readInt();
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
        dest.writeString(name);
        dest.writeString(description);
        dest.writeString(url);
        dest.writeString(admin);
        dest.writeInt(privacy);
        dest.writeInt(translate);
        dest.writeInt(member_count);
        dest.writeLong(datecreated != null ? datecreated.getTime() : -1L);
        dest.writeLong(datemodified != null ? datemodified.getTime() : -1L);
    }

    @Override
    public String toString() {
        return "GroupParcel{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", url='" + url + '\'' +
                ", admin='" + admin + '\'' +
                ", privacy='" + privacy + '\'' +
                ", translate='" + translate + '\'' +
                ", member_count='" + member_count + '\'' +
                ", datecreated='" + datecreated + '\'' +
                ", datemodified='" + datemodified + '\'' +
                '}';
    }

}
