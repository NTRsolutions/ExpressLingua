package com.neosolusi.expresslingua.data.dao;

import com.neosolusi.expresslingua.data.entity.Episode;

import io.realm.Realm;

public class EpisodeDao extends Dao<Episode> {

    public EpisodeDao(Realm db) {
        super(db, Episode.class);
    }

}
