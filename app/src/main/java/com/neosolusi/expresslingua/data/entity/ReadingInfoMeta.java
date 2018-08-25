package com.neosolusi.expresslingua.data.entity;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class ReadingInfoMeta extends RealmObject {

    @PrimaryKey
    private long id;
    private long menu_id;
    private int wordCount;
    private int wordMarked;
    private int sentenceCount;
    private int sentenceMarked;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getMenu_id() {
        return menu_id;
    }

    public void setMenu_id(long menu_id) {
        this.menu_id = menu_id;
    }

    public int getWordCount() {
        return wordCount;
    }

    public void setWordCount(int wordCount) {
        this.wordCount = wordCount;
    }

    public int getWordMarked() {
        return wordMarked;
    }

    public void setWordMarked(int wordMarked) {
        this.wordMarked = wordMarked;
    }

    public int getSentenceCount() {
        return sentenceCount;
    }

    public void setSentenceCount(int sentenceCount) {
        this.sentenceCount = sentenceCount;
    }

    public int getSentenceMarked() {
        return sentenceMarked;
    }

    public void setSentenceMarked(int sentenceMarked) {
        this.sentenceMarked = sentenceMarked;
    }
}
