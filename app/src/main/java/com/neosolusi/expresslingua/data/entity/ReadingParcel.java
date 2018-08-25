package com.neosolusi.expresslingua.data.entity;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.Gson;

import java.util.Date;

public class ReadingParcel implements Parcelable {

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

    public ReadingParcel(Reading reading) {
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

    protected ReadingParcel(Parcel in) {
        id = in.readLong();
        file_id = in.readInt();
        actor = in.readString();
        sequence_no = in.readInt();
        sentence = in.readString();
        translation = in.readString();
        mastering_level = in.readInt();
        already_read = in.readInt();
        sec = in.readInt();
        uploaded = in.readByte() != 0;
        selected = in.readInt();
        bookmarked = in.readByte() != 0x00;
        kal_panjang = in.readInt();
        long tmpDatecreated = in.readLong();
        datecreated = tmpDatecreated != -1 ? new Date(tmpDatecreated) : null;
        long tmpDatemodified = in.readLong();
        datemodified = tmpDatemodified != -1 ? new Date(tmpDatemodified) : null;
        start_duration = in.readString();
        end_duration = in.readString();
    }

    public static final Creator<ReadingParcel> CREATOR = new Creator<ReadingParcel>() {
        @Override
        public ReadingParcel createFromParcel(Parcel in) {
            return new ReadingParcel(in);
        }

        @Override
        public ReadingParcel[] newArray(int size) {
            return new ReadingParcel[size];
        }
    };

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

    @Override public int describeContents() {
        return 0;
    }

    @Override public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeInt(file_id);
        dest.writeString(actor);
        dest.writeInt(sequence_no);
        dest.writeString(sentence);
        dest.writeString(translation);
        dest.writeInt(mastering_level);
        dest.writeInt(already_read);
        dest.writeInt(sec);
        dest.writeByte((byte) (uploaded ? 1 : 0));
        dest.writeInt(selected);
        dest.writeByte((byte) (bookmarked ? 0x01 : 0x00));
        dest.writeInt(kal_panjang);
        dest.writeLong(datecreated != null ? datecreated.getTime() : -1L);
        dest.writeLong(datemodified != null ? datemodified.getTime() : -1L);
        dest.writeString(start_duration);
        dest.writeString(end_duration);
    }

    @Override public String toString() {
        return new Gson().toJson(this);
    }
}
