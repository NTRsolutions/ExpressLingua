package com.neosolusi.expresslingua.features.challenge;

public class PuzzleItem {

    public long id;
    public String word;
    public boolean selected;

    public PuzzleItem(long id, String word, boolean selected) {
        this.id = id;
        this.word = word;
        this.selected = selected;
    }
}
