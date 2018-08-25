package com.neosolusi.expresslingua.algorithm;

import android.content.SharedPreferences;

public class Sentence extends SM2 {

    private static volatile Sentence mInstance;
    private static final Object LOCK = new Object();

    private Sentence(SharedPreferences preferences, SharedPreferences.Editor preferencesEdit) {
        super(preferences, preferencesEdit);
    }

    public synchronized static Sentence getInstance(SharedPreferences pref, SharedPreferences.Editor prefEdit) {
        if (mInstance == null) {
            synchronized (LOCK) {
                mInstance = new Sentence(pref, prefEdit);
            }
        }

        return mInstance;
    }

    @Override protected String flashcardType() {
        return "sentence";
    }
}
