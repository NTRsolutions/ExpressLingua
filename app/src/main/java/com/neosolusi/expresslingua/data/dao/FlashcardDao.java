package com.neosolusi.expresslingua.data.dao;

import android.arch.lifecycle.LiveData;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.neosolusi.expresslingua.AppConstants;
import com.neosolusi.expresslingua.AppServices;
import com.neosolusi.expresslingua.AppUtils;
import com.neosolusi.expresslingua.ExpressLingua;
import com.neosolusi.expresslingua.algorithm.SM2.State;
import com.neosolusi.expresslingua.data.entity.Dictionary;
import com.neosolusi.expresslingua.data.entity.Flashcard;
import com.neosolusi.expresslingua.data.entity.Reading;
import com.neosolusi.expresslingua.data.util.RealmResultsLiveData;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import io.realm.Case;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

import static com.neosolusi.expresslingua.AppConstants.PREFERENCE_MAX_ALLOWED_FLASHCARD;

public class FlashcardDao extends Dao<Flashcard> {

    public FlashcardDao(Realm db) {
        super(db, Flashcard.class);
    }

    @Override public void insertAsync(List<Flashcard> entities) {
        db.executeTransactionAsync(realm -> {
            long id = 0;
            List<Flashcard> cardToRemove = new ArrayList<>();
            for (Flashcard entity : entities) {
                if (entity == null || entity.getTranslation() == null) continue;
                if (entity.getTranslation().trim().equalsIgnoreCase("")) {
                    Log.d("Flashcard InsertAsync", "Found entity with empty translation");
                } else if (entity.getCard().trim().equalsIgnoreCase("")) {
                    Log.d("Flashcard InsertAsync", "Found entity with empty card");
                }

                Reading reading = findReadingFromFlashcard(realm, entity);
                if (reading != null) {
                    if (AppUtils.isWord(entity.getCard())) {
                        Dictionary dictionary = findDictionaryFromFlashcard(realm, entity);
                        if (dictionary != null && dictionary.getLocal_word() > AppConstants.DEFAULT_LOCAL_WORD_CONSTANT) {
                            cardToRemove.add(entity);
                            continue;
                        }
                    }
                    entity.setReference(reading.getId());
                    entity.setCategory(Reading.class.getSimpleName());
                } else {
                    Dictionary dictionary = findDictionaryFromFlashcard(realm, entity);
                    if (dictionary != null) {
                        if (dictionary.getLocal_word() > AppConstants.DEFAULT_LOCAL_WORD_CONSTANT) {
                            cardToRemove.add(entity);
                            continue;
                        }
                        entity.setReference(dictionary.getId());
                        entity.setCategory(Dictionary.class.getSimpleName());
                    } else {
                        entity.setReference(UUID.randomUUID().getMostSignificantBits() & Long.MAX_VALUE);
                        entity.setCategory("User");
                    }
                }

                // Skip record if exists to prevent double data
                Flashcard found = realm.where(Flashcard.class).equalTo("reference", entity.getReference()).findFirst();
                if (found != null) {
                    entities.remove(entity);
                    continue;
                }

                entity.setId(id++);
                entity.setType(AppUtils.isWord(entity.getCard()) ? "word" : "sentence");
                entity.setUploaded(true);
                entity.setReviewed(false);
                entity.setState(State.NEW);
                entity.setRepeat(0);
                entity.setE_factor(2.5);
                entity.setInterval(1);
                entity.setNext_show(entity.getDatecreated());
                entity.setEasy_counter(0);
            }
            entities.removeAll(cardToRemove);
            realm.insert(entities);
        });
    }

    public void housekeeping() {
        db.executeTransactionAsync(realm -> {
            RealmResults<Reading> readings = realm.where(Reading.class).findAll();
            RealmResults<Dictionary> dictionaries = realm.where(Dictionary.class).findAll();

            if (dictionaries == null || dictionaries.isEmpty()) return;

            RealmResults<Flashcard> entities = realm.where(Flashcard.class).equalTo("already_read", 0).findAll();
            entities.deleteAllFromRealm();

            entities = realm.where(Flashcard.class).equalTo("reference", 0).findAll();

            for (Flashcard entity : entities) {
                Reading reading = readings.where().equalTo("translation", entity.getTranslation(), Case.INSENSITIVE).findFirst();
                if (reading != null) {
                    // Reading already in flashcard, so we set already_read to 1
                    reading.setAlready_read(1);

                    entity.setReference(reading.getId());
                    entity.setCategory(Reading.class.getSimpleName());
                } else {
                    Dictionary dictionary = dictionaries.where().equalTo("translation", entity.getTranslation(), Case.INSENSITIVE).findFirst();
                    if (dictionary != null) {
                        entity.setReference(dictionary.getId());
                        entity.setCategory(Dictionary.class.getSimpleName());
                    } else {
                        entity.setReference(UUID.randomUUID().getMostSignificantBits() & Long.MAX_VALUE);
                        entity.setCategory("User");
                    }
                }
            }
        });
    }

    public void housekeepingAgain() {
        db.executeTransactionAsync(realm -> {
            RealmResults<Flashcard> flashcards = realm.where(Flashcard.class).equalTo("translation", "").equalTo("type", "sentence").findAll();
            flashcards.deleteAllFromRealm();

            List<Flashcard> cardToRemove = new ArrayList<>();
            flashcards = realm.where(Flashcard.class).equalTo("type", "word").findAll();
            for (Flashcard flashcard : flashcards) {
                long count = realm.where(Flashcard.class).equalTo("reference", flashcard.getReference()).count();
                if (count > 1) {
                    flashcard.deleteFromRealm();
                    continue;
                }

                if (flashcard.getCategory().equalsIgnoreCase(Dictionary.class.getSimpleName())) {
                    Dictionary dictionary = realm.where(Dictionary.class).equalTo("id", flashcard.getReference()).findFirst();
                    if (dictionary != null && dictionary.getLocal_word() > AppConstants.DEFAULT_LOCAL_WORD_CONSTANT) {
                        cardToRemove.add(flashcard);
                    }
                }
            }
            for (Flashcard flashcard : cardToRemove) {
                flashcard.deleteFromRealm();
            }
        });
    }

    private Reading findReadingFromFlashcard(Realm db, Flashcard card) {
        RealmResults<Reading> readings = db.where(Reading.class).findAll();
        Reading reading;

        if (card.getTranslation().trim().equalsIgnoreCase("")) {
            reading = readings.where().equalTo("sentence", card.getCard(), Case.INSENSITIVE).findFirst();
        } else {
            reading = readings.where().equalTo("translation", card.getTranslation(), Case.INSENSITIVE).findFirst();
        }

        return reading;
    }

    private Dictionary findDictionaryFromFlashcard(Realm db, Flashcard card) {
        RealmResults<Dictionary> dictionaries = db.where(Dictionary.class).findAll();
        Dictionary dictionary;

        if (card.getTranslation().trim().equalsIgnoreCase("")) {
            dictionary = dictionaries.where().equalTo("word", card.getCard(), Case.INSENSITIVE).findFirst();
        } else {
            dictionary = dictionaries.where().equalTo("translation", card.getTranslation(), Case.INSENSITIVE).findFirst();
        }

        return dictionary;
    }

    public long makeNewId() {
        if (count() == 0) {
            return 1;
        } else {
            return where().max("id").intValue() + 1;
        }
    }

    /**
     * Update flashcard does exists in database from user input
     *
     * @param entity Not managed flashcard object
     */
    public void update(Flashcard entity) {
        super.copyOrUpdate(entity);
    }

    public void update(List<Flashcard> entities) {
        super.copyOrUpdate(entities);
    }

    public LiveData<RealmResults<Flashcard>> getFlashcards(String type) {
        return new RealmResultsLiveData<>(db.where(Flashcard.class)
                .equalTo("already_read", 1)
                .equalTo("type", type)
                .greaterThanOrEqualTo("mastering_level", 3)
                .lessThanOrEqualTo("next_show", new Date())
                .findAllSortedAsync("mastering_level", Sort.ASCENDING));
    }

    public boolean isAllowToAddFlashcard(String type) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(ExpressLingua.getInstance());

        // Read value from server config
        int maxFlashcard = pref.getInt(PREFERENCE_MAX_ALLOWED_FLASHCARD, 0);
        maxFlashcard = maxFlashcard > 0 ? maxFlashcard : AppConstants.DEFAULT_MAX_ALLOWED_FLASHCARD;

        return where().equalTo("already_read", 1).equalTo("type", type).findAll().size() < maxFlashcard;
    }

    public void resetSelectedFlashcards(RealmResults<Flashcard> flashcards) {
        if (!db.isInTransaction()) db.beginTransaction();

        for (Flashcard flashcard : flashcards) {
            flashcard.setSelected(0);
            copyOrUpdate(flashcard);
        }

        db.commitTransaction();
    }

}
