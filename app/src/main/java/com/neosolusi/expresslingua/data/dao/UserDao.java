package com.neosolusi.expresslingua.data.dao;

import com.neosolusi.expresslingua.data.entity.User;

import java.util.UUID;

import io.realm.Realm;
import io.realm.RealmResults;

public class UserDao extends Dao<User> {

    public UserDao(Realm db) {
        super(db, User.class);
    }

    @Override public User copyOrUpdate(User entity) {
        db.beginTransaction();
        RealmResults<User> users = where().findAll();
        for (User checkUser : users) {
            if (checkUser.getUserid().equals(entity.getUserid())) {
                checkUser.setActive(true);
            } else {
                checkUser.setActive(false);
            }
        }
        db.commitTransaction();

        User realmUser = findFirstEqualTo("userid", entity.getUserid());
        if (realmUser != null) {
            entity.setId(realmUser.getId());
        } else {
            entity.setId(UUID.randomUUID().getMostSignificantBits() & Long.MAX_VALUE);
        }
        entity.setActive(true);

        return super.copyOrUpdate(entity);
    }

    @Override public void copyOrUpdateAsync(User entity) {
        db.executeTransactionAsync(realm -> {
            RealmResults<User> users = realm.where(User.class).findAll();
            for (User checkUser : users) {
                if (checkUser.getUserid().equals(entity.getUserid())) {
                    checkUser.setActive(true);
                } else {
                    checkUser.setActive(false);
                }
            }

            User realmUser = realm.where(User.class).equalTo("userid", entity.getUserid()).findFirst();
            if (realmUser != null) {
                entity.setId(realmUser.getId());
            } else {
                entity.setId(UUID.randomUUID().getMostSignificantBits() & Long.MAX_VALUE);
            }
            entity.setActive(true);
            realm.copyToRealmOrUpdate(entity);
        });
    }

    public User findActiveUser() {
        return where().equalTo("active", true).findFirst();
    }

    public User findActiveUserCopy() {
        if (findActiveUser() == null) return null;

        return db.copyFromRealm(findActiveUser());
    }
}
