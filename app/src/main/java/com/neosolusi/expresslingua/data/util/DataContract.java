package com.neosolusi.expresslingua.data.util;

import android.arch.lifecycle.LiveData;

import java.util.HashMap;
import java.util.List;

import io.realm.Case;
import io.realm.RealmObject;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import io.realm.Sort;

public interface DataContract<T extends RealmObject> {
    T findFirstEqualTo(String column, String criteria);
    T findFirstEqualTo(String column, boolean criteria);
    T findFirstEqualTo(String column, int criteria);
    T findFirstEqualTo(String column, long criteria);
    T findFirstEqualTo(HashMap<String, Object> criterias);
    T findFirstCopyEqualTo(String column, String criteria);
    T findFirstCopyEqualTo(String column, boolean criteria);
    T findFirstCopyEqualTo(String column, int criteria);
    T findFirstCopyEqualTo(String column, long criteria);
    T findFirstCopyEqualTo(HashMap<String, Object> criterias);
    T findFirstNotEqualTo(String column, String criteria);
    T findFirstNotEqualTo(String column, long criteria);
    RealmResults<T> findAll();
    RealmResults<T> findAllEqualTo(String column, String search);
    RealmResults<T> findAllEqualTo(String column, long search);
    RealmResults<T> findAllEqualTo(String column, boolean search);
    RealmResults<T> findAllEqualTo(String column, int search);
    LiveData<RealmResults<T>> findAllAsync();
    LiveData<RealmResults<T>> findAllAsync(String column, Sort sort);
    LiveData<RealmResults<T>> findAllEqualToAsync(String column, boolean search);
    LiveData<RealmResults<T>> findAllEqualToAsync(String column, int search, String sortColumn, Sort sort);
    LiveData<RealmResults<T>> findAllEqualToAsync(String column, long search, String sortColumn, Sort sort);
    LiveData<RealmResults<T>> findAllEqualToAsync(HashMap<String, Object> criterias);
    LiveData<RealmResults<T>> findAllEqualToAsync(HashMap<String, Object> criterias, String sortColumn, Sort sort);
    void insertAsync(T entity);
    void insertAsync(List<T> entities);
    T copyFromDb(T entity);
    List<T> copyFromDb(Iterable<T> entities);
    T copyOrUpdate(T entity);
    void copyOrUpdate(List<T> entities);
    void copyOrUpdateAsync(T entity);
    void copyOrUpdateAsync(List<T> entities);
    void deleteAll();
    void delete(T entity);
    int count();
    RealmQuery<T> where();
}
