package com.neosolusi.expresslingua.features.flashcard;

import android.view.View;

import com.neosolusi.expresslingua.data.entity.Flashcard;

import io.realm.RealmResults;

public interface OnInteractionListener {
    void onClick(View view, Flashcard entity, int position, int selectType);

    void onDoubleClick(RealmResults<Flashcard> entities, int position);

    void onTextSelected(Flashcard entity, String text, int position);

    void onSpeak(View view, Flashcard entity, int position, boolean isSlow);

    void onMoreOptionClick(View view, Flashcard entity);
}
