package com.neosolusi.expresslingua.data.entity;

import com.google.gson.annotations.SerializedName;
import com.neosolusi.expresslingua.algorithm.SM2.State;

import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Flashcard extends RealmObject {

    @PrimaryKey
    private long id;

    private long reference;

    @SerializedName("word")
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

    public Flashcard() {
        // Required empty constructor
    }

    public Flashcard(FlashcardParcel flashcard) {
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

    public void setCard(String word) {
        this.card = word;
    }

    public String getTranslation() {
        return translation;
    }

    public void setTranslation(String translation) {
        this.translation = translation;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
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

    public void setDatecreated(Date datecreated) {
        this.datecreated = datecreated;
    }

    public Date getDatemodified() {
        return datemodified;
    }

    public void setDatemodified(Date datemodified) {
        this.datemodified = datemodified;
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

    public boolean isUploaded() {
        return uploaded;
    }

    public void setUploaded(boolean uploaded) {
        this.uploaded = uploaded;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getRepeat() {
        return repeat;
    }

    public void setRepeat(int repeat) {
        this.repeat = repeat;
    }

    public double getInterval() {
        return interval;
    }

    public void setInterval(double interval) {
        this.interval = interval;
    }

    public double getE_factor() {
        return e_factor;
    }

    public void setE_factor(double e_factor) {
        this.e_factor = e_factor;
    }

    public Date getNext_show() {
        return next_show;
    }

    public void setNext_show(Date next_show) {
        this.next_show = next_show;
    }

    public boolean isReviewed() {
        return reviewed;
    }

    public void setReviewed(boolean reviewed) {
        this.reviewed = reviewed;
    }

    public State getState() {
        return State.valueOf(this.state);
    }

    public void setState(State state) {
        this.state = state.toString();
    }

    public String checkState() {
        return this.state;
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

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Flashcard flashcard = (Flashcard) o;

        return id == flashcard.id;
    }

    @Override public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }

}
