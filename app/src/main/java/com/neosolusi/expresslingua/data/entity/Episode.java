package com.neosolusi.expresslingua.data.entity;

import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Episode extends RealmObject {

    @PrimaryKey
    private long episode_id;
    private String name_episode;
    private String title;
    private String title_trans;
    private int sequence_no;
    private Date datecreated;
    private Date datemodified;

    public long getEpisode_id() {
        return episode_id;
    }

    public void setEpisode_id(long episode_id) {
        this.episode_id = episode_id;
    }

    public String getName_episode() {
        return name_episode;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle_trans() {
        return title_trans;
    }

    public int getSequence_no() {
        return sequence_no;
    }

    public void setSequence_no(int sequence_no) {
        this.sequence_no = sequence_no;
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
