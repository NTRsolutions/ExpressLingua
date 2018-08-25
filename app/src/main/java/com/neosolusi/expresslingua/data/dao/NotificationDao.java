package com.neosolusi.expresslingua.data.dao;

import com.neosolusi.expresslingua.data.entity.Notification;

import java.util.List;

import io.realm.Realm;

public class NotificationDao extends Dao<Notification> {

    public NotificationDao(Realm db) {
        super(db, Notification.class);
    }

    @Override public void insertAsync(List<Notification> entities) {
        db.executeTransactionAsync(realm -> {
            realm.delete(Notification.class);
            realm.insert(entities);
        });
    }

}
