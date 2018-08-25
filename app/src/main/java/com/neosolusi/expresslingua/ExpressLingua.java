package com.neosolusi.expresslingua;

import android.app.Application;

import com.neosolusi.expresslingua.data.util.InitialData;
import com.neosolusi.expresslingua.data.util.Migrations;

import io.realm.Realm;
import io.realm.RealmConfiguration;

public class ExpressLingua extends Application {

    private static ExpressLingua mInstance;

    public static ExpressLingua getInstance() {
        return mInstance;
    }

    @Override public void onCreate() {
        super.onCreate();

        mInstance = this;

        // Realm configuration
        Realm.init(mInstance);
        RealmConfiguration realmConfiguration = new RealmConfiguration.Builder()
                .name("expresslingua.realm")
                .migration(new Migrations())
                .schemaVersion(8)
                .initialData(new InitialData())
                .build();
//        Realm.deleteRealm(realmConfiguration);
        Realm.setDefaultConfiguration(realmConfiguration);
    }

}
