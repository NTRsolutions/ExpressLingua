package com.neosolusi.expresslingua.data.dao;

import android.arch.lifecycle.LiveData;

import com.neosolusi.expresslingua.data.util.DataContract;
import com.neosolusi.expresslingua.data.util.RealmResultsLiveData;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.realm.Case;
import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import io.realm.Sort;

public abstract class Dao<T extends RealmObject> implements DataContract<T> {

    protected Realm db;
    private Class<T> type;

    public Dao(Realm db, Class<T> type) {
        this.db = db;
        this.type = type;
    }

    public void refresh() {
        db.refresh();
    }

    public RealmQuery<T> where() {
        return db.where(type);
    }

    private RealmQuery<T> buildWhere(HashMap<String, Object> criterias) {
        RealmQuery<T> query = where();

        for (Map.Entry<String, Object> entry : criterias.entrySet()) {
            if (entry.getValue() instanceof Integer) {
                query.equalTo(entry.getKey(), (int) entry.getValue());
            } else if (entry.getValue() instanceof Long) {
                query.equalTo(entry.getKey(), (long) entry.getValue());
            } else if (entry.getValue() instanceof String) {
                query.equalTo(entry.getKey(), (String) entry.getValue());
            } else if (entry.getValue() instanceof Boolean) {
                query.equalTo(entry.getKey(), (boolean) entry.getValue());
            }
        }

        return query;
    }

    @Override public T findFirstEqualTo(String column, String criteria) {
        return where().equalTo(column, criteria, Case.INSENSITIVE).findFirst();
    }

    @Override public T findFirstEqualTo(String column, boolean criteria) {
        return where().equalTo(column, criteria).findFirst();
    }

    @Override public T findFirstEqualTo(String column, int criteria) {
        return where().equalTo(column, criteria).findFirst();
    }

    @Override public T findFirstEqualTo(String column, long criteria) {
        return where().equalTo(column, criteria).findFirst();
    }

    @Override public T findFirstEqualTo(HashMap<String, Object> criterias) {
        return buildWhere(criterias).findFirst();
    }

    @Override public T findFirstCopyEqualTo(String column, String criteria) {
        T entity = where().equalTo(column, criteria, Case.INSENSITIVE).findFirst();
        if (entity == null) return null;
        return db.copyFromRealm(entity);
    }

    @Override public T findFirstCopyEqualTo(String column, boolean criteria) {
        T entity = where().equalTo(column, criteria).findFirst();
        if (entity == null) return null;
        return db.copyFromRealm(entity);
    }

    @Override public T findFirstCopyEqualTo(String column, int criteria) {
        T entity = where().equalTo(column, criteria).findFirst();
        if (entity == null) return null;
        return db.copyFromRealm(entity);
    }

    @Override public T findFirstCopyEqualTo(String column, long criteria) {
        T entity = where().equalTo(column, criteria).findFirst();
        if (entity == null) return null;
        return db.copyFromRealm(entity);
    }

    @Override public T findFirstCopyEqualTo(HashMap<String, Object> criterias) {
        T entity = buildWhere(criterias).findFirst();
        if (entity == null) return null;
        return db.copyFromRealm(entity);
    }

    @Override public T findFirstNotEqualTo(String column, String criteria) {
        return where().notEqualTo(column, criteria, Case.INSENSITIVE).findFirst();
    }

    @Override public T findFirstNotEqualTo(String column, long criteria) {
        return where().notEqualTo(column, criteria).findFirst();
    }

    @Override public RealmResults<T> findAll() {
        return where().findAll();
    }

    @Override public RealmResults<T> findAllEqualTo(String column, String search) {
        return where().equalTo(column, search).findAll();
    }

    @Override public RealmResults<T> findAllEqualTo(String column, long search) {
        return where().equalTo(column, search).findAll();
    }

    @Override public RealmResults<T> findAllEqualTo(String column, boolean search) {
        return where().equalTo(column, search).findAll();
    }

    @Override public RealmResults<T> findAllEqualTo(String column, int search) {
        return where().equalTo(column, search).findAll();
    }

    @Override public LiveData<RealmResults<T>> findAllAsync() {
        return new RealmResultsLiveData<>(where().findAllAsync());
    }

    @Override public LiveData<RealmResults<T>> findAllAsync(String column, Sort sort) {
        return new RealmResultsLiveData<>(where().findAllSortedAsync(column, sort));
    }

    @Override public LiveData<RealmResults<T>> findAllEqualToAsync(String column, boolean search) {
        return new RealmResultsLiveData<>(where().equalTo(column, search).findAllAsync());
    }

    @Override public LiveData<RealmResults<T>> findAllEqualToAsync(String column, int search, String sortColumn, Sort sort) {
        return new RealmResultsLiveData<>(where().equalTo(column, search).findAllSortedAsync(sortColumn, sort));
    }

    @Override public LiveData<RealmResults<T>> findAllEqualToAsync(String column, long search, String sortColumn, Sort sort) {
        return new RealmResultsLiveData<>(where().equalTo(column, search).findAllSortedAsync(sortColumn, sort));
    }

    @Override public LiveData<RealmResults<T>> findAllEqualToAsync(HashMap<String, Object> criterias) {
        return new RealmResultsLiveData<>(buildWhere(criterias).findAllAsync());
    }

    @Override public LiveData<RealmResults<T>> findAllEqualToAsync(HashMap<String, Object> criterias, String sortColumn, Sort sort) {
        return new RealmResultsLiveData<>(buildWhere(criterias).findAllSortedAsync(sortColumn, sort));
    }

    @Override public void insertAsync(T entity) {
        db.executeTransactionAsync(realm -> realm.insert(entity));
    }

    @Override public void insertAsync(List<T> entities) {
        db.executeTransactionAsync(realm -> realm.insert(entities));
    }

    @Override public T copyFromDb(T entity) {
        return entity == null ? null : db.copyFromRealm(entity);
    }

    @Override public List<T> copyFromDb(Iterable<T> entities) {
        return db.copyFromRealm(entities);
    }

    @Override public T copyOrUpdate(T entity) {
        if (db.isInTransaction()) {
            entity = db.copyToRealmOrUpdate(entity);
        } else {
            try {
                db.beginTransaction();
                entity = db.copyToRealmOrUpdate(entity);
                db.commitTransaction();
            } catch (Exception e) {
                db.cancelTransaction();
                throw e;
            }
        }

        return entity;
    }

    @Override public void copyOrUpdate(List<T> entities) {
        if (db.isInTransaction()) {
            db.copyToRealmOrUpdate(entities);
        } else {
            try {
                db.beginTransaction();
                db.copyToRealmOrUpdate(entities);
                db.commitTransaction();
            } catch (Exception e) {
                db.cancelTransaction();
                throw e;
            }
        }
    }

    @Override public void copyOrUpdateAsync(T entity) {
        db.executeTransactionAsync(realm -> realm.copyToRealmOrUpdate(entity));
    }

    @Override public void copyOrUpdateAsync(List<T> entities) {
        db.executeTransactionAsync(realm -> realm.copyToRealmOrUpdate(entities));
    }

    @Override public void deleteAll() {
        db.executeTransaction(realm -> {
            RealmResults<T> entities = realm.where(type).findAll();
            entities.deleteAllFromRealm();
        });
    }

    @Override public void delete(T entity) {
        db.executeTransaction(realm -> entity.deleteFromRealm());
    }

    @Override public int count() {
        return (int) where().count();
    }

}
