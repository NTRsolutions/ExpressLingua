package com.neosolusi.expresslingua.data.dao;

import com.neosolusi.expresslingua.data.entity.Group;
import com.neosolusi.expresslingua.data.entity.Member;

import java.util.List;

import io.realm.Realm;

public class GroupDao extends Dao<Group> {

    public GroupDao(Realm db) {
        super(db, Group.class);
    }

    @Override public void copyOrUpdateAsync(Group entity) {
        super.copyOrUpdateAsync(entity);
//        db.executeTransactionAsync(localDb -> {
//            Group group = localDb.where(Group.class).equalTo("name", entity.getName()).findFirst();
//            if (group != null) {
//                List<Member> members = localDb.where(Member.class).equalTo("group_id", group.getId()).findAll();
//                for (Member member : members) {
//                    Member tempMember = localDb.copyFromRealm(member);
//                    tempMember.setGroup_id(entity.getId());
//                    member.deleteFromRealm();
//                    localDb.copyToRealmOrUpdate(tempMember);
//                }
//
//                group.deleteFromRealm();
//            }
//
//            localDb.copyToRealmOrUpdate(entity);
//        });
    }
}

