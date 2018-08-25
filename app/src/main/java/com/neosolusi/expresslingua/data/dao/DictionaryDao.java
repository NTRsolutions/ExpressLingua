package com.neosolusi.expresslingua.data.dao;

import com.neosolusi.expresslingua.data.entity.Dictionary;

import io.realm.Realm;

public class DictionaryDao extends Dao<Dictionary> {

    public DictionaryDao(Realm db) {
        super(db, Dictionary.class);
    }

}
