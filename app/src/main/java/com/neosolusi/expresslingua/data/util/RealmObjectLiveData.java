package com.neosolusi.expresslingua.data.util;

import android.arch.lifecycle.LiveData;

import io.realm.RealmChangeListener;
import io.realm.RealmObject;

public class RealmObjectLiveData<T extends RealmObject> extends LiveData<T> {

    private RealmObject result;
    private RealmChangeListener<T> listener;

    public RealmObjectLiveData(RealmObject result) {
        this.result = result;
        this.listener = this::setValue;
    }

    @Override protected void onActive() {
        super.onActive();
        result.addChangeListener(listener);
    }

    @Override protected void onInactive() {
        super.onInactive();
        result.removeChangeListener(listener);
    }
}
