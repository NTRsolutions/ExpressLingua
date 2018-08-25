package com.neosolusi.expresslingua.data.dao;

import com.neosolusi.expresslingua.data.entity.ChallengeHard;

import io.realm.Realm;

public class ChallengeHardDao extends Dao<ChallengeHard> {

    public ChallengeHardDao(Realm db) {
        super(db, ChallengeHard.class);
    }

}
