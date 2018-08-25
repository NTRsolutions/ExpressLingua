package com.neosolusi.expresslingua.data.dao;

import com.neosolusi.expresslingua.data.entity.Reading;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;

public class ReadingDao extends Dao<Reading> {

    public ReadingDao(Realm db) {
        super(db, Reading.class);
    }

    @Override public void insertAsync(List<Reading> entities) {
        for (Reading entity : entities) {
            entity.setAlready_read(0);
            entity.setSelected(0);
            entity.setUploaded(true);
            entity.setBookmarked(false);
        }
        super.insertAsync(entities);
    }

    @Override public void copyOrUpdateAsync(List<Reading> entities) {
        db.executeTransactionAsync(realm -> {
            RealmResults<Reading> records = realm.where(Reading.class).findAll();
            for (Reading entity : entities) {
                Reading reading = records.where().equalTo("sentence", entity.getSentence()).findFirst();
                if (reading != null) {
                    if (reading.getDatemodified().after(entity.getDatemodified())) {
                        entity.setMastering_level(reading.getMastering_level());
                        entity.setAlready_read(reading.getAlready_read());
                        entity.setSelected(reading.getSelected());
                        entity.setUploaded(reading.isUploaded());
                        entity.setBookmarked(reading.isBookmarked());
                    }
                } else {
                    entity.setAlready_read(0);
                    entity.setSelected(0);
                    entity.setUploaded(true);
                    entity.setBookmarked(false);
                }
            }
            realm.copyToRealmOrUpdate(entities);
        });
    }

    public void updateFromUserData(List<Reading> entities) {
        db.executeTransactionAsync(realm -> {
           RealmResults<Reading> records = realm.where(Reading.class).findAll();
           for (Reading entity : entities) {
               Reading reading = records.where().equalTo("sequence_no", entity.getSequence_no()).findFirst();
               if (reading == null) continue;
                reading.setMastering_level(entity.getMastering_level());
                reading.setAlready_read(1);
           }
        });
    }

    public void resetSelectedReadings(RealmResults<Reading> readings) {
        List<Reading> copies = db.copyFromRealm(readings);
        for (Reading reading : copies) {
            reading.setSelected(0);
        }

        super.copyOrUpdateAsync(copies);
    }

    public void resetSelectedReading(Reading reading) {
        Reading readingCopy = db.copyFromRealm(reading);
        readingCopy.setSelected(0);
        super.copyOrUpdateAsync(readingCopy);
    }

    public void selectAllReadings(RealmResults<Reading> readings, boolean toggle) {
        List<Reading> copies = db.copyFromRealm(readings);
        for (Reading reading : copies) {
            reading.setSelected(toggle ? 1 : 0);
        }

        super.copyOrUpdateAsync(copies);
    }

}
