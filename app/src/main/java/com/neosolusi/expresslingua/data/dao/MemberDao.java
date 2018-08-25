package com.neosolusi.expresslingua.data.dao;

import com.neosolusi.expresslingua.data.entity.Member;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import io.realm.Realm;
import io.realm.RealmResults;

public class MemberDao extends Dao<Member> {

    public MemberDao(Realm db) {
        super(db, Member.class);
    }

    @Override public void insertAsync(List<Member> entities) {
        db.executeTransactionAsync(realm -> {
            for (Member member : entities) {
                member.setId(UUID.randomUUID().getMostSignificantBits() & Long.MAX_VALUE);
            }
            realm.copyToRealmOrUpdate(entities);
        });
    }

    @Override public void copyOrUpdateAsync(List<Member> entities) {
        db.executeTransactionAsync(realm -> {
            List<Member> members = new ArrayList<>();
            RealmResults<Member> records = realm.where(Member.class).findAll();
            for (Member entity : entities) {
                Member member = records.where().equalTo("user_id", entity.getUser_id()).findFirst();
                if (member != null) {
                    entity.setId(member.getId());
                } else {
                    entity.setId(UUID.randomUUID().getMostSignificantBits() & Long.MAX_VALUE);
                }
                members.add(entity);
            }
            realm.copyToRealmOrUpdate(members);
        });
    }

    @Override public Member copyOrUpdate(Member entity) {
        db.executeTransaction(localDb -> {
            Member member = localDb.where(Member.class).equalTo("user_id", entity.getUser_id()).equalTo("group_id", entity.getGroup_id()).findFirst();
            if (member != null) {
                member.setUploaded(entity.isUploaded());
            } else {
                entity.setId(UUID.randomUUID().getMostSignificantBits() & Long.MAX_VALUE);
                localDb.copyToRealmOrUpdate(entity);
            }
        });
        return db.where(Member.class).equalTo("user_id", entity.getUser_id()).equalTo("group_id", entity.getGroup_id()).findFirst();
    }
}
