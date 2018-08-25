package com.neosolusi.expresslingua.features.challenge;

import com.neosolusi.expresslingua.data.entity.Challenge;
import com.neosolusi.expresslingua.data.entity.Reading;
import com.neosolusi.expresslingua.data.entity.ReadingInfo;

public interface OnInteractionListener {

    void speak(Reading reading, boolean isSlow);

    void resetPlayer();

    void setPlayerInfo(ReadingInfo info);

    void correctAnswers(Challenge challenge);

    void wrongAnswers(Challenge challenge);

    void nextChallenge();

    void showFinishChallenges();

    void showNoChallenges();

    boolean useTranslate();

}
