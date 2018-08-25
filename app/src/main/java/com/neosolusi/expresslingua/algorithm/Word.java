package com.neosolusi.expresslingua.algorithm;

import android.content.SharedPreferences;

public class Word extends SM2 {

    private static volatile Word mInstance;
    private static final Object LOCK = new Object();

    private Word(SharedPreferences preferences, SharedPreferences.Editor preferencesEdit) {
        super(preferences, preferencesEdit);
    }

    public synchronized static Word getInstance(SharedPreferences pref, SharedPreferences.Editor prefEdit) {
        if (mInstance == null) {
            synchronized (LOCK) {
                mInstance = new Word(pref, prefEdit);
            }
        }

        return mInstance;
    }

    @Override protected String flashcardType() {
        return "word";
    }

}
