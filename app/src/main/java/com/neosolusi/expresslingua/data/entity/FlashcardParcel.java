package com.neosolusi.expresslingua.data.entity;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.Gson;
import com.neosolusi.expresslingua.algorithm.SM2.State;

import java.util.Date;

public class FlashcardParcel implements Parcelable {

    private long id;
    private long reference;
    private String card;
    private String translation;
    private String category;
    private String type;
    private int mastering_level;
    private int already_read;
    private int selected;
    private boolean uploaded;
    private Date datecreated;
    private Date datemodified;
    private boolean reviewed;
    private String state;
    private int repeat;
    private double interval;
    private double e_factor;
    private Date next_show;
    private int easy_counter;
    private int read_repeat;

    public FlashcardParcel(Flashcard flashcard) {
        this.id = flashcard.getId();
        this.reference = flashcard.getReference();
        this.card = flashcard.getCard();
        this.translation = flashcard.getTranslation();
        this.category = flashcard.getCategory();
        this.type = flashcard.getType();
        this.mastering_level = flashcard.getMastering_level();
        this.already_read = flashcard.getAlready_read();
        this.selected = flashcard.getSelected();
        this.uploaded = flashcard.isUploaded();
        this.datecreated = flashcard.getDatecreated();
        this.datemodified = flashcard.getDatemodified();
        this.reviewed = flashcard.isReviewed();
        this.state = flashcard.getState().name();
        this.repeat = flashcard.getRepeat();
        this.interval = flashcard.getInterval();
        this.e_factor = flashcard.getE_factor();
        this.next_show = flashcard.getNext_show();
        this.easy_counter = flashcard.getEasy_counter();
        this.read_repeat = flashcard.getRead_repeat();
    }

    protected FlashcardParcel(Parcel in) {
        id = in.readLong();
        reference = in.readLong();
        card = in.readString();
        translation = in.readString();
        category = in.readString();
        type = in.readString();
        mastering_level = in.readInt();
        already_read = in.readInt();
        selected = in.readInt();
        uploaded = in.readByte() != 0x00;
        long tmpDatecreated = in.readLong();
        datecreated = tmpDatecreated != -1 ? new Date(tmpDatecreated) : null;
        long tmpDatemodified = in.readLong();
        datemodified = tmpDatemodified != -1 ? new Date(tmpDatemodified) : null;
        reviewed = in.readByte() != 0x00;
        state = in.readString();
        repeat = in.readInt();
        interval = in.readDouble();
        e_factor = in.readDouble();
        long tmpNext_show = in.readLong();
        next_show = tmpNext_show != -1 ? new Date(tmpNext_show) : null;
        easy_counter = in.readInt();
        read_repeat = in.readInt();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getReference() {
        return reference;
    }

    public void setReference(long reference) {
        this.reference = reference;
    }

    public String getCard() {
        return card;
    }

    public void setCard(String card) {
        this.card = card;
    }

    public String getTranslation() {
        return translation;
    }

    public String getCategory() {
        return category;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getMastering_level() {
        return mastering_level;
    }

    public int getAlready_read() {
        return already_read;
    }

    public void setAlready_read(int already_read) {
        this.already_read = already_read;
    }

    public int getSelected() {
        return selected;
    }

    public void setSelected(int selected) {
        this.selected = selected;
    }

    public Date getDatecreated() {
        return datecreated;
    }

    public Date getDatemodified() {
        return datemodified;
    }

    public boolean isUploaded() {
        return uploaded;
    }

    public boolean isReviewed() {
        return reviewed;
    }

    public State getState() {
        return State.valueOf(this.state);
    }

    public int getRepeat() {
        return repeat;
    }

    public double getInterval() {
        return interval;
    }

    public double getE_factor() {
        return e_factor;
    }

    public Date getNext_show() {
        return next_show;
    }

    public void setNext_show(Date next_show) {
        this.next_show = next_show;
    }

    public int getEasy_counter() {
        return easy_counter;
    }

    public void setEasy_counter(int easy_counter) {
        this.easy_counter = easy_counter;
    }

    public int getRead_repeat() {
        return read_repeat;
    }

    public void setRead_repeat(int read_repeat) {
        this.read_repeat = read_repeat;
    }

    public static final Creator<FlashcardParcel> CREATOR = new Creator<FlashcardParcel>() {
        @Override
        public FlashcardParcel createFromParcel(Parcel in) {
            return new FlashcardParcel(in);
        }

        @Override
        public FlashcardParcel[] newArray(int size) {
            return new FlashcardParcel[size];
        }
    };

    @Override public int describeContents() {
        return 0;
    }

    @Override public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeLong(reference);
        dest.writeString(card);
        dest.writeString(translation);
        dest.writeString(category);
        dest.writeString(type);
        dest.writeInt(mastering_level);
        dest.writeInt(already_read);
        dest.writeInt(selected);
        dest.writeByte((byte) (uploaded ? 0x01 : 0x00));
        dest.writeLong(datecreated != null ? datecreated.getTime() : -1L);
        dest.writeLong(datemodified != null ? datemodified.getTime() : -1L);
        dest.writeByte((byte) (reviewed ? 0x01 : 0x00));
        dest.writeString(state);
        dest.writeInt(repeat);
        dest.writeDouble(interval);
        dest.writeDouble(e_factor);
        dest.writeLong(next_show != null ? next_show.getTime() : -1L);
        dest.writeInt(easy_counter);
        dest.writeInt(read_repeat);
    }

    @Override public String toString() {
        return new Gson().toJson(this);
    }
}
