package com.neosolusi.expresslingua.data.dao;

import com.neosolusi.expresslingua.AppConstants;
import com.neosolusi.expresslingua.data.entity.Challenge;
import com.neosolusi.expresslingua.data.entity.ChallengeHard;
import com.neosolusi.expresslingua.data.entity.Flashcard;
import com.neosolusi.expresslingua.data.entity.Reading;

import java.util.Date;
import java.util.UUID;

import io.realm.Realm;
import io.realm.RealmResults;

public class ChallengeDao extends Dao<Challenge> {

    public ChallengeDao(Realm db) {
        super(db, Challenge.class);
    }

    public void housekeeping() {
        db.executeTransactionAsync(new Realm.Transaction() {
            Realm asyncDb;

            @Override public void execute(Realm realm) {
                asyncDb = realm;
                RealmResults<Challenge> challenges = realm.where(Challenge.class).findAll();

                for (Challenge challenge : challenges) {
                    Reading reading = findReadingById(challenge.getReference());
                    if (reading == null) {
                        challenge.deleteFromRealm();
                        continue;
                    }

                    Flashcard flashcard = findFlashcardByReference(reading.getId());
                    if (flashcard == null) {
                        challenge.deleteFromRealm();
                        continue;
                    }
                }
            }

            private Reading findReadingById(long id) {
                return asyncDb.where(Reading.class).equalTo("id", id).findFirst();
            }

            private Flashcard findFlashcardByReference(long id) {
                return asyncDb.where(Flashcard.class).equalTo("reference", id).equalTo("category", Reading.class.getSimpleName()).findFirst();
            }
        });
    }

    public void autoCreateChallenges() {
        db.executeTransactionAsync(realm -> {
            RealmResults<Reading> readings = realm.where(Reading.class)
                    .equalTo("already_read", 1)
                    .greaterThan("mastering_level", 2)
                    .findAll();

            Reading readingCheck = realm.where(Reading.class)
                    .equalTo("already_read", 1)
                    .greaterThan("mastering_level", 0)
                    .greaterThanOrEqualTo("file_id", AppConstants.DEFAULT_LESSON_TO_SHOW_HARD_CHALLENGE)
                    .findFirst();

            for (Reading reading : readings) {
                if (readingCheck == null) {
                    if (reading.getKal_panjang() > 0) {
                        createHardChallenge(realm, reading);
                    } else {
                        createEasyChallenge(realm, reading);
                    }
                } else {
                    createEasyChallenge(realm, reading);
                }
            }
        });
    }

    private void createEasyChallenge(Realm db, Reading reading) {
        Challenge challenge = db.createObject(Challenge.class, UUID.randomUUID().getMostSignificantBits() & Long.MAX_VALUE);
        challenge.setCategory("Listening");
        challenge.setSeen(false);
        challenge.setCorrect(false);
        challenge.setSkip(false);
        challenge.setDatecreated(new Date());
        challenge.setReference(reading.getId());
    }

    private void createHardChallenge(Realm db, Reading reading) {
        ChallengeHard challenge = db.createObject(ChallengeHard.class, UUID.randomUUID().getMostSignificantBits() & Long.MAX_VALUE);
        challenge.setCategory("Listening");
        challenge.setSeen(false);
        challenge.setCorrect(false);
        challenge.setSkip(false);
        challenge.setDatecreated(new Date());
        challenge.setReference(reading.getId());
    }

    public void update(Challenge entity) {
        super.copyOrUpdate(entity);
    }

    public void resetSelectedChallenges(RealmResults<Challenge> challenges) {
        if (!db.isInTransaction()) db.beginTransaction();

        for (Challenge challenge : challenges) {
            challenge.setSelected(0);
            copyOrUpdate(challenge);
        }

        db.commitTransaction();
    }

}
