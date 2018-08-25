package com.neosolusi.expresslingua.data.util;

import android.arch.lifecycle.LiveData;

import io.realm.RealmChangeListener;
import io.realm.RealmObject;
import io.realm.RealmResults;

public class RealmResultsLiveData<T extends RealmObject> extends LiveData<RealmResults<T>> {

    private RealmResults<T> results;
    private RealmChangeListener<RealmResults<T>> listener;

    public RealmResultsLiveData(RealmResults<T> results) {
        this.results = results;
        this.listener = this::setValue;
    }

    @Override protected void onActive() {
        super.onActive();
        results.addChangeListener(listener);
    }

    @Override protected void onInactive() {
        super.onInactive();
        results.removeChangeListener(listener);
    }

}
