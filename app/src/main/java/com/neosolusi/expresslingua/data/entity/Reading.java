package com.neosolusi.expresslingua.data.entity;

import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Reading extends RealmObject {

    @PrimaryKey
    private long id;
    private int file_id;
    private String actor;
    private int sequence_no;
    private String sentence;
    private String translation;
    private int mastering_level;
    private int already_read;
    private int sec;
    private boolean uploaded;
    private int selected;
    private boolean bookmarked;
    private int kal_panjang;
    private Date datecreated;
    private Date datemodified;
    private String start_duration;
    private String end_duration;

    public Reading() {
        // Required empty constructor
    }

    public Reading(ReadingParcel reading) {
        this.id = reading.getId();
        this.file_id = reading.getFile_id();
        this.actor = reading.getActor();
        this.sequence_no = reading.getSequence_no();
        this.sentence = reading.getSentence();
        this.translation = reading.getTranslation();
        this.mastering_level = reading.getMastering_level();
        this.already_read = reading.getAlready_read();
        this.sec = reading.getSec();
        this.uploaded = reading.isUploaded();
        this.selected = reading.getSelected();
        this.bookmarked = reading.isBookmarked();
        this.kal_panjang = reading.getKal_panjang();
        this.datecreated = reading.getDatecreated();
        this.datemodified = reading.getDatemodified();
        this.start_duration = reading.getStart_duration();
        this.end_duration = reading.getEnd_duration();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getFile_id() {
        return file_id;
    }

    public void setFile_id(int file_id) {
        this.file_id = file_id;
    }

    public String getActor() {
        return actor;
    }

    public void setActor(String actor) {
        this.actor = actor;
    }

    public int getSequence_no() {
        return sequence_no;
    }

    public void setSequence_no(int sequence_no) {
        this.sequence_no = sequence_no;
    }

    public String getSentence() {
        return sentence;
    }

    public void setSentence(String sentence) {
        this.sentence = sentence;
    }

    public String getTranslation() {
        return translation;
    }

    public void setTranslation(String translation) {
        this.translation = translation;
    }

    public int getMastering_level() {
        return mastering_level;
    }

    public void setMastering_level(int mastering_level) {
        this.mastering_level = mastering_level;
    }

    public int getAlready_read() {
        return already_read;
    }

    public void setAlready_read(int already_read) {
        this.already_read = already_read;
    }

    public int getSec() {
        return sec;
    }

    public void setSec(int sec) {
        this.sec = sec;
    }

    public boolean isUploaded() {
        return uploaded;
    }

    public void setUploaded(boolean uploaded) {
        this.uploaded = uploaded;
    }

    public int getSelected() {
        return selected;
    }

    public void setSelected(int selected) {
        this.selected = selected;
    }

    public boolean isBookmarked() {
        return bookmarked;
    }

    public void setBookmarked(boolean bookmarked) {
        this.bookmarked = bookmarked;
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

    public String getStart_duration() {
        return start_duration;
    }

    public void setStart_duration(String start_duration) {
        this.start_duration = start_duration;
    }

    public String getEnd_duration() {
        return end_duration;
    }

    public void setEnd_duration(String end_duration) {
        this.end_duration = end_duration;
    }

    public int getKal_panjang() {
        return kal_panjang;
    }

    public void setKal_panjang(int kal_panjang) {
        this.kal_panjang = kal_panjang;
    }
}
