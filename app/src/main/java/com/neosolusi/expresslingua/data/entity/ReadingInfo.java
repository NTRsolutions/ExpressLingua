package com.neosolusi.expresslingua.data.entity;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class ReadingInfo extends RealmObject {

    @PrimaryKey
    private long menu_id;
    private String audio_file_name;
    private String title;
    private String short_title;
    private String title_trans;
    private int file_id;
    private int episode_id;

    @SerializedName("jml_blok_kalimat")
    private int sentences_count;

    @SerializedName("jml_kata")
    private int words_count;

    private Date datecreated;
    private Date datemodified;
    private boolean download_complete;

    public String getAudio_file_name() {
        return audio_file_name;
    }

    public void setAudio_file_name(String audio_file_name) {
        this.audio_file_name = audio_file_name;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getShort_title() {
        return short_title;
    }

    public void setShort_title(String short_title) {
        this.short_title = short_title;
    }

    public String getTitle_trans() {
        return title_trans;
    }

    public void setTitle_trans(String title_trans) {
        this.title_trans = title_trans;
    }

    public long getMenu_id() {
        return menu_id;
    }

    public void setMenu_id(long menu_id) {
        this.menu_id = menu_id;
    }

    public int getFile_id() {
        return file_id;
    }

    public void setFile_id(int file_id) {
        this.file_id = file_id;
    }

    public int getEpisode_id() {
        return episode_id;
    }

    public void setEpisode_id(int episode_id) {
        this.episode_id = episode_id;
    }

    public boolean isDownload_complete() {
        return download_complete;
    }

    public void setDownload_complete(boolean download_complete) {
        this.download_complete = download_complete;
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

    public int getSentences_count() {
        return sentences_count;
    }

    public void setSentences_count(int sentences_count) {
        this.sentences_count = sentences_count;
    }

    public int getWords_count() {
        return words_count;
    }

    public void setWords_count(int words_count) {
        this.words_count = words_count;
    }
}
