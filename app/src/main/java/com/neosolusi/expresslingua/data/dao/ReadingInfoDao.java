package com.neosolusi.expresslingua.data.dao;

import com.neosolusi.expresslingua.data.entity.ReadingInfo;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;

public class ReadingInfoDao extends Dao<ReadingInfo> {

    public ReadingInfoDao(Realm db) {
        super(db, ReadingInfo.class);
    }

    @Override public void copyOrUpdateAsync(List<ReadingInfo> entities) {
        RealmResults<ReadingInfo> records = findAll();
        for (ReadingInfo entity : entities) {
            ReadingInfo info = records.where().equalTo("menu_id", entity.getMenu_id()).findFirst();
            if (info != null) {
                entity.setDownload_complete(info.isDownload_complete());
            }
        }
        super.copyOrUpdateAsync(entities);
    }

}
