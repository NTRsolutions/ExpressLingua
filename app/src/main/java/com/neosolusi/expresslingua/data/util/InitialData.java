package com.neosolusi.expresslingua.data.util;

import com.neosolusi.expresslingua.data.entity.Flashcard;

import io.realm.Realm;
import io.realm.RealmResults;

public class InitialData implements Realm.Transaction {

    @Override public void execute(Realm realm) {
//        RealmResults<Flashcard> flashcards = realm.where(Flashcard.class).equalTo("mastering_level", 4).findAll();
//        for (Flashcard flashcard : flashcards) {
//            if (flashcard.getEasy_counter() == 0) flashcard.setEasy_counter(1);
//        }
    }
}
