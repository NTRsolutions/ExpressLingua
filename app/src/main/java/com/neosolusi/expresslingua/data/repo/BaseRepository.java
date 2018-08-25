package com.neosolusi.expresslingua.data.repo;

import android.arch.lifecycle.LiveData;

import com.neosolusi.expresslingua.data.dao.Dao;
import com.neosolusi.expresslingua.data.util.DataContract;

import java.util.HashMap;
import java.util.List;

import io.realm.Case;
import io.realm.RealmObject;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import io.realm.Sort;

public abstract class BaseRepository<T extends RealmObject> implements DataContract<T> {

    protected final Dao mDao;

    public BaseRepository(Dao dao) {
        this.mDao = dao;
    }

    abstract public boolean isFetchNeeded();

    abstract public void wakeup();

    @Override public T findFirstEqualTo(String column, String criteria) {
        return (T) mDao.findFirstEqualTo(column, criteria);
    }

    @Override public T findFirstEqualTo(String column, boolean criteria) {
        return (T) mDao.findFirstEqualTo(column, criteria);
    }

    @Override public T findFirstEqualTo(String column, int criteria) {
        return (T) mDao.findFirstEqualTo(column, criteria);
    }

    @Override public T findFirstEqualTo(String column, long criteria) {
        return (T) mDao.findFirstEqualTo(column, criteria);
    }

    @Override public T findFirstEqualTo(HashMap<String, Object> criterias) {
        return (T) mDao.findFirstEqualTo(criterias);
    }

    @Override public T findFirstCopyEqualTo(String column, String criteria) {
        return (T) mDao.findFirstCopyEqualTo(column, criteria);
    }

    @Override public T findFirstCopyEqualTo(String column, boolean criteria) {
        return (T) mDao.findFirstCopyEqualTo(column, criteria);
    }

    @Override public T findFirstCopyEqualTo(String column, int criteria) {
        return (T) mDao.findFirstCopyEqualTo(column, criteria);
    }

    @Override public T findFirstCopyEqualTo(String column, long criteria) {
        return (T) mDao.findFirstCopyEqualTo(column, criteria);
    }

    @Override public T findFirstCopyEqualTo(HashMap<String, Object> criterias) {
        return (T) mDao.findFirstCopyEqualTo(criterias);
    }

    @Override public T findFirstNotEqualTo(String column, String criteria) {
        return (T) mDao.findFirstNotEqualTo(column, criteria);
    }

    @Override public T findFirstNotEqualTo(String column, long criteria) {
        return (T) mDao.findFirstNotEqualTo(column, criteria);
    }

    @Override public RealmResults<T> findAll() {
        return mDao.findAll();
    }

    @Override public RealmResults<T> findAllEqualTo(String column, String search) {
        return mDao.findAllEqualTo(column, search);
    }

    @Override public RealmResults<T> findAllEqualTo(String column, long search) {
        return mDao.findAllEqualTo(column, search);
    }

    @Override public RealmResults<T> findAllEqualTo(String column, boolean search) {
        return mDao.findAllEqualTo(column, search);
    }

    @Override public RealmResults<T> findAllEqualTo(String column, int search) {
        return mDao.findAllEqualTo(column, search);
    }

    @Override public LiveData<RealmResults<T>> findAllAsync() {
        return mDao.findAllAsync();
    }

    @Override public LiveData<RealmResults<T>> findAllAsync(String column, Sort sort) {
        return mDao.findAllAsync(column, sort);
    }

    @Override public LiveData<RealmResults<T>> findAllEqualToAsync(String column, boolean search) {
        return mDao.findAllEqualToAsync(column, search);
    }

    @Override public LiveData<RealmResults<T>> findAllEqualToAsync(String column, int search, String sortColumn, Sort sort) {
        return mDao.findAllEqualToAsync(column, search, sortColumn, sort);
    }

    @Override public LiveData<RealmResults<T>> findAllEqualToAsync(String column, long search, String sortColumn, Sort sort) {
        return mDao.findAllEqualToAsync(column, search, sortColumn, sort);
    }

    @Override public LiveData<RealmResults<T>> findAllEqualToAsync(HashMap<String, Object> criterias) {
        return mDao.findAllEqualToAsync(criterias);
    }

    @Override public LiveData<RealmResults<T>> findAllEqualToAsync(HashMap<String, Object> criterias, String sortColumn, Sort sort) {
        return mDao.findAllEqualToAsync(criterias, sortColumn, sort);
    }

    @Override public void insertAsync(T entity) {
        mDao.insertAsync(entity);
    }

    @Override public void insertAsync(List<T> entities) {
        mDao.insertAsync(entities);
    }

    @Override public T copyFromDb(T entity) {
        return (T) mDao.copyFromDb(entity);
    }

    @Override public List<T> copyFromDb(Iterable<T> entities) {
        return mDao.copyFromDb(entities);
    }

    @Override public T copyOrUpdate(T entity) {
        return (T) mDao.copyOrUpdate(entity);
    }

    @Override public void copyOrUpdate(List<T> entities) {
        mDao.copyOrUpdate(entities);
    }

    @Override public void copyOrUpdateAsync(T entity) {
        mDao.copyOrUpdateAsync(entity);
    }

    @Override public void copyOrUpdateAsync(List<T> entities) {
        mDao.copyOrUpdateAsync(entities);
    }

    @Override public void deleteAll() {
        mDao.deleteAll();
    }

    @Override public void delete(T entity) {
        mDao.delete(entity);
    }

    @Override public int count() {
        return mDao.count();
    }

    @Override public RealmQuery<T> where() {
        return mDao.where();
    }
}
