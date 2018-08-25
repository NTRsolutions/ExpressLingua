package com.neosolusi.expresslingua.data.dao;

import com.neosolusi.expresslingua.data.entity.Contact;

import io.realm.Realm;

public class ContactDao extends Dao<Contact> {

    public ContactDao(Realm db) {
        super(db, Contact.class);
    }

}
